<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="bupt.is.ta.model.Job" %>
<%@ page import="bupt.is.ta.service.SkillMatchService.MatchResult" %>
<%
    Job job = (Job) request.getAttribute("job");
    MatchResult match = (MatchResult) request.getAttribute("match");
    if (job == null || match == null) { response.sendRedirect(request.getContextPath() + "/ta/jobs"); return; }
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>确认投递 - <%= job.getCourseName() %></title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<header class="app-header">
    <h1>TA 招聘系统 · 学生端</h1>
</header>
<nav class="app-nav">
    <a href="<%= request.getContextPath() %>/ta/jobs">职位大厅</a>
    <a href="<%= request.getContextPath() %>/ta/applications">我的申请</a>
</nav>
<main class="app-main">
    <h2 class="page-title">确认投递：<%= job.getCourseName() %></h2>
    <div class="section">
        <h3>系统匹配结果</h3>
        <p><strong>岗位需要技能：</strong><%= match.getRequiredSkills() %></p>
        <p><strong>你的技能：</strong><%= match.getStudentSkills() %></p>
        <p><strong>匹配度：</strong><%= Math.round(match.getScore() * 100) %>%</p>
        <p><strong>缺失技能：</strong><%= match.getMissingSkills() %></p>
        <% if (match.getScore() < 0.5) { %>
        <p class="alert alert-warning">提示：匹配度较低，建议补充相关技能后再投递；你也可以坚持投递并在面试中解释你的学习计划。</p>
        <% } %>
        <form method="post" action="<%= request.getContextPath() %>/ta/confirmApply" class="form-actions">
            <input type="hidden" name="jobId" value="<%= job.getId() %>"/>
            <button type="submit" class="btn btn-success">确认投递</button>
            <a href="<%= request.getContextPath() %>/ta/jobs" class="btn btn-secondary">返回职位大厅</a>
        </form>
    </div>
</main>
</body>
</html>
