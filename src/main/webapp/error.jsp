<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Operation Message</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<header class="app-header">
    <h1>TA Recruitment System</h1>
</header>
<div class="app-main section">
    <h2 class="page-title">Message</h2>
    <p class="alert alert-error"><%= request.getAttribute("error") != null ? request.getAttribute("error") : "Operation failed" %></p>
    <a href="javascript:history.back()" class="btn btn-secondary">Back</a>
</div>
</body>
</html>
