<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title><tiles:getAsString name="title" /></title>
    <link rel="stylesheet" href="<s:url value='/assets/css/icms.css'/>" />
</head>
<body class="app-body">

    <header class="topbar">
        <div class="topbar-brand">
            <span class="brand-logo">ICMS</span>
            <span class="brand-sub">Insurance Claim Management</span>
        </div>
        <div class="topbar-user">
            <span class="user-name"><s:property value="#session.ICMS_USER.fullName" /></span>
            <span class="role-chip role-<s:property value='#session.ICMS_USER.role'/>">
                <s:property value="#session.ICMS_USER.role" />
            </span>
            <s:a action="logout" namespace="/" cssClass="btn-logout">Logout</s:a>
        </div>
    </header>

    <div class="app-shell">
        <aside class="sidebar">
            <s:set var="role" value="#session.ICMS_USER.role" />
            <nav class="side-nav">

                <s:if test="#role == 'CUSTOMER'">
                    <s:a action="dashboard" namespace="/customer" cssClass="nav-item">Dashboard</s:a>
                    <s:a action="claims" namespace="/customer" cssClass="nav-item">My Claims</s:a>
                    <s:a action="newClaim" namespace="/customer" cssClass="nav-item">New Claim</s:a>
                    <s:a action="profile" namespace="/customer" cssClass="nav-item">My Profile</s:a>
                    <s:a action="faq" namespace="/" cssClass="nav-item">FAQs &amp; Help</s:a>
                </s:if>

                <s:elseif test="#role == 'AGENT'">
                    <s:a action="dashboard" namespace="/agent" cssClass="nav-item">Dashboard</s:a>
                    <s:a action="claims" namespace="/agent" cssClass="nav-item">Claims</s:a>
                    <s:a action="communications" namespace="/agent" cssClass="nav-item">Communications</s:a>
                </s:elseif>

                <s:elseif test="#role == 'SURVEYOR'">
                    <s:a action="dashboard" namespace="/surveyor" cssClass="nav-item">Dashboard</s:a>
                </s:elseif>

                <s:elseif test="#role == 'MANAGER'">
                    <s:a action="dashboard" namespace="/manager" cssClass="nav-item">Dashboard</s:a>
                    <s:a action="approvals" namespace="/manager" cssClass="nav-item">Approval Queue</s:a>
                    <s:a action="reports" namespace="/manager" cssClass="nav-item">Reports &amp; Analytics</s:a>
                </s:elseif>

                <s:elseif test="#role == 'ADMIN'">
                    <s:a action="dashboard" namespace="/admin" cssClass="nav-item">Dashboard</s:a>
                    <s:a action="users" namespace="/admin" cssClass="nav-item">User Management</s:a>
                    <s:a action="roles" namespace="/admin" cssClass="nav-item">Role Management</s:a>
                    <s:a action="documents" namespace="/admin" cssClass="nav-item">Claim Config</s:a>
                    <s:a action="sla" namespace="/admin" cssClass="nav-item">SLA Config</s:a>
                    <s:a action="thresholds" namespace="/admin" cssClass="nav-item">Approval Thresholds</s:a>
                    <s:a action="templates" namespace="/admin" cssClass="nav-item">Notification Templates</s:a>
                    <s:a action="audit" namespace="/admin" cssClass="nav-item">Audit Logs</s:a>
                </s:elseif>
            </nav>
        </aside>

        <main class="content">
            <tiles:insertAttribute name="body" />
        </main>
    </div>

    <script src="<s:url value='/assets/js/icms.js'/>"></script>
</body>
</html>
