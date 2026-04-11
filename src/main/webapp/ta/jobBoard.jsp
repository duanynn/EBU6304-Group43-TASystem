<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="bupt.is.ta.model.Job" %>
<%@ page import="bupt.is.ta.model.User" %>
<%@ page import="bupt.is.ta.model.Application" %>
<%@ page import="java.util.Map" %>
<%
    List<Job> jobs = (List<Job>) request.getAttribute("jobs");
    if (jobs == null) jobs = java.util.List.of();
    Map<String, Integer> fitScores = (Map<String, Integer>) request.getAttribute("fitScores");
    if (fitScores == null) fitScores = java.util.Map.of();
    Map<String, Application.Status> appliedJobStatus = (Map<String, Application.Status>) request.getAttribute("appliedJobStatus");
    if (appliedJobStatus == null) appliedJobStatus = java.util.Map.of();
    String jobBoardHint = (String) request.getAttribute("jobBoardHint");
    User current = (User) session.getAttribute("currentUser");
    Boolean triggerBackgroundAi = (Boolean) request.getAttribute("triggerBackgroundAi");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Job Board - TA Recruitment System</title>
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
    <h2 class="page-title">Open Positions</h2>
    <% if (jobBoardHint != null && !jobBoardHint.isBlank()) { %>
    <div class="alert alert-warning"><%= jobBoardHint %></div>
    <% } %>
    <div class="section">
        <table class="data-table">
            <thead>
                <tr>
                    <th>Course</th>
                    <th>Openings</th>
                    <th>Required Skills</th>
                    <th>Working Time</th>
                    <th>AI Fit Estimate</th>
                    <th>Action</th>
                </tr>
            </thead>
            <tbody>
                <% for (Job job : jobs) { %>
                <tr>
                    <td><%= job.getCourseName() %></td>
                    <td><%= job.getRequiredCount() %></td>
                    <td><%= job.getRequiredSkills() != null ? String.join(", ", job.getRequiredSkills()) : "-" %></td>
                    <td><%= job.getRequiredWorkTime() == null || job.getRequiredWorkTime().isBlank() ? "-" : job.getRequiredWorkTime() %></td>
                    <td>
                        <%
                            int fit = fitScores.getOrDefault(job.getId(), 0);
                            String fitClass = fit >= 80 ? "fit-high" : (fit >= 55 ? "fit-mid" : "fit-low");
                        %>
                        <span class="fit-pill <%= fitClass %>"><%= fit %>%</span>
                    </td>
                    <td>
                        <% Application.Status existingStatus = appliedJobStatus.get(job.getId()); %>
                        <% if (existingStatus != null) { %>
                            <span class="btn btn-small btn-secondary" style="pointer-events:none;opacity:.8;">
                                <%= existingStatus == Application.Status.ACCEPTED ? "Accepted" : "Applied (" + existingStatus + ")" %>
                            </span>
                        <% } else { %>
                        <form method="post" action="<%= request.getContextPath() %>/ta/apply" style="display:inline">
                            <input type="hidden" name="jobId" value="<%= job.getId() %>"/>
                            <button type="submit" class="btn btn-small">Apply</button>
                        </form>
                        <% } %>
                    </td>
                </tr>
                <% } %>
                <% if (jobs.isEmpty()) { %>
                <tr><td colspan="6" class="empty-hint">No open positions yet. Please check back later.</td></tr>
                <% } %>
            </tbody>
        </table>
    </div>
</main>
<% if (Boolean.TRUE.equals(triggerBackgroundAi)) { %>
<script>
(function () {
    fetch('<%= request.getContextPath() %>/ta/refreshNewJobsAi', {
        method: 'POST',
        headers: { 'X-Requested-With': 'XMLHttpRequest' },
        keepalive: true
    }).catch(function () { /* Background refresh failure should not block browsing. */ });
})();
</script>
<% } %>
</body>
</html>
