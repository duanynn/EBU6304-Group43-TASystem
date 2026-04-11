<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="bupt.is.ta.model.User" %>
<%@ page import="bupt.is.ta.model.Job" %>
<%@ page import="bupt.is.ta.service.SkillMatchService.MatchResult" %>
<%
    User current = (User) session.getAttribute("currentUser");
    User student = (User) request.getAttribute("student");
    Job job = (Job) request.getAttribute("job");
    MatchResult match = (MatchResult) request.getAttribute("match");
    if (student == null || job == null || match == null) {
        response.sendRedirect(request.getContextPath() + "/mo/dashboard");
        return;
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Candidate CV - <%= student.getName() == null ? student.getId() : student.getName() %></title>
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
    <a href="<%= request.getContextPath() %>/mo/applicants?jobId=<%= job.getId() %>" class="back-link">← Back to Applicant List</a>
    <h2 class="page-title">Candidate Web CV</h2>
    <div class="cv-view-layout">
        <section class="section cv-profile-card">
            <h3>Candidate Information</h3>
            <div class="kv-grid">
                <div><span class="kv-label">Name</span><span class="kv-value"><%= student.getName() == null ? "-" : student.getName() %></span></div>
                <div><span class="kv-label">Student ID</span><span class="kv-value"><%= student.getId() %></span></div>
                <div><span class="kv-label">GPA</span><span class="kv-value"><%= student.getGpa() == null ? "-" : student.getGpa() %></span></div>
                <div><span class="kv-label">Available Time</span><span class="kv-value"><%= student.getAvailableTime() == null || student.getAvailableTime().isBlank() ? "-" : student.getAvailableTime() %></span></div>
            </div>
            <div style="margin-top:10px;">
                <div class="kv-label" style="margin-bottom:6px;">Skill Tags</div>
                <div class="chip-wrap">
                    <% if (student.getSkillTags() != null && !student.getSkillTags().isEmpty()) {
                        for (String sk : student.getSkillTags()) { %>
                    <span class="chip"><%= sk %></span>
                    <% }} else { %>
                    <span class="muted">N/A</span>
                    <% } %>
                </div>
            </div>
        </section>

        <section class="section cv-match-card">
            <h3>Position Fit Info (Cached)</h3>
            <div class="cv-match-top">
                <div>
                    <div class="kv-label">Position</div>
                    <div class="kv-value"><%= job.getCourseName() %></div>
                </div>
                <div>
                    <span class="fit-pill <%= match.getAiScore() >= 75 ? "fit-high" : (match.getAiScore() >= 45 ? "fit-mid" : "fit-low") %>">
                        Fit <%= Math.round(match.getAiScore()) %>%
                    </span>
                </div>
            </div>
            <p><strong>Required Skills:</strong> <%= job.getRequiredSkills() == null || job.getRequiredSkills().isEmpty() ? "-" : String.join(" / ", job.getRequiredSkills()) %></p>
            <p><strong>Working Time:</strong> <%= job.getRequiredWorkTime() == null || job.getRequiredWorkTime().isBlank() ? "-" : job.getRequiredWorkTime() %></p>
            <p><strong>Fit Summary:</strong> <%= match.getAiFitSummary() == null || match.getAiFitSummary().isBlank() ? "N/A" : match.getAiFitSummary() %></p>
            <p><strong>Strengths:</strong> <%= match.getAiStrengths() == null || match.getAiStrengths().isEmpty() ? "N/A" : String.join("; ", match.getAiStrengths()) %></p>
            <p><strong>Gaps:</strong> <%= match.getAiGaps() == null || match.getAiGaps().isEmpty() ? "N/A" : String.join("; ", match.getAiGaps()) %></p>
            <p><strong>Advice:</strong> <%= match.getAiAdvice() == null || match.getAiAdvice().isBlank() ? "N/A" : match.getAiAdvice() %></p>
        </section>
    </div>

    <section class="section cv-resume-card">
        <h3>CV Content (Text Parsing)</h3>
        <p><strong>Summary:</strong> <%= student.getProfile().getSummary() == null || student.getProfile().getSummary().isBlank() ? "N/A" : student.getProfile().getSummary() %></p>
        <p><strong>Education:</strong> <%= student.getProfile().getEducation() == null || student.getProfile().getEducation().isBlank() ? "N/A" : student.getProfile().getEducation() %></p>
        <p><strong>Projects:</strong> <%= student.getProfile().getProjects() == null || student.getProfile().getProjects().isBlank() ? "N/A" : student.getProfile().getProjects() %></p>
        <p><strong>Awards:</strong> <%= student.getProfile().getCertificates() == null || student.getProfile().getCertificates().isBlank() ? "N/A" : student.getProfile().getCertificates() %></p>
        <details style="margin-top:10px;">
            <summary>View Raw CV Text (excerpt)</summary>
            <pre class="raw-cv"><%= student.getProfile().getRawCvText() == null || student.getProfile().getRawCvText().isBlank() ? "N/A" : student.getProfile().getRawCvText() %></pre>
        </details>
    </section>
</main>
</body>
</html>
