package bupt.is.ta.web;

import bupt.is.ta.model.User;
import bupt.is.ta.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet({
        "/ta/changePassword",
        "/mo/changePassword",
        "/admin/changePassword"
})
public class AccountController extends HttpServlet {

    private final UserService userService = new UserService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User current = session != null ? (User) session.getAttribute("currentUser") : null;
        if (current == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }

        String path = req.getServletPath();
        if (!isPathAllowedForRole(path, current.getRole())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String oldPassword = req.getParameter("oldPassword");
        String newPassword = req.getParameter("newPassword");
        String confirmPassword = req.getParameter("confirmPassword");
        String referer = req.getHeader("Referer");
        if (referer == null || referer.isBlank()) {
            referer = defaultHome(req, current);
        }

        if (oldPassword == null || newPassword == null || confirmPassword == null
                || oldPassword.isBlank() || newPassword.isBlank()) {
            session.setAttribute("accountMessage", "All password fields are required.");
            resp.sendRedirect(referer);
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            session.setAttribute("accountMessage", "New passwords do not match.");
            resp.sendRedirect(referer);
            return;
        }
        if (userService.authenticate(current.getId(), oldPassword).isEmpty()) {
            session.setAttribute("accountMessage", "Current password is incorrect.");
            resp.sendRedirect(referer);
            return;
        }

        current.setPassword(newPassword.trim());
        try {
            userService.save(current);
            session.setAttribute("currentUser", current);
            session.setAttribute("accountMessage", "Password updated successfully.");
        } catch (Exception e) {
            session.setAttribute("accountMessage", "Could not update password. Please try again.");
        }
        resp.sendRedirect(referer);
    }

    private static boolean isPathAllowedForRole(String path, User.Role role) {
        return switch (role) {
            case TA -> "/ta/changePassword".equals(path);
            case MO -> "/mo/changePassword".equals(path);
            case ADMIN -> "/admin/changePassword".equals(path);
        };
    }

    private static String defaultHome(HttpServletRequest req, User user) {
        return switch (user.getRole()) {
            case TA -> req.getContextPath() + "/ta/profile";
            case MO -> req.getContextPath() + "/mo/home";
            case ADMIN -> req.getContextPath() + "/admin/overview";
        };
    }
}
