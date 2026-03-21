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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet("/register")
public class RegisterController extends HttpServlet {

    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/register.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = req.getParameter("id");
        String password = req.getParameter("password");
        String name = req.getParameter("name");
        String gpaStr = req.getParameter("gpa");
        String skillsStr = req.getParameter("skillTags");

        if (id == null || id.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            req.setAttribute("error", "学号和密码不能为空");
            req.getRequestDispatcher("/register.jsp").forward(req, resp);
            return;
        }
        id = id.trim();
        if (!id.matches("\\d{10}")) {
            req.setAttribute("error", "学号必须为 10 位数字");
            req.getRequestDispatcher("/register.jsp").forward(req, resp);
            return;
        }
        if (userService.findById(id).isPresent()) {
            req.setAttribute("error", "该学号已被注册，请直接登录或更换学号");
            req.getRequestDispatcher("/register.jsp").forward(req, resp);
            return;
        }

        User ta = new User();
        ta.setId(id);
        ta.setPassword(password.trim());
        ta.setName(name != null ? name.trim() : id);
        ta.setRole(User.Role.TA);
        if (gpaStr != null && !gpaStr.trim().isEmpty()) {
            try {
                ta.setGpa(Double.parseDouble(gpaStr.trim()));
            } catch (NumberFormatException ignored) { }
        }
        if (skillsStr != null && !skillsStr.trim().isEmpty()) {
            List<String> tags = Arrays.stream(skillsStr.split("[,，\\s]+"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            ta.setSkillTags(tags);
        } else {
            ta.setSkillTags(new ArrayList<>());
        }

        try {
            userService.save(ta);
        } catch (Exception e) {
            req.setAttribute("error", "注册失败，请稍后重试");
            req.getRequestDispatcher("/register.jsp").forward(req, resp);
            return;
        }

        HttpSession session = req.getSession(true);
        session.setAttribute("currentUser", ta);
        resp.sendRedirect(req.getContextPath() + "/ta/jobs");
    }
}
