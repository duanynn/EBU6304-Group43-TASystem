<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="bupt.is.ta.model.Application" %>
<%@ page import="bupt.is.ta.model.Job" %>
<%@ page import="bupt.is.ta.model.User" %>
<%@ page import="bupt.is.ta.service.SkillMatchService" %>
<%
    Job job = (Job) request.getAttribute("job");
    List<Application> applications = (List<Application>) request.getAttribute("applications");
    Map<Application, SkillMatchService.MatchResult> matchMap = (Map<Application, SkillMatchService.MatchResult>) request.getAttribute("matchMap");
    Map<String, User> studentMap = (Map<String, User>) request.getAttribute("studentMap");
    if (job == null) job = new bupt.is.ta.model.Job();
    if (applications == null) applications = List.of();
    if (matchMap == null) matchMap = Map.of();
    if (studentMap == null) studentMap = Map.of();
    User current = (User) session.getAttribute("currentUser");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Applicants - <%= job.getCourseName() %></title>
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
    <a href="<%= request.getContextPath() %>/mo/dashboard" class="back-link">← Back to Job List</a>
    <h2 class="page-title">Applicants: <%= job.getCourseName() %></h2>
    <div class="section">
        <div class="table-tools">
            <label>Status
                <select id="appStatusFilter">
                    <option value="">All</option>
                    <option value="PENDING">PENDING</option>
                    <option value="INTERVIEWING">INTERVIEWING</option>
                    <option value="ACCEPTED">ACCEPTED</option>
                    <option value="REJECTED">REJECTED</option>
                </select>
            </label>
            <label>Keyword
                <input type="text" id="appKeywordFilter" placeholder="Name/Student ID">
            </label>
        </div>
        <table class="data-table">
            <thead>
                <tr>
                    <th>Student ID</th><th>Name</th><th>Available Time</th><th>Fit</th><th>CV</th><th>Status</th><th>Action</th>
                </tr>
            </thead>
            <tbody>
                <% for (Application app : applications) {
                    User stu = studentMap.get(app.getStudentId());
                    SkillMatchService.MatchResult match = matchMap.get(app);
                    int pct = match != null ? (int) Math.round(match.getAiScore()) : 0;
                    String badgeClass = pct >= 80 ? "badge-high" : (pct < 50 ? "badge-low" : "badge-mid");
                    String badge = pct >= 80 ? "High Match" : (pct < 50 ? "Low Match" : "Match");
                %>
                <tr data-status="<%= app.getStatus() %>"
                    data-keyword="<%= ((stu != null ? stu.getName() : "") + " " + app.getStudentId()).toLowerCase() %>">
                    <td><%= app.getStudentId() %></td>
                    <td><%= stu != null ? stu.getName() : "-" %></td>
                    <td><%= stu != null && stu.getAvailableTime() != null && !stu.getAvailableTime().isBlank() ? stu.getAvailableTime() : "-" %></td>
                    <td><span class="<%= badgeClass %>"><%= pct %>% (<%= badge %>)</span></td>
                    <td>
                        <% if (stu != null && stu.getCvPath() != null && !stu.getCvPath().isBlank()) { %>
                        <a class="btn btn-small" href="<%= request.getContextPath() %>/mo/cv/view?studentId=<%= app.getStudentId() %>&jobId=<%= job.getId() %>">View Web CV</a>
                        <% } else { %>
                        -
                        <% } %>
                    </td>
                    <td><span class="status-tag status-<%= app.getStatus().name().toLowerCase() %>"><%= app.getStatus() %></span></td>
                    <td>
                        <% if (app.getStatus() != Application.Status.ACCEPTED && app.getStatus() != Application.Status.REJECTED) { %>
                        <form method="post" action="<%= request.getContextPath() %>/mo/updateStatus" style="display:inline">
                            <input type="hidden" name="applicationId" value="<%= app.getId() %>"/>
                            <input type="hidden" name="status" value="ACCEPTED"/>
                            <button type="submit" class="btn btn-small btn-success">Accept</button>
                        </form>
                        <form method="post" action="<%= request.getContextPath() %>/mo/updateStatus" style="display:inline">
                            <input type="hidden" name="applicationId" value="<%= app.getId() %>"/>
                            <input type="hidden" name="status" value="REJECTED"/>
                            <button type="submit" class="btn btn-small btn-danger">Reject</button>
                        </form>
                        <form method="post" action="<%= request.getContextPath() %>/mo/updateStatus" style="display:inline">
                            <input type="hidden" name="applicationId" value="<%= app.getId() %>"/>
                            <input type="hidden" name="status" value="INTERVIEWING"/>
                            <button type="submit" class="btn btn-small btn-secondary">Move to Interview</button>
                        </form>
                        <% } else { %>
                        <%= app.getStatus() %>
                        <% } %>
                    </td>
                </tr>
                <% } %>
                <% if (applications.isEmpty()) { %>
                <tr><td colspan="7" class="empty-hint">No applicants yet.</td></tr>
                <% } %>
            </tbody>
        </table>
    </div>
</main>
<script>
    (function () {
        var status = document.getElementById('appStatusFilter');
        var keyword = document.getElementById('appKeywordFilter');
        var rows = document.querySelectorAll('.data-table tbody tr[data-status]');
        function applyFilter() {
            var sv = status ? status.value : '';
            var kv = keyword ? keyword.value.trim().toLowerCase() : '';
            rows.forEach(function (row) {
                var okStatus = !sv || row.dataset.status === sv;
                var text = row.dataset.keyword || '';
                var okKeyword = !kv || text.indexOf(kv) >= 0;
                row.style.display = (okStatus && okKeyword) ? '' : 'none';
            });
        }
        if (status) status.addEventListener('change', applyFilter);
        if (keyword) keyword.addEventListener('input', applyFilter);
    })();
</script>
</body>
</html>
