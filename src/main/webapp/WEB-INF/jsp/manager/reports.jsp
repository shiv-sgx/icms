<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div class="page-head">
    <h1 class="page-title">Reports &amp; Analytics</h1>
    <p class="page-sub">Operational insights · export any report as CSV</p>
</div>

<s:iterator value="reports">
    <div class="panel">
        <div class="panel-head">
            <s:property value="title" />
            <s:a action="exportReport" namespace="/manager" cssClass="link-more"><s:param name="key" value="key" />Export CSV</s:a>
        </div>
        <div class="panel-body no-pad">
            <table class="table">
                <thead><tr><s:iterator value="headers"><th><s:property /></th></s:iterator></tr></thead>
                <tbody>
                <s:iterator value="rows">
                    <tr><s:iterator><td><s:property /></td></s:iterator></tr>
                </s:iterator>
                <s:if test="empty"><tr><td class="empty">No data.</td></tr></s:if>
                </tbody>
            </table>
        </div>
    </div>
</s:iterator>
