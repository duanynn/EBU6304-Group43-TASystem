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
    String dataBaseDir = request.getAttribute("dataBaseDir") != null ? (String) request.getAttribute("dataBaseDir") : "N/A";
    String cvAbsPath = request.getAttribute("cvAbsPath") != null ? (String) request.getAttribute("cvAbsPath") : "N/A";
    User current = (User) session.getAttribute("currentUser");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>System Configuration - Admin</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<header class="app-header">
    <h1>TA Recruitment System - Admin</h1>
    <span class="user-info"><%= current != null ? current.getName() : "" %> <a href="<%= request.getContextPath() %>/login">Logout</a></span>
</header>
<nav class="app-nav">
    <a href="<%= request.getContextPath() %>/admin/overview">Overview</a>
    <a href="<%= request.getContextPath() %>/admin/workload">Workload</a>
    <a href="<%= request.getContextPath() %>/admin/users">Users</a>
    <a href="<%= request.getContextPath() %>/admin/config">System Config</a>
</nav>
<main class="app-main">
    <h2 class="page-title">System Parameters</h2>

    <!-- Data Storage Info -->
    <div class="section" style="background:#f0f9ff;border:1px solid #bae6fd;border-radius:6px;padding:16px;margin-bottom:20px;">
        <h3 style="margin-top:0;color:#0369a1;">Data Storage Locations</h3>
        <p class="muted" style="margin-bottom:8px;">
            All structured data (users, jobs, applications, configuration) is stored as JSON files on the server's file system.
        </p>
        <table style="width:100%;border-collapse:collapse;font-size:0.9em;">
            <tr style="background:#e0f2fe;">
                <th style="text-align:left;padding:6px 10px;border:1px solid #bae6fd;">Data Type</th>
                <th style="text-align:left;padding:6px 10px;border:1px solid #bae6fd;">File / Directory</th>
                <th style="text-align:left;padding:6px 10px;border:1px solid #bae6fd;">Description</th>
            </tr>
            <tr>
                <td style="padding:6px 10px;border:1px solid #bae6fd;">Users</td>
                <td style="padding:6px 10px;border:1px solid #bae6fd;font-family:monospace;"><%= dataBaseDir %>/users/&lt;id&gt;.json<br><span style="color:#94a3b8;">(aggregate: users.json)</span></td>
                <td style="padding:6px 10px;border:1px solid #bae6fd;">One JSON file per user account (TA, MO, Admin)</td>
            </tr>
            <tr style="background:#f8fafc;">
                <td style="padding:6px 10px;border:1px solid #bae6fd;">Jobs</td>
                <td style="padding:6px 10px;border:1px solid #bae6fd;font-family:monospace;"><%= dataBaseDir %>/jobs.json</td>
                <td style="padding:6px 10px;border:1px solid #bae6fd;">All TA job postings</td>
            </tr>
            <tr>
                <td style="padding:6px 10px;border:1px solid #bae6fd;">Applications</td>
                <td style="padding:6px 10px;border:1px solid #bae6fd;font-family:monospace;"><%= dataBaseDir %>/applications.json</td>
                <td style="padding:6px 10px;border:1px solid #bae6fd;">All student TA applications and their statuses</td>
            </tr>
            <tr style="background:#f8fafc;">
                <td style="padding:6px 10px;border:1px solid #bae6fd;">System Config</td>
                <td style="padding:6px 10px;border:1px solid #bae6fd;font-family:monospace;"><%= dataBaseDir %>/config.json</td>
                <td style="padding:6px 10px;border:1px solid #bae6fd;">System-wide configuration (this page)</td>
            </tr>
            <tr>
                <td style="padding:6px 10px;border:1px solid #bae6fd;">CV Files</td>
                <td style="padding:6px 10px;border:1px solid #bae6fd;font-family:monospace;"><%= cvAbsPath %></td>
                <td style="padding:6px 10px;border:1px solid #bae6fd;">Uploaded student CV/resume files (location depends on Storage Mode below)</td>
            </tr>
        </table>
    </div>

    <div class="section">
        <form method="post" action="<%= request.getContextPath() %>/admin/config">
            <div class="form-group">
                <label>Max Courses Per TA (Circuit-Breaker Threshold)</label>
                <input type="number" name="maxCoursesPerTA" value="<%= maxCourses %>" min="1">
            </div>
            <div class="form-group">
                <label>Relative CV Storage Path</label>
                <input type="text" name="cvRelativePath" value="<%= cvPath %>">
                <p class="muted">Only used when Storage Mode is <strong>WEBAPP</strong>. Path is relative to the web application root (e.g. <code>/WEB-INF/data/cvs</code>).</p>
            </div>
            <div class="form-group">
                <label>Storage Mode</label>
                <select name="storageMode">
                    <option value="WEBAPP" <%= "WEBAPP".equals(storageMode) ? "selected" : "" %>>WEBAPP</option>
                    <option value="USER_HOME" <%= "USER_HOME".equals(storageMode) ? "selected" : "" %>>USER_HOME</option>
                </select>
                <div style="margin-top:8px;padding:10px 14px;background:#f8fafc;border:1px solid #e2e8f0;border-radius:4px;font-size:0.88em;line-height:1.6;">
                    <p style="margin:0 0 6px 0;"><strong>WEBAPP</strong> &mdash; CV files are stored <em>inside</em> the deployed web application directory.
                        Files are placed at the path configured above (Relative CV Storage Path).
                        Simple to use, but uploaded files
                        <strong>may be lost if the application is redeployed or the server is restarted with a clean deployment</strong>.</p>
                    <p style="margin:0;"><strong>USER_HOME</strong> &mdash; CV files are stored in the <em>server operating-system user's home directory</em>
                        (<code>~/ebu_data/cvs</code>).
                        Files <strong>persist across application redeployments</strong>, but the server process must have write access to the home directory.</p>
                    <p style="margin:6px 0 0 0;color:#0369a1;"><strong>Current resolved CV path:</strong> <code><%= cvAbsPath %></code></p>
                </div>
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
