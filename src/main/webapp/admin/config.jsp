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
    String dashscopeApiKey = request.getAttribute("dashscopeApiKey") != null ? (String) request.getAttribute("dashscopeApiKey") : "";
    String dashscopeEndpoint = request.getAttribute("dashscopeEndpoint") != null ? (String) request.getAttribute("dashscopeEndpoint") : "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";
    String dashscopeModel = request.getAttribute("dashscopeModel") != null ? (String) request.getAttribute("dashscopeModel") : "qwen-plus";
    String currentSemester = request.getAttribute("configCurrentSemester") != null ? (String) request.getAttribute("configCurrentSemester") : "2025-2026-S2";
    String applicationDeadline = request.getAttribute("configApplicationDeadline") != null ? (String) request.getAttribute("configApplicationDeadline") : "";
    User current = (User) session.getAttribute("currentUser");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>System Configuration - Admin</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css?v=20260518-ui2">
</head>
<body>
<header class="app-header">
    <h1>TA Recruitment System - Admin</h1>
    <span class="user-info user-info-with-avatar"><%= current != null ? current.getName() : "" %>
        <jsp:include page="/WEB-INF/jsp/account-menu.jsp"/>
    </span>
</header>
<nav class="app-nav">
    <a href="<%= request.getContextPath() %>/admin/overview">Overview</a>
    <a href="<%= request.getContextPath() %>/admin/openJobs">Open Jobs</a>
    <a href="<%= request.getContextPath() %>/admin/workload">Workload</a>
    <a href="<%= request.getContextPath() %>/admin/users">Users</a>
    <a href="<%= request.getContextPath() %>/admin/config">System Config</a>
</nav>
<main class="app-main">
    <h2 class="page-title">System Parameters</h2>
    <div class="section">
        <form method="post" action="<%= request.getContextPath() %>/admin/config">
            <div class="form-group">
                <label>Current Semester</label>
                <input type="text" name="currentSemester" value="<%= currentSemester %>" placeholder="e.g. 2025-2026-S2">
            </div>
            <div class="form-group">
                <label>Application Deadline (ISO-8601, empty = no limit)</label>
                <input type="text" name="applicationDeadline" value="<%= applicationDeadline %>" placeholder="2026-05-24T23:59:59Z">
                <p class="muted">Example: 2026-05-24T23:59:59Z. Leave blank to keep applications open.</p>
            </div>
            <div class="form-group">
                <label>Max Courses Per TA (Circuit-Breaker Threshold)</label>
                <input type="number" name="maxCoursesPerTA" value="<%= maxCourses %>" min="1">
            </div>
            <div class="form-group">
                <label>Relative CV Storage Path</label>
                <input type="text" name="cvRelativePath" value="<%= cvPath %>">
            </div>
            <div class="form-group">
                <label>Storage Mode</label>
                <select name="storageMode">
                    <option value="WEBAPP" <%= "WEBAPP".equals(storageMode) ? "selected" : "" %>>WEBAPP</option>
                    <option value="USER_HOME" <%= "USER_HOME".equals(storageMode) ? "selected" : "" %>>USER_HOME</option>
                </select>
            </div>
            <hr style="margin:16px 0;border:0;border-top:1px solid #e2e8f0;">
            <div class="form-group">
                <label>DashScope API Key</label>
                <input type="text" name="dashscopeApiKey" value="<%= dashscopeApiKey %>" placeholder="sk-...">
                <p class="muted">Current Status: <%= (dashscopeApiKey != null && !dashscopeApiKey.isBlank()) ? "Configured" : "Not Configured" %></p>
            </div>
            <div class="form-group">
                <label>DashScope Endpoint</label>
                <input type="text" name="dashscopeEndpoint" value="<%= dashscopeEndpoint %>">
            </div>
            <div class="form-group">
                <label>DashScope Model</label>
                <input type="text" name="dashscopeModel" value="<%= dashscopeModel %>">
            </div>
            <div class="form-actions">
                <button type="submit" class="btn">Save Configuration</button>
            </div>
        </form>
    </div>
</main>
</body>
</html>
