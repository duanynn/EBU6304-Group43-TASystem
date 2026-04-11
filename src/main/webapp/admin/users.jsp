<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="bupt.is.ta.model.User" %>
<%
    @SuppressWarnings("unchecked")
    List<User> users = (List<User>) request.getAttribute("users");
    if (users == null) users = List.of();
    User current = (User) session.getAttribute("currentUser");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>User Management - Admin</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<header class="app-header">
    <h1>TA Recruitment System - Admin</h1>
    <span class="user-info"><%= current != null ? current.getName() : "" %> <a href="<%= request.getContextPath() %>/login">Logout</a></span>
</header>
<nav class="app-nav">
    <a href="<%= request.getContextPath() %>/admin/overview">Overview</a>
    <a href="<%= request.getContextPath() %>/admin/workload">Workload</a>
    <a href="<%= request.getContextPath() %>/admin/users">Users</a>
    <a href="<%= request.getContextPath() %>/admin/config">System Config</a>
</nav>
<main class="app-main">
    <h2 class="page-title">Create Instructor (MO) Account</h2>
    <div class="section">
        <% if (request.getAttribute("error") != null) { %>
        <p class="alert alert-error"><%= request.getAttribute("error") %></p>
        <% } %>
        <form method="post" action="<%= request.getContextPath() %>/admin/users">
            <div class="form-group">
                <label>Staff ID</label>
                <input type="text" name="id" required pattern="\d{10}" maxlength="10" placeholder="10 digits" title="Staff ID must be 10 digits">
            </div>
            <div class="form-group">
                <label>Name</label>
                <input type="text" name="name" required>
            </div>
            <div class="form-group">
                <label>Initial Password</label>
                <input type="password" name="password" required>
            </div>
            <div class="form-actions">
                <button type="submit" class="btn">Create Instructor Account</button>
            </div>
        </form>
    </div>
    <h2 class="page-title">All Users</h2>
    <div class="section">
        <table class="data-table">
            <thead>
                <tr><th>ID</th><th>Name</th><th>Role</th></tr>
            </thead>
            <tbody>
                <% for (User u : users) { %>
                <tr>
                    <td><%= u.getId() %></td>
                    <td><%= u.getName() != null ? u.getName() : "-" %></td>
                    <td><%= u.getRole() != null ? u.getRole() : "-" %></td>
                </tr>
                <% } %>
            </tbody>
        </table>
    </div>
</main>
</body>
</html>
