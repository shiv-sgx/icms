<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div class="page-head">
    <h1 class="page-title">Welcome, <s:property value="#session.ICMS_USER.fullName" /></h1>
    <p class="page-sub">Customer Portal</p>
</div>

<s:if test="!hasProfile">
    <div class="alert alert-error">No policyholder profile is linked to your account. Please contact support.</div>
</s:if>

<div class="card-grid">
    <div class="stat-card"><div class="stat-label">Total Claims</div><div class="stat-value"><s:property value="totalClaims" /></div></div>
    <div class="stat-card"><div class="stat-label">Open Claims</div><div class="stat-value"><s:property value="openClaims" /></div></div>
    <div class="stat-card"><div class="stat-label">Settled</div><div class="stat-value"><s:property value="settledClaims" /></div></div>
    <div class="stat-card action-card">
        <div class="stat-label">Need to file a claim?</div>
        <s:a action="newClaim" namespace="/customer" cssClass="btn btn-primary">+ New Claim</s:a>
    </div>
</div>

<div class="grid-2">
    <div class="panel">
        <div class="panel-head">Recent Claims <s:a action="claims" namespace="/customer" cssClass="link-more">View all</s:a></div>
        <div class="panel-body no-pad">
            <s:if test="recentClaims.isEmpty()">
                <p class="empty">No claims yet. <s:a action="newClaim" namespace="/customer">File your first claim</s:a>.</p>
            </s:if>
            <s:else>
                <table class="table">
                    <thead><tr><th>Claim</th><th>Type</th><th>Filed</th><th>Status</th></tr></thead>
                    <tbody>
                    <s:iterator value="recentClaims">
                        <tr>
                            <td><s:a action="claim" namespace="/customer"><s:param name="id" value="id" /><s:property value="claimNo" /></s:a></td>
                            <td><s:property value="claimType" /></td>
                            <td><s:property value="filedAt" /></td>
                            <td><span class="pill <s:property value='statusPill'/>"><s:property value="statusLabel" /></span></td>
                        </tr>
                    </s:iterator>
                    </tbody>
                </table>
            </s:else>
        </div>
    </div>

    <div class="panel">
        <div class="panel-head">Notifications</div>
        <div class="panel-body no-pad">
            <s:if test="notifications.isEmpty()">
                <p class="empty">You're all caught up.</p>
            </s:if>
            <s:else>
                <ul class="notif-list">
                    <s:iterator value="notifications">
                        <li class="notif-item notif-<s:property value='type'/>">
                            <span class="notif-dot"></span>
                            <span class="notif-msg"><s:property value="message" /></span>
                        </li>
                    </s:iterator>
                </ul>
            </s:else>
        </div>
    </div>
</div>
