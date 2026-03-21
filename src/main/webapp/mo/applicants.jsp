<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="bupt.is.ta.model.Application" %>
<%@ page import="bupt.is.ta.model.Job" %>
<%@ page import="bupt.is.ta.model.User" %>
<%@ page import="bupt.is.ta.service.SkillMatchService" %>
<%
    Job job = (Job) request.getAttribute("job");
    List<Application> applications = (List<Application>) request.getAttribute("applications");
    Map<Application, SkillMatchService.MatchResult> matchMap = (Map<Application, SkillMatchService.MatchResult>) request.getAttribute("matchMap");
    Map<String, User> studentMap = (Map<String, User>) request.getAttribute("studentMap");
    if (job == null) job = new bupt.is.ta.model.Job();
    if (applications == null) applications = List.of();
    if (matchMap == null) matchMap = Map.of();
    if (studentMap == null) studentMap = Map.of();
    User current = (User) session.getAttribute("currentUser");
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>申请人 - <%= job.getCourseName() %></title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<header class="app-header">
    <h1>TA 招聘系统 · 老师工作台</h1>
    <span class="user-info"><%= current != null ? current.getName() : "" %> <a href="<%= request.getContextPath() %>/login">退出</a></span>
</header>
<nav class="app-nav">
    <a href="<%= request.getContextPath() %>/mo/dashboard">我的岗位</a>
    <a href="<%= request.getContextPath() %>/mo/postJob">发布新岗位</a>
</nav>
<main class="app-main">
    <a href="<%= request.getContextPath() %>/mo/dashboard" class="back-link">← 返回岗位列表</a>
    <h2 class="page-title">申请人列表：<%= job.getCourseName() %></h2>
    <div class="section">
        <table class="data-table">
            <thead>
                <tr>
                    <th>学号</th><th>姓名</th><th>匹配度</th><th>状态</th><th>操作</th>
                </tr>
            </thead>
            <tbody>
                <% for (Application app : applications) {
                    User stu = studentMap.get(app.getStudentId());
                    SkillMatchService.MatchResult match = matchMap.get(app);
                    int pct = match != null ? (int) Math.round(match.getScore() * 100) : 0;
                    String badgeClass = pct >= 80 ? "badge-high" : (pct < 50 ? "badge-low" : "badge-mid");
                    String badge = pct >= 80 ? "High Match" : (pct < 50 ? "Low Match" : "Match");
                %>
                <tr>
                    <td><%= app.getStudentId() %></td>
                    <td><%= stu != null ? stu.getName() : "-" %></td>
                    <td><span class="<%= badgeClass %>"><%= pct %>% (<%= badge %>)</span></td>
                    <td><%= app.getStatus() %></td>
                    <td>
                        <% if (app.getStatus() != Application.Status.ACCEPTED && app.getStatus() != Application.Status.REJECTED) { %>
                        <form method="post" action="<%= request.getContextPath() %>/mo/updateStatus" style="display:inline">
                            <input type="hidden" name="applicationId" value="<%= app.getId() %>"/>
                            <input type="hidden" name="status" value="ACCEPTED"/>
                            <button type="submit" class="btn btn-small btn-success">录用</button>
                        </form>
                        <form method="post" action="<%= request.getContextPath() %>/mo/updateStatus" style="display:inline">
                            <input type="hidden" name="applicationId" value="<%= app.getId() %>"/>
                            <input type="hidden" name="status" value="REJECTED"/>
                            <button type="submit" class="btn btn-small btn-danger">拒绝</button>
                        </form>
                        <form method="post" action="<%= request.getContextPath() %>/mo/updateStatus" style="display:inline">
                            <input type="hidden" name="applicationId" value="<%= app.getId() %>"/>
                            <input type="hidden" name="status" value="INTERVIEWING"/>
                            <button type="submit" class="btn btn-small btn-secondary">进入面试</button>
                        </form>
                        <% } else { %>
                        <%= app.getStatus() %>
                        <% } %>
                    </td>
                </tr>
                <% } %>
                <% if (applications.isEmpty()) { %>
                <tr><td colspan="5" class="empty-hint">暂无申请人</td></tr>
                <% } %>
            </tbody>
        </table>
    </div>
</main>
</body>
</html>
