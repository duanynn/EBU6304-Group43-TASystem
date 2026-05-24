<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="bupt.is.ta.model.Job" %>
<%@ page import="bupt.is.ta.model.User" %>
<%@ page import="bupt.is.ta.model.Application" %>
<%@ page import="bupt.is.ta.service.SkillMatchService" %>
<%@ page import="bupt.is.ta.web.TAController" %>
<%@ page import="bupt.is.ta.util.JobDisplayUtil" %>
<%@ page import="bupt.is.ta.util.JobScheduleUtil" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.Map" %>
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
    Map<String, TAController.JobAdviceView> jobAdviceByJobId = (Map<String, TAController.JobAdviceView>) request.getAttribute("jobAdviceByJobId");
    if (jobAdviceByJobId == null) jobAdviceByJobId = java.util.Map.of();
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
    Boolean applicationOpen = (Boolean) request.getAttribute("applicationOpen");
    if (applicationOpen == null) applicationOpen = Boolean.TRUE;
    String currentSemester = (String) request.getAttribute("currentSemester");
    if (currentSemester == null) currentSemester = "";
    String applicationDeadlineDisplay = (String) request.getAttribute("applicationDeadlineDisplay");
    if (applicationDeadlineDisplay == null) applicationDeadlineDisplay = "";
    String searchJobType = (String) request.getAttribute("searchJobType");
    if (searchJobType == null) searchJobType = "";
    Set<String> conflictJobIds = (Set<String>) request.getAttribute("conflictJobIds");
    if (conflictJobIds == null) conflictJobIds = java.util.Set.of();
    Map<String, Application> applicationByJobId = (Map<String, Application>) request.getAttribute("applicationByJobId");
    if (applicationByJobId == null) applicationByJobId = Map.of();
    Long interviewPendingCount = (Long) request.getAttribute("interviewPendingCount");
    if (interviewPendingCount == null) interviewPendingCount = 0L;
    @SuppressWarnings("unchecked")
    List<Application> pendingInterviewApps = (List<Application>) request.getAttribute("pendingInterviewApps");
    if (pendingInterviewApps == null) pendingInterviewApps = List.of();
    int bestFit = 0;
    int strongFitCount = 0;
    for (Job item : jobs) {
        int score = fitScores.getOrDefault(item.getId(), 0);
        if (score > bestFit) bestFit = score;
        if (score >= 75) strongFitCount++;
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Job Board - TA Recruitment System</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css?v=20260518-ui2">
</head>
<body class="layout-wide">
<header class="app-header app-header-with-avatar">
    <h1>TA Recruitment System - Student Portal</h1>
    <span class="user-info user-info-with-avatar">
        <jsp:include page="/WEB-INF/jsp/avatar.jsp"><jsp:param name="size" value="36"/></jsp:include>
        <%= h(current != null ? current.getName() : "") %>
        <jsp:include page="/WEB-INF/jsp/account-menu.jsp"/>
    </span>
</header>
<nav class="app-nav">
    <a href="<%= request.getContextPath() %>/ta/jobs">Job Board</a>
    <a href="<%= request.getContextPath() %>/ta/schedule">My Schedule<% if (interviewPendingCount > 0) { %> <span class="nav-badge"><%= interviewPendingCount %></span><% } %></a>
    <a href="<%= request.getContextPath() %>/ta/applications">My Applications</a>
    <a href="<%= request.getContextPath() %>/ta/profile">My Profile</a>
</nav>
<main class="app-main">
    <div class="page-head">
        <div>
            <h2 class="page-title">Open Positions</h2>
            <p class="page-subtitle">
                Semester <%= h(currentSemester) %>
                <% if (!applicationDeadlineDisplay.isBlank()) { %> | Deadline: <%= h(applicationDeadlineDisplay) %><% } %>
                <br>
                <%= Boolean.TRUE.equals(searchPerformed)
                        ? resultCount + " BM25 result(s) from " + totalOpenJobs + " open position(s)"
                        : totalOpenJobs + " open position(s)" %>
            </p>
        </div>
        <a class="btn btn-secondary" href="<%= request.getContextPath() %>/ta/profile">Update Profile</a>
    </div>
    <% if (!Boolean.TRUE.equals(applicationOpen)) { %>
    <div class="alert alert-warning">The application period has closed. You can still browse positions, but new applications are disabled.</div>
    <% } %>
    <% if (jobBoardHint != null && !jobBoardHint.isBlank()) { %>
    <div class="alert alert-warning"><%= h(jobBoardHint) %></div>
    <% } %>
    <% if (interviewPendingCount > 0) { %>
    <div class="alert alert-warning interview-pending-alert">
        <strong><%= interviewPendingCount %> interview invitation(s)</strong> need your response.
        <a class="btn btn-small" href="<%= request.getContextPath() %>/ta/applications">Review &amp; respond</a>
    </div>
    <% } %>

    <% if (Boolean.TRUE.equals(triggerBackgroundAi)) { %>
    <div class="alert alert-warning">Refreshing AI fit scores for new jobs. This page will update when the refresh finishes.</div>
    <% } %>

    <section class="section job-board-shell">
        <form class="toolbar-form" method="get" action="<%= request.getContextPath() %>/ta/jobs">
            <label>Search
                <input type="text" name="q" value="<%= h(searchQuery) %>" placeholder="Course, Java, lab, Friday">
            </label>
            <label>Job type
                <select name="type">
                    <option value="" <%= searchJobType.isBlank() ? "selected" : "" %>>All types</option>
                    <option value="MODULE_TA" <%= "MODULE_TA".equals(searchJobType) ? "selected" : "" %>>Module TA</option>
                    <option value="INVIGILATION" <%= "INVIGILATION".equals(searchJobType) ? "selected" : "" %>>Invigilation</option>
                    <option value="OTHER" <%= "OTHER".equals(searchJobType) ? "selected" : "" %>>Other</option>
                </select>
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

        <div class="job-board-summary">
            <div>
                <span class="kv-label">Best profile fit</span>
                <strong><%= jobs.isEmpty() ? "-" : bestFit + "%" %></strong>
            </div>
            <div>
                <span class="kv-label">Recommended roles</span>
                <strong><%= strongFitCount %></strong>
            </div>
            <div>
                <span class="kv-label">Ranking mode</span>
                <strong><%= "fit".equals(searchSort) ? "Best fit" : (Boolean.TRUE.equals(searchPerformed) ? "BM25 relevance" : "Newest") %></strong>
            </div>
        </div>

        <% if (jobs.isEmpty()) { %>
        <p class="empty-hint">
            <%= Boolean.TRUE.equals(searchPerformed) ? "No open positions match this search." : "No open positions yet. Please check back later." %>
        </p>
        <% } else { %>
        <div class="job-card-list">
            <% for (Job job : jobs) {
                int fit = fitScores.getOrDefault(job.getId(), 0);
                int relevance = searchScores.getOrDefault(job.getId(), 0);
                String fitClass = fit >= 80 ? "fit-high" : (fit >= 55 ? "fit-mid" : "fit-low");
                List<String> skills = job.getRequiredSkills() == null ? java.util.List.of() : job.getRequiredSkills();
                String workTime = JobScheduleUtil.displayWorkTime(job);
                if ("-".equals(workTime)) workTime = "Time not specified";
                boolean scheduleConflict = conflictJobIds.contains(job.getId());
                String description = job.getDescription() == null || job.getDescription().isBlank() ? "No description has been provided yet. Review the required skills and working time before applying." : job.getDescription();
                TAController.JobAdviceView adviceView = jobAdviceByJobId.get(job.getId());
                SkillMatchService.MatchResult match = adviceView == null ? null : adviceView.getMatch();
                String fitSummary = match == null || match.getAiFitSummary() == null || match.getAiFitSummary().isBlank()
                        ? "Fit score is based on your profile skills, GPA, and available evidence."
                        : match.getAiFitSummary();
                List<String> gaps = match == null || match.getAiGaps() == null ? java.util.List.of() : match.getAiGaps();
                if (gaps.isEmpty() && match != null && match.getMissingSkills() != null) gaps = match.getMissingSkills();
                Application.Status existingStatus = appliedJobStatus.get(job.getId());
            %>
            <article class="job-board-card">
                <% if (existingStatus == Application.Status.INTERVIEWING) {
                    Application myApp = applicationByJobId.get(job.getId());
                    String inv = myApp != null ? JobScheduleUtil.formatInterviewSlot(myApp.getInterviewSlot()) : "";
                    if (inv.isBlank()) inv = "See My Applications for details";
                %>
                <div class="alert alert-info"><strong>Interview scheduled:</strong> <%= h(inv) %>
                    <% if (myApp != null && !myApp.getInterviewMessage().isBlank()) { %> — <%= h(myApp.getInterviewMessage().length() > 80 ? myApp.getInterviewMessage().substring(0, 80) + "..." : myApp.getInterviewMessage()) %><% } %>
                </div>
                <% } %>
                <% if (match != null && match.hasScheduleFit() && match.getScheduleScore() < 50) { %>
                <div class="alert alert-warning"><%= h(match.getScheduleSummary()) %>. Consider updating <a href="<%= request.getContextPath() %>/ta/profile">your availability</a>.</div>
                <% } else if (match != null && match.hasScheduleFit()) { %>
                <p class="muted schedule-fit-hint"><%= h(match.getScheduleSummary()) %></p>
                <% } %>
                <div class="job-card-head">
                    <div class="job-card-title">
                        <span class="job-type-badge <%= JobDisplayUtil.jobTypeCssClass(job) %>"><%= h(JobDisplayUtil.jobTypeLabel(job)) %></span>
                        <h3><%= h(job.getCourseName()) %></h3>
                        <p><%= h(workTime) %> - <%= job.getRequiredCount() %> opening<%= job.getRequiredCount() == 1 ? "" : "s" %></p>
                    </div>
                    <div class="fit-score-panel">
                        <span class="fit-pill <%= fitClass %>"><%= fit %>% fit</span>
                        <span class="mini-meter"><span class="<%= fitClass %>" style="width:<%= fit %>%"></span></span>
                        <% if (Boolean.TRUE.equals(searchPerformed)) { %>
                        <span class="search-score">Search <%= relevance %>%</span>
                        <% } %>
                    </div>
                </div>

                <p class="job-description"><%= h(description) %></p>

                <div class="job-card-grid">
                    <div>
                        <span class="kv-label">Required skills</span>
                        <div class="chip-wrap compact">
                            <% if (!skills.isEmpty()) {
                                for (String skill : skills) { %>
                            <span class="chip"><%= h(skill) %></span>
                            <% }} else { %>
                            <span class="muted">No skill tags</span>
                            <% } %>
                        </div>
                    </div>
                    <div>
                        <span class="kv-label">Recommendation insight</span>
                        <p class="fit-summary"><%= h(fitSummary) %></p>
                        <div class="chip-wrap compact">
                            <% if (!gaps.isEmpty()) {
                                for (String gap : gaps) { %>
                            <span class="chip chip-gap"><%= h(gap) %></span>
                            <% }} else { %>
                            <span class="chip chip-success">No obvious gap</span>
                            <% } %>
                        </div>
                    </div>
                </div>

                <div class="job-card-actions">
                    <span class="muted">Job ID: <%= h(job.getId()) %></span>
                    <% if (existingStatus != null) { %>
                    <span class="btn btn-small btn-secondary disabled-action">
                        <%= existingStatus == Application.Status.ACCEPTED ? "Accepted" : (existingStatus == Application.Status.INTERVIEWING ? "Interviewing" : "Applied (" + existingStatus + ")") %>
                    </span>
                    <% } else if (!Boolean.TRUE.equals(applicationOpen)) { %>
                    <span class="btn btn-small btn-secondary disabled-action">Applications closed</span>
                    <% } else if (scheduleConflict) { %>
                    <span class="btn btn-small btn-secondary disabled-action" title="This job overlaps with another position you already applied for.">Schedule conflict</span>
                    <% } else { %>
                    <form method="post" action="<%= request.getContextPath() %>/ta/apply">
                        <input type="hidden" name="jobId" value="<%= h(job.getId()) %>"/>
                        <button type="submit" class="btn btn-small">Review Match</button>
                    </form>
                    <% } %>
                </div>
            </article>
            <% } %>
        </div>
        <% } %>
    </section>
</main>
<% if (Boolean.TRUE.equals(triggerBackgroundAi)) { %>
<script>
(function () {
    fetch('<%= request.getContextPath() %>/ta/refreshNewJobsAi', {
        method: 'POST',
        headers: { 'X-Requested-With': 'XMLHttpRequest' },
        keepalive: true
    }).then(function (r) { return r.json(); }).then(function (data) {
        if (data && data.updated) window.location.reload();
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
