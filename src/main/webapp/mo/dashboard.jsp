<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="bupt.is.ta.model.Job" %>
<%@ page import="bupt.is.ta.model.User" %>
<%@ page import="bupt.is.ta.util.JobDisplayUtil" %>
<%@ page import="bupt.is.ta.util.JobScheduleUtil" %>
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
    List<Job> jobs = (List<Job>) request.getAttribute("jobs");
    if (jobs == null) jobs = List.of();
    User current = (User) session.getAttribute("currentUser");
    long openCount = jobs.stream().filter(Job::isOpen).count();
    long closedCount = jobs.size() - openCount;
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>My Jobs - Instructor Workspace</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css?v=20260518-ui2">
</head>
<body class="layout-wide">
<header class="app-header">
    <h1>TA Recruitment System - Instructor Workspace</h1>
    <span class="user-info user-info-with-avatar"><%= h(current != null ? current.getName() : "") %>
        <jsp:include page="/WEB-INF/jsp/account-menu.jsp"/>
    </span>
</header>
<nav class="app-nav">
    <a href="<%= request.getContextPath() %>/mo/home">My Home</a>
    <a href="<%= request.getContextPath() %>/mo/dashboard">My Jobs</a>
    <a href="<%= request.getContextPath() %>/mo/postJob">Post New Job</a>
</nav>
<main class="app-main">
    <div class="page-head">
        <div>
            <h2 class="page-title">Jobs I Posted</h2>
            <p class="page-subtitle"><%= openCount %> open / <%= closedCount %> closed</p>
        </div>
        <a class="btn" href="<%= request.getContextPath() %>/mo/postJob">Post New Job</a>
    </div>
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
                    <th>Type</th><th>Course</th>
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
                    data-keyword="<%= h(((j.getCourseName() == null ? "" : j.getCourseName()) + " " + (j.getDescription() == null ? "" : j.getDescription()) + " " + (j.getRequiredSkills() == null ? "" : String.join(" ", j.getRequiredSkills()))).toLowerCase()) %>">
                    <td><span class="job-type-badge <%= JobDisplayUtil.jobTypeCssClass(j) %>"><%= h(JobDisplayUtil.jobTypeLabel(j)) %></span></td>
                    <td>
                        <strong><%= h(j.getCourseName()) %></strong>
                        <% if (j.getDescription() != null && !j.getDescription().isBlank()) { %>
                        <p class="table-description"><%= h(j.getDescription()) %></p>
                        <% } %>
                    </td>
                    <td><%= j.getRequiredCount() %></td>
                    <td><%= h(j.getRequiredSkills() != null ? String.join(", ", j.getRequiredSkills()) : "-") %></td>
                    <td class="cell-nowrap"><%= h(JobScheduleUtil.displayWorkTime(j)) %></td>
                    <td><span class="status-tag <%= j.isOpen() ? "status-open" : "status-closed" %>"><%= j.isOpen() ? "Open" : "Closed" %></span></td>
                    <td>
                        <a href="<%= request.getContextPath() %>/mo/applicants?jobId=<%= h(j.getId()) %>" class="btn btn-small">View Applicants</a>
                        <form method="post" action="<%= request.getContextPath() %>/mo/updateJobStatus" style="display:inline">
                            <input type="hidden" name="jobId" value="<%= h(j.getId()) %>">
                            <input type="hidden" name="open" value="<%= j.isOpen() ? "false" : "true" %>">
                            <button type="submit" class="btn btn-small <%= j.isOpen() ? "btn-secondary" : "btn-success" %>">
                                <%= j.isOpen() ? "Close" : "Reopen" %>
                            </button>
                        </form>
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
