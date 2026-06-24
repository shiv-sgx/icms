<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div class="page-head"><h1 class="page-title">Approval Amount Thresholds</h1>
    <p class="page-sub">Amount bands that determine the required approval level</p></div>

<s:if test="flashMessage != null"><div class="alert alert-<s:property value='flashType'/>"><s:property value="flashMessage" /></div></s:if>

<div class="panel">
    <div class="panel-body no-pad">
        <table class="table">
            <thead><tr><th>Level</th><th>Label</th><th>Min / Max (&#8377;)</th></tr></thead>
            <tbody>
            <s:iterator value="thresholdList">
                <tr>
                    <td><span class="pill pill-info"><s:property value="level" /></span></td>
                    <td><s:property value="label" /></td>
                    <td>
                        <s:form action="updateThreshold" namespace="/admin" method="post" cssClass="inline-form">
                            <s:hidden name="id" value="%{id}" />
                            <input type="number" step="0.01" name="minAmount" class="input" value="<s:property value='minAmount'/>" />
                            <input type="number" step="0.01" name="maxAmount" class="input" value="<s:property value='maxAmount'/>" placeholder="(no limit)" />
                            <button type="submit" class="btn btn-light btn-sm">Save</button>
                        </s:form>
                    </td>
                </tr>
            </s:iterator>
            </tbody>
        </table>
    </div>
</div>
