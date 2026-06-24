<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div class="login-card">
    <h1 class="login-title">Sign in</h1>
    <p class="login-hint">Access your claims workspace</p>

    <s:if test="hasActionErrors()">
        <div class="alert alert-error">
            <s:actionerror />
        </div>
    </s:if>

    <s:form action="doLogin" namespace="/" method="post" cssClass="login-form">
        <div class="field">
            <label for="username">Username</label>
            <s:textfield name="username" id="username" cssClass="input"
                         autocomplete="username" placeholder="e.g. agent" />
        </div>
        <div class="field">
            <label for="password">Password</label>
            <s:password name="password" id="password" cssClass="input"
                        autocomplete="current-password" placeholder="Your password" />
        </div>
        <s:submit value="Sign in" cssClass="btn btn-primary btn-block" />
    </s:form>

    <p class="login-foot">Insurance Claim Management System</p>
</div>
