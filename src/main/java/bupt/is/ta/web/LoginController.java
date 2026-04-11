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
import java.util.Optional;

@WebServlet("/login")
public class LoginController extends HttpServlet {

    private final UserService userService = new UserService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = req.getParameter("id");
        String password = req.getParameter("password");

        Optional<User> userOpt = userService.authenticate(id, password);
        if (userOpt.isEmpty()) {
            req.setAttribute("error", "Invalid id or password");
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
            return;
        }

        User user = userOpt.get();
        HttpSession session = req.getSession(true);
        session.setAttribute("currentUser", user);
        if (user.getRole() == User.Role.TA) {
            session.setAttribute("taProfilePromptPending", Boolean.TRUE);
        }

        switch (user.getRole()) {
            case TA -> resp.sendRedirect(req.getContextPath() + "/ta/dashboard");
            case MO -> resp.sendRedirect(req.getContextPath() + "/mo/dashboard");
            case ADMIN -> resp.sendRedirect(req.getContextPath() + "/admin/overview");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        resp.sendRedirect(req.getContextPath() + "/login.jsp");
    }
}

