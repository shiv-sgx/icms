<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title><tiles:getAsString name="title" /></title>
    <link rel="stylesheet" href="<s:url value='/assets/css/icms.css'/>" />
</head>
<body class="public-body">
    <div class="public-wrap">
        <div class="brand-mark">
            <span class="brand-logo">ICMS</span>
            <span class="brand-sub">Insurance Claim Management System</span>
        </div>
        <tiles:insertAttribute name="body" />
    </div>
</body>
</html>
