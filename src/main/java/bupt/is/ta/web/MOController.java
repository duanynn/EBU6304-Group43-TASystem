package bupt.is.ta.web;

import bupt.is.ta.model.Application;
import bupt.is.ta.model.Job;
import bupt.is.ta.model.User;
import bupt.is.ta.model.UserProfile;
import bupt.is.ta.service.AiAdviceService;
import bupt.is.ta.service.ApplicationService;
import bupt.is.ta.service.JobService;
import bupt.is.ta.service.ScheduleConflictService;
import bupt.is.ta.service.ScheduleFitService;
import bupt.is.ta.service.SkillMatchService;
import bupt.is.ta.service.UserService;
import bupt.is.ta.store.DataStore;
import bupt.is.ta.util.JobAdviceSignatureUtil;
import bupt.is.ta.util.JobScheduleUtil;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@WebServlet({"/mo/home", "/mo/dashboard", "/mo/postJob", "/mo/generateJobDescription", "/mo/applicants", "/mo/updateStatus", "/mo/updateJobStatus", "/mo/cv/view", "/mo/cv/download"})
public class MOController extends HttpServlet {

    private static final Gson GSON = new Gson();

    private final JobService jobService = new JobService();
    private final ApplicationService applicationService = new ApplicationService();
    private final SkillMatchService skillMatchService = new SkillMatchService();
    private final AiAdviceService aiAdviceService = new AiAdviceService();
    private final ScheduleConflictService scheduleConflictService = new ScheduleConflictService();
    private final ScheduleFitService scheduleFitService = new ScheduleFitService();
    private final DataStore store = DataStore.getInstance();
    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        HttpSession session = req.getSession(false);
        User current = (User) session.getAttribute("currentUser");

        switch (path) {
            case "/mo/home" -> {
                List<Job> jobs = jobService.listJobsByMo(current.getId());
                List<String> courses = jobs.stream()
                        .map(Job::getCourseName)
                        .filter(n -> n != null && !n.isBlank())
                        .distinct()
                        .sorted(String.CASE_INSENSITIVE_ORDER)
                        .collect(Collectors.toList());
                req.setAttribute("courseNames", courses);
                Object msg = session.getAttribute("accountMessage");
                if (msg instanceof String s && !s.isBlank()) {
                    req.setAttribute("accountMessage", s);
                    session.removeAttribute("accountMessage");
                }
                req.getRequestDispatcher("/mo/home.jsp").forward(req, resp);
            }
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
                                    User s = findUserById(a.getStudentId());
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

                int maxCourses = store.getConfig().getMaxCoursesPerTA();
                Map<String, Long> acceptedCountByStudent = new HashMap<>();
                for (Application app : sortedApps) {
                    long count = applicationService.countAcceptedByStudent(app.getStudentId());
                    acceptedCountByStudent.put(app.getStudentId(), count);
                }

                req.setAttribute("job", job);
                req.setAttribute("applications", sortedApps);
                req.setAttribute("matchMap", matchMap);
                req.setAttribute("maxCoursesPerTA", maxCourses);
                req.setAttribute("acceptedCountByStudent", acceptedCountByStudent);
                Map<String, User> studentMap = sortedApps.stream()
                        .map(Application::getStudentId)
                        .distinct()
                        .collect(Collectors.toMap(id -> id, this::findUserById, (a, b) -> a));
                req.setAttribute("studentMap", studentMap);
                Map<Application, ScheduleFitService.ScheduleFitResult> scheduleFitMap = new HashMap<>();
                for (Application app : sortedApps) {
                    User s = studentMap.get(app.getStudentId());
                    scheduleFitMap.put(app, scheduleFitService.computeFit(job, s));
                }
                req.setAttribute("scheduleFitMap", scheduleFitMap);
                Object applicantsHint = session.getAttribute("moApplicantsHint");
                if (applicantsHint instanceof String hint && !hint.isBlank()) {
                    req.setAttribute("applicantsHint", hint);
                    session.removeAttribute("moApplicantsHint");
                }
                req.getRequestDispatcher("/mo/applicants.jsp").forward(req, resp);
            }
            case "/mo/cv/view" -> handleViewCv(req, resp);
            case "/mo/cv/download" -> handleDownloadCv(req, resp);
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
                case "/mo/home" -> handleSaveHome(req, resp, current);
                case "/mo/postJob" -> handlePostJob(req, resp, current);
                case "/mo/generateJobDescription" -> handleGenerateJobDescription(req, resp);
                case "/mo/updateStatus" -> handleUpdateStatus(req, resp);
                case "/mo/updateJobStatus" -> handleUpdateJobStatus(req, resp, current);
                default -> resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void handlePostJob(HttpServletRequest req, HttpServletResponse resp, User current) throws Exception {
        String courseName = req.getParameter("courseName");
        int requiredCount = parsePositiveInt(req.getParameter("requiredCount"), 1);
        String[] skillsParam = req.getParameterValues("requiredSkills");
        String description = req.getParameter("description");
        List<String> skills = parseSkills(skillsParam);

        JobScheduleUtil.ParseResult parseResult = JobScheduleUtil.parseSlotRows(
                req.getParameterValues("slotDay"),
                req.getParameterValues("slotStart"),
                req.getParameterValues("slotEnd"));
        if (!parseResult.isOk()) {
            req.setAttribute("scheduleError", parseResult.getError());
            req.setAttribute("courseName", courseName);
            req.setAttribute("requiredCount", requiredCount);
            req.setAttribute("requiredSkills", skillsParam != null && skillsParam.length > 0 ? skillsParam[0] : "");
            req.setAttribute("description", description);
            req.setAttribute("jobType", req.getParameter("jobType"));
            req.getRequestDispatcher("/mo/postJob.jsp").forward(req, resp);
            return;
        }

        Job job = new Job();
        job.setJobType(parseJobType(req.getParameter("jobType")));
        job.setCourseName(courseName);
        job.setMoId(current.getId());
        job.setRequiredCount(requiredCount);
        job.setRequiredSkills(skills);
        job.setScheduleSlots(parseResult.getSlots());
        job.setRequiredWorkTime(JobScheduleUtil.formatSummary(parseResult.getSlots()));
        job.setDescription(description);

        jobService.save(job);
        resp.sendRedirect(req.getContextPath() + "/mo/dashboard");
    }

    private void handleGenerateJobDescription(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String courseName = req.getParameter("courseName");
        int requiredCount = parsePositiveInt(req.getParameter("requiredCount"), 1);
        List<String> skills = parseSkills(req.getParameterValues("requiredSkills"));
        JobScheduleUtil.ParseResult parseResult = JobScheduleUtil.parseSlotRows(
                req.getParameterValues("slotDay"),
                req.getParameterValues("slotStart"),
                req.getParameterValues("slotEnd"));
        String requiredWorkTime = parseResult.isOk()
                ? JobScheduleUtil.formatSummary(parseResult.getSlots())
                : trim(req.getParameter("requiredWorkTime"));
        Job.JobType jobType = parseJobType(req.getParameter("jobType"));

        String description = aiAdviceService.generateJobDescription(courseName, requiredCount, skills, requiredWorkTime, jobType);
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write(GSON.toJson(Map.of("description", description)));
    }

    private Job.JobType parseJobType(String raw) {
        if (raw == null || raw.isBlank()) {
            return Job.JobType.MODULE_TA;
        }
        try {
            return Job.JobType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return Job.JobType.MODULE_TA;
        }
    }

    private List<String> parseSkills(String[] skillsParam) {
        List<String> skills = new ArrayList<>();
        if (skillsParam == null) {
            return skills;
        }
        for (String s : skillsParam) {
            if (s == null) continue;
            for (String part : s.split("[,，\\s]+")) {
                String t = part.trim();
                if (!t.isEmpty()) skills.add(t);
            }
        }
        return skills;
    }

    private int parsePositiveInt(String value, int fallback) {
        try {
            int parsed = Integer.parseInt(value);
            return Math.max(1, parsed);
        } catch (Exception e) {
            return fallback;
        }
    }

    private void handleUpdateJobStatus(HttpServletRequest req, HttpServletResponse resp, User current) throws Exception {
        String jobId = req.getParameter("jobId");
        String openValue = req.getParameter("open");
        Job job = jobService.findById(jobId).orElse(null);
        if (job == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Job not found");
            return;
        }
        if (current == null || !current.getId().equals(job.getMoId())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "You can only update your own jobs");
            return;
        }
        job.setOpen(Boolean.parseBoolean(openValue));
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
            int maxCourses = store.getConfig().getMaxCoursesPerTA();
            if (acceptedCount >= maxCourses) {
                req.setAttribute("error", "This student has reached the maximum workload limit. Acceptance was blocked.");
                req.getRequestDispatcher("/error.jsp").forward(req, resp);
                return;
            }
        }

        Application.Status status = Application.Status.valueOf(newStatus.toUpperCase());
        app.setStatus(status);

        if (status == Application.Status.INTERVIEWING) {
            JobScheduleUtil.ParseResult interviewParse = JobScheduleUtil.parseSlotRows(
                    new String[]{req.getParameter("interviewDay")},
                    new String[]{req.getParameter("interviewStart")},
                    new String[]{req.getParameter("interviewEnd")});
            if (!interviewParse.isOk()) {
                req.setAttribute("error", interviewParse.getError());
                req.getRequestDispatcher("/error.jsp").forward(req, resp);
                return;
            }
            if (!interviewParse.getSlots().isEmpty()) {
                app.setInterviewSlot(interviewParse.getSlots().get(0));
            }
            app.setInterviewLocation(trim(req.getParameter("interviewLocation")));
            app.setInterviewRequiresWrittenTest("on".equalsIgnoreCase(req.getParameter("interviewWrittenTest"))
                    || "true".equalsIgnoreCase(req.getParameter("interviewWrittenTest")));
            app.setInterviewScope(trim(req.getParameter("interviewScope")));
            String interviewMessage = trim(req.getParameter("interviewMessage"));
            app.setInterviewMessage(interviewMessage);
            if (app.getInterviewResponse() != Application.InterviewResponse.ACCEPTED) {
                app.setInterviewResponse(Application.InterviewResponse.PENDING);
                app.setInterviewRespondedAt(null);
            }
            app.setInterviewUpdatedAt(java.time.Instant.now());
        }

        applicationService.update(app);

        if ("ACCEPTED".equalsIgnoreCase(newStatus)) {
            int autoRejected = scheduleConflictService.rejectOverlappingApplications(app);
            if (autoRejected > 0) {
                HttpSession session = req.getSession();
                session.setAttribute("moApplicantsHint",
                        autoRejected + " overlapping application(s) were automatically rejected.");
            }
        }
        String referer = req.getHeader("Referer");
        if (referer == null || referer.isEmpty()) {
            referer = req.getContextPath() + "/mo/dashboard";
        }
        resp.sendRedirect(referer);
    }

    private void handleSaveHome(HttpServletRequest req, HttpServletResponse resp, User current) throws Exception {
        current.setName(trim(req.getParameter("name")));
        current.setCollege(trim(req.getParameter("college")));
        userService.save(current);
        req.getSession().setAttribute("currentUser", current);
        req.getSession().setAttribute("accountMessage", "Profile saved.");
        resp.sendRedirect(req.getContextPath() + "/mo/home");
    }

    private User findUserById(String id) {
        return store.getUsers().stream()
                .filter(u -> u.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    private boolean hasApplicationForJob(String studentId, String jobId) {
        return applicationService.findByStudentAndJob(studentId, jobId).isPresent();
    }

    private SkillMatchService.MatchResult resolveCachedMatch(Job job, User student) {
        if (student == null) {
            return skillMatchService.match(job.getRequiredSkills(), List.of());
        }
        UserProfile profile = student.getProfile();
        String signature = JobAdviceSignatureUtil.build(job, student, profile);
        UserProfile.JobAiAdviceCache cache = profile.findJobAiAdviceCache(job.getId(), signature);
        List<String> required = job.getRequiredSkills() == null ? List.of() : job.getRequiredSkills();
        List<String> studentSkills = student.getSkillTags() == null ? List.of() : student.getSkillTags();
        if (cache != null) {
            SkillMatchService.MatchResult cached = skillMatchService.fromJobAdviceCache(cache, required, studentSkills);
            return skillMatchService.mergeScheduleFit(cached, job, student);
        }
        return skillMatchService.match(required, studentSkills, profile.getSummary(), profile.getRawCvText(), false, job, student);
    }

    private void handleViewCv(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        String studentId = req.getParameter("studentId");
        String jobId = req.getParameter("jobId");
        User student = findUserById(studentId);
        Job job = jobService.findById(jobId).orElse(null);
        if (student == null || job == null || !hasApplicationForJob(studentId, jobId)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid parameters");
            return;
        }
        if (student.getCvPath() == null || student.getCvPath().isBlank()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "This student has not uploaded a CV");
            return;
        }
        Application application = applicationService.findByStudentAndJob(studentId, jobId).orElse(null);
        SkillMatchService.MatchResult match = resolveCachedMatch(job, student);
        req.setAttribute("student", student);
        req.setAttribute("job", job);
        req.setAttribute("match", match);
        req.setAttribute("application", application);
        req.getRequestDispatcher("/mo/studentCvView.jsp").forward(req, resp);
    }

    private void handleDownloadCv(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String studentId = req.getParameter("studentId");
        String jobId = req.getParameter("jobId");
        User student = findUserById(studentId);
        Job job = jobService.findById(jobId).orElse(null);
        if (student == null || job == null || !hasApplicationForJob(studentId, jobId)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid parameters");
            return;
        }
        String cvPath = student.getCvPath();
        if (cvPath == null || cvPath.isBlank()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "This student has not uploaded a CV");
            return;
        }
        Path file = Path.of(cvPath);
        if (!Files.exists(file) || !Files.isRegularFile(file)) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "CV file not found");
            return;
        }
        String fileName = file.getFileName().toString();
        String contentType = Files.probeContentType(file);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        resp.setContentType(contentType);
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + fileName.replace("\"", "") + "\"");
        resp.setContentLengthLong(Files.size(file));
        Files.copy(file, resp.getOutputStream());
        resp.getOutputStream().flush();
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
