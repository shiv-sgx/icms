<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div class="page-head"><h1 class="page-title">Notification Templates</h1>
    <p class="page-sub">Message templates for SMS / email notifications</p></div>

<s:if test="flashMessage != null"><div class="alert alert-<s:property value='flashType'/>"><s:property value="flashMessage" /></div></s:if>

<s:iterator value="templateList">
    <div class="panel">
        <div class="panel-head"><s:property value="name" /> <span class="pill pill-muted"><s:property value="channel" /></span></div>
        <div class="panel-body">
            <s:form action="updateTemplate" namespace="/admin" method="post">
                <s:hidden name="id" value="%{id}" />
                <div class="field">
                    <label>Body</label>
                    <textarea name="body" class="input" rows="2"><s:property value="body"/></textarea>
                </div>
                <label class="checkbox-line"><input type="checkbox" name="active" value="true" <s:if test="active">checked</s:if> /> Active</label>
                <button type="submit" class="btn btn-light btn-sm">Save</button>
            </s:form>
        </div>
    </div>
</s:iterator>
