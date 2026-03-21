<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="bupt.is.ta.model.User" %>
<%
    String cvPath = request.getAttribute("configCvPath") != null ? (String) request.getAttribute("configCvPath") : "/WEB-INF/data/cvs";
    String storageMode = request.getAttribute("configStorageMode") != null ? (String) request.getAttribute("configStorageMode") : "WEBAPP";
    int maxCourses = 2;
    if (request.getAttribute("configMaxCourses") != null) {
        try {
            int v = ((Number) request.getAttribute("configMaxCourses")).intValue();
            if (v >= 1) maxCourses = v;
        } catch (Exception e) { }
    }
    User current = (User) session.getAttribute("currentUser");
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>系统配置 - 管理员</title>
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
    <h2 class="page-title">系统参数</h2>
    <div class="section">
        <form method="post" action="<%= request.getContextPath() %>/admin/config">
            <div class="form-group">
                <label>每名学生最多带课数（熔断阈值）</label>
                <input type="number" name="maxCoursesPerTA" value="<%= maxCourses %>" min="1">
            </div>
            <div class="form-group">
                <label>简历存储相对路径</label>
                <input type="text" name="cvRelativePath" value="<%= cvPath %>">
            </div>
            <div class="form-group">
                <label>存储模式</label>
                <select name="storageMode">
                    <option value="WEBAPP" <%= "WEBAPP".equals(storageMode) ? "selected" : "" %>>WEBAPP</option>
                    <option value="USER_HOME" <%= "USER_HOME".equals(storageMode) ? "selected" : "" %>>USER_HOME</option>
                </select>
            </div>
            <div class="form-actions">
                <button type="submit" class="btn">保存配置</button>
            </div>
        </form>
    </div>
</main>
</body>
</html>
