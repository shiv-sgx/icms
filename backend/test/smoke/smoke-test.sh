#!/usr/bin/env bash
# ============================================================================
# ICMS end-to-end smoke test — NODE/REST + JWT edition.
# Drives the full claim lifecycle across all five roles against the new API and
# asserts every transition against the SAME MySQL database (proving parity with
# the legacy Struts app's smoke-test.sh):
#   Customer submit -> Agent ack/assign -> Surveyor assess -> Agent forward ->
#   Manager approve -> Agent settle -> SETTLED
#
# Prereqs: backend running (npm start) and DB seeded.
# Usage: BASE_URL=http://localhost:3000/api/v1 ./smoke-test.sh
# ============================================================================
set -uo pipefail

B="${BASE_URL:-http://localhost:3000/api/v1}"
PW="${DEMO_PASSWORD:-Password@123}"
DB_HOST="${DB_HOST:-127.0.0.1}"; DB_USER="${DB_USER:-root}"; DB_PASSWORD="${DB_PASSWORD:-}"
MYSQL_OPTS=(-h "$DB_HOST" -u "$DB_USER")
[ -n "$DB_PASSWORD" ] && MYSQL_OPTS+=(-p"$DB_PASSWORD")

PASS=0; FAIL=0
ok()  { echo "  PASS: $1"; PASS=$((PASS+1)); }
bad() { echo "  FAIL: $1"; FAIL=$((FAIL+1)); }

# Extract a JSON field via node (no jq dependency).
jfield() { node -e "let d='';process.stdin.on('data',c=>d+=c).on('end',()=>{try{const o=JSON.parse(d);console.log(process.argv[1].split('.').reduce((a,k)=>a&&a[k],o)??'')}catch(e){console.log('')}})" "$1"; }

login() { # login <user> -> token
    curl -s -X POST "$B/auth/login" -H 'Content-Type: application/json' \
        -d "{\"username\":\"$1\",\"password\":\"$PW\"}" | jfield data.token
}
post() { # post <token> <path> <json>
    curl -s -X POST "$B$2" -H "Authorization: Bearer $1" -H 'Content-Type: application/json' -d "$3"
}
claim_status() { mysql "${MYSQL_OPTS[@]}" icms -N -e "SELECT status FROM claims WHERE id=$1;" 2>/dev/null; }
assert_status() { local got; got="$(claim_status "$1")"; [ "$got" = "$2" ] && ok "$3 (status=$got)" || bad "$3 (expected $2, got '$got')"; }

TOK_C=$(login customer); TOK_A=$(login agent); TOK_S=$(login surveyor); TOK_M=$(login manager)
[ -n "$TOK_C" ] && ok "customer authenticated (JWT)" || { bad "login"; exit 1; }

echo "== 1. Customer submits a claim =="
RESP=$(post "$TOK_C" "/customer/claims" '{"policyId":1,"claimSubtype":"Accident","incidentDate":"2026-06-01","incidentLocation":"Smoke Test Rd","city":"Mumbai","description":"E2E smoke test claim","estimatedLoss":"90000","mode":"submit"}')
CID=$(echo "$RESP" | jfield data.id)
if [ -n "$CID" ]; then ok "claim created (id=$CID)"; else bad "claim creation: $RESP"; exit 1; fi
assert_status "$CID" "SUBMITTED" "claim is SUBMITTED"

echo "== 2. Agent acknowledges and assigns a surveyor =="
post "$TOK_A" "/agent/claims/$CID/acknowledge" '{}' >/dev/null
assert_status "$CID" "UNDER_REVIEW" "claim acknowledged"
post "$TOK_A" "/agent/claims/$CID/assign-surveyor" '{"surveyorId":5}' >/dev/null
assert_status "$CID" "SURVEY_SCHEDULED" "surveyor assigned"

echo "== 3. Surveyor submits assessment (net payable computed server-side) =="
post "$TOK_S" "/surveyor/claims/$CID/assessment" '{"visitDate":"2026-06-05","policyDeductible":"5000","depreciationPct":"0","salvageValue":"0","recommendation":"PARTIAL_APPROVE","components":[{"component":"Front Bumper","severity":"SEVERE","repairCost":"60000","replaceFlag":true},{"component":"Bonnet","severity":"MODERATE","repairCost":"40000","replaceFlag":false}]}' >/dev/null
assert_status "$CID" "UNDER_ASSESSMENT" "assessment submitted"
NET=$(mysql "${MYSQL_OPTS[@]}" icms -N -e "SELECT net_payable FROM assessments WHERE claim_id=$CID ORDER BY id DESC LIMIT 1;" 2>/dev/null)
[ "$NET" = "95000.00" ] && ok "net payable computed = $NET" || bad "net payable (expected 95000.00, got '$NET')"

echo "== 4. Agent forwards for approval =="
post "$TOK_A" "/agent/claims/$CID/forward" '{}' >/dev/null
assert_status "$CID" "PENDING_APPROVAL" "forwarded for approval"
PEND=$(mysql "${MYSQL_OPTS[@]}" icms -N -e "SELECT COUNT(*) FROM approvals WHERE claim_id=$CID AND decision='PENDING';" 2>/dev/null)
[ "${PEND:-0}" -ge 1 ] && ok "manager approval pending (count=$PEND)" || bad "pending approval row"

echo "== 5. Manager approves =="
post "$TOK_M" "/manager/claims/$CID/decision" '{"decision":"APPROVED","remarks":"E2E approve"}' >/dev/null
assert_status "$CID" "APPROVED" "manager approved"

echo "== 6. Agent processes settlement =="
post "$TOK_A" "/agent/claims/$CID/settlement" '{"amount":"95000","paymentMethod":"NEFT","accountHolder":"Ravi Patel","justification":"E2E settlement"}' >/dev/null
assert_status "$CID" "SETTLEMENT_PROCESSING" "settlement authorised"
for n in 1 2 3; do post "$TOK_A" "/agent/claims/$CID/settlement/advance" '{}' >/dev/null; done
assert_status "$CID" "SETTLED" "settlement confirmed -> claim SETTLED"
SPAY=$(mysql "${MYSQL_OPTS[@]}" icms -N -e "SELECT status FROM settlements WHERE claim_id=$CID;" 2>/dev/null)
[ "$SPAY" = "PAYMENT_CONFIRMED" ] && ok "settlement payment CONFIRMED" || bad "settlement payment (got '$SPAY')"

echo ""
echo "============================================================"
echo " Smoke test complete: $PASS passed, $FAIL failed."
echo "============================================================"
[ "$FAIL" -eq 0 ] || exit 1
