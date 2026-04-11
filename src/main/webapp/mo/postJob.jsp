<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="bupt.is.ta.model.User" %>
<%
    User current = (User) session.getAttribute("currentUser");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Post New Job - Instructor Workspace</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<header class="app-header">
    <h1>TA Recruitment System - Instructor Workspace</h1>
    <span class="user-info"><%= current != null ? current.getName() : "" %> <a href="<%= request.getContextPath() %>/login">Logout</a></span>
</header>
<nav class="app-nav">
    <a href="<%= request.getContextPath() %>/mo/dashboard">My Jobs</a>
    <a href="<%= request.getContextPath() %>/mo/postJob">Post New Job</a>
</nav>
<main class="app-main">
    <h2 class="page-title">Post New Job</h2>
    <div class="section">
        <form method="post" action="<%= request.getContextPath() %>/mo/postJob">
            <div class="form-group">
                <label>Course Name</label>
                <input type="text" name="courseName" required placeholder="e.g. Software Engineering">
            </div>
            <div class="form-group">
                <label>Required TA Count</label>
                <input type="number" name="requiredCount" value="1" min="1" required>
            </div>
            <div class="form-group">
                <label>Required Skills (comma-separated)</label>
                <input type="text" name="requiredSkills" placeholder="Java, Git, Python">
            </div>
            <div class="form-group">
                <label>Required Working Time</label>
                <input type="text" name="requiredWorkTime" required placeholder="e.g. Tue afternoon / Thu evening / 8 hrs weekly">
            </div>
            <div class="form-actions">
                <button type="submit" class="btn">Post</button>
                <a href="<%= request.getContextPath() %>/mo/dashboard" class="btn btn-secondary">Cancel</a>
            </div>
        </form>
    </div>
</main>
</body>
</html>
