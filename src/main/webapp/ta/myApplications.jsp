<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="bupt.is.ta.model.Application" %>
<%@ page import="bupt.is.ta.model.Job" %>
<%@ page import="bupt.is.ta.model.User" %>
<%@ page import="bupt.is.ta.store.DataStore" %>
<%
    List<Application> applications = (List<Application>) request.getAttribute("applications");
    if (applications == null) applications = List.of();
    User current = (User) session.getAttribute("currentUser");
    List<Job> allJobs = DataStore.getInstance().getJobs();
    java.util.Map<String, Job> jobMap = allJobs.stream().collect(java.util.stream.Collectors.toMap(Job::getId, j -> j, (a,b)->a));
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>My Applications - TA Recruitment System</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<header class="app-header">
    <h1>TA Recruitment System - Student Portal</h1>
    <span class="user-info"><%= current != null ? current.getName() : "" %> <a href="<%= request.getContextPath() %>/login">Logout</a></span>
</header>
<nav class="app-nav">
    <a href="<%= request.getContextPath() %>/ta/jobs">Job Board</a>
    <a href="<%= request.getContextPath() %>/ta/applications">My Applications</a>
    <a href="<%= request.getContextPath() %>/ta/profile">My Profile</a>
</nav>
<main class="app-main">
    <h2 class="page-title">My Applications</h2>
    <div class="section">
        <table class="data-table">
            <thead>
                <tr><th>Course/Position</th><th>Status</th><th>Applied At</th></tr>
            </thead>
            <tbody>
                <% for (Application app : applications) {
                    Job j = jobMap.get(app.getJobId());
                %>
                <tr>
                    <td><%= j != null ? j.getCourseName() : app.getJobId() %></td>
                    <td><%= app.getStatus() %></td>
                    <td><%= app.getAppliedAt() != null ? app.getAppliedAt().toString() : "-" %></td>
                </tr>
                <% } %>
                <% if (applications.isEmpty()) { %>
                <tr><td colspan="3" class="empty-hint">No application records yet.</td></tr>
                <% } %>
            </tbody>
        </table>
    </div>
</main>
</body>
</html>
