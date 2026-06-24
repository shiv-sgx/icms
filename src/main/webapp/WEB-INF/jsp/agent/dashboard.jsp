<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div class="page-head">
    <h1 class="page-title">Agent Dashboard</h1>
    <p class="page-sub"><s:property value="#session.ICMS_USER.fullName" /> &middot; <s:property value="#session.ICMS_USER.branch" /></p>
</div>

<div class="card-grid">
    <div class="stat-card"><div class="stat-label">Open Claims</div><div class="stat-value"><s:property value="openClaims" /></div></div>
    <div class="stat-card"><div class="stat-label">Awaiting Survey</div><div class="stat-value"><s:property value="awaitingSurvey" /></div></div>
    <div class="stat-card"><div class="stat-label">Pending Approval</div><div class="stat-value"><s:property value="pendingApproval" /></div></div>
    <div class="stat-card"><div class="stat-label">Settled</div><div class="stat-value"><s:property value="settled" /></div></div>
</div>

<div class="panel">
    <div class="panel-head">Worklist <s:a action="claims" namespace="/agent" cssClass="link-more">All claims</s:a></div>
    <div class="panel-body no-pad">
        <table class="table">
            <thead><tr><th>Claim No.</th><th>Claimant</th><th>Type</th><th>Filed</th><th>Risk</th><th>Status</th></tr></thead>
            <tbody>
            <s:iterator value="worklist">
                <tr>
                    <td><s:a action="claim" namespace="/agent"><s:param name="id" value="id" /><s:property value="claimNo" /></s:a></td>
                    <td><s:property value="claimantName" /></td>
                    <td><s:property value="claimType" /></td>
                    <td><s:property value="filedAt" /></td>
                    <td><span class="pill risk-<s:property value='riskLevel'/>"><s:property value="riskLevel" /></span></td>
                    <td><span class="pill <s:property value='statusPill'/>"><s:property value="statusLabel" /></span></td>
                </tr>
            </s:iterator>
            <s:if test="worklist.isEmpty()"><tr><td colspan="6" class="empty">No claims need attention right now.</td></tr></s:if>
            </tbody>
        </table>
    </div>
</div>
