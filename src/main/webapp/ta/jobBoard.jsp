<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="bupt.is.ta.model.Job" %>
<%@ page import="bupt.is.ta.model.User" %>
<%@ page import="bupt.is.ta.model.Application" %>
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
    if (jobs == null) jobs = java.util.List.of();
    Map<String, Integer> fitScores = (Map<String, Integer>) request.getAttribute("fitScores");
    if (fitScores == null) fitScores = java.util.Map.of();
    Map<String, Integer> searchScores = (Map<String, Integer>) request.getAttribute("searchScores");
    if (searchScores == null) searchScores = java.util.Map.of();
    Map<String, Application.Status> appliedJobStatus = (Map<String, Application.Status>) request.getAttribute("appliedJobStatus");
    if (appliedJobStatus == null) appliedJobStatus = java.util.Map.of();
    String jobBoardHint = (String) request.getAttribute("jobBoardHint");
    String searchQuery = (String) request.getAttribute("searchQuery");
    if (searchQuery == null) searchQuery = "";
    String searchSort = (String) request.getAttribute("searchSort");
    if (searchSort == null || searchSort.isBlank()) searchSort = "newest";
    Boolean searchPerformed = (Boolean) request.getAttribute("searchPerformed");
    Integer totalOpenJobs = (Integer) request.getAttribute("totalOpenJobs");
    Integer resultCount = (Integer) request.getAttribute("resultCount");
    if (totalOpenJobs == null) totalOpenJobs = jobs.size();
    if (resultCount == null) resultCount = jobs.size();
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
    <span class="user-info"><%= h(current != null ? current.getName() : "") %> <a href="<%= request.getContextPath() %>/login">Logout</a></span>
</header>
<nav class="app-nav">
    <a href="<%= request.getContextPath() %>/ta/jobs">Job Board</a>
    <a href="<%= request.getContextPath() %>/ta/applications">My Applications</a>
    <a href="<%= request.getContextPath() %>/ta/profile">My Profile</a>
</nav>
<main class="app-main">
    <div class="page-head">
        <div>
            <h2 class="page-title">Open Positions</h2>
            <p class="page-subtitle">
                <%= Boolean.TRUE.equals(searchPerformed)
                        ? resultCount + " BM25 result(s) from " + totalOpenJobs + " open position(s)"
                        : totalOpenJobs + " open position(s)" %>
            </p>
        </div>
        <a class="btn btn-secondary" href="<%= request.getContextPath() %>/ta/profile">Update Profile</a>
    </div>
    <% if (jobBoardHint != null && !jobBoardHint.isBlank()) { %>
    <div class="alert alert-warning"><%= h(jobBoardHint) %></div>
    <% } %>

    <section class="section table-section">
        <form class="toolbar-form" method="get" action="<%= request.getContextPath() %>/ta/jobs">
            <label>Search
                <input type="text" name="q" value="<%= h(searchQuery) %>" placeholder="Course, Java, lab, Friday">
            </label>
            <label>Sort
                <select name="sort">
                    <option value="relevance" <%= "relevance".equals(searchSort) ? "selected" : "" %>>BM25 relevance</option>
                    <option value="fit" <%= "fit".equals(searchSort) ? "selected" : "" %>>Best fit</option>
                    <option value="newest" <%= "newest".equals(searchSort) ? "selected" : "" %>>Newest</option>
                </select>
            </label>
            <button type="submit" class="btn">Search</button>
            <% if (Boolean.TRUE.equals(searchPerformed)) { %>
            <a class="btn btn-secondary" href="<%= request.getContextPath() %>/ta/jobs">Clear</a>
            <% } %>
        </form>

        <table class="data-table job-table">
            <thead>
                <tr>
                    <th>Position</th>
                    <th>Requirements</th>
                    <th>AI Fit</th>
                    <th>Search</th>
                    <th>Openings</th>
                    <th>Action</th>
                </tr>
            </thead>
            <tbody>
                <% for (Job job : jobs) {
                    int fit = fitScores.getOrDefault(job.getId(), 0);
                    int relevance = searchScores.getOrDefault(job.getId(), 0);
                    String fitClass = fit >= 80 ? "fit-high" : (fit >= 55 ? "fit-mid" : "fit-low");
                    List<String> skills = job.getRequiredSkills() == null ? java.util.List.of() : job.getRequiredSkills();
                    String workTime = job.getRequiredWorkTime() == null || job.getRequiredWorkTime().isBlank() ? "-" : job.getRequiredWorkTime();
                %>
                <tr>
                    <td class="job-title-cell">
                        <strong><%= h(job.getCourseName()) %></strong>
                        <span class="muted">ID: <%= h(job.getId()) %></span>
                    </td>
                    <td>
                        <div class="chip-wrap compact">
                            <% if (!skills.isEmpty()) {
                                for (String skill : skills) { %>
                            <span class="chip"><%= h(skill) %></span>
                            <% }} else { %>
                            <span class="muted">No skill tags</span>
                            <% } %>
                        </div>
                        <div class="muted">Time: <%= h(workTime) %></div>
                    </td>
                    <td>
                        <div class="score-stack">
                            <span class="fit-pill <%= fitClass %>"><%= fit %>%</span>
                            <span class="mini-meter"><span style="width:<%= fit %>%"></span></span>
                        </div>
                    </td>
                    <td>
                        <% if (Boolean.TRUE.equals(searchPerformed)) { %>
                        <div class="score-stack">
                            <span class="fit-pill fit-search"><%= relevance %>%</span>
                            <span class="mini-meter search-meter"><span style="width:<%= relevance %>%"></span></span>
                        </div>
                        <% } else { %>
                        <span class="muted">-</span>
                        <% } %>
                    </td>
                    <td><%= job.getRequiredCount() %></td>
                    <td>
                        <% Application.Status existingStatus = appliedJobStatus.get(job.getId()); %>
                        <% if (existingStatus != null) { %>
                            <span class="btn btn-small btn-secondary disabled-action">
                                <%= existingStatus == Application.Status.ACCEPTED ? "Accepted" : "Applied (" + existingStatus + ")" %>
                            </span>
                        <% } else { %>
                        <form method="post" action="<%= request.getContextPath() %>/ta/apply" style="display:inline">
                            <input type="hidden" name="jobId" value="<%= h(job.getId()) %>"/>
                            <button type="submit" class="btn btn-small">Apply</button>
                        </form>
                        <% } %>
                    </td>
                </tr>
                <% } %>
                <% if (jobs.isEmpty()) { %>
                <tr><td colspan="6" class="empty-hint">
                    <%= Boolean.TRUE.equals(searchPerformed) ? "No open positions match this search." : "No open positions yet. Please check back later." %>
                </td></tr>
                <% } %>
            </tbody>
        </table>
    </section>
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
<script>
(function () {
    var form = document.querySelector('.toolbar-form');
    if (!form) return;
    var query = form.querySelector('input[name="q"]');
    var sort = form.querySelector('select[name="sort"]');
    var sortTouched = false;
    if (sort) {
        sort.addEventListener('change', function () {
            sortTouched = true;
        });
    }
    form.addEventListener('submit', function () {
        if (!query || !sort || sortTouched) return;
        if (query.value.trim() && sort.value === 'newest') {
            sort.value = 'relevance';
        }
    });
})();
</script>
</body>
</html>
