<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>学生注册 - TA 招聘系统</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<div class="login-wrap">
    <h2>学生注册</h2>
    <% if (request.getAttribute("error") != null) { %>
        <p class="error"><%= request.getAttribute("error") %></p>
    <% } %>
    <form method="post" action="<%= request.getContextPath() %>/register">
        <label>学号 <span style="color:red">*</span>
            <input type="text" name="id" required pattern="\d{10}" maxlength="10" placeholder="10位数字" title="学号必须为10位数字">
        </label>
        <label>密码 <span style="color:red">*</span>
            <input type="password" name="password" required placeholder="请设置密码">
        </label>
        <label>姓名
            <input type="text" name="name" placeholder="如不填则使用学号">
        </label>
        <label>GPA（选填）
            <input type="text" name="gpa" placeholder="如 3.8">
        </label>
        <label>技能标签（选填，多个用逗号分隔）
            <input type="text" name="skillTags" placeholder="如 Java, Python, 英语六级">
        </label>
        <button type="submit" class="btn">注册</button>
        <p style="margin-top:16px; font-size:14px;">
            <a href="<%= request.getContextPath() %>/login.jsp">已有账号？去登录</a>
        </p>
    </form>
</div>
</body>
</html>
