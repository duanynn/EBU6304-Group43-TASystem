<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>TA Recruitment System - Login</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body class="login-page">
<div class="login-wrap">
    <h2>TA Recruitment System - Login</h2>
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
    </p>
</div>
</body>
</html>
