<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div class="page-head">
    <h1 class="page-title">My Profile</h1>
    <p class="page-sub">Account and policy details</p>
</div>

<div class="grid-2">
    <div class="panel">
        <div class="panel-head">Account</div>
        <div class="panel-body">
            <dl class="kv">
                <dt>Name</dt><dd><s:property value="#session.ICMS_USER.fullName" /></dd>
                <dt>Username</dt><dd><s:property value="#session.ICMS_USER.username" /></dd>
                <dt>Email</dt><dd><s:property value="#session.ICMS_USER.email" /></dd>
            </dl>
        </div>
    </div>

    <div class="panel">
        <div class="panel-head">Policyholder</div>
        <div class="panel-body">
            <s:if test="hasProfile">
                <dl class="kv">
                    <dt>Name</dt><dd><s:property value="policyholder.fullName" /></dd>
                    <dt>Mobile</dt><dd><s:property value="policyholder.mobile" /></dd>
                    <dt>Address</dt><dd><s:property value="policyholder.address" />, <s:property value="policyholder.city" />, <s:property value="policyholder.state" /> <s:property value="policyholder.pinCode" /></dd>
                </dl>
            </s:if>
            <s:else><p class="muted">No policyholder profile linked.</p></s:else>
        </div>
    </div>
</div>

<div class="panel">
    <div class="panel-head">My Policies</div>
    <div class="panel-body no-pad">
        <table class="table">
            <thead><tr><th>Policy No.</th><th>Type</th><th>Sum Insured</th><th>Expiry</th><th>Status</th></tr></thead>
            <tbody>
            <s:iterator value="policies">
                <tr>
                    <td><s:property value="policyNo" /></td>
                    <td><s:property value="type" /></td>
                    <td>&#8377; <s:property value="sumInsured" /></td>
                    <td><s:property value="expiryDate" /></td>
                    <td><span class="pill pill-ok"><s:property value="status" /></span></td>
                </tr>
            </s:iterator>
            <s:if test="policies.isEmpty()"><tr><td colspan="5" class="empty">No policies on record.</td></tr></s:if>
            </tbody>
        </table>
    </div>
</div>
