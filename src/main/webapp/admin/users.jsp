<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="bupt.is.ta.model.User" %>
<%
    @SuppressWarnings("unchecked")
    List<User> users = (List<User>) request.getAttribute("users");
    if (users == null) users = List.of();
    User current = (User) session.getAttribute("currentUser");
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>用户管理 - 管理员</title>
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
    <h2 class="page-title">新增老师（MO）账号</h2>
    <div class="section">
        <% if (request.getAttribute("error") != null) { %>
        <p class="alert alert-error"><%= request.getAttribute("error") %></p>
        <% } %>
        <form method="post" action="<%= request.getContextPath() %>/admin/users">
            <div class="form-group">
                <label>工号 (ID)</label>
                <input type="text" name="id" required pattern="\d{10}" maxlength="10" placeholder="10位数字" title="工号必须为10位数字">
            </div>
            <div class="form-group">
                <label>姓名</label>
                <input type="text" name="name" required>
            </div>
            <div class="form-group">
                <label>初始密码</label>
                <input type="password" name="password" required>
            </div>
            <div class="form-actions">
                <button type="submit" class="btn">创建老师账号</button>
            </div>
        </form>
    </div>
    <h2 class="page-title">全部用户列表</h2>
    <div class="section">
        <table class="data-table">
            <thead>
                <tr><th>ID</th><th>姓名</th><th>角色</th></tr>
            </thead>
            <tbody>
                <% for (User u : users) { %>
                <tr>
                    <td><%= u.getId() %></td>
                    <td><%= u.getName() != null ? u.getName() : "-" %></td>
                    <td><%= u.getRole() != null ? u.getRole() : "-" %></td>
                </tr>
                <% } %>
            </tbody>
        </table>
    </div>
</main>
</body>
</html>
