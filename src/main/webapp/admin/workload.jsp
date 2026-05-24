<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="bupt.is.ta.model.User" %>
<%@ page import="bupt.is.ta.model.Application" %>
<%
    @SuppressWarnings("unchecked")
    List<Map.Entry<String, Long>> workload = (List<Map.Entry<String, Long>>) request.getAttribute("workload");
    Map<String, User> userMap = (Map<String, User>) request.getAttribute("userMap");
    if (workload == null) workload = List.of();
    if (userMap == null) userMap = Map.of();
    Integer maxCoursesPerTA = (Integer) request.getAttribute("maxCoursesPerTA");
    if (maxCoursesPerTA == null) maxCoursesPerTA = 2;
    User current = (User) session.getAttribute("currentUser");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Workload Statistics - Admin</title>
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
    <h2 class="page-title">Accepted TA Course Count (By Student)</h2>
    <p class="page-subtitle muted">Circuit-breaker threshold: <%= maxCoursesPerTA %> course(s) per TA</p>
    <div class="section">
        <table class="data-table">
            <thead>
                <tr><th>Student ID</th><th>Name</th><th>Assigned Course Count</th><th>Status</th></tr>
            </thead>
            <tbody>
                <% for (Map.Entry<String, Long> e : workload) {
                    User u = userMap.get(e.getKey());
                %>
                <tr>
                    <td><%= e.getKey() %></td>
                    <td><%= u != null ? u.getName() : "-" %></td>
                    <td><%= e.getValue() %></td>
                    <td><%= e.getValue() >= maxCoursesPerTA ? "At limit" : "Available" %></td>
                </tr>
                <% } %>
                <% if (workload.isEmpty()) { %>
                <tr><td colspan="4" class="empty-hint">No accepted records yet.</td></tr>
                <% } %>
            </tbody>
        </table>
    </div>
</main>
</body>
</html>
