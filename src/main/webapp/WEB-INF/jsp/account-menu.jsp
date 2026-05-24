<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="bupt.is.ta.model.User" %>
<script src="<%= request.getContextPath() %>/js/theme.js"></script>
<%
    User menuUser = (User) session.getAttribute("currentUser");
    String ctx = request.getContextPath();
    String changePasswordPath = ctx + "/ta/changePassword";
    if (menuUser != null) {
        changePasswordPath = switch (menuUser.getRole()) {
            case MO -> ctx + "/mo/changePassword";
            case ADMIN -> ctx + "/admin/changePassword";
            default -> ctx + "/ta/changePassword";
        };
    }
    String userId = menuUser != null ? menuUser.getId() : "";
    String returnPath = request.getRequestURI();
    String queryString = request.getQueryString();
    if (queryString != null && !queryString.isBlank()) {
        returnPath = returnPath + "?" + queryString;
    }
    if (returnPath.startsWith(ctx)) {
        returnPath = returnPath.substring(ctx.length());
    }
    if (returnPath.isEmpty()) {
        returnPath = "/";
    }
    String accountFlash = null;
    String accountFlashType = null;
    String changePwdMsg = null;
    String changePwdType = null;
    boolean openChangePwdModal = false;
    if (session != null) {
        Object msg = session.getAttribute("accountMessage");
        if (msg instanceof String s && !s.isBlank()) {
            accountFlash = s;
            Object type = session.getAttribute("accountMessageType");
            accountFlashType = type instanceof String t && !t.isBlank() ? t : "info";
            session.removeAttribute("accountMessage");
            session.removeAttribute("accountMessageType");
        }
        Object cpMsg = session.getAttribute("changePasswordMessage");
        if (cpMsg instanceof String s && !s.isBlank()) {
            changePwdMsg = s;
            Object type = session.getAttribute("changePasswordMessageType");
            changePwdType = type instanceof String t && !t.isBlank() ? t : "error";
            session.removeAttribute("changePasswordMessage");
            session.removeAttribute("changePasswordMessageType");
        }
        Object openModal = session.getAttribute("openChangePasswordModal");
        if (Boolean.TRUE.equals(openModal)) {
            openChangePwdModal = true;
            session.removeAttribute("openChangePasswordModal");
        }
    }
    String accountFlashClass = "alert alert-info";
    if ("error".equals(accountFlashType)) {
        accountFlashClass = "alert alert-error";
    } else if ("success".equals(accountFlashType)) {
        accountFlashClass = "alert alert-success";
    }
%>
<div class="account-menu" data-user-id="<%= userId %>">
    <button type="button" class="account-menu-trigger" aria-label="Account menu" aria-expanded="false">
        <svg class="account-menu-icon" width="18" height="18" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
            <circle cx="12" cy="5" r="1.75" fill="currentColor"/>
            <circle cx="12" cy="12" r="1.75" fill="currentColor"/>
            <circle cx="12" cy="19" r="1.75" fill="currentColor"/>
        </svg>
    </button>
    <div class="account-menu-panel" hidden>
        <button type="button" class="account-menu-item" data-action="change-password">Change password</button>
        <div class="account-menu-theme">
            <span class="account-menu-label">Theme</span>
            <label><input type="radio" name="themeChoice" value="default" checked> Default</label>
            <label><input type="radio" name="themeChoice" value="eye"> Eye-care</label>
            <label><input type="radio" name="themeChoice" value="night"> Night</label>
        </div>
        <a class="account-menu-item account-menu-logout" href="<%= ctx %>/login">Logout</a>
    </div>
</div>
<% if (accountFlash != null) { %>
<div class="<%= accountFlashClass %> account-flash-banner account-flash-top" role="alert"><%= accountFlash.replace("<", "&lt;") %></div>
<% } %>
<div id="changePasswordModal" class="modal-overlay" hidden>
    <div class="modal-card">
        <h3>Change password</h3>
        <p class="muted modal-hint">Enter your <strong>login password</strong> (not ID card suffix). Account: <code><%= userId %></code></p>
        <div id="changePasswordModalAlert" class="change-password-modal-alert" hidden role="alert"></div>
        <form method="post" action="<%= changePasswordPath %>" id="changePasswordForm" accept-charset="UTF-8">
            <input type="hidden" name="returnUrl" value="<%= returnPath.replace("\"", "&quot;") %>">
            <div class="form-group">
                <label>Current password (login password)</label>
                <input type="password" name="oldPassword" id="changePasswordOld" required autocomplete="current-password">
            </div>
            <div class="form-group">
                <label>New password</label>
                <input type="password" name="newPassword" required autocomplete="new-password">
            </div>
            <div class="form-group">
                <label>Confirm new password</label>
                <input type="password" name="confirmPassword" required autocomplete="new-password">
            </div>
            <div class="form-actions">
                <button type="submit" class="btn">Save</button>
                <button type="button" class="btn btn-secondary" data-close-modal>Cancel</button>
            </div>
        </form>
    </div>
</div>
<% if (changePwdMsg != null) { %>
<div id="changePasswordFlashPayload" hidden
     data-open="<%= openChangePwdModal %>"
     data-type="<%= changePwdType %>"
     data-message="<%= changePwdMsg.replace("\"", "&quot;").replace("<", "&lt;") %>"></div>
<% } %>
<script src="<%= ctx %>/js/account-menu.js?v=20260519-pwd5"></script>
