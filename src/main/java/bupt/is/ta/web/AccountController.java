package bupt.is.ta.web;

import bupt.is.ta.model.User;
import bupt.is.ta.service.UserService;
import bupt.is.ta.util.SafeRedirectUtil;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@WebServlet({
        "/ta/changePassword",
        "/mo/changePassword",
        "/admin/changePassword"
})
public class AccountController extends HttpServlet {

    private static final Gson GSON = new Gson();
    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        User current = session != null ? (User) session.getAttribute("currentUser") : null;
        if (current == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }
        resp.sendRedirect(SafeRedirectUtil.resolveRedirectUrl(req, current, null, null));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        boolean ajax = isAjax(req);

        HttpSession session = req.getSession(false);
        User current = session != null ? (User) session.getAttribute("currentUser") : null;
        if (current == null) {
            if (ajax) {
                writeJson(resp, false, "Please log in again.", "error");
            } else {
                resp.sendRedirect(req.getContextPath() + "/login.jsp");
            }
            return;
        }

        String path = req.getServletPath();
        if (!isPathAllowedForRole(path, current.getRole())) {
            if (ajax) {
                writeJson(resp, false, "Not allowed.", "error");
            } else {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
            return;
        }

        String redirectUrl = SafeRedirectUtil.resolveRedirectUrl(
                req, current, req.getParameter("returnUrl"), req.getHeader("Referer"));

        String oldPassword = trim(req.getParameter("oldPassword"));
        String newPassword = trim(req.getParameter("newPassword"));
        String confirmPassword = trim(req.getParameter("confirmPassword"));

        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            respondError(session, ajax, resp, redirectUrl, "All password fields are required.");
            return;
        }
        if (newPassword.length() < 3) {
            respondError(session, ajax, resp, redirectUrl,
                    "New password is too short (minimum 3 characters).");
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            respondError(session, ajax, resp, redirectUrl, "New passwords do not match.");
            return;
        }
        if (!userService.verifyPasswordFromStore(current.getId(), oldPassword)) {
            respondError(session, ajax, resp, redirectUrl,
                    "Current password is incorrect. Use the same password you use to log in.");
            return;
        }
        if (newPassword.equals(oldPassword)) {
            respondError(session, ajax, resp, redirectUrl,
                    "New password must be different from your current password.");
            return;
        }

        Optional<User> storedUser = userService.findByIdFromStore(current.getId());
        if (storedUser.isEmpty()) {
            respondError(session, ajax, resp, redirectUrl, "Account not found.");
            return;
        }

        User toUpdate = storedUser.get();
        toUpdate.setPassword(newPassword);
        try {
            userService.save(toUpdate);
            session.setAttribute("currentUser", toUpdate);
            if (ajax) {
                writeJson(resp, true, "Password updated successfully.", "success");
            } else {
                flash(session, "Password updated successfully.", "success");
                resp.sendRedirect(redirectUrl);
            }
        } catch (Exception e) {
            respondError(session, ajax, resp, redirectUrl,
                    "Could not update password. Please try again.");
        }
    }

    private void respondError(HttpSession session, boolean ajax, HttpServletResponse resp,
                              String redirectUrl, String message) throws IOException {
        if (ajax) {
            writeJson(resp, false, message, "error");
        } else {
            flashModal(session, message, "error");
            resp.sendRedirect(redirectUrl);
        }
    }

    private static boolean isAjax(HttpServletRequest req) {
        return "XMLHttpRequest".equals(req.getHeader("X-Requested-With"))
                || "1".equals(trim(req.getParameter("ajax")));
    }

    private static void writeJson(HttpServletResponse resp, boolean success, String message, String type)
            throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", success);
        body.put("message", message);
        body.put("type", type);
        try (PrintWriter writer = resp.getWriter()) {
            writer.write(GSON.toJson(body));
        }
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private static void flash(HttpSession session, String message, String type) {
        session.setAttribute("accountMessage", message);
        session.setAttribute("accountMessageType", type);
    }

    private static void flashModal(HttpSession session, String message, String type) {
        session.setAttribute("changePasswordMessage", message);
        session.setAttribute("changePasswordMessageType", type);
        session.setAttribute("openChangePasswordModal", Boolean.TRUE);
    }

    private static boolean isPathAllowedForRole(String path, User.Role role) {
        return switch (role) {
            case TA -> "/ta/changePassword".equals(path);
            case MO -> "/mo/changePassword".equals(path);
            case ADMIN -> "/admin/changePassword".equals(path);
        };
    }
}
