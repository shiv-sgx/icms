<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div class="page-head with-action">
    <div>
        <h1 class="page-title">My Claims</h1>
        <p class="page-sub">All claims filed under your policies</p>
    </div>
    <s:a action="newClaim" namespace="/customer" cssClass="btn btn-primary">+ New Claim</s:a>
</div>

<div class="panel">
    <div class="panel-body no-pad">
        <s:if test="claims == null || claims.items.isEmpty()">
            <p class="empty">No claims found. <s:a action="newClaim" namespace="/customer">File a new claim</s:a>.</p>
        </s:if>
        <s:else>
            <table class="table">
                <thead>
                    <tr><th>Claim No.</th><th>Type</th><th>Policy</th><th>Incident</th><th>Estimated</th><th>Status</th></tr>
                </thead>
                <tbody>
                <s:iterator value="claims.items">
                    <tr>
                        <td><s:a action="claim" namespace="/customer"><s:param name="id" value="id" /><s:property value="claimNo" /></s:a></td>
                        <td><s:property value="claimType" /><s:if test="claimSubtype != null"> · <s:property value="claimSubtype" /></s:if></td>
                        <td><s:property value="policyNo" /></td>
                        <td><s:property value="incidentDate" /></td>
                        <td>&#8377; <s:property value="estimatedLoss" /></td>
                        <td><span class="pill <s:property value='statusPill'/>"><s:property value="statusLabel" /></span></td>
                    </tr>
                </s:iterator>
                </tbody>
            </table>
        </s:else>
    </div>
</div>

<s:if test="claims != null && claims.totalPages > 1">
    <div class="pager">
        <s:if test="claims.hasPrev">
            <s:a action="claims" namespace="/customer" cssClass="btn btn-light"><s:param name="page" value="claims.page - 1" />&laquo; Prev</s:a>
        </s:if>
        <span class="pager-info">Page <s:property value="claims.page" /> of <s:property value="claims.totalPages" /></span>
        <s:if test="claims.hasNext">
            <s:a action="claims" namespace="/customer" cssClass="btn btn-light"><s:param name="page" value="claims.page + 1" />Next &raquo;</s:a>
        </s:if>
    </div>
</s:if>
