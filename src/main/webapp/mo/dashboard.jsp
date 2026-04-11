<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="bupt.is.ta.model.Job" %>
<%@ page import="bupt.is.ta.model.User" %>
<%
    List<Job> jobs = (List<Job>) request.getAttribute("jobs");
    if (jobs == null) jobs = List.of();
    User current = (User) session.getAttribute("currentUser");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>My Jobs - Instructor Workspace</title>
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
    <h2 class="page-title">Jobs I Posted</h2>
    <div class="section">
        <div class="table-tools">
            <label>Status
                <select id="jobStatusFilter">
                    <option value="">All</option>
                    <option value="open">Open</option>
                    <option value="closed">Closed</option>
                </select>
            </label>
            <label>Keyword
                <input type="text" id="jobKeywordFilter" placeholder="Course/Skill keyword">
            </label>
        </div>
        <table class="data-table">
            <thead>
                <tr>
                    <th>Course</th>
                    <th>Openings</th>
                    <th>Required Skills</th>
                    <th>Working Time</th>
                    <th>Status</th>
                    <th>Action</th>
                </tr>
            </thead>
            <tbody>
                <% for (Job j : jobs) { %>
                <tr data-open="<%= j.isOpen() ? "open" : "closed" %>"
                    data-keyword="<%= ((j.getCourseName() == null ? "" : j.getCourseName()) + " " + (j.getRequiredSkills() == null ? "" : String.join(" ", j.getRequiredSkills()))).toLowerCase() %>">
                    <td><%= j.getCourseName() %></td>
                    <td><%= j.getRequiredCount() %></td>
                    <td><%= j.getRequiredSkills() != null ? String.join(", ", j.getRequiredSkills()) : "-" %></td>
                    <td><%= j.getRequiredWorkTime() == null || j.getRequiredWorkTime().isBlank() ? "-" : j.getRequiredWorkTime() %></td>
                    <td><span class="status-tag <%= j.isOpen() ? "status-open" : "status-closed" %>"><%= j.isOpen() ? "Open" : "Closed" %></span></td>
                    <td>
                        <a href="<%= request.getContextPath() %>/mo/applicants?jobId=<%= j.getId() %>" class="btn btn-small">View Applicants</a>
                    </td>
                </tr>
                <% } %>
                <% if (jobs.isEmpty()) { %>
                <tr><td colspan="6" class="empty-hint">No jobs yet. Click "Post New Job".</td></tr>
                <% } %>
            </tbody>
        </table>
    </div>
</main>
<script>
    (function () {
        var status = document.getElementById('jobStatusFilter');
        var keyword = document.getElementById('jobKeywordFilter');
        var rows = document.querySelectorAll('.data-table tbody tr[data-open]');
        function applyFilter() {
            var sv = status ? status.value : '';
            var kv = keyword ? keyword.value.trim().toLowerCase() : '';
            rows.forEach(function (row) {
                var okStatus = !sv || row.dataset.open === sv;
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
