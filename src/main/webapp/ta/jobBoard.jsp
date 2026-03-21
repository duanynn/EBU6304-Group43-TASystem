<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="bupt.is.ta.model.Job" %>
<%@ page import="bupt.is.ta.model.User" %>
<%
    List<Job> jobs = (List<Job>) request.getAttribute("jobs");
    if (jobs == null) jobs = java.util.List.of();
    User current = (User) session.getAttribute("currentUser");
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>职位大厅 - TA 招聘系统</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<header class="app-header">
    <h1>TA 招聘系统 · 学生端</h1>
    <span class="user-info"><%= current != null ? current.getName() : "" %> <a href="<%= request.getContextPath() %>/login">退出</a></span>
</header>
<nav class="app-nav">
    <a href="<%= request.getContextPath() %>/ta/jobs">职位大厅</a>
    <a href="<%= request.getContextPath() %>/ta/applications">我的申请</a>
</nav>
<main class="app-main">
    <h2 class="page-title">开放岗位</h2>
    <div class="section">
        <table class="data-table">
            <thead>
                <tr>
                    <th>课程名</th>
                    <th>需要人数</th>
                    <th>要求技能</th>
                    <th>操作</th>
                </tr>
            </thead>
            <tbody>
                <% for (Job job : jobs) { %>
                <tr>
                    <td><%= job.getCourseName() %></td>
                    <td><%= job.getRequiredCount() %></td>
                    <td><%= job.getRequiredSkills() != null ? String.join(", ", job.getRequiredSkills()) : "-" %></td>
                    <td>
                        <form method="post" action="<%= request.getContextPath() %>/ta/apply" style="display:inline">
                            <input type="hidden" name="jobId" value="<%= job.getId() %>"/>
                            <button type="submit" class="btn btn-small">申请</button>
                        </form>
                    </td>
                </tr>
                <% } %>
                <% if (jobs.isEmpty()) { %>
                <tr><td colspan="4" class="empty-hint">暂无开放岗位，请稍后再看</td></tr>
                <% } %>
            </tbody>
        </table>
    </div>
</main>
</body>
</html>
