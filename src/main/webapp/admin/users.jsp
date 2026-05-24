<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="bupt.is.ta.model.User" %>
<%
    @SuppressWarnings("unchecked")
    List<User> users = (List<User>) request.getAttribute("users");
    if (users == null) users = List.of();
    @SuppressWarnings("unchecked")
    List<User> moUsers = (List<User>) request.getAttribute("moUsers");
    if (moUsers == null) moUsers = List.of();
    User current = (User) session.getAttribute("currentUser");
    String adminMessage = (String) request.getAttribute("adminMessage");
    String adminMessageType = (String) request.getAttribute("adminMessageType");
    String alertClass = "error".equals(adminMessageType) ? "alert alert-error"
            : ("success".equals(adminMessageType) ? "alert alert-success" : "alert alert-info");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>User Management - Admin</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css?v=20260518-ui2">
</head>
<body>
<header class="app-header">
    <h1>TA Recruitment System - Admin</h1>
    <span class="user-info user-info-with-avatar"><%= current != null ? current.getName() : "" %>
        <jsp:include page="/WEB-INF/jsp/account-menu.jsp"/>
    </span>
</header>
<nav class="app-nav">
    <a href="<%= request.getContextPath() %>/admin/overview">Overview</a>
    <a href="<%= request.getContextPath() %>/admin/openJobs">Open Jobs</a>
    <a href="<%= request.getContextPath() %>/admin/workload">Workload</a>
    <a href="<%= request.getContextPath() %>/admin/users">Users</a>
    <a href="<%= request.getContextPath() %>/admin/config">System Config</a>
</nav>
<main class="app-main">
    <% if (adminMessage != null && !adminMessage.isBlank()) { %>
    <div class="<%= alertClass %>"><%= adminMessage %></div>
    <% } %>

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

    <h2 class="page-title">Reset MO Password</h2>
    <div class="section">
        <p class="muted">Reset a Module Organiser (MO) password to the default <strong>111</strong>. The MO should use this password on next login.</p>
        <% if (moUsers.isEmpty()) { %>
        <p class="empty-hint">No MO accounts yet. Create one above first.</p>
        <% } else { %>
        <form method="post" action="<%= request.getContextPath() %>/admin/users" class="form-narrow">
            <input type="hidden" name="action" value="resetMoPassword">
            <div class="form-group">
                <label>MO account</label>
                <select name="moId" required>
                    <option value="">— Select MO —</option>
                    <% for (User mo : moUsers) { %>
                    <option value="<%= mo.getId() %>"><%= mo.getId() %> — <%= mo.getName() != null ? mo.getName() : "Unnamed" %></option>
                    <% } %>
                </select>
            </div>
            <div class="form-actions">
                <button type="submit" class="btn btn-secondary">Reset to default (111)</button>
            </div>
        </form>
        <% } %>
    </div>

    <h2 class="page-title">All Users</h2>
    <div class="section">
        <table class="data-table">
            <thead>
                <tr><th>Avatar</th><th>ID</th><th>Name</th><th>Role</th></tr>
            </thead>
            <tbody>
                <% for (User u : users) {
                    if (u.getRole() == User.Role.ADMIN) continue;
                    request.setAttribute("avatarUser", u);
                %>
                <tr>
                    <td><jsp:include page="/WEB-INF/jsp/avatar.jsp"><jsp:param name="size" value="36"/></jsp:include></td>
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
