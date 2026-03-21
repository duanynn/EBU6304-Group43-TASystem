<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>TA 招聘系统 - 登录</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<div class="login-wrap">
    <h2>TA 招聘系统 · 登录</h2>
    <% if (request.getAttribute("error") != null) { %>
        <p class="error"><%= request.getAttribute("error") %></p>
    <% } %>
    <form method="post" action="<%= request.getContextPath() %>/login">
        <label>账号 (ID / 学号 / 工号)
            <input type="text" name="id" required placeholder="请输入 ID">
        </label>
        <label>密码
            <input type="password" name="password" required placeholder="请输入密码">
        </label>
        <button type="submit" class="btn">登录</button>
    </form>
    <p style="margin-top:16px; font-size:14px;">
        <a href="<%= request.getContextPath() %>/register">学生注册</a>
    </p>
</div>
</body>
</html>
