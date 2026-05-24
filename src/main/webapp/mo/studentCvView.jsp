<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="bupt.is.ta.model.User" %>
<%@ page import="bupt.is.ta.model.Job" %>
<%@ page import="bupt.is.ta.service.SkillMatchService.MatchResult" %>
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
    User current = (User) session.getAttribute("currentUser");
    User student = (User) request.getAttribute("student");
    Job job = (Job) request.getAttribute("job");
    MatchResult match = (MatchResult) request.getAttribute("match");
    Application jobApplication = (Application) request.getAttribute("application");
    if (student == null || job == null || match == null) {
        response.sendRedirect(request.getContextPath() + "/mo/dashboard");
        return;
    }
    int fitScore = (int) Math.round(match.getAiScore());
    String fitClass = fitScore >= 75 ? "fit-high" : (fitScore >= 45 ? "fit-mid" : "fit-low");
    List<String> studentSkills = student.getSkillTags() == null ? List.of() : student.getSkillTags();
    List<String> requiredSkills = job.getRequiredSkills() == null ? List.of() : job.getRequiredSkills();
    List<String> strengths = match.getAiStrengths() == null ? List.of() : match.getAiStrengths();
    List<String> gaps = match.getAiGaps() == null ? List.of() : match.getAiGaps();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Candidate CV - <%= h(student.getName() == null ? student.getId() : student.getName()) %></title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css?v=20260518-ui2">
</head>
<body>
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
    <a href="<%= request.getContextPath() %>/mo/applicants?jobId=<%= h(job.getId()) %>" class="back-link">Back to Applicant List</a>
    <div class="page-head">
        <div>
            <h2 class="page-title">Candidate Web CV</h2>
            <p class="page-subtitle"><%= h(student.getName() == null ? student.getId() : student.getName()) %> for <%= h(job.getCourseName()) %></p>
        </div>
        <span class="fit-pill <%= fitClass %>">Fit <%= fitScore %>%</span>
    </div>

    <div class="cv-view-layout">
        <section class="section cv-profile-card">
            <h3>Candidate Information</h3>
            <div class="kv-grid">
                <div><span class="kv-label">Name</span><span class="kv-value"><%= h(student.getName() == null ? "-" : student.getName()) %></span></div>
                <div><span class="kv-label">Student ID</span><span class="kv-value"><%= h(student.getId()) %></span></div>
                <div><span class="kv-label">GPA</span><span class="kv-value"><%= h(student.getGpa() == null ? "-" : student.getGpa()) %></span></div>
                <div><span class="kv-label">Available Time</span><span class="kv-value"><%= h(student.getAvailableTime() == null || student.getAvailableTime().isBlank() ? "-" : student.getAvailableTime()) %></span></div>
            </div>
            <div class="field-block">
                <div class="kv-label">Skill Tags</div>
                <div class="chip-wrap">
                    <% if (!studentSkills.isEmpty()) {
                        for (String sk : studentSkills) { %>
                    <span class="chip chip-neutral"><%= h(sk) %></span>
                    <% }} else { %>
                    <span class="muted">N/A</span>
                    <% } %>
                </div>
            </div>
        </section>

        <section class="section cv-match-card">
            <h3>Position Fit</h3>
            <div class="match-summary-grid two-col">
                <div class="score-card">
                    <span class="score-label">Overall Fit</span>
                    <strong><%= fitScore %>%</strong>
                    <span class="score-meter"><span class="<%= fitClass %>" style="width:<%= fitScore %>%"></span></span>
                    <span class="muted"><%= match.isAiGenerated() ? "Real-time AI" : "Cached/local result" %></span>
                </div>
                <div class="score-card">
                    <span class="score-label">Rule Match</span>
                    <strong><%= Math.round(match.getScore() * 100) %>%</strong>
                    <span class="score-meter"><span style="width:<%= Math.round(match.getScore() * 100) %>%"></span></span>
                    <span class="muted">Required skill overlap</span>
                </div>
            </div>
            <div class="field-block">
                <div class="kv-label">Required Skills</div>
                <div class="chip-wrap compact">
                    <% if (!requiredSkills.isEmpty()) {
                        for (String skill : requiredSkills) { %>
                    <span class="chip"><%= h(skill) %></span>
                    <% }} else { %>
                    <span class="muted">N/A</span>
                    <% } %>
                </div>
            </div>
            <p><strong>Working Time:</strong> <%= h(job.getRequiredWorkTime() == null || job.getRequiredWorkTime().isBlank() ? "-" : job.getRequiredWorkTime()) %></p>
            <p><strong>Fit Summary:</strong> <%= h(match.getAiFitSummary() == null || match.getAiFitSummary().isBlank() ? "N/A" : match.getAiFitSummary()) %></p>
        </section>
    </div>

    <section class="section">
        <h3>Evaluation Detail</h3>
        <div class="match-detail-grid compact-grid">
            <div>
                <h3>Strengths</h3>
                <ul class="simple-list">
                    <% if (!strengths.isEmpty()) {
                        for (String strength : strengths) { %>
                    <li><%= h(strength) %></li>
                    <% }} else { %><li class="muted">N/A</li><% } %>
                </ul>
            </div>
            <div>
                <h3>Gaps</h3>
                <ul class="simple-list">
                    <% if (!gaps.isEmpty()) {
                        for (String gap : gaps) { %>
                    <li><%= h(gap) %></li>
                    <% }} else { %><li class="muted">N/A</li><% } %>
                </ul>
            </div>
        </div>
        <p><strong>Advice:</strong> <%= h(match.getAiAdvice() == null || match.getAiAdvice().isBlank() ? "N/A" : match.getAiAdvice()) %></p>
    </section>

    <% if (jobApplication != null && jobApplication.getNote() != null && !jobApplication.getNote().isBlank()) { %>
    <section class="section">
        <h3>Application Note</h3>
        <p class="field-block"><%= h(jobApplication.getNote()) %></p>
    </section>
    <% } %>

    <section class="section cv-resume-card">
        <h3>CV Content</h3>
        <p><strong>Summary:</strong> <%= h(student.getProfile().getSummary() == null || student.getProfile().getSummary().isBlank() ? "N/A" : student.getProfile().getSummary()) %></p>
        <p><strong>Education:</strong> <%= h(student.getProfile().getEducation() == null || student.getProfile().getEducation().isBlank() ? "N/A" : student.getProfile().getEducation()) %></p>
        <p><strong>Projects:</strong> <%= h(student.getProfile().getProjects() == null || student.getProfile().getProjects().isBlank() ? "N/A" : student.getProfile().getProjects()) %></p>
        <p><strong>Awards:</strong> <%= h(student.getProfile().getCertificates() == null || student.getProfile().getCertificates().isBlank() ? "N/A" : student.getProfile().getCertificates()) %></p>
        <details class="details-block">
            <summary>View Raw CV Text</summary>
            <pre class="raw-cv"><%= h(student.getProfile().getRawCvText() == null || student.getProfile().getRawCvText().isBlank() ? "N/A" : student.getProfile().getRawCvText()) %></pre>
        </details>
    </section>
</main>
</body>
</html>
