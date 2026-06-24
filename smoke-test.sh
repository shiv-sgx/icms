#!/usr/bin/env bash
# ============================================================================
# ICMS end-to-end smoke test: drives the full claim lifecycle across all five
# role portals against the running app and asserts every transition.
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
ok()   { echo "  PASS: $1"; PASS=$((PASS+1)); }
bad()  { echo "  FAIL: $1"; FAIL=$((FAIL+1)); }

login() { # login <user> <cookiejar>
    curl -s -c "$2" -o /dev/null --data-urlencode "username=$1" --data-urlencode "password=$PW" "$B/doLogin"
}
claim_status() { mysql "${MYSQL_OPTS[@]}" icms -N -e "SELECT status FROM claims WHERE id=$1;" 2>/dev/null; }
assert_status() { # assert_status <claimId> <expected> <label>
    local got; got="$(claim_status "$1")"
    if [ "$got" = "$2" ]; then ok "$3 (status=$got)"; else bad "$3 (expected $2, got '$got')"; fi
}

CJ_C=/tmp/st_cust; CJ_A=/tmp/st_agent; CJ_S=/tmp/st_surv; CJ_M=/tmp/st_mgr
rm -f "$CJ_C" "$CJ_A" "$CJ_S" "$CJ_M"

echo "== 1. Customer submits a claim =="
login customer "$CJ_C"
LOC=$(curl -s -b "$CJ_C" -o /dev/null -w '%{redirect_url}' \
    --data-urlencode "policyId=1" --data-urlencode "claimSubtype=Accident" \
    --data-urlencode "incidentDate=2026-06-01" --data-urlencode "incidentLocation=Smoke Test Rd" \
    --data-urlencode "city=Mumbai" --data-urlencode "description=E2E smoke test claim" \
    --data-urlencode "estimatedLoss=90000" --data-urlencode "mode=submit" "$B/customer/createClaim")
CID=$(echo "$LOC" | grep -oE 'id=[0-9]+' | head -1 | cut -d= -f2)
if [ -n "$CID" ]; then ok "claim created (id=$CID)"; else bad "claim creation"; echo "ABORT"; exit 1; fi
assert_status "$CID" "SUBMITTED" "claim is SUBMITTED"

echo "== 2. Agent acknowledges and assigns a surveyor =="
login agent "$CJ_A"
curl -s -b "$CJ_A" -o /dev/null --data-urlencode "claimId=$CID" "$B/agent/acknowledge"
assert_status "$CID" "UNDER_REVIEW" "claim acknowledged"
curl -s -b "$CJ_A" -o /dev/null --data-urlencode "claimId=$CID" --data-urlencode "surveyorId=5" "$B/agent/assignSurveyor"
assert_status "$CID" "SURVEY_SCHEDULED" "surveyor assigned"

echo "== 3. Surveyor submits assessment (net payable computed) =="
login surveyor "$CJ_S"
curl -s -b "$CJ_S" -o /dev/null \
    --data-urlencode "id=$CID" --data-urlencode "visitDate=2026-06-05" \
    --data-urlencode "compName=Front Bumper" --data-urlencode "compSeverity=SEVERE" --data-urlencode "compCost=60000" --data-urlencode "compReplace=true" \
    --data-urlencode "compName=Bonnet" --data-urlencode "compSeverity=MODERATE" --data-urlencode "compCost=40000" --data-urlencode "compReplace=false" \
    --data-urlencode "policyDeductible=5000" --data-urlencode "depreciationPct=0" --data-urlencode "salvageValue=0" \
    --data-urlencode "recommendation=PARTIAL_APPROVE" "$B/surveyor/submitAssessment"
assert_status "$CID" "UNDER_ASSESSMENT" "assessment submitted"
NET=$(mysql "${MYSQL_OPTS[@]}" icms -N -e "SELECT net_payable FROM assessments WHERE claim_id=$CID;" 2>/dev/null)
if [ "$NET" = "95000.00" ]; then ok "net payable computed = $NET"; else bad "net payable (expected 95000.00, got '$NET')"; fi

echo "== 4. Agent forwards for approval =="
curl -s -b "$CJ_A" -o /dev/null --data-urlencode "claimId=$CID" "$B/agent/forward"
assert_status "$CID" "PENDING_APPROVAL" "forwarded for approval"
PEND=$(mysql "${MYSQL_OPTS[@]}" icms -N -e "SELECT COUNT(*) FROM approvals WHERE claim_id=$CID AND decision='PENDING';" 2>/dev/null)
if [ "$PEND" -ge 1 ]; then ok "manager approval pending (count=$PEND)"; else bad "pending approval row"; fi

echo "== 5. Manager approves =="
login manager "$CJ_M"
curl -s -b "$CJ_M" -o /dev/null --data-urlencode "claimId=$CID" --data-urlencode "decision=APPROVED" --data-urlencode "remarks=E2E approve" "$B/manager/decide"
assert_status "$CID" "APPROVED" "manager approved"

echo "== 6. Agent processes settlement =="
curl -s -b "$CJ_A" -o /dev/null --data-urlencode "id=$CID" --data-urlencode "amount=95000" \
    --data-urlencode "paymentMethod=NEFT" --data-urlencode "accountHolder=Ravi Patel" \
    --data-urlencode "justification=E2E settlement" "$B/agent/processSettlement"
assert_status "$CID" "SETTLEMENT_PROCESSING" "settlement authorised"
for n in 1 2 3; do curl -s -b "$CJ_A" -o /dev/null --data-urlencode "id=$CID" "$B/agent/advanceSettlement"; done
assert_status "$CID" "SETTLED" "settlement confirmed -> claim SETTLED"
SPAY=$(mysql "${MYSQL_OPTS[@]}" icms -N -e "SELECT status FROM settlements WHERE claim_id=$CID;" 2>/dev/null)
if [ "$SPAY" = "PAYMENT_CONFIRMED" ]; then ok "settlement payment CONFIRMED"; else bad "settlement payment (got '$SPAY')"; fi

echo ""
echo "============================================================"
echo " Smoke test complete: $PASS passed, $FAIL failed."
echo "============================================================"
[ "$FAIL" -eq 0 ] || exit 1
