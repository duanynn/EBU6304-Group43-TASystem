<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="bupt.is.ta.model.User" %>
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
    @SuppressWarnings("unchecked")
    List<String> courseNames = (List<String>) request.getAttribute("courseNames");
    if (courseNames == null) courseNames = List.of();
    String accountMessage = (String) request.getAttribute("accountMessage");
    String ctx = request.getContextPath();
    request.setAttribute("avatarUser", current);
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>My Home - Instructor Workspace</title>
    <link rel="stylesheet" href="<%= ctx %>/css/style.css?v=20260521-account">
    <script src="<%= ctx %>/js/theme.js"></script>
</head>
<body class="layout-wide">
<header class="app-header app-header-with-avatar">
    <h1>TA Recruitment System - Instructor Workspace</h1>
    <span class="user-info user-info-with-avatar">
        <%= h(current != null ? current.getName() : "") %>
        <jsp:include page="/WEB-INF/jsp/account-menu.jsp"/>
    </span>
</header>
<nav class="app-nav">
    <a href="<%= ctx %>/mo/home">My Home</a>
    <a href="<%= ctx %>/mo/dashboard">My Jobs</a>
    <a href="<%= ctx %>/mo/postJob">Post New Job</a>
</nav>
<main class="app-main">
    <% if (accountMessage != null && !accountMessage.isBlank()) { %>
    <div class="alert alert-info"><%= h(accountMessage) %></div>
    <% } %>
    <div class="mo-home-hero section">
        <div class="mo-home-photo">
            <jsp:include page="/WEB-INF/jsp/avatar.jsp"><jsp:param name="size" value="96"/></jsp:include>
        </div>
        <div class="mo-home-info">
            <h2 class="page-title"><%= h(current != null ? current.getName() : "Instructor") %></h2>
            <p class="page-subtitle muted">Instructor profile — courses sync from jobs you have posted.</p>
        </div>
    </div>
    <div class="section">
        <h3>Edit profile</h3>
        <form method="post" action="<%= ctx %>/mo/home" class="mo-home-form">
            <div class="form-group">
                <label>Name</label>
                <input type="text" name="name" value="<%= h(current != null ? current.getName() : "") %>" required>
            </div>
            <div class="form-group">
                <label>College / Faculty</label>
                <input type="text" name="college" value="<%= h(current != null ? current.getCollege() : "") %>" placeholder="e.g. School of Computer Science">
            </div>
            <button type="submit" class="btn">Save</button>
        </form>
    </div>
    <div class="section">
        <h3>Courses (from posted jobs)</h3>
        <% if (courseNames.isEmpty()) { %>
        <p class="muted">No courses yet. <a href="<%= ctx %>/mo/postJob">Post a job</a> to add course names here.</p>
        <% } else { %>
        <ul class="simple-list mo-course-list">
            <% for (String course : courseNames) { %>
            <li><%= h(course) %></li>
            <% } %>
        </ul>
        <% } %>
    </div>
</main>
</body>
</html>
