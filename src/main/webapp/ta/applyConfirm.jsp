<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="bupt.is.ta.model.Job" %>
<%@ page import="bupt.is.ta.service.SkillMatchService.MatchResult" %>
<%
    Job job = (Job) request.getAttribute("job");
    MatchResult match = (MatchResult) request.getAttribute("match");
    if (job == null || match == null) { response.sendRedirect(request.getContextPath() + "/ta/jobs"); return; }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Confirm Application - <%= job.getCourseName() %></title>
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
    <h2 class="page-title">Confirm Application: <%= job.getCourseName() %></h2>
    <div class="section">
        <h3>Matching Result</h3>
        <p><strong>Working Time:</strong> <%= job.getRequiredWorkTime() == null || job.getRequiredWorkTime().isBlank() ? "-" : job.getRequiredWorkTime() %></p>
        <p><strong>Required Skills:</strong> <%= match.getRequiredSkills() %></p>
        <p><strong>Your Skills:</strong> <%= match.getStudentSkills() %></p>
        <p><strong>Rule-based Score:</strong> <%= Math.round(match.getScore() * 100) %>%</p>
        <p><strong>AI Overall Score:</strong> <%= Math.round(match.getAiScore()) %>%</p>
        <p><strong>Fit Summary:</strong> <%= match.getAiFitSummary() == null || match.getAiFitSummary().isBlank() ? "N/A" : match.getAiFitSummary() %></p>
        <p><strong>Missing Skills:</strong> <%= match.getMissingSkills() %></p>
        <% if (match.getAiAdvice() != null && !match.getAiAdvice().isBlank()) { %>
        <p><strong>AI Advice:</strong> <%= match.getAiAdvice() %></p>
        <p><strong>Strengths:</strong> <%= match.getAiStrengths() == null || match.getAiStrengths().isEmpty() ? "N/A" : match.getAiStrengths() %></p>
        <p><strong>Gaps:</strong> <%= match.getAiGaps() == null || match.getAiGaps().isEmpty() ? "N/A" : match.getAiGaps() %></p>
        <p class="muted">Advice Source: <%= match.isAiGenerated() ? "Real-time AI" : "Local fallback" %></p>
        <% } %>
        <% if (match.getScore() < 0.5) { %>
        <p class="alert alert-warning">Low match score: consider improving related skills before applying, or explain your learning plan during interview.</p>
        <% } %>
        <form method="post" action="<%= request.getContextPath() %>/ta/confirmApply" class="form-actions">
            <input type="hidden" name="jobId" value="<%= job.getId() %>"/>
            <button type="submit" class="btn btn-success">Confirm Application</button>
            <a href="<%= request.getContextPath() %>/ta/jobs" class="btn btn-secondary">Back to Job Board</a>
        </form>
    </div>
</main>
</body>
</html>
