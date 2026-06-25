#!/usr/bin/env bash
# ============================================================================
# ICMS end-to-end smoke test: drives the full claim lifecycle across all five
# role portals against the running app and asserts every transition at BOTH
# layers -- the HTTP outcome (action accepted, not bounced to /login) and the
# resulting status in the database.
#   Customer submit -> Agent acknowledge/assign -> Surveyor assess ->
#   Agent forward -> Manager approve -> Agent settle -> SETTLED
#
# Prereqs: app running (./run.sh) and DB seeded (./setup.sh).
# Usage: ./smoke-test.sh
# ============================================================================
set -uo pipefail

B="${BASE_URL:-http://localhost:8080}"
PW="${DEMO_PASSWORD:-Password@123}"
DB_HOST="${DB_HOST:-127.0.0.1}"; DB_USER="${DB_USER:-root}"; DB_PASSWORD="${DB_PASSWORD:-}"
MYSQL_OPTS=(-h "$DB_HOST" -u "$DB_USER")
[ -n "$DB_PASSWORD" ] && MYSQL_OPTS+=(-p"$DB_PASSWORD")

PASS=0; FAIL=0
LAST_REDIR=""
ok()   { echo "  PASS: $1"; PASS=$((PASS+1)); }
bad()  { echo "  FAIL: $1"; FAIL=$((FAIL+1)); }

login() { # login <user> <cookiejar> <role>  -- asserts the 302 lands on the role dashboard
    local redir
    redir=$(curl -s -c "$2" -o /dev/null -w '%{redirect_url}' \
        --data-urlencode "username=$1" --data-urlencode "password=$PW" "$B/doLogin")
    if printf '%s' "$redir" | grep -q "/$3/dashboard"; then
        ok "login $1 -> /$3/dashboard"
    else
        bad "login $1 (expected /$3/dashboard, got '$redir')"
    fi
}

post() { # post <label> <cookiejar> <url> [curl-data-args...]
    # HTTP-layer assertion: the action returned a response and did not bounce
    # back to /login (auth/role failure). Exposes the redirect via LAST_REDIR.
    local label="$1" cj="$2" url="$3"; shift 3
    local out code
    out=$(curl -s -b "$cj" -o /dev/null -w '%{http_code} %{redirect_url}' "$@" "$url")
    code="${out%% *}"; LAST_REDIR="${out#* }"
    if [ "$code" = "000" ]; then
        bad "$label (no HTTP response - app down?)"
    elif printf '%s' "$LAST_REDIR" | grep -q '/login'; then
        bad "$label (HTTP $code bounced to /login - not authenticated)"
    else
        ok "$label (HTTP $code)"
    fi
}

claim_status() { mysql "${MYSQL_OPTS[@]}" icms -N -e "SELECT status FROM claims WHERE id=$1;" 2>/dev/null; }
assert_status() { # assert_status <claimId> <expected> <label>
    local got; got="$(claim_status "$1")"
    if [ "$got" = "$2" ]; then ok "$3 (status=$got)"; else bad "$3 (expected $2, got '$got')"; fi
}

CJ_C=/tmp/st_cust; CJ_A=/tmp/st_agent; CJ_S=/tmp/st_surv; CJ_M=/tmp/st_mgr
rm -f "$CJ_C" "$CJ_A" "$CJ_S" "$CJ_M"

echo "== 1. Customer submits a claim =="
login customer "$CJ_C" customer
post "claim submitted" "$CJ_C" "$B/customer/createClaim" \
    --data-urlencode "policyId=1" --data-urlencode "claimSubtype=Accident" \
    --data-urlencode "incidentDate=2026-06-01" --data-urlencode "incidentLocation=Smoke Test Rd" \
    --data-urlencode "city=Mumbai" --data-urlencode "description=E2E smoke test claim" \
    --data-urlencode "estimatedLoss=90000" --data-urlencode "mode=submit"
CID=$(echo "$LAST_REDIR" | grep -oE 'id=[0-9]+' | head -1 | cut -d= -f2)
if [ -n "$CID" ]; then ok "claim created (id=$CID)"; else bad "claim creation"; echo "ABORT"; exit 1; fi
assert_status "$CID" "SUBMITTED" "claim is SUBMITTED"

echo "== 2. Agent acknowledges and assigns a surveyor =="
login agent "$CJ_A" agent
post "agent acknowledged" "$CJ_A" "$B/agent/acknowledge" --data-urlencode "claimId=$CID"
assert_status "$CID" "UNDER_REVIEW" "claim acknowledged"
post "surveyor assigned" "$CJ_A" "$B/agent/assignSurveyor" --data-urlencode "claimId=$CID" --data-urlencode "surveyorId=5"
assert_status "$CID" "SURVEY_SCHEDULED" "surveyor assigned"

echo "== 3. Surveyor submits assessment (net payable computed) =="
login surveyor "$CJ_S" surveyor
post "assessment submitted" "$CJ_S" "$B/surveyor/submitAssessment" \
    --data-urlencode "id=$CID" --data-urlencode "visitDate=2026-06-05" \
    --data-urlencode "compName=Front Bumper" --data-urlencode "compSeverity=SEVERE" --data-urlencode "compCost=60000" --data-urlencode "compReplace=true" \
    --data-urlencode "compName=Bonnet" --data-urlencode "compSeverity=MODERATE" --data-urlencode "compCost=40000" --data-urlencode "compReplace=false" \
    --data-urlencode "policyDeductible=5000" --data-urlencode "depreciationPct=0" --data-urlencode "salvageValue=0" \
    --data-urlencode "recommendation=PARTIAL_APPROVE"
assert_status "$CID" "UNDER_ASSESSMENT" "assessment submitted"
NET=$(mysql "${MYSQL_OPTS[@]}" icms -N -e "SELECT net_payable FROM assessments WHERE claim_id=$CID;" 2>/dev/null)
if [ "$NET" = "95000.00" ]; then ok "net payable computed = $NET"; else bad "net payable (expected 95000.00, got '$NET')"; fi

echo "== 4. Agent forwards for approval =="
post "forwarded for approval" "$CJ_A" "$B/agent/forward" --data-urlencode "claimId=$CID"
assert_status "$CID" "PENDING_APPROVAL" "forwarded for approval"
PEND=$(mysql "${MYSQL_OPTS[@]}" icms -N -e "SELECT COUNT(*) FROM approvals WHERE claim_id=$CID AND decision='PENDING';" 2>/dev/null)
if [ "${PEND:-0}" -ge 1 ] 2>/dev/null; then ok "manager approval pending (count=$PEND)"; else bad "pending approval row (count='$PEND')"; fi

echo "== 5. Manager approves =="
login manager "$CJ_M" manager
post "manager decided" "$CJ_M" "$B/manager/decide" --data-urlencode "claimId=$CID" --data-urlencode "decision=APPROVED" --data-urlencode "remarks=E2E approve"
assert_status "$CID" "APPROVED" "manager approved"

echo "== 6. Agent processes settlement =="
post "settlement authorised" "$CJ_A" "$B/agent/processSettlement" --data-urlencode "id=$CID" --data-urlencode "amount=95000" \
    --data-urlencode "paymentMethod=NEFT" --data-urlencode "accountHolder=James Miller" \
    --data-urlencode "justification=E2E settlement"
assert_status "$CID" "SETTLEMENT_PROCESSING" "settlement authorised"
for n in 1 2 3; do post "advance settlement #$n" "$CJ_A" "$B/agent/advanceSettlement" --data-urlencode "id=$CID"; done
assert_status "$CID" "SETTLED" "settlement confirmed -> claim SETTLED"
SPAY=$(mysql "${MYSQL_OPTS[@]}" icms -N -e "SELECT status FROM settlements WHERE claim_id=$CID;" 2>/dev/null)
if [ "$SPAY" = "PAYMENT_CONFIRMED" ]; then ok "settlement payment CONFIRMED"; else bad "settlement payment (got '$SPAY')"; fi

echo ""
echo "============================================================"
echo " Smoke test complete: $PASS passed, $FAIL failed."
echo "============================================================"
[ "$FAIL" -eq 0 ] || exit 1
