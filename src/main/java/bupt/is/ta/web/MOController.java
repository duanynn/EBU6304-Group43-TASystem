package bupt.is.ta.web;

import bupt.is.ta.model.Application;
import bupt.is.ta.model.Job;
import bupt.is.ta.model.User;
import bupt.is.ta.model.UserProfile;
import bupt.is.ta.service.ApplicationService;
import bupt.is.ta.service.JobService;
import bupt.is.ta.service.SkillMatchService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@WebServlet({"/mo/dashboard", "/mo/postJob", "/mo/applicants", "/mo/updateStatus", "/mo/cv/view"})
public class MOController extends HttpServlet {

    private final JobService jobService = new JobService();
    private final ApplicationService applicationService = new ApplicationService();
    private final SkillMatchService skillMatchService = new SkillMatchService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        HttpSession session = req.getSession(false);
        User current = (User) session.getAttribute("currentUser");

        switch (path) {
            case "/mo/dashboard" -> {
                List<Job> jobs = jobService.listJobsByMo(current.getId());
                req.setAttribute("jobs", jobs);
                req.getRequestDispatcher("/mo/dashboard.jsp").forward(req, resp);
            }
            case "/mo/postJob" -> {
                req.getRequestDispatcher("/mo/postJob.jsp").forward(req, resp);
            }
            case "/mo/applicants" -> {
                String jobId = req.getParameter("jobId");
                Job job = jobService.findById(jobId).orElse(null);
                if (job == null) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Job not found");
                    return;
                }
                List<Application> apps = applicationService.listByJob(jobId);

                Map<Application, SkillMatchService.MatchResult> matchMap = apps.stream()
                        .collect(Collectors.toMap(
                                a -> a,
                                a -> {
                                    User s = findUserById(req, a.getStudentId());
                                    if (s == null) {
                                        return skillMatchService.match(job.getRequiredSkills(), List.of());
                                    }
                                    return resolveCachedMatch(job, s);
                                }
                        ));

                List<Application> sortedApps = apps.stream()
                        .sorted(Comparator.comparingDouble(
                                (Application a) -> matchMap.get(a).getAiScore()).reversed())
                        .collect(Collectors.toList());

                req.setAttribute("job", job);
                req.setAttribute("applications", sortedApps);
                req.setAttribute("matchMap", matchMap);
                Map<String, User> studentMap = sortedApps.stream()
                        .map(Application::getStudentId)
                        .distinct()
                        .collect(Collectors.toMap(id -> id, id -> findUserById(req, id), (a, b) -> a));
                req.setAttribute("studentMap", studentMap);
                req.getRequestDispatcher("/mo/applicants.jsp").forward(req, resp);
            }
            case "/mo/cv/view" -> handleViewCv(req, resp);
            default -> resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        HttpSession session = req.getSession(false);
        User current = (User) session.getAttribute("currentUser");

        try {
            switch (path) {
                case "/mo/postJob" -> handlePostJob(req, resp, current);
                case "/mo/updateStatus" -> handleUpdateStatus(req, resp);
                default -> resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void handlePostJob(HttpServletRequest req, HttpServletResponse resp, User current) throws Exception {
        String courseName = req.getParameter("courseName");
        int requiredCount = Integer.parseInt(req.getParameter("requiredCount"));
        String[] skillsParam = req.getParameterValues("requiredSkills");
        String requiredWorkTime = req.getParameter("requiredWorkTime");
        List<String> skills = new ArrayList<>();
        if (skillsParam != null) {
            for (String s : skillsParam) {
                for (String part : s.split("[,，\\s]+")) {
                    String t = part.trim();
                    if (!t.isEmpty()) skills.add(t);
                }
            }
        }

        Job job = new Job();
        job.setCourseName(courseName);
        job.setMoId(current.getId());
        job.setRequiredCount(requiredCount);
        job.setRequiredSkills(skills);
        job.setRequiredWorkTime(requiredWorkTime);

        jobService.save(job);
        resp.sendRedirect(req.getContextPath() + "/mo/dashboard");
    }

    private void handleUpdateStatus(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String appId = req.getParameter("applicationId");
        String newStatus = req.getParameter("status");

        Application app = applicationService.findById(appId).orElse(null);
        if (app == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Application not found");
            return;
        }

        if ("ACCEPTED".equalsIgnoreCase(newStatus)) {
            long acceptedCount = applicationService.countAcceptedByStudent(app.getStudentId());
            int maxCourses = bupt.is.ta.store.DataStore.getInstance().getConfig().getMaxCoursesPerTA();
            if (acceptedCount >= maxCourses) {
                req.setAttribute("error", "This student has reached the maximum workload limit. Acceptance was blocked.");
                req.getRequestDispatcher("/error.jsp").forward(req, resp);
                return;
            }
        }

        app.setStatus(Application.Status.valueOf(newStatus.toUpperCase()));
        applicationService.update(app);
        String referer = req.getHeader("Referer");
        if (referer == null || referer.isEmpty()) {
            referer = req.getContextPath() + "/mo/dashboard";
        }
        resp.sendRedirect(referer);
    }

    private User findUserById(HttpServletRequest req, String id) {
        return bupt.is.ta.store.DataStore.getInstance().getUsers().stream()
                .filter(u -> u.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    private SkillMatchService.MatchResult resolveCachedMatch(Job job, User student) {
        if (student == null) {
            return skillMatchService.match(job.getRequiredSkills(), List.of());
        }
        UserProfile profile = student.getProfile();
        String signature = buildJobAdviceSignature(job, student);
        UserProfile.JobAiAdviceCache cache = profile.findJobAiAdviceCache(job.getId(), signature);
        if (cache != null) {
            return new SkillMatchService.MatchResult(
                    job.getRequiredSkills() == null ? List.of() : job.getRequiredSkills(),
                    student.getSkillTags() == null ? List.of() : student.getSkillTags(),
                    List.of(),
                    cache.getAiGaps() == null ? List.of() : cache.getAiGaps(),
                    Math.max(0.0, Math.min(1.0, cache.getAiScore() / 100.0)),
                    cache.getAiScore(),
                    cache.getAiAdvice(),
                    cache.getAiStrengths(),
                    cache.getAiGaps(),
                    cache.getAiFitSummary(),
                    cache.isAiGenerated()
            );
        }
        return skillMatchService.match(job.getRequiredSkills(), student.getSkillTags(), profile.getSummary(), profile.getRawCvText(), false);
    }

    private String buildJobAdviceSignature(Job job, User student) {
        String required = (job.getRequiredSkills() == null ? List.<String>of() : job.getRequiredSkills()).stream()
                .map(s -> s == null ? "" : s.trim().toLowerCase())
                .collect(Collectors.joining("|"));
        String studentSkills = (student.getSkillTags() == null ? List.<String>of() : student.getSkillTags()).stream()
                .map(s -> s == null ? "" : s.trim().toLowerCase())
                .collect(Collectors.joining("|"));
        String parsedAt = student.getProfile().getLastParsedAt() == null ? "" : student.getProfile().getLastParsedAt();
        String summary = student.getProfile().getSummary() == null ? "" : student.getProfile().getSummary();
        return required + "::" + studentSkills + "::" + parsedAt + "::" + summary.hashCode();
    }

    private void handleViewCv(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        String studentId = req.getParameter("studentId");
        String jobId = req.getParameter("jobId");
        User student = findUserById(req, studentId);
        Job job = jobService.findById(jobId).orElse(null);
        if (student == null || job == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid parameters");
            return;
        }
        if (student.getCvPath() == null || student.getCvPath().isBlank()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "This student has not uploaded a CV");
            return;
        }
        SkillMatchService.MatchResult match = resolveCachedMatch(job, student);
        req.setAttribute("student", student);
        req.setAttribute("job", job);
        req.setAttribute("match", match);
        req.getRequestDispatcher("/mo/studentCvView.jsp").forward(req, resp);
    }
}

