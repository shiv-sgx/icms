<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div class="page-head"><h1 class="page-title">User Management</h1></div>
<s:set var="allRoles" value="roles" />

<s:if test="flashMessage != null">
    <div class="alert alert-<s:property value='flashType'/>"><s:property value="flashMessage" /></div>
</s:if>

<!-- Create user -->
<div class="panel">
    <div class="panel-head">Create User</div>
    <div class="panel-body">
        <form action="<s:url action='createUser' namespace='/admin'/>" method="post" class="form-card">
            <div class="form-row">
                <div class="field"><label>Full Name</label><input type="text" name="fullName" class="input" required /></div>
                <div class="field"><label>Email</label><input type="email" name="email" class="input" required /></div>
                <div class="field"><label>Username</label><input type="text" name="username" class="input" required /></div>
            </div>
            <div class="form-row">
                <div class="field"><label>Password</label><input type="text" name="password" class="input" required placeholder="min 6 chars" /></div>
                <div class="field">
                    <label>Role</label>
                    <select name="roleId" class="input" required>
                        <s:iterator value="roles"><option value="<s:property value='id'/>"><s:property value="name" /></option></s:iterator>
                    </select>
                </div>
                <div class="field"><label>Branch</label><input type="text" name="branch" class="input" /></div>
            </div>
            <div class="form-actions"><button type="submit" class="btn btn-primary">Create User</button></div>
        </form>
    </div>
</div>

<!-- Search + list -->
<form action="<s:url action='users' namespace='/admin'/>" method="get" class="filter-bar">
    <input type="text" name="q" class="input" placeholder="Search by name, email, role..." value="<s:property value='q'/>" />
    <select name="role" class="input">
        <option value="">All roles</option>
        <s:iterator value="roles"><option value="<s:property value='name'/>" <s:if test="role == name">selected</s:if>><s:property value="name" /></option></s:iterator>
    </select>
    <button type="submit" class="btn btn-primary">Search</button>
    <s:a action="users" namespace="/admin" cssClass="btn btn-light">Reset</s:a>
</form>

<div class="panel">
    <div class="panel-body no-pad">
        <table class="table">
            <thead><tr><th>User</th><th>Role / Status</th><th>Last Login</th><th>Update</th><th>Reset Password</th></tr></thead>
            <tbody>
            <s:iterator value="users.items">
                <tr>
                    <td><strong><s:property value="fullName" /></strong><br/><span class="muted small"><s:property value="username" /> · <s:property value="email" /></span></td>
                    <td><span class="pill pill-info"><s:property value="roleName" /></span>
                        <span class="pill <s:if test='status == \"ACTIVE\"'>pill-ok</s:if><s:else>pill-muted</s:else>"><s:property value="status" /></span></td>
                    <td class="muted small"><s:property value="lastLogin" /></td>
                    <td>
                        <s:form action="updateUser" namespace="/admin" method="post" cssClass="inline-form">
                            <s:hidden name="userId" value="%{id}" />
                            <select name="status" class="input"><option value="ACTIVE" <s:if test="status=='ACTIVE'">selected</s:if>>ACTIVE</option><option value="INACTIVE" <s:if test="status=='INACTIVE'">selected</s:if>>INACTIVE</option></select>
                            <select name="roleId" class="input"><s:iterator value="#allRoles" var="r"><option value="<s:property value='#r.id'/>" <s:if test="#r.name == roleName">selected</s:if>><s:property value="#r.name" /></option></s:iterator></select>
                            <button type="submit" class="btn btn-light btn-sm">Save</button>
                        </s:form>
                    </td>
                    <td>
                        <s:form action="resetPassword" namespace="/admin" method="post" cssClass="inline-form">
                            <s:hidden name="userId" value="%{id}" />
                            <input type="text" name="newPassword" class="input" placeholder="new password" />
                            <button type="submit" class="btn btn-light btn-sm">Reset</button>
                        </s:form>
                    </td>
                </tr>
            </s:iterator>
            <s:if test="users == null || users.items.isEmpty()"><tr><td colspan="5" class="empty">No users found.</td></tr></s:if>
            </tbody>
        </table>
    </div>
</div>

<s:if test="users != null && users.totalPages > 1">
    <div class="pager">
        <s:if test="users.hasPrev"><s:a action="users" namespace="/admin" cssClass="btn btn-light"><s:param name="page" value="users.page - 1" /><s:param name="q" value="q"/><s:param name="role" value="role"/>&laquo; Prev</s:a></s:if>
        <span class="pager-info">Page <s:property value="users.page" /> of <s:property value="users.totalPages" /></span>
        <s:if test="users.hasNext"><s:a action="users" namespace="/admin" cssClass="btn btn-light"><s:param name="page" value="users.page + 1" /><s:param name="q" value="q"/><s:param name="role" value="role"/>Next &raquo;</s:a></s:if>
    </div>
</s:if>
