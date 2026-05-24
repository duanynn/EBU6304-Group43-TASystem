<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>TA Recruitment System - Login</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css?v=20260518-ui2">
</head>
<body class="login-page">
<div class="login-wrap">
    <h2>TA Recruitment System - Login</h2>
    <div class="login-demo-accounts muted" style="font-size:13px;line-height:1.6;margin-bottom:14px;padding:10px 12px;background:var(--surface-subtle,#f8fafc);border:1px solid var(--border,#d9dee8);border-radius:8px;">
        <strong>Demo accounts (testing)</strong><br>
        TA: <code>2021001001</code> / <code>123</code> — ID suffix <code>001001</code> (for forgot password)<br>
        TA: <code>2021001002</code> / <code>123</code> — ID suffix <code>001002</code><br>
        MO: <code>0000000001</code> / <code>123</code> &nbsp;|&nbsp; Admin: <code>admin</code> / <code>admin</code>
    </div>
    <% if (request.getParameter("reset") != null) { %>
        <p class="alert alert-info">Password reset successfully. Please log in with your new password.</p>
    <% } %>
    <% if (request.getAttribute("error") != null) { %>
        <p class="error"><%= request.getAttribute("error") %></p>
    <% } %>
    <form method="post" action="<%= request.getContextPath() %>/login">
        <label>Account (ID / Student ID / Staff ID)
            <input type="text" name="id" required placeholder="Enter ID">
        </label>
        <label>Password
            <input type="password" name="password" required placeholder="Enter password">
        </label>
        <button type="submit" class="btn">Login</button>
    </form>
    <p style="margin-top:16px; font-size:14px;">
        <a href="<%= request.getContextPath() %>/register">Student Registration</a>
        &nbsp;|&nbsp;
        <a href="<%= request.getContextPath() %>/forgotPassword">Forgot password (students)</a>
    </p>
</div>
</body>
</html>
