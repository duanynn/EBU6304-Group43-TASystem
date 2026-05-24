<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="bupt.is.ta.model.Application" %>
<%@ page import="bupt.is.ta.model.Job" %>
<%@ page import="bupt.is.ta.model.User" %>
<%@ page import="bupt.is.ta.service.SkillMatchService" %>
<%@ page import="bupt.is.ta.service.ScheduleFitService" %>
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
    Job job = (Job) request.getAttribute("job");
    List<Application> applications = (List<Application>) request.getAttribute("applications");
    Map<Application, SkillMatchService.MatchResult> matchMap = (Map<Application, SkillMatchService.MatchResult>) request.getAttribute("matchMap");
    Map<String, User> studentMap = (Map<String, User>) request.getAttribute("studentMap");
    if (job == null) job = new bupt.is.ta.model.Job();
    if (applications == null) applications = List.of();
    if (matchMap == null) matchMap = Map.of();
    if (studentMap == null) studentMap = Map.of();
    Integer maxCoursesPerTA = (Integer) request.getAttribute("maxCoursesPerTA");
    if (maxCoursesPerTA == null) maxCoursesPerTA = 2;
    Map<String, Long> acceptedCountByStudent = (Map<String, Long>) request.getAttribute("acceptedCountByStudent");
    if (acceptedCountByStudent == null) acceptedCountByStudent = Map.of();
    Map<Application, ScheduleFitService.ScheduleFitResult> scheduleFitMap = (Map<Application, ScheduleFitService.ScheduleFitResult>) request.getAttribute("scheduleFitMap");
    if (scheduleFitMap == null) scheduleFitMap = Map.of();
    User current = (User) session.getAttribute("currentUser");
    String applicantsHint = (String) request.getAttribute("applicantsHint");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Applicants - <%= h(job.getCourseName()) %></title>
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
    <a href="<%= request.getContextPath() %>/mo/dashboard" class="back-link">Back to Job List</a>
    <div class="page-head">
        <div>
            <h2 class="page-title">Applicants</h2>
            <p class="page-subtitle"><%= h(job.getCourseName()) %> - ranked by fit score</p>
        </div>
        <span class="status-tag <%= job.isOpen() ? "status-open" : "status-closed" %>"><%= job.isOpen() ? "Open" : "Closed" %></span>
    </div>
    <% if (applicantsHint != null && !applicantsHint.isBlank()) { %>
    <div class="alert alert-info"><%= h(applicantsHint) %></div>
    <% } %>
    <% if (job.getDescription() != null && !job.getDescription().isBlank()) { %>
    <div class="section job-context">
        <span class="kv-label">Job description</span>
        <p><%= h(job.getDescription()) %></p>
    </div>
    <% } %>
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
                    <th>Student ID</th><th>Name</th><th>Available Time</th><th>Workload</th><th>Fit</th><th>Note</th><th>CV</th><th>Status</th><th>Action</th>
                </tr>
            </thead>
            <tbody>
                <% for (Application app : applications) {
                    User stu = studentMap.get(app.getStudentId());
                    SkillMatchService.MatchResult match = matchMap.get(app);
                    int pct = match != null ? (int) Math.round(match.getAiScore()) : 0;
                    String badgeClass = pct >= 80 ? "badge-high" : (pct < 50 ? "badge-low" : "badge-mid");
                    String badge = pct >= 80 ? "High Match" : (pct < 50 ? "Low Match" : "Match");
                    long acceptedCount = acceptedCountByStudent.getOrDefault(app.getStudentId(), 0L);
                    boolean atWorkloadLimit = acceptedCount >= maxCoursesPerTA;
                    String note = app.getNote() == null ? "" : app.getNote();
                    String noteShort = note.length() > 40 ? note.substring(0, 40) + "..." : note;
                %>
                <tr data-status="<%= app.getStatus() %>"
                    data-keyword="<%= h(((stu != null ? stu.getName() : "") + " " + app.getStudentId()).toLowerCase()) %>">
                    <td class="cell-nowrap"><%= h(app.getStudentId()) %></td>
                    <td class="cell-nowrap">
                        <% if (stu != null) {
                            request.setAttribute("avatarUser", stu);
                        %>
                        <span class="name-with-avatar">
                            <jsp:include page="/WEB-INF/jsp/avatar.jsp"><jsp:param name="size" value="32"/></jsp:include>
                            <%= h(stu.getName()) %>
                        </span>
                        <% } else { %>-<% } %>
                    </td>
                    <td class="cell-nowrap"><%= h(stu != null ? JobScheduleUtil.displayAvailability(stu) : "-") %></td>
                    <td class="cell-nowrap">
                        <span class="<%= atWorkloadLimit ? "badge-low" : "badge-mid" %>"><%= acceptedCount %>/<%= maxCoursesPerTA %> courses</span>
                        <% if (atWorkloadLimit) { %><span class="muted">At limit</span><% } %>
                    </td>
                    <td>
                        <div class="score-stack">
                            <span class="<%= badgeClass %>"><%= pct %>% (<%= badge %>)</span>
                            <span class="mini-meter"><span style="width:<%= pct %>%"></span></span>
                            <% ScheduleFitService.ScheduleFitResult sf = scheduleFitMap.get(app);
                               if (sf != null && sf.isCalculable()) { %>
                            <span class="muted">Schedule fit: <%= sf.getScheduleScore() %>%</span>
                            <% } else { %>
                            <span class="muted">Schedule fit: N/A</span>
                            <% } %>
                        </div>
                    </td>
                    <td title="<%= h(note) %>"><%= note.isBlank() ? "-" : h(noteShort) %></td>
                    <td>
                        <% if (stu != null && stu.getCvPath() != null && !stu.getCvPath().isBlank()) { %>
                        <a class="btn btn-small" href="<%= request.getContextPath() %>/mo/cv/view?studentId=<%= h(app.getStudentId()) %>&jobId=<%= h(job.getId()) %>">View Web CV</a>
                        <a class="btn btn-small btn-secondary" href="<%= request.getContextPath() %>/mo/cv/download?studentId=<%= h(app.getStudentId()) %>&jobId=<%= h(job.getId()) %>">Download CV</a>
                        <% } else { %>
                        -
                        <% } %>
                    </td>
                    <td><span class="status-tag status-<%= app.getStatus().name().toLowerCase() %>"><%= app.getStatus() %></span></td>
                    <td class="action-cell">
                        <% if (app.getInterviewResponse() == Application.InterviewResponse.DECLINED) { %>
                        <p class="alert alert-warning" style="margin:6px 0;padding:6px 10px;font-size:12px;">学生已拒绝面试邀约</p>
                        <% } else if (app.getStatus() == Application.Status.INTERVIEWING) {
                            String inv = JobScheduleUtil.formatInterviewSlot(app.getInterviewSlot());
                            if (inv.isBlank()) inv = "Time TBD";
                        %>
                        <p class="muted"><strong>Interview:</strong> <%= h(inv) %></p>
                        <% if (!app.getInterviewLocation().isBlank()) { %>
                        <p class="muted"><strong>Location:</strong> <%= h(app.getInterviewLocation()) %></p>
                        <% } %>
                        <% if (app.isInterviewRequiresWrittenTest()) { %><p class="muted">Written test required</p><% } %>
                        <% if (!app.getInterviewScope().isBlank()) { %>
                        <p class="muted" title="<%= h(app.getInterviewScope()) %>"><strong>Scope:</strong> <%= h(app.getInterviewScope().length() > 50 ? app.getInterviewScope().substring(0, 50) + "..." : app.getInterviewScope()) %></p>
                        <% } %>
                        <% if (!app.getInterviewMessage().isBlank()) { %>
                        <p class="muted" title="<%= h(app.getInterviewMessage()) %>"><%= h(app.getInterviewMessage().length() > 60 ? app.getInterviewMessage().substring(0, 60) + "..." : app.getInterviewMessage()) %></p>
                        <% } %>
                        <% if (app.getInterviewResponse() == Application.InterviewResponse.ACCEPTED) { %>
                        <p class="muted">Student accepted the invite.</p>
                        <% } else if (app.getInterviewResponse() == Application.InterviewResponse.PENDING) { %>
                        <p class="muted">Awaiting student response.</p>
                        <% } %>
                        <% } %>
                        <% if (app.getStatus() != Application.Status.ACCEPTED && app.getStatus() != Application.Status.REJECTED) { %>
                        <form method="post" action="<%= request.getContextPath() %>/mo/updateStatus" style="display:inline">
                            <input type="hidden" name="applicationId" value="<%= h(app.getId()) %>"/>
                            <input type="hidden" name="status" value="ACCEPTED"/>
                            <button type="submit" class="btn btn-small btn-success" <%= atWorkloadLimit ? "disabled title=\"Student has reached max courses\"" : "" %>>Accept</button>
                        </form>
                        <form method="post" action="<%= request.getContextPath() %>/mo/updateStatus" style="display:inline">
                            <input type="hidden" name="applicationId" value="<%= h(app.getId()) %>"/>
                            <input type="hidden" name="status" value="REJECTED"/>
                            <button type="submit" class="btn btn-small btn-danger">Reject</button>
                        </form>
                        <button type="button" class="btn btn-small btn-secondary toggle-interview-btn" data-target="interview-panel-<%= h(app.getId()) %>">
                            <%= app.getStatus() == Application.Status.INTERVIEWING ? "Edit interview" : "Schedule interview" %>
                        </button>
                        <div id="interview-panel-<%= h(app.getId()) %>" class="interview-panel mo-interview-form" hidden>
                            <form method="post" action="<%= request.getContextPath() %>/mo/updateStatus">
                                <input type="hidden" name="applicationId" value="<%= h(app.getId()) %>"/>
                                <input type="hidden" name="status" value="INTERVIEWING"/>
                                <div class="schedule-slot-row">
                                    <select name="interviewDay">
                                        <% for (int d = 1; d <= 7; d++) {
                                            String[] labels = {"", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
                                            int selDay = app.getInterviewSlot() != null ? app.getInterviewSlot().getDayOfWeek() : 3;
                                        %>
                                        <option value="<%= d %>" <%= d == selDay ? "selected" : "" %>><%= labels[d] %></option>
                                        <% } %>
                                    </select>
                                    <input type="time" name="interviewStart" min="08:00" max="23:00" value="<%= app.getInterviewSlot() != null ? h(app.getInterviewSlot().getStartTime()) : "14:00" %>">
                                    <span class="schedule-slot-sep">to</span>
                                    <input type="time" name="interviewEnd" min="08:00" max="23:00" value="<%= app.getInterviewSlot() != null ? h(app.getInterviewSlot().getEndTime()) : "15:00" %>">
                                </div>
                                <div class="form-group">
                                    <label>Location / online link</label>
                                    <input type="text" name="interviewLocation" class="input-area" value="<%= h(app.getInterviewLocation()) %>" placeholder="Room 3-201 or Zoom link">
                                </div>
                                <div class="form-group">
                                    <label><input type="checkbox" name="interviewWrittenTest" <%= app.isInterviewRequiresWrittenTest() ? "checked" : "" %>> Written test required</label>
                                </div>
                                <div class="form-group">
                                    <label>Scope / topics</label>
                                    <textarea name="interviewScope" rows="2" class="input-area" placeholder="Topics to be assessed"><%= h(app.getInterviewScope()) %></textarea>
                                </div>
                                <div class="form-group">
                                    <label>Additional notes</label>
                                    <textarea name="interviewMessage" rows="2" class="input-area" placeholder="Preparation or other notes"><%= h(app.getInterviewMessage()) %></textarea>
                                </div>
                                <button type="submit" class="btn btn-small">Save interview invite</button>
                            </form>
                        </div>
                        <% } else if (app.getStatus() != Application.Status.INTERVIEWING) { %>
                        <%= app.getStatus() %>
                        <% } %>
                    </td>
                </tr>
                <% } %>
                <% if (applications.isEmpty()) { %>
                <tr><td colspan="9" class="empty-hint">No applicants yet.</td></tr>
                <% } %>
            </tbody>
        </table>
    </div>
</main>
<script src="<%= request.getContextPath() %>/js/schedule-time.js"></script>
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
        document.querySelectorAll('.toggle-interview-btn').forEach(function (btn) {
            btn.addEventListener('click', function () {
                var panel = document.getElementById(btn.getAttribute('data-target'));
                if (panel) panel.hidden = !panel.hidden;
            });
        });
    })();
</script>
</body>
</html>
