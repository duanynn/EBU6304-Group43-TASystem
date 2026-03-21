<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="bupt.is.ta.model.Job" %>
<%@ page import="bupt.is.ta.model.User" %>
<%
    List<Job> jobs = (List<Job>) request.getAttribute("jobs");
    if (jobs == null) jobs = List.of();
    User current = (User) session.getAttribute("currentUser");
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>我的岗位 - 老师工作台</title>
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
    <h2 class="page-title">我发布的岗位</h2>
    <div class="section">
        <table class="data-table">
            <thead>
                <tr>
                    <th>课程名</th>
                    <th>需要人数</th>
                    <th>要求技能</th>
                    <th>状态</th>
                    <th>操作</th>
                </tr>
            </thead>
            <tbody>
                <% for (Job j : jobs) { %>
                <tr>
                    <td><%= j.getCourseName() %></td>
                    <td><%= j.getRequiredCount() %></td>
                    <td><%= j.getRequiredSkills() != null ? String.join(", ", j.getRequiredSkills()) : "-" %></td>
                    <td><%= j.isOpen() ? "招募中" : "已关闭" %></td>
                    <td>
                        <a href="<%= request.getContextPath() %>/mo/applicants?jobId=<%= j.getId() %>" class="btn btn-small">查看申请人</a>
                    </td>
                </tr>
                <% } %>
                <% if (jobs.isEmpty()) { %>
                <tr><td colspan="5" class="empty-hint">暂无岗位，请点击“发布新岗位”</td></tr>
                <% } %>
            </tbody>
        </table>
    </div>
</main>
</body>
</html>
