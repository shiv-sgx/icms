<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div class="page-head"><h1 class="page-title">Claim Configuration</h1>
    <p class="page-sub">Document requirements by claim type</p></div>

<s:if test="flashMessage != null"><div class="alert alert-<s:property value='flashType'/>"><s:property value="flashMessage" /></div></s:if>

<div class="panel">
    <div class="panel-head">Add Requirement</div>
    <div class="panel-body">
        <form action="<s:url action='addDocument' namespace='/admin'/>" method="post" class="form-card">
            <div class="form-row">
                <div class="field"><label>Claim Type</label>
                    <select name="claimType" class="input" required>
                        <s:iterator value="{'MOTOR','HEALTH','PROPERTY','LIFE','TRAVEL','LIABILITY'}"><option><s:property /></option></s:iterator>
                    </select>
                </div>
                <div class="field"><label>Subtype (optional)</label><input type="text" name="claimSubtype" class="input" /></div>
                <div class="field"><label>Document Type</label><input type="text" name="docType" class="input" required /></div>
                <div class="field"><label>Required</label>
                    <label class="checkbox-line"><input type="checkbox" name="required" value="true" checked /> Required</label>
                </div>
            </div>
            <div class="form-actions"><button type="submit" class="btn btn-primary">Add</button></div>
        </form>
    </div>
</div>

<div class="panel">
    <div class="panel-body no-pad">
        <table class="table">
            <thead><tr><th>Claim Type</th><th>Subtype</th><th>Document</th><th>Required</th><th></th></tr></thead>
            <tbody>
            <s:iterator value="docReqList">
                <tr>
                    <td><s:property value="claimType" /></td>
                    <td><s:property value="claimSubtype" /><s:if test="claimSubtype == null"><span class="muted">— any —</span></s:if></td>
                    <td><s:property value="docType" /></td>
                    <td><s:if test="required"><span class="pill pill-ok">Required</span></s:if><s:else><span class="pill pill-muted">Optional</span></s:else></td>
                    <td>
                        <s:form action="deleteDocument" namespace="/admin" method="post" cssClass="inline-form">
                            <s:hidden name="id" value="%{id}" />
                            <button type="submit" class="btn btn-light btn-sm" data-confirm="Remove this requirement?">Delete</button>
                        </s:form>
                    </td>
                </tr>
            </s:iterator>
            <s:if test="docReqList.isEmpty()"><tr><td colspan="5" class="empty">No requirements configured.</td></tr></s:if>
            </tbody>
        </table>
    </div>
</div>
