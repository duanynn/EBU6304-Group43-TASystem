<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="bupt.is.ta.model.User" %>
<%@ page import="bupt.is.ta.service.ScheduleViewService" %>
<%@ page import="bupt.is.ta.model.Job" %>
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

    private String jobTypeLabel(Job.JobType type) {
        if (type == null) return "Module TA";
        return switch (type) {
            case MODULE_TA -> "Module TA";
            case INVIGILATION -> "Invigilation";
            case OTHER -> "Other";
        };
    }
%>
<%
    User current = (User) session.getAttribute("currentUser");
    ScheduleViewService.TimetableView view = (ScheduleViewService.TimetableView) request.getAttribute("timetableView");
    if (view == null) {
        view = new ScheduleViewService.TimetableView(List.of(), List.of(),
                ScheduleViewService.GRID_START_MINUTE, ScheduleViewService.GRID_END_MINUTE);
    }
    List<String> timeLabels = view.getTimeLabels();
    int gridHeightPx = view.getGridHeightPx();
    int gridStart = view.getGridStartMinute();
    int gridEnd = view.getGridEndMinute();
    String[] dayNames = {"", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
    Long interviewPendingCount = (Long) request.getAttribute("interviewPendingCount");
    if (interviewPendingCount == null) interviewPendingCount = 0L;
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>My Weekly Schedule - Student Portal</title>
    <link rel="stylesheet" href="<%= ctx %>/css/style.css?v=20260520-schedule3">
</head>
<body class="layout-wide">
<header class="app-header app-header-with-avatar">
    <h1>TA Recruitment System - Student Portal</h1>
    <span class="user-info user-info-with-avatar">
        <jsp:include page="/WEB-INF/jsp/avatar.jsp"><jsp:param name="size" value="36"/></jsp:include>
        <%= current != null ? h(current.getName()) : "" %>
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
    <div class="page-head">
        <div>
            <h2 class="page-title">My Weekly Schedule</h2>
            <p class="page-subtitle">Fixed view 08:00–23:00. Your availability, open positions, applications, and interview invites.</p>
        </div>
        <a class="btn btn-secondary" href="<%= ctx %>/ta/profile">Edit availability</a>
    </div>

    <div class="schedule-legend section">
        <span class="legend-item slot-availability">My availability</span>
        <span class="legend-item slot-open">Open position</span>
        <span class="legend-item slot-pending">Pending</span>
        <span class="legend-item slot-interviewing">Interview</span>
        <span class="legend-item slot-accepted">Accepted</span>
        <span class="legend-item slot-conflict">Time conflict</span>
    </div>

    <div class="weekly-timetable-wrap section table-section">
        <div class="weekly-timetable-v2">
            <div class="timetable-head-row">
                <div class="timetable-corner"></div>
                <% for (int d = 1; d <= 7; d++) { %>
                <div class="timetable-day-head"><%= dayNames[d] %></div>
                <% } %>
            </div>
            <div class="timetable-body-row">
                <div class="timetable-time-axis" style="height: <%= gridHeightPx %>px;">
                    <% for (int i = 0; i < timeLabels.size(); i++) {
                        boolean showHour = view.isHourLabel(i);
                    %>
                    <div class="timetable-time-slot" style="height: 32px;">
                        <% if (showHour) { %><span><%= h(timeLabels.get(i)) %></span><% } %>
                    </div>
                    <% } %>
                </div>
                <% for (int d = 1; d <= 7; d++) { %>
                <div class="timetable-day-col" style="height: <%= gridHeightPx %>px;">
                    <% for (ScheduleViewService.TimetableBlock block : view.getBlocks()) {
                        if (block.getDayOfWeek() != d) continue;
                        String css = block.getLayerCssClass();
                        if (block.isConflict()) css += " slot-conflict";
                        if (block.isInterviewInvite()) css += " slot-interview-invite";
                        int laneCount = block.getLaneCount();
                        int laneIndex = block.getLaneIndex();
                        double widthPct = 100.0 / laneCount;
                        double leftPct = laneIndex * widthPct;
                        double topPct = block.getTopPercent(gridStart, gridEnd);
                        double heightPct = block.getHeightPercent(gridStart, gridEnd);
                        String posStyle = "top:" + topPct + "%;height:" + heightPct + "%;width:calc(" + widthPct + "% - 4px);left:calc(" + leftPct + "% + 2px);";
                        String title = block.getCourseName() + " (" + jobTypeLabel(block.getJobType()) + ") " + block.formatTimeRange();
                    %>
                    <div class="timetable-slot <%= css %>" style="<%= posStyle %>" title="<%= h(title) %>">
                        <% if (block.isConflict()) { %><span class="slot-conflict-badge">!</span><% } %>
                        <% if (block.isInterviewInvite()) { %><span class="slot-interview-badge">Interview</span><% } %>
                        <strong><%= h(block.getCourseName()) %></strong>
                        <span class="slot-time"><%= h(block.formatTimeRange()) %></span>
                        <span class="slot-meta"><%= h(jobTypeLabel(block.getJobType())) %></span>
                    </div>
                    <% } %>
                </div>
                <% } %>
            </div>
        </div>
    </div>

    <% if (!view.getUnstructuredJobs().isEmpty()) { %>
    <section class="section">
        <h3>Positions without structured weekly slots</h3>
        <p class="muted">These jobs only have a text description for working time.</p>
        <ul class="simple-list">
            <% for (String name : view.getUnstructuredJobs()) { %>
            <li><%= h(name) %></li>
            <% } %>
        </ul>
    </section>
    <% } %>
</main>
</body>
</html>
