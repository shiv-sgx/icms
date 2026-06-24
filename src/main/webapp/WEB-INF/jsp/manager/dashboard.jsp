<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div class="page-head">
    <h1 class="page-title">Manager Dashboard</h1>
    <p class="page-sub"><s:property value="#session.ICMS_USER.fullName" /> &middot; <s:property value="#session.ICMS_USER.branch" /></p>
</div>

<div class="card-grid">
    <div class="stat-card"><div class="stat-label">Pending Approval</div><div class="stat-value"><s:property value="pendingApproval" /></div></div>
    <div class="stat-card"><div class="stat-label">High Risk</div><div class="stat-value"><s:property value="highRisk" /></div></div>
    <div class="stat-card"><div class="stat-label">SLA Breaches</div><div class="stat-value"><s:property value="slaBreaches" /></div></div>
    <div class="stat-card"><div class="stat-label">Settled</div><div class="stat-value"><s:property value="settled" /></div></div>
</div>

<div class="grid-2">
    <div class="panel">
        <div class="panel-head">Approval Queue <s:a action="approvals" namespace="/manager" cssClass="link-more">View all</s:a></div>
        <div class="panel-body no-pad">
            <table class="table">
                <thead><tr><th>Claim</th><th>Type</th><th>Estimated</th><th>Risk</th></tr></thead>
                <tbody>
                <s:iterator value="queue">
                    <tr>
                        <td><s:a action="claim" namespace="/manager"><s:param name="id" value="id" /><s:property value="claimNo" /></s:a></td>
                        <td><s:property value="claimType" /></td>
                        <td>&#8377; <s:property value="estimatedLoss" /></td>
                        <td><span class="pill risk-<s:property value='riskLevel'/>"><s:property value="riskLevel" /></span></td>
                    </tr>
                </s:iterator>
                <s:if test="queue.isEmpty()"><tr><td colspan="4" class="empty">No claims awaiting approval.</td></tr></s:if>
                </tbody>
            </table>
        </div>
    </div>

    <div class="panel">
        <div class="panel-head">Agent Performance <s:a action="reports" namespace="/manager" cssClass="link-more">Reports</s:a></div>
        <div class="panel-body no-pad">
            <table class="table">
                <thead><tr><s:iterator value="agentPerformance.headers"><th><s:property /></th></s:iterator></tr></thead>
                <tbody>
                <s:iterator value="agentPerformance.rows">
                    <tr><s:iterator><td><s:property /></td></s:iterator></tr>
                </s:iterator>
                <s:if test="agentPerformance == null || agentPerformance.empty"><tr><td colspan="4" class="empty">No data.</td></tr></s:if>
                </tbody>
            </table>
        </div>
    </div>
</div>
