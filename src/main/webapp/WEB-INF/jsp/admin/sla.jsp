<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div class="page-head"><h1 class="page-title">SLA Configuration</h1>
    <p class="page-sub">Target hours per workflow stage</p></div>

<s:if test="flashMessage != null"><div class="alert alert-<s:property value='flashType'/>"><s:property value="flashMessage" /></div></s:if>

<div class="panel">
    <div class="panel-body no-pad">
        <table class="table">
            <thead><tr><th>Stage</th><th>Hours</th><th></th></tr></thead>
            <tbody>
            <s:iterator value="slaList">
                <tr>
                    <td><s:property value="stage" /></td>
                    <td>
                        <s:form action="updateSla" namespace="/admin" method="post" cssClass="inline-form">
                            <s:hidden name="id" value="%{id}" />
                            <input type="number" min="1" name="hours" class="input" value="<s:property value='hours'/>" />
                            <button type="submit" class="btn btn-light btn-sm">Save</button>
                        </s:form>
                    </td>
                    <td></td>
                </tr>
            </s:iterator>
            </tbody>
        </table>
    </div>
</div>
