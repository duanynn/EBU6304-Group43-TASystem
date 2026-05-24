<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="bupt.is.ta.model.Application" %>
<%@ page import="bupt.is.ta.model.Job" %>
<%@ page import="bupt.is.ta.model.User" %>
<%@ page import="bupt.is.ta.store.DataStore" %>
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
    List<Application> applications = (List<Application>) request.getAttribute("applications");
    if (applications == null) applications = List.of();
    User current = (User) session.getAttribute("currentUser");
    List<Job> allJobs = DataStore.getInstance().getJobs();
    java.util.Map<String, Job> jobMap = allJobs.stream().collect(java.util.stream.Collectors.toMap(Job::getId, j -> j, (a,b)->a));
    Long interviewPendingCount = (Long) request.getAttribute("interviewPendingCount");
    if (interviewPendingCount == null) interviewPendingCount = 0L;
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>My Applications - TA Recruitment System</title>
    <link rel="stylesheet" href="<%= ctx %>/css/style.css?v=20260520-schedule3">
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
    <a href="<%= ctx %>/ta/jobs">Job Board</a>
    <a href="<%= ctx %>/ta/schedule">My Schedule<% if (interviewPendingCount > 0) { %> <span class="nav-badge"><%= interviewPendingCount %></span><% } %></a>
    <a href="<%= ctx %>/ta/applications">My Applications</a>
    <a href="<%= ctx %>/ta/profile">My Profile</a>
</nav>
<main class="app-main">
    <h2 class="page-title">My Applications</h2>
    <% if (interviewPendingCount > 0) { %>
    <div class="alert alert-warning">You have <%= interviewPendingCount %> interview invitation(s) awaiting your response. Please accept or decline below.</div>
    <% } %>
    <div class="section">
        <table class="data-table">
            <thead>
                <tr><th>Course/Position</th><th>Type</th><th>Status</th><th>Applied At</th></tr>
            </thead>
            <tbody>
                <% for (Application app : applications) {
                    Job j = jobMap.get(app.getJobId());
                %>
                <tr>
                    <td>
                        <strong><%= j != null ? h(j.getCourseName()) : h(app.getJobId()) %></strong>
                        <% if (j != null) { %>
                        <div class="muted"><%= h(JobScheduleUtil.displayWorkTime(j)) %></div>
                        <% } %>
                        <% if (app.getStatus() == Application.Status.INTERVIEWING) { %>
                        <div class="interview-card">
                            <span class="slot-interview-badge" style="position:static;display:inline-block;margin-bottom:6px;">Interview invite</span>
                            <% String inv = JobScheduleUtil.formatInterviewSlot(app.getInterviewSlot());
                               if (inv.isBlank()) inv = "Time to be confirmed";
                            %>
                            <p><strong>When:</strong> <%= h(inv) %></p>
                            <% if (!app.getInterviewLocation().isBlank()) { %>
                            <p><strong>Location:</strong> <%= h(app.getInterviewLocation()) %></p>
                            <% } %>
                            <% if (app.isInterviewRequiresWrittenTest()) { %>
                            <p><strong>Written test:</strong> Required</p>
                            <% } %>
                            <% if (!app.getInterviewScope().isBlank()) { %>
                            <p><strong>Scope:</strong> <%= h(app.getInterviewScope()) %></p>
                            <% } %>
                            <% if (!app.getInterviewMessage().isBlank()) { %>
                            <p><strong>Notes:</strong> <%= h(app.getInterviewMessage()) %></p>
                            <% } %>
                            <% if (app.needsInterviewResponse()) { %>
                            <div class="interview-response-actions">
                                <form method="post" action="<%= ctx %>/ta/interviewResponse" style="display:inline">
                                    <input type="hidden" name="applicationId" value="<%= h(app.getId()) %>"/>
                                    <input type="hidden" name="action" value="accept"/>
                                    <button type="submit" class="btn btn-small btn-success">Accept</button>
                                </form>
                                <form method="post" action="<%= ctx %>/ta/interviewResponse" style="display:inline" onsubmit="return confirm('Decline this interview? Your application will be withdrawn.');">
                                    <input type="hidden" name="applicationId" value="<%= h(app.getId()) %>"/>
                                    <input type="hidden" name="action" value="decline"/>
                                    <button type="submit" class="btn btn-small btn-danger">Decline</button>
                                </form>
                            </div>
                            <% } else if (app.getInterviewResponse() == Application.InterviewResponse.ACCEPTED) { %>
                            <p class="muted"><strong>You accepted</strong> this interview invitation.</p>
                            <% } %>
                            <a href="<%= ctx %>/ta/schedule">View on weekly schedule</a>
                        </div>
                        <% } %>
                    </td>
                    <td class="cell-nowrap"><%= j != null ? h(JobDisplayUtil.jobTypeLabel(j)) : "-" %></td>
                    <td class="cell-nowrap"><span class="status-tag status-<%= app.getStatus().name().toLowerCase() %>"><%= app.getStatus() %></span></td>
                    <td class="cell-nowrap"><%= app.getAppliedAt() != null ? app.getAppliedAt().toString() : "-" %></td>
                </tr>
                <% } %>
                <% if (applications.isEmpty()) { %>
                <tr><td colspan="4" class="empty-hint">No application records yet.</td></tr>
                <% } %>
            </tbody>
        </table>
    </div>
</main>
</body>
</html>
