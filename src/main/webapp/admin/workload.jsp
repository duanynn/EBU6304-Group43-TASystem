<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="bupt.is.ta.model.User" %>
<%@ page import="bupt.is.ta.model.Application" %>
<%
    @SuppressWarnings("unchecked")
    List<Map.Entry<String, Long>> workload = (List<Map.Entry<String, Long>>) request.getAttribute("workload");
    Map<String, User> userMap = (Map<String, User>) request.getAttribute("userMap");
    if (workload == null) workload = List.of();
    if (userMap == null) userMap = Map.of();
    User current = (User) session.getAttribute("currentUser");
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>工作量统计 - 管理员</title>
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
    <h2 class="page-title">已录用助教带课数量（按学生统计）</h2>
    <div class="section">
        <table class="data-table">
            <thead>
                <tr><th>学号/ID</th><th>姓名</th><th>已带课程数</th></tr>
            </thead>
            <tbody>
                <% for (Map.Entry<String, Long> e : workload) {
                    User u = userMap.get(e.getKey());
                %>
                <tr>
                    <td><%= e.getKey() %></td>
                    <td><%= u != null ? u.getName() : "-" %></td>
                    <td><%= e.getValue() %></td>
                </tr>
                <% } %>
                <% if (workload.isEmpty()) { %>
                <tr><td colspan="3" class="empty-hint">暂无已录用记录</td></tr>
                <% } %>
            </tbody>
        </table>
    </div>
</main>
</body>
</html>
