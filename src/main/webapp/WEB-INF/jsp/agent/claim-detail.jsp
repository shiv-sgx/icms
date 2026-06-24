<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:set var="claim" value="bundle.claim" />
<s:set var="st" value="bundle.claim.status" />

<div class="page-head with-action">
    <div>
        <h1 class="page-title"><s:property value="#claim.claimNo" />
            <span class="pill <s:property value='#claim.statusPill'/>"><s:property value="#claim.statusLabel" /></span>
            <span class="pill risk-<s:property value='#claim.riskLevel'/>"><s:property value="#claim.riskLevel" /> RISK</span>
        </h1>
        <p class="page-sub"><s:property value="#claim.claimType" /><s:if test="#claim.claimSubtype != null"> · <s:property value="#claim.claimSubtype" /></s:if>
            &middot; <s:property value="#claim.claimantName" /> &middot; Policy <s:property value="#claim.policyNo" /></p>
    </div>
    <s:a action="claims" namespace="/agent" cssClass="btn btn-light">&laquo; Back</s:a>
</div>

<s:if test="flashMessage != null">
    <div class="alert alert-<s:property value='flashType'/>"><s:property value="flashMessage" /></div>
</s:if>

<!-- Action bar -->
<div class="action-bar">
    <s:if test="#st == 'SUBMITTED'">
        <s:form action="acknowledge" namespace="/agent" method="post" cssClass="inline-form">
            <s:hidden name="claimId" value="%{#claim.id}" />
            <button type="submit" class="btn btn-primary">Acknowledge</button>
        </s:form>
    </s:if>

    <s:if test="#st == 'SUBMITTED' || #st == 'UNDER_REVIEW' || #st == 'SURVEY_SCHEDULED' || #st == 'UNDER_ASSESSMENT'">
        <s:form action="assignSurveyor" namespace="/agent" method="post" cssClass="inline-form">
            <s:hidden name="claimId" value="%{#claim.id}" />
            <select name="surveyorId" class="input" required>
                <option value="">Assign surveyor…</option>
                <s:iterator value="surveyors">
                    <option value="<s:property value='id'/>" <s:if test="id == #claim.surveyorId">selected</s:if>><s:property value="fullName" /> (<s:property value="branch" />)</option>
                </s:iterator>
            </select>
            <button type="submit" class="btn btn-primary">Assign</button>
        </s:form>
    </s:if>

    <s:if test="#st == 'UNDER_REVIEW' || #st == 'UNDER_ASSESSMENT' || #st == 'SURVEY_SCHEDULED' || #st == 'ON_HOLD'">
        <s:form action="forward" namespace="/agent" method="post" cssClass="inline-form">
            <s:hidden name="claimId" value="%{#claim.id}" />
            <button type="submit" class="btn btn-primary" data-confirm="Forward this claim for approval?">Forward for Approval</button>
        </s:form>
    </s:if>

    <s:if test="#st == 'APPROVED' || #st == 'SETTLEMENT_PROCESSING' || #st == 'SETTLED' || #st == 'CLOSED'">
        <s:a action="settlement" namespace="/agent" cssClass="btn btn-primary"><s:param name="id" value="#claim.id" />Settlement</s:a>
    </s:if>
</div>

<!-- Timeline -->
<div class="panel">
    <div class="panel-head">Claim Status Timeline</div>
    <div class="panel-body">
        <ol class="timeline">
            <s:iterator value="bundle.timeline">
                <li class="tl-node tl-<s:property value='state'/>"><span class="tl-dot"></span><span class="tl-label"><s:property value="label" /></span></li>
            </s:iterator>
        </ol>
    </div>
</div>

<div class="grid-2">
    <!-- Summary + internal notes -->
    <div class="panel">
        <div class="panel-head">Claim Summary</div>
        <div class="panel-body">
            <dl class="kv">
                <dt>Incident</dt><dd><s:property value="#claim.incidentDate" /> <s:property value="#claim.incidentTime" /></dd>
                <dt>Location</dt><dd><s:property value="#claim.incidentLocation" /> <s:property value="#claim.city" /></dd>
                <dt>Estimated Loss</dt><dd>&#8377; <s:property value="#claim.estimatedLoss" /></dd>
                <dt>Fraud Score</dt><dd><s:property value="#claim.fraudScore" /> / 100</dd>
                <dt>Surveyor</dt><dd><s:property value="#claim.surveyorName" /><s:if test="#claim.surveyorName == null">Not assigned</s:if></dd>
            </dl>
            <p class="desc"><s:property value="#claim.description" /></p>
            <s:form action="saveNote" namespace="/agent" method="post">
                <s:hidden name="claimId" value="%{#claim.id}" />
                <label class="lbl">Internal Notes</label>
                <textarea name="notes" class="input" rows="2"><s:property value="#claim.internalNotes"/></textarea>
                <button type="submit" class="btn btn-light btn-sm">Save Notes</button>
            </s:form>
        </div>
    </div>

    <!-- Documents -->
    <div class="panel">
        <div class="panel-head">Documents</div>
        <div class="panel-body no-pad">
            <table class="table">
                <thead><tr><th>Document</th><th>Status</th><th>Verification</th></tr></thead>
                <tbody>
                <s:iterator value="bundle.documents">
                    <tr>
                        <td><s:property value="docType" /><s:if test="fileName != null"><br/><span class="muted small"><s:property value="fileName" /></span></s:if></td>
                        <td><span class="pill <s:if test='uploaded'>pill-ok</s:if><s:else>pill-warn</s:else>"><s:property value="uploadStatus" /></span></td>
                        <td><span class="pill pill-muted"><s:property value="verificationStatus" /></span></td>
                    </tr>
                </s:iterator>
                <s:if test="bundle.documents.isEmpty()"><tr><td colspan="3" class="empty">No documents.</td></tr></s:if>
                </tbody>
            </table>
        </div>
    </div>
</div>

<!-- Assessment review -->
<s:if test="bundle.hasAssessment">
    <div class="panel">
        <div class="panel-head">Assessment Report <span class="muted">by <s:property value="bundle.assessment.surveyorName" /></span></div>
        <div class="panel-body">
            <div class="card-grid">
                <div class="stat-card"><div class="stat-label">Gross Assessed</div><div class="stat-value">&#8377; <s:property value="bundle.assessment.grossAssessed" /></div></div>
                <div class="stat-card"><div class="stat-label">Deductible</div><div class="stat-value">&#8377; <s:property value="bundle.assessment.policyDeductible" /></div></div>
                <div class="stat-card"><div class="stat-label">Net Payable</div><div class="stat-value">&#8377; <s:property value="bundle.assessment.netPayable" /></div></div>
                <div class="stat-card"><div class="stat-label">Recommendation</div><div class="stat-value small"><s:property value="bundle.assessment.recommendation" /></div></div>
            </div>
            <s:if test="!bundle.components.isEmpty()">
                <table class="table">
                    <thead><tr><th>Component</th><th>Severity</th><th>Repair Cost</th><th>Replace?</th></tr></thead>
                    <tbody>
                    <s:iterator value="bundle.components">
                        <tr><td><s:property value="component" /></td><td><s:property value="severity" /></td><td>&#8377; <s:property value="repairCost" /></td><td><s:if test="replaceFlag">Yes</s:if><s:else>No</s:else></td></tr>
                    </s:iterator>
                    </tbody>
                </table>
            </s:if>
        </div>
    </div>
</s:if>

<!-- Approvals chain -->
<s:if test="!bundle.approvals.isEmpty()">
    <div class="panel">
        <div class="panel-head">Approval Workflow</div>
        <div class="panel-body no-pad">
            <table class="table">
                <thead><tr><th>Level</th><th>Role</th><th>Approver</th><th>Decision</th><th>Remarks</th></tr></thead>
                <tbody>
                <s:iterator value="bundle.approvals">
                    <tr>
                        <td><s:property value="level" /></td>
                        <td><s:property value="approverRole" /></td>
                        <td><s:property value="approverName" /></td>
                        <td><span class="pill <s:if test='decision == \"APPROVED\"'>pill-ok</s:if><s:elseif test='decision == \"PENDING\"'>pill-warn</s:elseif><s:else>pill-danger</s:else>"><s:property value="decision" /></span></td>
                        <td><s:property value="remarks" /></td>
                    </tr>
                </s:iterator>
                </tbody>
            </table>
        </div>
    </div>
</s:if>

<!-- Communication -->
<div class="panel">
    <div class="panel-head">Communication Center</div>
    <div class="panel-body">
        <div class="thread">
            <s:iterator value="bundle.messages">
                <div class="msg <s:if test='senderId == #session.ICMS_USER.id'>msg-own</s:if>">
                    <div class="msg-meta"><s:property value="senderName" /> &middot; <s:property value="createdAt" /></div>
                    <div class="msg-body"><s:property value="content" /></div>
                </div>
            </s:iterator>
            <s:if test="bundle.messages.isEmpty()"><p class="empty">No messages yet.</p></s:if>
        </div>
        <s:form action="message" namespace="/agent" method="post" cssClass="msg-form">
            <s:hidden name="claimId" value="%{#claim.id}" />
            <input type="text" name="content" class="input" placeholder="Message the claimant..." required />
            <button type="submit" class="btn btn-primary">Send</button>
        </s:form>
    </div>
</div>
