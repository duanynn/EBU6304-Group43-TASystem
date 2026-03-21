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
    <title>发布新岗位 - 老师工作台</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<header class="app-header">
    <h1>TA 招聘系统 · 老师工作台</h1>
    <span class="user-info"><%= current != null ? current.getName() : "" %> <a href="<%= request.getContextPath() %>/login">退出</a></span>
</header>
<nav class="app-nav">
    <a href="<%= request.getContextPath() %>/mo/dashboard">我的岗位</a>
    <a href="<%= request.getContextPath() %>/mo/postJob">发布新岗位</a>
</nav>
<main class="app-main">
    <h2 class="page-title">发布新岗位</h2>
    <div class="section">
        <form method="post" action="<%= request.getContextPath() %>/mo/postJob">
            <div class="form-group">
                <label>课程名称</label>
                <input type="text" name="courseName" required placeholder="如：软件工程">
            </div>
            <div class="form-group">
                <label>需要助教人数</label>
                <input type="number" name="requiredCount" value="1" min="1" required>
            </div>
            <div class="form-group">
                <label>要求技能（多个用逗号分隔）</label>
                <input type="text" name="requiredSkills" placeholder="Java, Git, Python">
            </div>
            <div class="form-actions">
                <button type="submit" class="btn">发布</button>
                <a href="<%= request.getContextPath() %>/mo/dashboard" class="btn btn-secondary">取消</a>
            </div>
        </form>
    </div>
</main>
</body>
</html>
