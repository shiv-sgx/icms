<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div class="page-head">
    <h1 class="page-title">Communication Center</h1>
    <p class="page-sub">Recent messages across all claims</p>
</div>

<div class="panel">
    <div class="panel-body no-pad">
        <table class="table">
            <thead><tr><th>Claim</th><th>From</th><th>Channel</th><th>Message</th><th>When</th></tr></thead>
            <tbody>
            <s:iterator value="messages">
                <tr>
                    <td><s:a action="claim" namespace="/agent"><s:param name="id" value="claimId" /><s:property value="claimNo" /></s:a></td>
                    <td><s:property value="senderName" /></td>
                    <td><span class="pill pill-muted"><s:property value="channel" /></span></td>
                    <td><s:property value="content" /></td>
                    <td class="muted small"><s:property value="createdAt" /></td>
                </tr>
            </s:iterator>
            <s:if test="messages.isEmpty()"><tr><td colspan="5" class="empty">No messages.</td></tr></s:if>
            </tbody>
        </table>
    </div>
</div>
