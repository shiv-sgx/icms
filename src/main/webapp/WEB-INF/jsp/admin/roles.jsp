<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div class="page-head"><h1 class="page-title">Role &amp; Permission Management</h1>
    <p class="page-sub">System roles and their user counts</p></div>

<div class="panel">
    <div class="panel-body no-pad">
        <table class="table">
            <thead><tr><th>Role</th><th>Description</th><th>Users</th></tr></thead>
            <tbody>
            <s:iterator value="roles">
                <tr>
                    <td><span class="pill pill-info"><s:property value="name" /></span></td>
                    <td><s:property value="description" /></td>
                    <td><s:property value="userCount" /></td>
                </tr>
            </s:iterator>
            </tbody>
        </table>
    </div>
</div>
