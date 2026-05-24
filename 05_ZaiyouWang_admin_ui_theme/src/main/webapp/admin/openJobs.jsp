<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="bupt.is.ta.model.User" %>
<%@ page import="bupt.is.ta.web.AdminController" %>
<%@ page import="bupt.is.ta.util.JobScheduleUtil" %>
<%@ page import="bupt.is.ta.util.JobDisplayUtil" %>
<%!
    private String h(Object value) {
        if (value == null) return "";
        return String.valueOf(value)
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
%>
<%
    @SuppressWarnings("unchecked")
    List<AdminController.OpenJobRow> rows = (List<AdminController.OpenJobRow>) request.getAttribute("openJobRows");
    if (rows == null) rows = List.of();
    Boolean openOnly = (Boolean) request.getAttribute("openOnly");
    if (openOnly == null) openOnly = Boolean.TRUE;
    User current = (User) session.getAttribute("currentUser");
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Open Jobs - Admin</title>
    <link rel="stylesheet" href="<%= ctx %>/css/style.css?v=20260520-admin-jobs">
</head>
<body>
<header class="app-header">
    <h1>TA Recruitment System - Admin</h1>
    <span class="user-info user-info-with-avatar"><%= current != null ? h(current.getName()) : "" %>
        <jsp:include page="/WEB-INF/jsp/account-menu.jsp"/>
    </span>
</header>
<nav class="app-nav">
    <a href="<%= ctx %>/admin/overview">Overview</a>
    <a href="<%= ctx %>/admin/openJobs">Open Jobs</a>
    <a href="<%= ctx %>/admin/workload">Workload</a>
    <a href="<%= ctx %>/admin/users">Users</a>
    <a href="<%= ctx %>/admin/config">System Config</a>
</nav>
<main class="app-main">
    <div class="page-head">
        <div>
            <h2 class="page-title">Open Positions Overview</h2>
            <p class="page-subtitle muted">Course schedule table for recruitment posts system-wide.</p>
        </div>
        <div class="form-actions">
            <a class="btn <%= openOnly ? "btn-primary" : "btn-secondary" %>" href="<%= ctx %>/admin/openJobs?filter=open">Open only</a>
            <a class="btn <%= openOnly ? "btn-secondary" : "btn-primary" %>" href="<%= ctx %>/admin/openJobs?filter=all">All jobs</a>
        </div>
    </div>
    <div class="section table-section">
        <table class="data-table">
            <thead>
            <tr>
                <th>Course</th>
                <th>Type</th>
                <th>Instructor</th>
                <th>Schedule</th>
                <th>Openings</th>
                <th>Applications</th>
                <th>Status</th>
            </tr>
            </thead>
            <tbody>
            <% for (AdminController.OpenJobRow row : rows) {
                var job = row.getJob();
                User mo = row.getMo();
            %>
            <tr>
                <td><strong><%= h(job.getCourseName()) %></strong></td>
                <td class="cell-nowrap"><%= h(JobDisplayUtil.jobTypeLabel(job)) %></td>
                <td>
                    <% if (mo != null) {
                        request.setAttribute("avatarUser", mo);
                    %>
                    <span class="name-with-avatar">
                        <jsp:include page="/WEB-INF/jsp/avatar.jsp"><jsp:param name="size" value="32"/></jsp:include>
                        <%= h(mo.getName()) %>
                    </span>
                    <% } else { %>-<% } %>
                </td>
                <td><%= h(JobScheduleUtil.displayWorkTime(job)) %></td>
                <td class="cell-nowrap"><%= job.getRequiredCount() %></td>
                <td class="cell-nowrap"><%= row.getApplicationCount() %></td>
                <td class="cell-nowrap">
                    <% if (job.isOpen()) { %>
                    <span class="status-tag status-pending">OPEN</span>
                    <% } else { %>
                    <span class="status-tag status-rejected">CLOSED</span>
                    <% } %>
                </td>
            </tr>
            <% } %>
            <% if (rows.isEmpty()) { %>
            <tr><td colspan="7" class="empty-hint">No jobs match this filter.</td></tr>
            <% } %>
            </tbody>
        </table>
    </div>
</main>
</body>
</html>
