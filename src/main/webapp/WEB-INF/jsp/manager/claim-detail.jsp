<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:set var="claim" value="bundle.claim" />

<div class="page-head with-action">
    <div>
        <h1 class="page-title"><s:property value="#claim.claimNo" />
            <span class="pill <s:property value='#claim.statusPill'/>"><s:property value="#claim.statusLabel" /></span>
            <span class="pill risk-<s:property value='#claim.riskLevel'/>"><s:property value="#claim.riskLevel" /> RISK</span>
        </h1>
        <p class="page-sub"><s:property value="#claim.claimType" /> &middot; <s:property value="#claim.claimantName" /> &middot; Fraud score <s:property value="#claim.fraudScore" />/100</p>
    </div>
    <s:a action="approvals" namespace="/manager" cssClass="btn btn-light">&laquo; Queue</s:a>
</div>

<s:if test="flashMessage != null">
    <div class="alert alert-<s:property value='flashType'/>"><s:property value="flashMessage" /></div>
</s:if>

<!-- Approval decision -->
<s:if test="#claim.status == 'PENDING_APPROVAL'">
    <div class="panel decision-panel">
        <div class="panel-head">Approval Decision</div>
        <div class="panel-body">
            <s:form action="decide" namespace="/manager" method="post" cssClass="decision-form">
                <s:hidden name="claimId" value="%{#claim.id}" />
                <div class="form-row">
                    <div class="field">
                        <label>Decision</label>
                        <select name="decision" class="input">
                            <option value="APPROVED">Approve</option>
                            <option value="REJECTED">Reject</option>
                            <option value="RETURNED">Return to Agent</option>
                            <option value="ON_HOLD">Put On Hold</option>
                        </select>
                    </div>
                    <div class="field grow"><label>Remarks</label><input type="text" name="remarks" class="input" placeholder="Reason / notes" /></div>
                </div>
                <div class="form-actions"><button type="submit" class="btn btn-primary">Submit Decision</button></div>
            </s:form>
        </div>
    </div>
</s:if>

<!-- Timeline -->
<div class="panel">
    <div class="panel-head">Status Timeline</div>
    <div class="panel-body">
        <ol class="timeline">
            <s:iterator value="bundle.timeline">
                <li class="tl-node tl-<s:property value='state'/>"><span class="tl-dot"></span><span class="tl-label"><s:property value="label" /></span></li>
            </s:iterator>
        </ol>
    </div>
</div>

<div class="grid-2">
    <div class="panel">
        <div class="panel-head">Claim Summary</div>
        <div class="panel-body">
            <dl class="kv">
                <dt>Policy</dt><dd><s:property value="#claim.policyNo" /></dd>
                <dt>Estimated Loss</dt><dd>&#8377; <s:property value="#claim.estimatedLoss" /></dd>
                <dt>Agent</dt><dd><s:property value="#claim.agentName" /></dd>
                <dt>Surveyor</dt><dd><s:property value="#claim.surveyorName" /></dd>
            </dl>
            <p class="desc"><s:property value="#claim.description" /></p>
        </div>
    </div>

    <!-- Assessment summary -->
    <div class="panel">
        <div class="panel-head">Assessment</div>
        <div class="panel-body">
            <s:if test="bundle.hasAssessment">
                <dl class="kv">
                    <dt>Gross Assessed</dt><dd>&#8377; <s:property value="bundle.assessment.grossAssessed" /></dd>
                    <dt>Net Payable</dt><dd class="big">&#8377; <s:property value="bundle.assessment.netPayable" /></dd>
                    <dt>Recommendation</dt><dd><s:property value="bundle.assessment.recommendation" /></dd>
                    <dt>Surveyor</dt><dd><s:property value="bundle.assessment.surveyorName" /></dd>
                </dl>
            </s:if>
            <s:else><p class="muted">No assessment submitted yet.</p></s:else>
        </div>
    </div>
</div>

<!-- Approval chain -->
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

<!-- Settlement override -->
<s:if test="bundle.hasSettlement">
    <div class="panel">
        <div class="panel-head">Settlement Override</div>
        <div class="panel-body">
            <dl class="kv">
                <dt>Current Amount</dt><dd class="big">&#8377; <s:property value="bundle.settlement.finalAmount" /></dd>
                <dt>Status</dt><dd><span class="pill pill-info"><s:property value="bundle.settlement.status" /></span></dd>
            </dl>
            <s:form action="overrideSettlement" namespace="/manager" method="post">
                <s:hidden name="claimId" value="%{#claim.id}" />
                <div class="form-row">
                    <div class="field"><label>Override Amount (&#8377;)</label><input type="number" step="0.01" min="0" name="amount" class="input" required /></div>
                    <div class="field grow"><label>Justification</label><input type="text" name="justification" class="input" /></div>
                </div>
                <button type="submit" class="btn btn-light" data-confirm="Override the settlement amount?">Override Settlement</button>
            </s:form>
        </div>
    </div>
</s:if>
