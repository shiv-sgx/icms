<#
.SYNOPSIS
    ICMS end-to-end smoke test (Windows-native PowerShell port of smoke-test.sh).

.DESCRIPTION
    Drives the full claim lifecycle across all five role portals against the
    running app and asserts every transition at BOTH layers:
      * HTTP  - the action redirected somewhere other than /login (i.e. the
                role was authenticated and the request was accepted).
      * DB    - the resulting claim / settlement status in MySQL.

      Customer submit -> Agent acknowledge/assign -> Surveyor assess ->
      Agent forward -> Manager approve -> Agent settle -> SETTLED

    The app exposes no REST API: it is Struts2 form-POST + 302 redirects with
    HTTP-session auth, so each role keeps its own WebSession (JSESSIONID cookie).

.NOTES
    Prereqs: app running (./run.sh) and DB seeded (./setup.sh).
    Usage:   .\smoke-test.ps1
             .\smoke-test.ps1 -MysqlExe '..\tools\mysql-8.0\bin\mysql.exe'
             $env:BASE_URL='http://localhost:8080'; .\smoke-test.ps1
#>
[CmdletBinding()]
param(
    [string]$BaseUrl      = $(if ($env:BASE_URL)     { $env:BASE_URL }     else { 'http://localhost:8080' }),
    [string]$DemoPassword = $(if ($env:DEMO_PASSWORD){ $env:DEMO_PASSWORD } else { 'Password@123' }),
    [string]$DbHost       = $(if ($env:DB_HOST)      { $env:DB_HOST }      else { '127.0.0.1' }),
    [string]$DbUser       = $(if ($env:DB_USER)      { $env:DB_USER }      else { 'root' }),
    [string]$DbPassword   = $(if ($env:DB_PASSWORD)  { $env:DB_PASSWORD }  else { '' }),
    [string]$MysqlExe     = $(if ($env:MYSQL)        { $env:MYSQL }        else { 'mysql' })
)

$ErrorActionPreference = 'Stop'

# ---- mysql client options -------------------------------------------------
# Pass the password via MYSQL_PWD rather than -p so the client does not print
# the "Using a password ... is insecure" warning to stderr (which, under
# $ErrorActionPreference='Stop', PowerShell 5.1 would turn into a thrown error).
$MysqlArgs = @('-h', $DbHost, '-u', $DbUser)
if ($DbPassword) { $env:MYSQL_PWD = $DbPassword }

# ---- pass/fail bookkeeping ------------------------------------------------
$script:Pass = 0
$script:Fail = 0
function Ok([string]$m)  { Write-Host "  PASS: $m" -ForegroundColor Green; $script:Pass++ }
function Bad([string]$m) { Write-Host "  FAIL: $m" -ForegroundColor Red;   $script:Fail++ }

# ---- helpers --------------------------------------------------------------

# Final URI of a (possibly auto-redirected) response, across PS 5.1 and 7+.
function Get-FinalUri($resp) {
    $br = $resp.BaseResponse
    if ($br -and ($br.PSObject.Properties.Name -contains 'ResponseUri') -and $br.ResponseUri) {
        return $br.ResponseUri                              # Windows PowerShell 5.1
    }
    if ($br -and ($br.PSObject.Properties.Name -contains 'RequestMessage') -and $br.RequestMessage) {
        return $br.RequestMessage.RequestUri               # PowerShell 7+
    }
    return $null
}

# Path component without Tomcat's URL-rewriting matrix param (";jsessionid=..."),
# which it appends on the first request of a session (before cookies are known).
function Get-CleanPath($uri) {
    if (-not $uri) { return '' }
    return ($uri.AbsolutePath -split ';')[0]
}

# Log in a role, following the redirect so the session keeps its JSESSIONID.
# Returns: @{ Session; Path; Ok }
function Connect-Role([string]$User) {
    $body = @{ username = $User; password = $DemoPassword }
    try {
        $resp = Invoke-WebRequest -Uri "$BaseUrl/doLogin" -Method Post -Body $body `
                    -SessionVariable s -UseBasicParsing
        $uri  = Get-FinalUri $resp
        return [pscustomobject]@{ Session = $s; Path = (Get-CleanPath $uri); Ok = $true }
    } catch {
        return [pscustomobject]@{ Session = $null; Path = ''; Ok = $false; Error = $_.Exception.Message }
    }
}

# POST a form on an existing session. $Body may be a hashtable (auto-encoded)
# or a pre-encoded application/x-www-form-urlencoded string (for repeated keys).
# Returns: @{ Ok; Code; Path; Query; Error }
function Invoke-Action($Session, [string]$Url, $Body) {
    $reqArgs = @{ Uri = $Url; Method = 'Post'; Body = $Body; WebSession = $Session; UseBasicParsing = $true }
    if ($Body -is [string]) { $reqArgs['ContentType'] = 'application/x-www-form-urlencoded' }
    try {
        $resp = Invoke-WebRequest @reqArgs
        $uri  = Get-FinalUri $resp
        return [pscustomobject]@{
            Ok    = $true
            Code  = [int]$resp.StatusCode
            Path  = (Get-CleanPath $uri)
            Query = $(if ($uri) { $uri.Query } else { '' })
        }
    } catch {
        return [pscustomobject]@{ Ok = $false; Code = 0; Path = ''; Query = ''; Error = $_.Exception.Message }
    }
}

function UrlEncode([string]$s) { [System.Uri]::EscapeDataString($s) }

# HTTP-layer assertion: the action was accepted (didn't bounce to /login, no
# transport error).
function Assert-Http($Result, [string]$Label) {
    if ($Result.Ok -and $Result.Code -ge 200 -and $Result.Code -lt 400 -and $Result.Path -notmatch '/login') {
        Ok "$Label (HTTP $($Result.Code) -> $($Result.Path))"
    } else {
        Bad "$Label (HTTP code=$($Result.Code) path='$($Result.Path)' $($Result.Error))"
    }
}

# Run a scalar query against the icms DB; returns trimmed first value or ''.
# Localizes ErrorActionPreference so any native-client stderr does not throw.
function Get-Scalar([string]$Sql) {
    $eap = $ErrorActionPreference
    $ErrorActionPreference = 'Continue'
    try {
        $out = & $MysqlExe @MysqlArgs --batch --skip-column-names icms -e $Sql 2>$null
    } catch {
        $out = $null
    } finally {
        $ErrorActionPreference = $eap
    }
    if ($null -eq $out) { return '' }
    return ([string](@($out)[0])).Trim()
}

function Get-ClaimStatus([string]$Id) { Get-Scalar "SELECT status FROM claims WHERE id=$Id;" }

# DB-layer assertion of claim status.
function Assert-Status([string]$Id, [string]$Expected, [string]$Label) {
    $got = Get-ClaimStatus $Id
    if ($got -eq $Expected) { Ok "$Label (status=$got)" }
    else { Bad "$Label (expected $Expected, got '$got')" }
}

# ===========================================================================
Write-Host "ICMS smoke test against $BaseUrl"
Write-Host ""

# ---- 1. Customer submits a claim -----------------------------------------
Write-Host "== 1. Customer submits a claim =="
$cust = Connect-Role 'customer'
if ($cust.Ok -and $cust.Path -eq '/customer/dashboard') { Ok "customer login -> $($cust.Path)" }
else { Bad "customer login (expected /customer/dashboard, got '$($cust.Path)' $($cust.Error))" }

$createBody = @{
    policyId         = '1'
    claimSubtype     = 'Accident'
    incidentDate     = '2026-06-01'
    incidentLocation = 'Smoke Test Rd'
    city             = 'Mumbai'
    description      = 'E2E smoke test claim'
    estimatedLoss    = '90000'
    mode             = 'submit'
}
$r = Invoke-Action $cust.Session "$BaseUrl/customer/createClaim" $createBody
Assert-Http $r "claim submit"

$cid = $null
if ($r.Query -match 'id=(\d+)') { $cid = $Matches[1] }
elseif ($r.Path -match 'id=(\d+)') { $cid = $Matches[1] }
if ($cid) { Ok "claim created (id=$cid)" }
else { Bad "claim creation (no id in redirect '$($r.Path)$($r.Query)')"; Write-Host "ABORT"; exit 1 }
Assert-Status $cid 'SUBMITTED' 'claim is SUBMITTED'

# ---- 2. Agent acknowledges and assigns a surveyor ------------------------
Write-Host "== 2. Agent acknowledges and assigns a surveyor =="
$agent = Connect-Role 'agent'
if ($agent.Ok -and $agent.Path -eq '/agent/dashboard') { Ok "agent login -> $($agent.Path)" }
else { Bad "agent login (expected /agent/dashboard, got '$($agent.Path)' $($agent.Error))" }

$r = Invoke-Action $agent.Session "$BaseUrl/agent/acknowledge" @{ claimId = $cid }
Assert-Http $r "agent acknowledge"
Assert-Status $cid 'UNDER_REVIEW' 'claim acknowledged'

$r = Invoke-Action $agent.Session "$BaseUrl/agent/assignSurveyor" @{ claimId = $cid; surveyorId = '5' }
Assert-Http $r "assign surveyor"
Assert-Status $cid 'SURVEY_SCHEDULED' 'surveyor assigned'

# ---- 3. Surveyor submits assessment (net payable computed) ---------------
Write-Host "== 3. Surveyor submits assessment (net payable computed) =="
$surv = Connect-Role 'surveyor'
if ($surv.Ok -and $surv.Path -eq '/surveyor/dashboard') { Ok "surveyor login -> $($surv.Path)" }
else { Bad "surveyor login (expected /surveyor/dashboard, got '$($surv.Path)' $($surv.Error))" }

# Repeated component params -> Struts parallel lists; build the body by hand
# because a hashtable cannot carry duplicate keys.
$assessBody = (@(
    "id=$cid",
    "visitDate=2026-06-05",
    "compName=$(UrlEncode 'Front Bumper')", "compSeverity=SEVERE",   "compCost=60000", "compReplace=true",
    "compName=$(UrlEncode 'Bonnet')",       "compSeverity=MODERATE", "compCost=40000", "compReplace=false",
    "policyDeductible=5000", "depreciationPct=0", "salvageValue=0",
    "recommendation=PARTIAL_APPROVE"
) -join '&')
$r = Invoke-Action $surv.Session "$BaseUrl/surveyor/submitAssessment" $assessBody
Assert-Http $r "submit assessment"
Assert-Status $cid 'UNDER_ASSESSMENT' 'assessment submitted'

$net = Get-Scalar "SELECT net_payable FROM assessments WHERE claim_id=$cid;"
if ($net -eq '95000.00') { Ok "net payable computed = $net" }
else { Bad "net payable (expected 95000.00, got '$net')" }

# ---- 4. Agent forwards for approval --------------------------------------
Write-Host "== 4. Agent forwards for approval =="
$r = Invoke-Action $agent.Session "$BaseUrl/agent/forward" @{ claimId = $cid }
Assert-Http $r "forward for approval"
Assert-Status $cid 'PENDING_APPROVAL' 'forwarded for approval'

$pend = Get-Scalar "SELECT COUNT(*) FROM approvals WHERE claim_id=$cid AND decision='PENDING';"
if ($pend -and [int]$pend -ge 1) { Ok "manager approval pending (count=$pend)" }
else { Bad "pending approval row (count='$pend')" }

# ---- 5. Manager approves --------------------------------------------------
Write-Host "== 5. Manager approves =="
$mgr = Connect-Role 'manager'
if ($mgr.Ok -and $mgr.Path -eq '/manager/dashboard') { Ok "manager login -> $($mgr.Path)" }
else { Bad "manager login (expected /manager/dashboard, got '$($mgr.Path)' $($mgr.Error))" }

$r = Invoke-Action $mgr.Session "$BaseUrl/manager/decide" @{ claimId = $cid; decision = 'APPROVED'; remarks = 'E2E approve' }
Assert-Http $r "manager decide"
Assert-Status $cid 'APPROVED' 'manager approved'

# ---- 6. Agent processes settlement ---------------------------------------
Write-Host "== 6. Agent processes settlement =="
$r = Invoke-Action $agent.Session "$BaseUrl/agent/processSettlement" @{
    id            = $cid
    amount        = '95000'
    paymentMethod = 'NEFT'
    accountHolder = 'James Miller'
    justification = 'E2E settlement'
}
Assert-Http $r "process settlement"
Assert-Status $cid 'SETTLEMENT_PROCESSING' 'settlement authorised'

foreach ($n in 1..3) {
    $a = Invoke-Action $agent.Session "$BaseUrl/agent/advanceSettlement" @{ id = $cid }
    Assert-Http $a "advance settlement #$n"
}
Assert-Status $cid 'SETTLED' 'settlement confirmed -> claim SETTLED'

$spay = Get-Scalar "SELECT status FROM settlements WHERE claim_id=$cid;"
if ($spay -eq 'PAYMENT_CONFIRMED') { Ok "settlement payment CONFIRMED" }
else { Bad "settlement payment (got '$spay')" }

# ---- summary --------------------------------------------------------------
Write-Host ""
Write-Host "============================================================"
Write-Host " Smoke test complete: $($script:Pass) passed, $($script:Fail) failed."
Write-Host "============================================================"
if ($script:Fail -gt 0) { exit 1 }
exit 0
