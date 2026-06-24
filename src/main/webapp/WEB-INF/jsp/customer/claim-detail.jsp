<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div class="page-head with-action">
    <div>
        <h1 class="page-title"><s:property value="claim.claimNo" />
            <span class="pill <s:property value='claim.statusPill'/>"><s:property value="claim.statusLabel" /></span>
        </h1>
        <p class="page-sub"><s:property value="claim.claimType" /><s:if test="claim.claimSubtype != null"> · <s:property value="claim.claimSubtype" /></s:if> &middot; Policy <s:property value="claim.policyNo" /></p>
    </div>
    <s:a action="claims" namespace="/customer" cssClass="btn btn-light">&laquo; Back to claims</s:a>
</div>

<s:if test="flashMessage != null">
    <div class="alert alert-<s:property value='flashType'/>"><s:property value="flashMessage" /></div>
</s:if>

<!-- Status timeline -->
<div class="panel">
    <div class="panel-head">Claim Status Timeline</div>
    <div class="panel-body">
        <ol class="timeline">
            <s:iterator value="timeline">
                <li class="tl-node tl-<s:property value='state'/>">
                    <span class="tl-dot"></span>
                    <span class="tl-label"><s:property value="label" /></span>
                </li>
            </s:iterator>
        </ol>
    </div>
</div>

<div class="grid-2">
    <!-- Claim summary -->
    <div class="panel">
        <div class="panel-head">Claim Summary</div>
        <div class="panel-body">
            <dl class="kv">
                <dt>Claimant</dt><dd><s:property value="claim.claimantName" /></dd>
                <dt>Incident Date</dt><dd><s:property value="claim.incidentDate" /> <s:property value="claim.incidentTime" /></dd>
                <dt>Location</dt><dd><s:property value="claim.incidentLocation" /> <s:property value="claim.city" /> <s:property value="claim.state" /></dd>
                <dt>Estimated Loss</dt><dd>&#8377; <s:property value="claim.estimatedLoss" /></dd>
                <s:if test="claim.vehicleRegNo != null"><dt>Vehicle No.</dt><dd><s:property value="claim.vehicleRegNo" /></dd></s:if>
                <s:if test="claim.firNumber != null"><dt>FIR Number</dt><dd><s:property value="claim.firNumber" /></dd></s:if>
                <s:if test="claim.hospitalName != null"><dt>Hospital</dt><dd><s:property value="claim.hospitalName" /></dd></s:if>
                <dt>Agent</dt><dd><s:property value="claim.agentName" /><s:if test="claim.agentName == null">Not yet assigned</s:if></dd>
                <dt>Filed</dt><dd><s:property value="claim.filedAt" /></dd>
            </dl>
            <p class="desc"><s:property value="claim.description" /></p>

            <s:if test="claim.withdrawable">
                <s:form action="withdraw" namespace="/customer" method="post">
                    <s:hidden name="claimId" value="%{claim.id}" />
                    <button type="submit" class="btn btn-danger" data-confirm="Withdraw this claim? This cannot be undone.">Withdraw Claim</button>
                </s:form>
            </s:if>
        </div>
    </div>

    <!-- Documents -->
    <div class="panel">
        <div class="panel-head">Documents</div>
        <div class="panel-body no-pad">
            <table class="table">
                <thead><tr><th>Document</th><th>Status</th><th>Verification</th></tr></thead>
                <tbody>
                <s:iterator value="documents">
                    <tr>
                        <td><s:property value="docType" /><s:if test="fileName != null"><br/><span class="muted small"><s:property value="fileName" /></span></s:if></td>
                        <td><span class="pill <s:if test='uploaded'>pill-ok</s:if><s:else>pill-warn</s:else>"><s:property value="uploadStatus" /></span></td>
                        <td><span class="pill pill-muted"><s:property value="verificationStatus" /></span></td>
                    </tr>
                </s:iterator>
                <s:if test="documents.isEmpty()"><tr><td colspan="3" class="empty">No documents required.</td></tr></s:if>
                </tbody>
            </table>
        </div>
        <div class="panel-foot">
            <s:form action="uploadDocument" namespace="/customer" method="post" enctype="multipart/form-data" cssClass="upload-form">
                <s:hidden name="claimId" value="%{claim.id}" />
                <input type="text" name="docType" class="input" placeholder="Document type (e.g. FIR Copy)" required />
                <input type="file" name="upload" class="input" required />
                <button type="submit" class="btn btn-primary">Upload</button>
            </s:form>
        </div>
    </div>
</div>

<!-- Communication thread -->
<div class="panel">
    <div class="panel-head">Communication Center</div>
    <div class="panel-body">
        <div class="thread">
            <s:iterator value="messages">
                <div class="msg <s:if test='senderId == #session.ICMS_USER.id'>msg-own</s:if>">
                    <div class="msg-meta"><s:property value="senderName" /> &middot; <s:property value="createdAt" /></div>
                    <div class="msg-body"><s:property value="content" /></div>
                </div>
            </s:iterator>
            <s:if test="messages.isEmpty()"><p class="empty">No messages yet.</p></s:if>
        </div>
        <s:form action="message" namespace="/customer" method="post" cssClass="msg-form">
            <s:hidden name="claimId" value="%{claim.id}" />
            <input type="text" name="content" class="input" placeholder="Type your message..." required />
            <button type="submit" class="btn btn-primary">Send</button>
        </s:form>
    </div>
</div>
