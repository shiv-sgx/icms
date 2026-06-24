<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div class="page-head">
    <h1 class="page-title">Admin Console</h1>
    <p class="page-sub">System administration &amp; monitoring</p>
</div>

<div class="card-grid">
    <div class="stat-card"><div class="stat-label">Users</div><div class="stat-value"><s:property value="stats.users" /></div></div>
    <div class="stat-card"><div class="stat-label">Claims</div><div class="stat-value"><s:property value="stats.claims" /></div></div>
    <div class="stat-card"><div class="stat-label">Roles</div><div class="stat-value"><s:property value="stats.roles" /></div></div>
    <div class="stat-card"><div class="stat-label">Audit Events</div><div class="stat-value"><s:property value="stats.auditEvents" /></div></div>
</div>

<div class="panel">
    <div class="panel-head">Database Connection Pool (HikariCP)</div>
    <div class="panel-body">
        <div class="card-grid">
            <div class="stat-card"><div class="stat-label">Active</div><div class="stat-value"><s:property value="stats.poolActive" /></div></div>
            <div class="stat-card"><div class="stat-label">Idle</div><div class="stat-value"><s:property value="stats.poolIdle" /></div></div>
            <div class="stat-card"><div class="stat-label">Total</div><div class="stat-value"><s:property value="stats.poolTotal" /></div></div>
        </div>
    </div>
</div>

<div class="panel">
    <div class="panel-head">Administration</div>
    <div class="panel-body">
        <div class="quick-links">
            <s:a action="users" namespace="/admin" cssClass="btn btn-light">User Management</s:a>
            <s:a action="roles" namespace="/admin" cssClass="btn btn-light">Role Management</s:a>
            <s:a action="documents" namespace="/admin" cssClass="btn btn-light">Claim Config</s:a>
            <s:a action="sla" namespace="/admin" cssClass="btn btn-light">SLA Config</s:a>
            <s:a action="thresholds" namespace="/admin" cssClass="btn btn-light">Approval Thresholds</s:a>
            <s:a action="templates" namespace="/admin" cssClass="btn btn-light">Notification Templates</s:a>
            <s:a action="audit" namespace="/admin" cssClass="btn btn-light">Audit Logs</s:a>
        </div>
    </div>
</div>
