<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div class="page-head">
    <h1 class="page-title">Approval Queue</h1>
    <p class="page-sub">Claims awaiting your decision</p>
</div>

<div class="panel">
    <div class="panel-body no-pad">
        <table class="table">
            <thead><tr><th>Claim No.</th><th>Claimant</th><th>Type</th><th>Estimated</th><th>Risk</th><th>Fraud</th><th></th></tr></thead>
            <tbody>
            <s:iterator value="claimsPage.items">
                <tr>
                    <td><s:property value="claimNo" /></td>
                    <td><s:property value="claimantName" /></td>
                    <td><s:property value="claimType" /></td>
                    <td>&#8377; <s:property value="estimatedLoss" /></td>
                    <td><span class="pill risk-<s:property value='riskLevel'/>"><s:property value="riskLevel" /></span></td>
                    <td><s:property value="fraudScore" /></td>
                    <td><s:a action="claim" namespace="/manager" cssClass="btn btn-primary btn-sm"><s:param name="id" value="id" />Review</s:a></td>
                </tr>
            </s:iterator>
            <s:if test="claimsPage == null || claimsPage.items.isEmpty()"><tr><td colspan="7" class="empty">No claims awaiting approval.</td></tr></s:if>
            </tbody>
        </table>
    </div>
</div>

<s:if test="claimsPage != null && claimsPage.totalPages > 1">
    <div class="pager">
        <s:if test="claimsPage.hasPrev"><s:a action="approvals" namespace="/manager" cssClass="btn btn-light"><s:param name="page" value="claimsPage.page - 1" />&laquo; Prev</s:a></s:if>
        <span class="pager-info">Page <s:property value="claimsPage.page" /> of <s:property value="claimsPage.totalPages" /></span>
        <s:if test="claimsPage.hasNext"><s:a action="approvals" namespace="/manager" cssClass="btn btn-light"><s:param name="page" value="claimsPage.page + 1" />Next &raquo;</s:a></s:if>
    </div>
</s:if>
