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
<div id="changePasswordModal" class="modal-overlay" hidden>
    <div class="modal-card">
        <h3>Change password</h3>
        <form method="post" action="<%= changePasswordPath %>" id="changePasswordForm">
            <div class="form-group">
                <label>Current password</label>
                <input type="password" name="oldPassword" required autocomplete="current-password">
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
<script src="<%= ctx %>/js/account-menu.js"></script>
