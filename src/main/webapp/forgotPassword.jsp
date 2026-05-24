<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Reset Password - TA Recruitment System</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css?v=20260521-account">
</head>
<body class="login-page">
<div class="login-wrap">
    <h2>Reset Student Password</h2>
    <p class="muted">Enter your student ID, the last 6 digits of your ID card (set at registration), and the verification code.</p>
    <% if (request.getAttribute("error") != null) { %>
    <p class="error"><%= request.getAttribute("error") %></p>
    <% } %>
    <form method="post" action="<%= request.getContextPath() %>/forgotPassword">
        <label>Student ID
            <input type="text" name="id" required pattern="\d{10}" maxlength="10" placeholder="10 digits">
        </label>
        <label>ID card last 6 digits
            <input type="password" name="idCardSuffix" required pattern="\d{6}" maxlength="6" placeholder="6 digits" autocomplete="off">
        </label>
        <label>Verification code
            <div class="captcha-row">
                <input type="text" name="captcha" required maxlength="6" placeholder="Letters shown" autocomplete="off">
                <img src="<%= request.getContextPath() %>/captcha?t=<%= System.currentTimeMillis() %>" alt="Captcha" class="captcha-img" width="120" height="40">
            </div>
        </label>
        <label>New password
            <input type="password" name="newPassword" required placeholder="New password">
        </label>
        <label>Confirm new password
            <input type="password" name="confirmPassword" required placeholder="Confirm password">
        </label>
        <button type="submit" class="btn">Reset password</button>
    </form>
    <p style="margin-top:16px; font-size:14px;">
        <a href="<%= request.getContextPath() %>/login.jsp">Back to login</a>
    </p>
</div>
</body>
</html>
