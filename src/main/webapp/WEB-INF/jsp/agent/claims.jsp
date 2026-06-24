<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div class="page-head">
    <h1 class="page-title">Claims Management</h1>
    <p class="page-sub">Search, filter, and action claims</p>
</div>

<form action="<s:url action='claims' namespace='/agent'/>" method="get" class="filter-bar">
    <input type="text" name="q" class="input" placeholder="Search claim no. or claimant..." value="<s:property value='q'/>" />
    <select name="status" class="input">
        <option value="">All statuses</option>
        <s:iterator value="@com.sgx.icms.domain.ClaimStatus@LIFECYCLE" var="st">
            <option value="${st}" <s:if test="status == #st">selected</s:if>>${st}</option>
        </s:iterator>
        <option value="REJECTED" <s:if test="status == 'REJECTED'">selected</s:if>>REJECTED</option>
        <option value="WITHDRAWN" <s:if test="status == 'WITHDRAWN'">selected</s:if>>WITHDRAWN</option>
        <option value="ON_HOLD" <s:if test="status == 'ON_HOLD'">selected</s:if>>ON_HOLD</option>
    </select>
    <select name="type" class="input">
        <option value="">All types</option>
        <s:iterator value="{'MOTOR','HEALTH','PROPERTY','LIFE','TRAVEL','LIABILITY'}" var="t">
            <option value="${t}" <s:if test="type == #t">selected</s:if>>${t}</option>
        </s:iterator>
    </select>
    <button type="submit" class="btn btn-primary">Filter</button>
    <s:a action="claims" namespace="/agent" cssClass="btn btn-light">Reset</s:a>
</form>

<div class="panel">
    <div class="panel-body no-pad">
        <table class="table">
            <thead><tr><th>Claim No.</th><th>Claimant</th><th>Type</th><th>Estimated</th><th>Surveyor</th><th>Risk</th><th>Status</th></tr></thead>
            <tbody>
            <s:iterator value="claims.items">
                <tr>
                    <td><s:a action="claim" namespace="/agent"><s:param name="id" value="id" /><s:property value="claimNo" /></s:a></td>
                    <td><s:property value="claimantName" /></td>
                    <td><s:property value="claimType" /></td>
                    <td>&#8377; <s:property value="estimatedLoss" /></td>
                    <td><s:property value="surveyorName" /><s:if test="surveyorName == null"><span class="muted">—</span></s:if></td>
                    <td><span class="pill risk-<s:property value='riskLevel'/>"><s:property value="riskLevel" /></span></td>
                    <td><span class="pill <s:property value='statusPill'/>"><s:property value="statusLabel" /></span></td>
                </tr>
            </s:iterator>
            <s:if test="claims == null || claims.items.isEmpty()"><tr><td colspan="7" class="empty">No claims match your filters.</td></tr></s:if>
            </tbody>
        </table>
    </div>
</div>

<s:if test="claims != null && claims.totalPages > 1">
    <div class="pager">
        <s:if test="claims.hasPrev">
            <s:a action="claims" namespace="/agent" cssClass="btn btn-light">
                <s:param name="page" value="claims.page - 1" /><s:param name="status" value="status" /><s:param name="type" value="type" /><s:param name="q" value="q" />&laquo; Prev</s:a>
        </s:if>
        <span class="pager-info">Page <s:property value="claims.page" /> of <s:property value="claims.totalPages" /> (<s:property value="claims.totalItems" /> claims)</span>
        <s:if test="claims.hasNext">
            <s:a action="claims" namespace="/agent" cssClass="btn btn-light">
                <s:param name="page" value="claims.page + 1" /><s:param name="status" value="status" /><s:param name="type" value="type" /><s:param name="q" value="q" />Next &raquo;</s:a>
        </s:if>
    </div>
</s:if>
