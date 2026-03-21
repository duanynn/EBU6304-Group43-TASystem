<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>操作提示</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<header class="app-header">
    <h1>TA 招聘系统</h1>
</header>
<div class="app-main section">
    <h2 class="page-title">提示</h2>
    <p class="alert alert-error"><%= request.getAttribute("error") != null ? request.getAttribute("error") : "操作未成功" %></p>
    <a href="javascript:history.back()" class="btn btn-secondary">返回</a>
</div>
</body>
</html>
