<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <title>Not found — ICMS</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/icms.css" />
</head>
<body class="public-body">
    <div class="public-wrap">
        <div class="brand-mark"><span class="brand-logo">ICMS</span></div>
        <div class="login-card">
            <h1 class="login-title">Page not found</h1>
            <p class="login-hint">The page you requested doesn't exist.</p>
            <a class="btn btn-primary btn-block" href="<%= request.getContextPath() %>/login">Back to sign in</a>
        </div>
    </div>
</body>
</html>
