<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div class="page-head">
    <h1 class="page-title">Surveyor Dashboard</h1>
    <p class="page-sub"><s:property value="#session.ICMS_USER.fullName" /> &middot; <s:property value="#session.ICMS_USER.branch" /></p>
</div>

<div class="card-grid">
    <div class="stat-card"><div class="stat-label">Assigned Claims</div><div class="stat-value"><s:property value="totalAssigned" /></div></div>
    <div class="stat-card"><div class="stat-label">Pending Survey</div><div class="stat-value"><s:property value="pendingSurvey" /></div></div>
    <div class="stat-card"><div class="stat-label">Assessed</div><div class="stat-value"><s:property value="assessed" /></div></div>
</div>

<div class="panel">
    <div class="panel-head">Assigned Claims</div>
    <div class="panel-body no-pad">
        <table class="table">
            <thead><tr><th>Claim No.</th><th>Type</th><th>Location</th><th>Estimated</th><th>Status</th><th></th></tr></thead>
            <tbody>
            <s:iterator value="claims.items">
                <tr>
                    <td><s:property value="claimNo" /></td>
                    <td><s:property value="claimType" /><s:if test="claimSubtype != null"> · <s:property value="claimSubtype" /></s:if></td>
                    <td><s:property value="city" /> <s:property value="state" /></td>
                    <td>&#8377; <s:property value="estimatedLoss" /></td>
                    <td><span class="pill <s:property value='statusPill'/>"><s:property value="statusLabel" /></span></td>
                    <td><s:a action="assess" namespace="/surveyor" cssClass="btn btn-primary btn-sm"><s:param name="id" value="id" />Assess</s:a></td>
                </tr>
            </s:iterator>
            <s:if test="claims == null || claims.items.isEmpty()"><tr><td colspan="6" class="empty">No claims assigned to you.</td></tr></s:if>
            </tbody>
        </table>
    </div>
</div>

<s:if test="claims != null && claims.totalPages > 1">
    <div class="pager">
        <s:if test="claims.hasPrev"><s:a action="dashboard" namespace="/surveyor" cssClass="btn btn-light"><s:param name="page" value="claims.page - 1" />&laquo; Prev</s:a></s:if>
        <span class="pager-info">Page <s:property value="claims.page" /> of <s:property value="claims.totalPages" /></span>
        <s:if test="claims.hasNext"><s:a action="dashboard" namespace="/surveyor" cssClass="btn btn-light"><s:param name="page" value="claims.page + 1" />Next &raquo;</s:a></s:if>
    </div>
</s:if>
