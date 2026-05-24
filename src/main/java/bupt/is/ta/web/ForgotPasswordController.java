package bupt.is.ta.web;

import bupt.is.ta.model.User;
import bupt.is.ta.service.UserService;
import bupt.is.ta.util.CaptchaUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/forgotPassword")
public class ForgotPasswordController extends HttpServlet {

    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        invalidateCaptcha(req.getSession(false));
        req.getRequestDispatcher("/forgotPassword.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(true);
        String id = trim(req.getParameter("id"));
        String suffix = trim(req.getParameter("idCardSuffix"));
        String captchaInput = trim(req.getParameter("captcha"));
        String newPassword = req.getParameter("newPassword");
        String confirmPassword = req.getParameter("confirmPassword");

        String expectedCaptcha = session != null ? (String) session.getAttribute(CaptchaController.SESSION_ATTR) : null;
        invalidateCaptcha(session);

        if (id.isBlank() || suffix.isBlank() || captchaInput.isBlank()
                || newPassword == null || newPassword.isBlank()) {
            forwardError(req, resp, "All fields are required.");
            return;
        }
        if (!id.matches("\\d{10}")) {
            forwardError(req, resp, "Student ID must be 10 digits.");
            return;
        }
        if (!suffix.matches("\\d{6}")) {
            forwardError(req, resp, "ID card suffix must be 6 digits.");
            return;
        }
        if (!CaptchaUtil.matches(expectedCaptcha, captchaInput)) {
            forwardError(req, resp, "Incorrect verification code. Please try again.");
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            forwardError(req, resp, "New passwords do not match.");
            return;
        }
        if (newPassword.length() < 3) {
            forwardError(req, resp, "Password is too short.");
            return;
        }

        User user = userService.findById(id).orElse(null);
        if (user == null || user.getRole() != User.Role.TA) {
            forwardError(req, resp, "Student account not found or password reset is not available for this account type.");
            return;
        }
        if (!suffix.equals(user.getIdCardSuffix())) {
            forwardError(req, resp, "ID card suffix does not match our records.");
            return;
        }

        user.setPassword(newPassword.trim());
        try {
            userService.save(user);
        } catch (Exception e) {
            forwardError(req, resp, "Could not reset password. Please try again later.");
            return;
        }

        resp.sendRedirect(req.getContextPath() + "/login.jsp?reset=1");
    }

    private void forwardError(HttpServletRequest req, HttpServletResponse resp, String message)
            throws ServletException, IOException {
        req.setAttribute("error", message);
        req.getRequestDispatcher("/forgotPassword.jsp").forward(req, resp);
    }

    private static void invalidateCaptcha(HttpSession session) {
        if (session != null) {
            session.removeAttribute(CaptchaController.SESSION_ATTR);
        }
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
