<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div class="page-head with-action">
    <div><h1 class="page-title">Audit Logs</h1><p class="page-sub">System activity trail</p></div>
    <s:a action="exportAudit" namespace="/admin" cssClass="btn btn-primary"><s:param name="actionName" value="actionName"/><s:param name="result" value="result"/>Export CSV</s:a>
</div>

<form action="<s:url action='audit' namespace='/admin'/>" method="get" class="filter-bar">
    <input type="text" name="actionName" class="input" placeholder="Filter by action (e.g. LOGIN)" value="<s:property value='actionName'/>" />
    <select name="result" class="input">
        <option value="">All results</option>
        <option value="SUCCESS" <s:if test="result == 'SUCCESS'">selected</s:if>>SUCCESS</option>
        <option value="FAILED" <s:if test="result == 'FAILED'">selected</s:if>>FAILED</option>
    </select>
    <button type="submit" class="btn btn-primary">Filter</button>
    <s:a action="audit" namespace="/admin" cssClass="btn btn-light">Reset</s:a>
</form>

<div class="panel">
    <div class="panel-body no-pad">
        <table class="table">
            <thead><tr><th>Timestamp</th><th>User</th><th>Role</th><th>Action</th><th>Entity</th><th>IP</th><th>Result</th></tr></thead>
            <tbody>
            <s:iterator value="logs.items">
                <tr>
                    <td class="small"><s:property value="ts" /></td>
                    <td><s:property value="username" /></td>
                    <td><s:property value="role" /></td>
                    <td><s:property value="action" /></td>
                    <td class="small"><s:property value="entity" /></td>
                    <td class="muted small"><s:property value="ipAddress" /></td>
                    <td><span class="pill <s:if test='result == \"SUCCESS\"'>pill-ok</s:if><s:else>pill-danger</s:else>"><s:property value="result" /></span></td>
                </tr>
            </s:iterator>
            <s:if test="logs == null || logs.items.isEmpty()"><tr><td colspan="7" class="empty">No audit entries.</td></tr></s:if>
            </tbody>
        </table>
    </div>
</div>

<s:if test="logs != null && logs.totalPages > 1">
    <div class="pager">
        <s:if test="logs.hasPrev"><s:a action="audit" namespace="/admin" cssClass="btn btn-light"><s:param name="page" value="logs.page - 1" /><s:param name="actionName" value="actionName"/><s:param name="result" value="result"/>&laquo; Prev</s:a></s:if>
        <span class="pager-info">Page <s:property value="logs.page" /> of <s:property value="logs.totalPages" /> (<s:property value="logs.totalItems" /> events)</span>
        <s:if test="logs.hasNext"><s:a action="audit" namespace="/admin" cssClass="btn btn-light"><s:param name="page" value="logs.page + 1" /><s:param name="actionName" value="actionName"/><s:param name="result" value="result"/>Next &raquo;</s:a></s:if>
    </div>
</s:if>
