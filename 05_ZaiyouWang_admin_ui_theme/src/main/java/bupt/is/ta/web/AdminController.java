package bupt.is.ta.web;

import bupt.is.ta.model.Application;
import bupt.is.ta.model.Config;
import bupt.is.ta.model.Job;
import bupt.is.ta.model.User;
import bupt.is.ta.service.ApplicationService;
import bupt.is.ta.service.JobService;
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

@WebServlet({"/admin/overview", "/admin/workload", "/admin/users", "/admin/config", "/admin/openJobs"})
public class AdminController extends HttpServlet {

    public static class OpenJobRow {
        private final Job job;
        private final User mo;
        private final long applicationCount;

        public OpenJobRow(Job job, User mo, long applicationCount) {
            this.job = job;
            this.mo = mo;
            this.applicationCount = applicationCount;
        }

        public Job getJob() {
            return job;
        }

        public User getMo() {
            return mo;
        }

        public long getApplicationCount() {
            return applicationCount;
        }
    }

    private final UserService userService = new UserService();
    private final JobService jobService = new JobService();
    private final ApplicationService applicationService = new ApplicationService();
    private final DataStore store = DataStore.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        switch (path) {
            case "/admin/overview" -> showOverview(req, resp);
            case "/admin/workload" -> showWorkload(req, resp);
            case "/admin/users" -> showUsers(req, resp);
            case "/admin/config" -> showConfig(req, resp);
            case "/admin/openJobs" -> showOpenJobs(req, resp);
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
        long totalTa = store.getUsers().stream().filter(u -> u.getRole() == User.Role.TA).count();
        long totalMo = store.getUsers().stream().filter(u -> u.getRole() == User.Role.MO).count();
        long totalPending = store.getApplications().stream().filter(a -> a.getStatus() == Application.Status.PENDING).count();
        long totalInterviewing = store.getApplications().stream().filter(a -> a.getStatus() == Application.Status.INTERVIEWING).count();
        long totalAccepted = store.getApplications().stream().filter(a -> a.getStatus() == Application.Status.ACCEPTED).count();
        long totalRejected = store.getApplications().stream().filter(a -> a.getStatus() == Application.Status.REJECTED).count();
        req.setAttribute("totalUsers", totalUsers);
        req.setAttribute("totalJobs", totalJobs);
        req.setAttribute("totalApplications", totalApplications);
        req.setAttribute("totalTa", totalTa);
        req.setAttribute("totalMo", totalMo);
        req.setAttribute("totalPending", totalPending);
        req.setAttribute("totalInterviewing", totalInterviewing);
        req.setAttribute("totalAccepted", totalAccepted);
        req.setAttribute("totalRejected", totalRejected);
        req.setAttribute("currentSemester", store.getConfig().getCurrentSemester());
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

        int maxCourses = store.getConfig().getMaxCoursesPerTA();
        req.setAttribute("workload", sorted);
        req.setAttribute("userMap", userMap);
        req.setAttribute("maxCoursesPerTA", maxCourses);
        req.getRequestDispatcher("/admin/workload.jsp").forward(req, resp);
    }

    private void showUsers(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("users", store.getUsers());
        req.getRequestDispatcher("/admin/users.jsp").forward(req, resp);
    }

    private void showOpenJobs(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        boolean openOnly = !"all".equalsIgnoreCase(trim(req.getParameter("filter")));
        Map<String, User> userMap = store.getUsers().stream()
                .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));
        List<OpenJobRow> rows = store.getJobs().stream()
                .filter(j -> !openOnly || j.isOpen())
                .sorted(Comparator.comparing(Job::getCourseName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .map(j -> {
                    User mo = userMap.get(j.getMoId());
                    long count = applicationService.listByJob(j.getId()).size();
                    return new OpenJobRow(j, mo, count);
                })
                .toList();
        req.setAttribute("openJobRows", rows);
        req.setAttribute("openOnly", openOnly);
        req.getRequestDispatcher("/admin/openJobs.jsp").forward(req, resp);
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private void showConfig(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Config c = store.getConfig();
        if (c == null) c = new Config();
        if (c.getCvRelativePath() == null) c.setCvRelativePath("/WEB-INF/data/cvs");
        if (c.getStorageMode() == null) c.setStorageMode("WEBAPP");
        if (c.getMaxCoursesPerTA() < 1) c.setMaxCoursesPerTA(2);
        if (c.getDashscopeEndpoint() == null || c.getDashscopeEndpoint().isBlank()) {
            c.setDashscopeEndpoint("https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions");
        }
        if (c.getDashscopeModel() == null || c.getDashscopeModel().isBlank()) {
            c.setDashscopeModel("qwen-plus");
        }
        if (c.getDashscopeApiKey() == null) {
            c.setDashscopeApiKey("");
        }
        if (c.getCurrentSemester() == null || c.getCurrentSemester().isBlank()) {
            c.setCurrentSemester("2025-2026-S2");
        }
        if (c.getApplicationDeadline() == null) {
            c.setApplicationDeadline("");
        }
        req.setAttribute("configMaxCourses", c.getMaxCoursesPerTA());
        req.setAttribute("configCurrentSemester", c.getCurrentSemester());
        req.setAttribute("configApplicationDeadline", c.getApplicationDeadline());
        req.setAttribute("configCvPath", c.getCvRelativePath());
        req.setAttribute("configStorageMode", c.getStorageMode());
        req.setAttribute("dashscopeApiKey", c.getDashscopeApiKey());
        req.setAttribute("dashscopeEndpoint", c.getDashscopeEndpoint());
        req.setAttribute("dashscopeModel", c.getDashscopeModel());
        req.getRequestDispatcher("/admin/config.jsp").forward(req, resp);
    }

    private void handleCreateMo(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String id = req.getParameter("id");
        String name = req.getParameter("name");
        String password = req.getParameter("password");

        if (id == null || id.trim().isEmpty()) {
            req.setAttribute("error", "Staff ID is required");
            req.setAttribute("users", store.getUsers());
            req.getRequestDispatcher("/admin/users.jsp").forward(req, resp);
            return;
        }
        id = id.trim();
        if (!id.matches("\\d{10}")) {
            req.setAttribute("error", "Staff ID must be 10 digits");
            req.setAttribute("users", store.getUsers());
            req.getRequestDispatcher("/admin/users.jsp").forward(req, resp);
            return;
        }
        if (userService.findById(id).isPresent()) {
            req.setAttribute("error", "This staff ID already exists");
            req.setAttribute("users", store.getUsers());
            req.getRequestDispatcher("/admin/users.jsp").forward(req, resp);
            return;
        }
        if (name == null || name.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            req.setAttribute("error", "Name and initial password are required");
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
        String dashscopeApiKey = req.getParameter("dashscopeApiKey");
        String dashscopeEndpoint = req.getParameter("dashscopeEndpoint");
        String dashscopeModel = req.getParameter("dashscopeModel");
        String currentSemester = req.getParameter("currentSemester");
        String applicationDeadline = req.getParameter("applicationDeadline");
        if (cvRelativePath == null) cvRelativePath = "/WEB-INF/data/cvs";
        if (storageMode == null) storageMode = "WEBAPP";
        if (dashscopeApiKey == null) dashscopeApiKey = "";
        if (dashscopeEndpoint == null || dashscopeEndpoint.isBlank()) {
            dashscopeEndpoint = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";
        }
        if (dashscopeModel == null || dashscopeModel.isBlank()) {
            dashscopeModel = "qwen-plus";
        }

        Config config = store.getConfig();
        config.setMaxCoursesPerTA(maxCourses);
        config.setCvRelativePath(cvRelativePath);
        config.setStorageMode(storageMode);
        config.setDashscopeApiKey(dashscopeApiKey.trim());
        config.setDashscopeEndpoint(dashscopeEndpoint.trim());
        config.setDashscopeModel(dashscopeModel.trim());
        if (currentSemester == null || currentSemester.isBlank()) {
            currentSemester = "2025-2026-S2";
        }
        config.setCurrentSemester(currentSemester.trim());
        config.setApplicationDeadline(applicationDeadline == null ? "" : applicationDeadline.trim());

        synchronized (store) {
            store.updateConfig(config);
            store.saveAll();
        }

        resp.sendRedirect(req.getContextPath() + "/admin/config");
    }
}

