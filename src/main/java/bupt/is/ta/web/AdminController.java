package bupt.is.ta.web;

import bupt.is.ta.model.Application;
import bupt.is.ta.model.Config;
import bupt.is.ta.model.User;
import bupt.is.ta.service.UserService;
import bupt.is.ta.store.DataStore;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@WebServlet({"/admin/overview", "/admin/workload", "/admin/users", "/admin/config"})
public class AdminController extends HttpServlet {

    private final UserService userService = new UserService();
    private final DataStore store = DataStore.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        switch (path) {
            case "/admin/overview" -> showOverview(req, resp);
            case "/admin/workload" -> showWorkload(req, resp);
            case "/admin/users" -> showUsers(req, resp);
            case "/admin/config" -> showConfig(req, resp);
            default -> resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        try {
            switch (path) {
                case "/admin/users" -> handleCreateMo(req, resp);
                case "/admin/config" -> handleUpdateConfig(req, resp);
                default -> resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void showOverview(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int totalUsers = store.getUsers().size();
        long totalJobs = store.getJobs().size();
        long totalApplications = store.getApplications().size();
        req.setAttribute("totalUsers", totalUsers);
        req.setAttribute("totalJobs", totalJobs);
        req.setAttribute("totalApplications", totalApplications);
        req.getRequestDispatcher("/admin/overview.jsp").forward(req, resp);
    }

    private void showWorkload(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<Application> accepted = store.getApplications().stream()
                .filter(a -> a.getStatus() == Application.Status.ACCEPTED)
                .toList();

        Map<String, Long> countByStudent = accepted.stream()
                .collect(Collectors.groupingBy(Application::getStudentId, Collectors.counting()));

        List<Map.Entry<String, Long>> sorted = countByStudent.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .toList();

        Map<String, User> userMap = store.getUsers().stream()
                .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));

        req.setAttribute("workload", sorted);
        req.setAttribute("userMap", userMap);
        req.getRequestDispatcher("/admin/workload.jsp").forward(req, resp);
    }

    private void showUsers(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("users", store.getUsers());
        req.getRequestDispatcher("/admin/users.jsp").forward(req, resp);
    }

    private void showConfig(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Config c = store.getConfig();
        if (c == null) c = new Config();
        if (c.getCvRelativePath() == null) c.setCvRelativePath("/WEB-INF/data/cvs");
        if (c.getStorageMode() == null) c.setStorageMode("WEBAPP");
        if (c.getMaxCoursesPerTA() < 1) c.setMaxCoursesPerTA(2);
        req.setAttribute("configMaxCourses", c.getMaxCoursesPerTA());
        req.setAttribute("configCvPath", c.getCvRelativePath());
        req.setAttribute("configStorageMode", c.getStorageMode());
        req.getRequestDispatcher("/admin/config.jsp").forward(req, resp);
    }

    private void handleCreateMo(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String id = req.getParameter("id");
        String name = req.getParameter("name");
        String password = req.getParameter("password");

        if (id == null || id.trim().isEmpty()) {
            req.setAttribute("error", "工号不能为空");
            req.setAttribute("users", store.getUsers());
            req.getRequestDispatcher("/admin/users.jsp").forward(req, resp);
            return;
        }
        id = id.trim();
        if (!id.matches("\\d{10}")) {
            req.setAttribute("error", "工号必须为 10 位数字");
            req.setAttribute("users", store.getUsers());
            req.getRequestDispatcher("/admin/users.jsp").forward(req, resp);
            return;
        }
        if (userService.findById(id).isPresent()) {
            req.setAttribute("error", "该工号已存在");
            req.setAttribute("users", store.getUsers());
            req.getRequestDispatcher("/admin/users.jsp").forward(req, resp);
            return;
        }
        if (name == null || name.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            req.setAttribute("error", "姓名和初始密码不能为空");
            req.setAttribute("users", store.getUsers());
            req.getRequestDispatcher("/admin/users.jsp").forward(req, resp);
            return;
        }

        User mo = new User();
        mo.setId(id);
        mo.setName(name.trim());
        mo.setPassword(password.trim());
        mo.setRole(User.Role.MO);

        userService.save(mo);
        resp.sendRedirect(req.getContextPath() + "/admin/users");
    }

    private void handleUpdateConfig(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String maxStr = req.getParameter("maxCoursesPerTA");
        int maxCourses = 2;
        if (maxStr != null && !maxStr.trim().isEmpty()) {
            try {
                maxCourses = Integer.parseInt(maxStr.trim());
            } catch (NumberFormatException ignored) { }
        }
        if (maxCourses < 1) maxCourses = 1;
        String cvRelativePath = req.getParameter("cvRelativePath");
        String storageMode = req.getParameter("storageMode");
        if (cvRelativePath == null) cvRelativePath = "/WEB-INF/data/cvs";
        if (storageMode == null) storageMode = "WEBAPP";

        Config config = store.getConfig();
        config.setMaxCoursesPerTA(maxCourses);
        config.setCvRelativePath(cvRelativePath);
        config.setStorageMode(storageMode);

        synchronized (store) {
            store.updateConfig(config);
            store.saveAll();
        }

        resp.sendRedirect(req.getContextPath() + "/admin/config");
    }
}

