<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="bupt.is.ta.model.User" %>
<%
    User current = (User) session.getAttribute("currentUser");
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>数据概览 - 管理员</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<header class="app-header">
    <h1>TA 招聘系统 · 管理员</h1>
    <span class="user-info"><%= current != null ? current.getName() : "" %> <a href="<%= request.getContextPath() %>/login">退出</a></span>
</header>
<nav class="app-nav">
    <a href="<%= request.getContextPath() %>/admin/overview">概览</a>
    <a href="<%= request.getContextPath() %>/admin/workload">工作量统计</a>
    <a href="<%= request.getContextPath() %>/admin/users">用户管理</a>
    <a href="<%= request.getContextPath() %>/admin/config">系统配置</a>
</nav>
<main class="app-main">
    <h2 class="page-title">系统数据概览</h2>
    <div class="section">
        <table class="data-table">
            <thead>
                <tr><th>指标</th><th>数量</th></tr>
            </thead>
            <tbody>
                <tr><td>注册用户总数</td><td><%= request.getAttribute("totalUsers") != null ? request.getAttribute("totalUsers") : 0 %></td></tr>
                <tr><td>岗位总数</td><td><%= request.getAttribute("totalJobs") != null ? request.getAttribute("totalJobs") : 0 %></td></tr>
                <tr><td>投递记录总数</td><td><%= request.getAttribute("totalApplications") != null ? request.getAttribute("totalApplications") : 0 %></td></tr>
            </tbody>
        </table>
    </div>
</main>
</body>
</html>
