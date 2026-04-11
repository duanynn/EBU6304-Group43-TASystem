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
    <title>Overview - Admin</title>
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
    <h2 class="page-title">System Overview</h2>
    <%
        long totalUsers = request.getAttribute("totalUsers") == null ? 0L : Long.parseLong(String.valueOf(request.getAttribute("totalUsers")));
        long totalJobs = request.getAttribute("totalJobs") == null ? 0L : Long.parseLong(String.valueOf(request.getAttribute("totalJobs")));
        long totalApplications = request.getAttribute("totalApplications") == null ? 0L : Long.parseLong(String.valueOf(request.getAttribute("totalApplications")));
        long totalTa = request.getAttribute("totalTa") == null ? 0L : Long.parseLong(String.valueOf(request.getAttribute("totalTa")));
        long totalMo = request.getAttribute("totalMo") == null ? 0L : Long.parseLong(String.valueOf(request.getAttribute("totalMo")));
        long totalPending = request.getAttribute("totalPending") == null ? 0L : Long.parseLong(String.valueOf(request.getAttribute("totalPending")));
        long totalInterviewing = request.getAttribute("totalInterviewing") == null ? 0L : Long.parseLong(String.valueOf(request.getAttribute("totalInterviewing")));
        long totalAccepted = request.getAttribute("totalAccepted") == null ? 0L : Long.parseLong(String.valueOf(request.getAttribute("totalAccepted")));
        long totalRejected = request.getAttribute("totalRejected") == null ? 0L : Long.parseLong(String.valueOf(request.getAttribute("totalRejected")));
        long maxApp = Math.max(1L, Math.max(Math.max(totalPending, totalInterviewing), Math.max(totalAccepted, totalRejected)));
        int pPending = (int)Math.round(totalPending * 100.0 / maxApp);
        int pInterviewing = (int)Math.round(totalInterviewing * 100.0 / maxApp);
        int pAccepted = (int)Math.round(totalAccepted * 100.0 / maxApp);
        int pRejected = (int)Math.round(totalRejected * 100.0 / maxApp);
    %>
    <div class="kpi-grid">
        <div class="kpi-card"><div class="kpi-title">Registered Users</div><div class="kpi-value"><%= totalUsers %></div></div>
        <div class="kpi-card"><div class="kpi-title">Total Jobs</div><div class="kpi-value"><%= totalJobs %></div></div>
        <div class="kpi-card"><div class="kpi-title">Total Applications</div><div class="kpi-value"><%= totalApplications %></div></div>
        <div class="kpi-card"><div class="kpi-title">TA / Instructor</div><div class="kpi-value"><%= totalTa %> / <%= totalMo %></div></div>
    </div>

    <div class="section">
        <h3>Application Status Monitoring</h3>
        <div class="chart-bars">
            <div class="chart-row"><span>PENDING</span><div class="chart-track"><i class="chart-fill fill-pending" style="width:<%= pPending %>%"></i></div><b><%= totalPending %></b></div>
            <div class="chart-row"><span>INTERVIEWING</span><div class="chart-track"><i class="chart-fill fill-interview" style="width:<%= pInterviewing %>%"></i></div><b><%= totalInterviewing %></b></div>
            <div class="chart-row"><span>ACCEPTED</span><div class="chart-track"><i class="chart-fill fill-accept" style="width:<%= pAccepted %>%"></i></div><b><%= totalAccepted %></b></div>
            <div class="chart-row"><span>REJECTED</span><div class="chart-track"><i class="chart-fill fill-reject" style="width:<%= pRejected %>%"></i></div><b><%= totalRejected %></b></div>
        </div>
        <p class="muted">Values refresh with page reload; bar animations help quickly spot distribution changes.</p>
    </div>
</main>
</body>
</html>
