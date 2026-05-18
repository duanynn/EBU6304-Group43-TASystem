<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="bupt.is.ta.model.Job" %>
<%@ page import="bupt.is.ta.service.SkillMatchService.MatchResult" %>
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
    MatchResult match = (MatchResult) request.getAttribute("match");
    if (job == null || match == null) { response.sendRedirect(request.getContextPath() + "/ta/jobs"); return; }
    int ruleScore = (int) Math.round(match.getScore() * 100);
    int aiScore = (int) Math.round(match.getAiScore());
    String aiClass = aiScore >= 80 ? "fit-high" : (aiScore >= 55 ? "fit-mid" : "fit-low");
    List<String> required = match.getRequiredSkills() == null ? List.of() : match.getRequiredSkills();
    List<String> studentSkills = match.getStudentSkills() == null ? List.of() : match.getStudentSkills();
    List<String> matched = match.getMatchedSkills() == null ? List.of() : match.getMatchedSkills();
    List<String> missing = match.getMissingSkills() == null ? List.of() : match.getMissingSkills();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Confirm Application - <%= h(job.getCourseName()) %></title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<header class="app-header">
    <h1>TA Recruitment System - Student Portal</h1>
</header>
<nav class="app-nav">
    <a href="<%= request.getContextPath() %>/ta/jobs">Job Board</a>
    <a href="<%= request.getContextPath() %>/ta/applications">My Applications</a>
    <a href="<%= request.getContextPath() %>/ta/profile">My Profile</a>
</nav>
<main class="app-main">
    <a href="<%= request.getContextPath() %>/ta/jobs" class="back-link">Back to Job Board</a>
    <div class="page-head">
        <div>
            <h2 class="page-title">Confirm Application</h2>
            <p class="page-subtitle"><%= h(job.getCourseName()) %> - <%= h(job.getRequiredWorkTime() == null || job.getRequiredWorkTime().isBlank() ? "Time not specified" : job.getRequiredWorkTime()) %></p>
        </div>
        <span class="fit-pill <%= aiClass %>">Fit <%= aiScore %>%</span>
    </div>

    <section class="section match-panel">
        <div class="match-summary-grid">
            <div class="score-card">
                <span class="score-label">AI Overall</span>
                <strong><%= aiScore %>%</strong>
                <span class="score-meter"><span class="<%= aiClass %>" style="width:<%= aiScore %>%"></span></span>
                <span class="muted"><%= match.isAiGenerated() ? "Real-time AI" : "Local fallback" %></span>
            </div>
            <div class="score-card">
                <span class="score-label">Rule Match</span>
                <strong><%= ruleScore %>%</strong>
                <span class="score-meter"><span class="<%= ruleScore >= 80 ? "fit-high" : (ruleScore >= 55 ? "fit-mid" : "fit-low") %>" style="width:<%= ruleScore %>%"></span></span>
                <span class="muted">Skill overlap</span>
            </div>
            <div class="score-card">
                <span class="score-label">Openings</span>
                <strong><%= job.getRequiredCount() %></strong>
                <span class="muted">Required TA count</span>
            </div>
        </div>

        <div class="match-detail-grid">
            <div>
                <h3>Required Skills</h3>
                <div class="chip-wrap">
                    <% if (!required.isEmpty()) { for (String skill : required) { %>
                    <span class="chip"><%= h(skill) %></span>
                    <% }} else { %><span class="muted">N/A</span><% } %>
                </div>
            </div>
            <div>
                <h3>Your Skills</h3>
                <div class="chip-wrap">
                    <% if (!studentSkills.isEmpty()) { for (String skill : studentSkills) { %>
                    <span class="chip chip-neutral"><%= h(skill) %></span>
                    <% }} else { %><span class="muted">N/A</span><% } %>
                </div>
            </div>
            <div>
                <h3>Matched</h3>
                <div class="chip-wrap">
                    <% if (!matched.isEmpty()) { for (String skill : matched) { %>
                    <span class="chip chip-success"><%= h(skill) %></span>
                    <% }} else { %><span class="muted">No direct skill match</span><% } %>
                </div>
            </div>
            <div>
                <h3>Gaps</h3>
                <div class="chip-wrap">
                    <% if (!missing.isEmpty()) { for (String skill : missing) { %>
                    <span class="chip chip-gap"><%= h(skill) %></span>
                    <% }} else { %><span class="muted">No required skill gaps</span><% } %>
                </div>
            </div>
        </div>

        <div class="advice-block">
            <h3>Fit Summary</h3>
            <p><%= h(match.getAiFitSummary() == null || match.getAiFitSummary().isBlank() ? "N/A" : match.getAiFitSummary()) %></p>
            <% if (match.getAiAdvice() != null && !match.getAiAdvice().isBlank()) { %>
            <h3>Improvement Advice</h3>
            <p><%= h(match.getAiAdvice()) %></p>
            <% } %>
            <div class="match-detail-grid compact-grid">
                <div>
                    <h3>Strengths</h3>
                    <ul class="simple-list">
                        <% if (match.getAiStrengths() != null && !match.getAiStrengths().isEmpty()) {
                            for (String strength : match.getAiStrengths()) { %>
                        <li><%= h(strength) %></li>
                        <% }} else { %><li class="muted">N/A</li><% } %>
                    </ul>
                </div>
                <div>
                    <h3>AI Gaps</h3>
                    <ul class="simple-list">
                        <% if (match.getAiGaps() != null && !match.getAiGaps().isEmpty()) {
                            for (String gap : match.getAiGaps()) { %>
                        <li><%= h(gap) %></li>
                        <% }} else { %><li class="muted">N/A</li><% } %>
                    </ul>
                </div>
            </div>
        </div>

        <% if (match.getScore() < 0.5) { %>
        <p class="alert alert-warning">Low match score: consider improving related skills before applying, or explain your learning plan during interview.</p>
        <% } %>
        <form method="post" action="<%= request.getContextPath() %>/ta/confirmApply" class="form-actions">
            <input type="hidden" name="jobId" value="<%= h(job.getId()) %>"/>
            <button type="submit" class="btn btn-success">Confirm Application</button>
            <a href="<%= request.getContextPath() %>/ta/jobs" class="btn btn-secondary">Back</a>
        </form>
    </section>
</main>
</body>
</html>
