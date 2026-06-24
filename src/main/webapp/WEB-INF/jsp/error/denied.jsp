<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div class="login-card">
    <h1 class="login-title">Access denied</h1>
    <p class="login-hint">You don't have permission to view that page.</p>
    <s:a action="login" namespace="/" cssClass="btn btn-primary btn-block">Back to sign in</s:a>
</div>
