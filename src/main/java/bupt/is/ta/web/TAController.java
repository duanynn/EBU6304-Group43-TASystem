package bupt.is.ta.web;

import bupt.is.ta.model.Application;
import bupt.is.ta.model.Job;
import bupt.is.ta.model.User;
import bupt.is.ta.model.UserProfile;
import bupt.is.ta.service.ApplicationService;
import bupt.is.ta.service.AvatarService;
import bupt.is.ta.service.CvParsingService;
import bupt.is.ta.service.FileStorageService;
import bupt.is.ta.service.JobService;
import bupt.is.ta.service.RecruitmentPolicyService;
import bupt.is.ta.service.ScheduleConflictService;
import bupt.is.ta.service.ScheduleViewService;
import bupt.is.ta.service.SkillMatchService;
import bupt.is.ta.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.Part;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@WebServlet({
        "/ta/dashboard", "/ta/jobs", "/ta/apply", "/ta/confirmApply", "/ta/applications", "/ta/schedule",
        "/ta/uploadCv", "/ta/deleteCv", "/ta/profile", "/ta/reparseCv", "/ta/loginProfileDecision", "/ta/refreshNewJobsAi",
        "/ta/interviewResponse"
})
@MultipartConfig(
        maxFileSize = UploadLimits.CV_MAX_BYTES,
        maxRequestSize = UploadLimits.MULTIPART_MAX_REQUEST_BYTES,
        fileSizeThreshold = 1024 * 1024
)
public class TAController extends HttpServlet {
    public static class JobAdviceView {
        private final Job job;
        private final SkillMatchService.MatchResult match;

        public JobAdviceView(Job job, SkillMatchService.MatchResult match) {
            this.job = job;
            this.match = match;
        }

        public Job getJob() {
            return job;
        }

        public SkillMatchService.MatchResult getMatch() {
            return match;
        }
    }

    private final JobService jobService = new JobService();
    private final ApplicationService applicationService = new ApplicationService();
    private final SkillMatchService skillMatchService = new SkillMatchService();
    private final FileStorageService fileStorageService = new FileStorageService();
    private final CvParsingService cvParsingService = new CvParsingService();
    private final UserService userService = new UserService();
    private final RecruitmentPolicyService recruitmentPolicyService = new RecruitmentPolicyService();
    private final ScheduleConflictService scheduleConflictService = new ScheduleConflictService();
    private final ScheduleViewService scheduleViewService = new ScheduleViewService();
    private final AvatarService avatarService = new AvatarService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        HttpSession session = req.getSession(false);
        User current = (User) session.getAttribute("currentUser");
        if ("/ta/profile".equals(path) && Boolean.TRUE.equals(session.getAttribute("taProfilePromptPending"))) {
            session.removeAttribute("taProfilePromptPending");
            req.setAttribute("showProfilePrompt", true);
        } else if ("1".equals(req.getParameter("showProfilePrompt")) && "/ta/profile".equals(path)) {
            req.setAttribute("showProfilePrompt", true);
        }

        switch (path) {
            case "/ta/dashboard", "/ta/jobs" -> {
                String query = trim(req.getParameter("q"));
                String sort = trim(req.getParameter("sort"));
                Job.JobType typeFilter = parseJobTypeFilter(req.getParameter("type"));
                JobService.JobSearchResult searchResult = jobService.searchOpenJobs(query, typeFilter);
                List<Job> jobs = new ArrayList<>(searchResult.getJobs());
                if (sort.isBlank()) {
                    sort = searchResult.isSearched() ? "relevance" : "newest";
                }
                List<Application> myApps = applicationService.listByStudent(current.getId());
                Map<String, Application.Status> appliedJobStatus = myApps.stream()
                        .collect(Collectors.toMap(Application::getJobId, Application::getStatus, (a, b) -> a));
                Map<String, Application> applicationByJobId = myApps.stream()
                        .collect(Collectors.toMap(Application::getJobId, a -> a, (a, b) -> a));
                long interviewPendingCount = applicationService.countPendingInterviewResponses(current.getId());
                req.setAttribute("appliedJobStatus", appliedJobStatus);
                req.setAttribute("applicationByJobId", applicationByJobId);
                req.setAttribute("interviewPendingCount", interviewPendingCount);
                req.setAttribute("pendingInterviewApps", myApps.stream()
                        .filter(Application::needsInterviewResponse)
                        .toList());
                Object jobBoardHint = session.getAttribute("taJobBoardHint");
                if (jobBoardHint instanceof String) {
                    String hint = (String) jobBoardHint;
                    if (hint != null && !hint.isBlank()) {
                        req.setAttribute("jobBoardHint", hint);
                    }
                    session.removeAttribute("taJobBoardHint");
                }
                List<JobAdviceView> jobAdviceList = buildJobAdviceList(current, false);
                Map<String, JobAdviceView> jobAdviceByJobId = jobAdviceList.stream()
                        .filter(item -> item != null && item.getJob() != null)
                        .collect(Collectors.toMap(item -> item.getJob().getId(), item -> item, (a, b) -> a, LinkedHashMap::new));
                Map<String, Integer> fitScores = new LinkedHashMap<>();
                for (Job job : jobs) {
                    int score = estimateFitScore(job, current);
                    for (JobAdviceView item : jobAdviceList) {
                        if (item == null || item.getJob() == null || item.getMatch() == null) continue;
                        if (job.getId().equals(item.getJob().getId())) {
                            score = (int) Math.round(item.getMatch().getAiScore());
                            break;
                        }
                    }
                    fitScores.put(job.getId(), score);
                }
                sortJobsForBoard(jobs, sort, fitScores, searchResult.getNormalizedScores());
                req.setAttribute("jobs", jobs);
                req.setAttribute("fitScores", fitScores);
                req.setAttribute("jobAdviceByJobId", jobAdviceByJobId);
                req.setAttribute("searchQuery", query);
                req.setAttribute("searchSort", sort);
                req.setAttribute("searchPerformed", searchResult.isSearched());
                req.setAttribute("searchScores", searchResult.getNormalizedScores());
                req.setAttribute("totalOpenJobs", searchResult.getTotalOpenJobs());
                req.setAttribute("resultCount", jobs.size());
                req.setAttribute("triggerBackgroundAi", needsBackgroundAiRefresh(current));
                req.setAttribute("applicationOpen", recruitmentPolicyService.isApplicationOpen());
                req.setAttribute("currentSemester", recruitmentPolicyService.getCurrentSemester());
                req.setAttribute("applicationDeadlineDisplay", recruitmentPolicyService.formatDeadlineForDisplay());
                req.setAttribute("searchJobType", typeFilter == null ? "" : typeFilter.name());
                Set<String> conflictJobIds = jobs.stream()
                        .filter(job -> !appliedJobStatus.containsKey(job.getId()))
                        .filter(job -> !scheduleConflictService.findConflictingApplications(current.getId(), job).isEmpty())
                        .map(Job::getId)
                        .collect(Collectors.toSet());
                req.setAttribute("conflictJobIds", conflictJobIds);
                req.getRequestDispatcher("/ta/jobBoard.jsp").forward(req, resp);
            }
            case "/ta/schedule" -> {
                ScheduleViewService.TimetableView view = scheduleViewService.buildForStudent(current.getId());
                req.setAttribute("timetableView", view);
                long interviewPendingCount = applicationService.countPendingInterviewResponses(current.getId());
                req.setAttribute("interviewPendingCount", interviewPendingCount);
                req.getRequestDispatcher("/ta/schedule.jsp").forward(req, resp);
            }
            case "/ta/applications" -> {
                List<Application> apps = applicationService.listByStudent(current.getId());
                req.setAttribute("applications", apps);
                long interviewPendingCount = applicationService.countPendingInterviewResponses(current.getId());
                req.setAttribute("interviewPendingCount", interviewPendingCount);
                req.getRequestDispatcher("/ta/myApplications.jsp").forward(req, resp);
            }
            case "/ta/profile" -> {
                Object loginHint = session.getAttribute("taLoginPromptHint");
                if (loginHint instanceof String) {
                    String hint = (String) loginHint;
                    if (hint != null && !hint.isBlank()) {
                        req.setAttribute("loginPromptHint", hint);
                    }
                    session.removeAttribute("taLoginPromptHint");
                }
                req.setAttribute("profileNeedsConfirm", Boolean.TRUE.equals(session.getAttribute("taProfileNeedsConfirm")));
                SkillMatchService.MatchResult profileMatch = buildProfileAdvice(current);
                req.setAttribute("profileMatch", profileMatch);
                req.setAttribute("profileInitialized", isProfileInitialized(current));
                req.setAttribute("profileCompletion", calculateProfileCompletion(current));
                req.setAttribute("avatarUrl", avatarService.resolveDisplayUrl(current));
                req.setAttribute("presetAvatars", avatarService.listPresetKeys(getServletContext()));
                req.getRequestDispatcher("/ta/profile.jsp").forward(req, resp);
            }
            default -> resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void sortJobsForBoard(List<Job> jobs,
                                  String sort,
                                  Map<String, Integer> fitScores,
                                  Map<String, Integer> searchScores) {
        if (jobs == null || jobs.size() < 2) {
            return;
        }
        if ("fit".equalsIgnoreCase(sort)) {
            jobs.sort(Comparator
                    .comparingInt((Job job) -> fitScores.getOrDefault(job.getId(), 0)).reversed()
                    .thenComparing(Comparator.comparing(TAController::createdAtOrEpoch).reversed()));
        } else if ("newest".equalsIgnoreCase(sort)) {
            jobs.sort(Comparator.comparing(TAController::createdAtOrEpoch).reversed());
        } else if ("relevance".equalsIgnoreCase(sort)) {
            jobs.sort(Comparator
                    .comparingInt((Job job) -> searchScores.getOrDefault(job.getId(), 0)).reversed()
                    .thenComparing(Comparator.comparing(TAController::createdAtOrEpoch).reversed()));
        }
    }

    private static Instant createdAtOrEpoch(Job job) {
        return job == null || job.getCreatedAt() == null ? Instant.EPOCH : job.getCreatedAt();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            handlePost(req, resp);
        } catch (IllegalStateException e) {
            if (UploadLimits.isSizeLimitExceeded(e)) {
                flashProfile(req.getSession(false), UploadLimits.requestTooLargeMessage(), "error");
                resp.sendRedirect(req.getContextPath() + "/ta/profile");
                return;
            }
            throw e;
        }
    }

    private void handlePost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        HttpSession session = req.getSession(false);
        User current = (User) session.getAttribute("currentUser");

        if ("/ta/apply".equals(path)) {
            handleApply(req, resp, current);
        } else if ("/ta/confirmApply".equals(path)) {
            handleConfirmApply(req, resp, current);
        } else if ("/ta/uploadCv".equals(path)) {
            handleUploadCv(req, resp, current);
        } else if ("/ta/deleteCv".equals(path)) {
            handleDeleteCv(req, resp, current);
        } else if ("/ta/profile".equals(path)) {
            handleSaveProfile(req, resp, current);
        } else if ("/ta/reparseCv".equals(path)) {
            handleReparseCv(req, resp, current);
        } else if ("/ta/loginProfileDecision".equals(path)) {
            handleLoginProfileDecision(req, resp, current);
        } else if ("/ta/refreshNewJobsAi".equals(path)) {
            handleRefreshNewJobsAi(req, resp, current);
        } else if ("/ta/interviewResponse".equals(path)) {
            handleInterviewResponse(req, resp, current);
        } else {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }

    private void handleConfirmApply(HttpServletRequest req, HttpServletResponse resp, User current) throws IOException {
        if (!recruitmentPolicyService.isApplicationOpen()) {
            req.getSession().setAttribute("taJobBoardHint",
                    "The application period has closed. New applications are no longer accepted.");
            resp.sendRedirect(req.getContextPath() + "/ta/jobs");
            return;
        }
        String jobId = req.getParameter("jobId");
        Job job = jobService.findById(jobId).orElse(null);
        if (job == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Job not found");
            return;
        }
        Application existing = applicationService.findByStudentAndJob(current.getId(), jobId).orElse(null);
        if (existing != null) {
            req.getSession().setAttribute("taJobBoardHint",
                    "You have already applied for this job (status: " + existing.getStatus() + "). Duplicate application is not allowed.");
            resp.sendRedirect(req.getContextPath() + "/ta/jobs");
            return;
        }
        List<Application> conflicts = scheduleConflictService.findConflictingApplications(current.getId(), job);
        if (!conflicts.isEmpty()) {
            req.getSession().setAttribute("taJobBoardHint",
                    scheduleConflictService.conflictMessage(job, conflicts));
            resp.sendRedirect(req.getContextPath() + "/ta/jobs");
            return;
        }
        Application app = new Application();
        app.setStudentId(current.getId());
        app.setJobId(jobId);
        String note = trim(req.getParameter("note"));
        if (note.length() > 500) {
            note = note.substring(0, 500);
        }
        app.setNote(note);
        try {
            applicationService.create(app);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        resp.sendRedirect(req.getContextPath() + "/ta/applications");
    }

    private void handleApply(HttpServletRequest req, HttpServletResponse resp, User current)
            throws IOException, ServletException {
        if (!recruitmentPolicyService.isApplicationOpen()) {
            req.getSession().setAttribute("taJobBoardHint",
                    "The application period has closed. New applications are no longer accepted.");
            resp.sendRedirect(req.getContextPath() + "/ta/jobs");
            return;
        }
        String jobId = req.getParameter("jobId");
        Job job = jobService.findById(jobId).orElse(null);
        if (job == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Job not found");
            return;
        }
        Application existing = applicationService.findByStudentAndJob(current.getId(), jobId).orElse(null);
        if (existing != null) {
            req.getSession().setAttribute("taJobBoardHint",
                    "You have already applied for this job (status: " + existing.getStatus() + "). Duplicate application is not allowed.");
            resp.sendRedirect(req.getContextPath() + "/ta/jobs");
            return;
        }
        List<Application> scheduleConflicts = scheduleConflictService.findConflictingApplications(current.getId(), job);
        if (!scheduleConflicts.isEmpty()) {
            req.getSession().setAttribute("taJobBoardHint",
                    scheduleConflictService.conflictMessage(job, scheduleConflicts));
            resp.sendRedirect(req.getContextPath() + "/ta/jobs");
            return;
        }

        List<String> required = job.getRequiredSkills() == null ? List.of() : job.getRequiredSkills();
        List<String> studentSkills = current.getSkillTags() == null ? List.of() : current.getSkillTags();
        UserProfile profile = current.getProfile();
        String signature = JobAdviceSignatureUtil.build(job, current, profile);
        UserProfile.JobAiAdviceCache cache = profile.findJobAiAdviceCache(job.getId(), signature);
        JobScheduleUtil.materializeAvailabilitySlots(current);
        JobScheduleUtil.materializeJobScheduleSlots(job);

        SkillMatchService.MatchResult matchResult;
        if (isJobCacheValid(cache)) {
            matchResult = skillMatchService.fromJobAdviceCache(cache, required, studentSkills);
            matchResult = skillMatchService.mergeScheduleFit(matchResult, job, current);
        } else {
            boolean allowAi = hasUploadedCv(current) && hasAiEvaluation(current);
            matchResult = skillMatchService.match(
                    required,
                    studentSkills,
                    profile.getSummary(),
                    profile.getRawCvText(),
                    allowAi,
                    job,
                    current
            );
        }

        req.setAttribute("job", job);
        req.setAttribute("match", matchResult);
        req.setAttribute("applicationOpen", recruitmentPolicyService.isApplicationOpen());
        req.setAttribute("scheduleDisplay", JobScheduleUtil.displayWorkTime(job));
        req.setAttribute("hasStructuredSchedule", JobScheduleUtil.hasStructuredSlots(job));
        req.getRequestDispatcher("/ta/applyConfirm.jsp").forward(req, resp);
    }

    private Job.JobType parseJobTypeFilter(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Job.JobType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private void handleUploadCv(HttpServletRequest req, HttpServletResponse resp, User current) throws IOException {
        try {
            Part cvPart = req.getPart("cvFile");
            if (cvPart == null || cvPart.getSize() <= 0) {
                req.getSession().setAttribute("taLoginPromptHint", "Please upload a CV file before parsing.");
                resp.sendRedirect(req.getContextPath() + "/ta/profile");
                return;
            }
            if (cvPart.getSize() > UploadLimits.CV_MAX_BYTES) {
                flashProfile(req.getSession(false), UploadLimits.cvSizeMessage(), "error");
                resp.sendRedirect(req.getContextPath() + "/ta/profile");
                return;
            }
            String submittedName = cvPart.getSubmittedFileName();
            String lower = submittedName == null ? "" : submittedName.toLowerCase();
            if (!(lower.endsWith(".pdf") || lower.endsWith(".doc") || lower.endsWith(".docx"))) {
                flashProfile(req.getSession(false), "Only PDF, DOC, and DOCX files are supported.", "error");
                resp.sendRedirect(req.getContextPath() + "/ta/profile");
                return;
            }

            String cvPath = fileStorageService.saveCv(getServletContext(), current.getId(), cvPart);
            current.setCvPath(cvPath);
            mergeProfileFromCv(current, Path.of(cvPath));
            userService.save(current);
            req.getSession().setAttribute("currentUser", current);
            req.getSession().setAttribute("taProfileNeedsConfirm", Boolean.TRUE);
            flashProfile(req.getSession(false), "CV uploaded and parsed successfully.", "success");
            resp.sendRedirect(req.getContextPath() + "/ta/profile");
        } catch (Exception e) {
            if (UploadLimits.isSizeLimitExceeded(e)) {
                flashProfile(req.getSession(false), UploadLimits.cvSizeMessage(), "error");
                resp.sendRedirect(req.getContextPath() + "/ta/profile");
                return;
            }
            flashProfile(req.getSession(false), "CV upload failed. Please check the file and try again.", "error");
            resp.sendRedirect(req.getContextPath() + "/ta/profile");
        }
    }

    private void handleDeleteCv(HttpServletRequest req, HttpServletResponse resp, User current) throws IOException {
        try {
            fileStorageService.deleteCv(current.getCvPath());
            current.setCvPath(null);
            current.getProfile().setRawCvText("");
            current.getProfile().setSummary("");
            current.getProfile().setEducation("");
            current.getProfile().setProjects("");
            current.getProfile().setCertificates("");
            current.getProfile().setExtractedSkills(List.of());
            current.getProfile().setJobAiAdviceCaches(List.of());
            current.getProfile().setLastAiAdvice("");
            current.getProfile().setLastAiFitSummary("");
            current.getProfile().setLastAiGaps(List.of());
            current.getProfile().setLastAiStrengths(List.of());
            current.getProfile().setLastAiAdviceTime(0);
            current.getProfile().setLastAiScore(-1);
            userService.save(current);
            req.getSession().setAttribute("currentUser", current);
            req.getSession().removeAttribute("taProfileNeedsConfirm");
            resp.sendRedirect(req.getContextPath() + "/ta/profile");
        } catch (Exception e) {
            throw new IOException("Failed to delete CV", e);
        }
    }

    private void handleInterviewResponse(HttpServletRequest req, HttpServletResponse resp, User current) throws IOException {
        try {
            String appId = trim(req.getParameter("applicationId"));
            String action = trim(req.getParameter("action"));
            Application app = applicationService.findById(appId).orElse(null);
            if (app == null || !current.getId().equals(app.getStudentId())) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Application not found");
                return;
            }
            if (!app.needsInterviewResponse()) {
                req.getSession().setAttribute("taJobBoardHint", "This interview invitation is no longer awaiting your response.");
                resp.sendRedirect(req.getContextPath() + "/ta/applications");
                return;
            }
            if ("accept".equalsIgnoreCase(action)) {
                app.setInterviewResponse(Application.InterviewResponse.ACCEPTED);
                app.setInterviewRespondedAt(Instant.now());
            } else if ("decline".equalsIgnoreCase(action)) {
                app.setInterviewResponse(Application.InterviewResponse.DECLINED);
                app.setInterviewRespondedAt(Instant.now());
                app.setStatus(Application.Status.REJECTED);
            } else {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action");
                return;
            }
            applicationService.update(app);
            resp.sendRedirect(req.getContextPath() + "/ta/applications");
        } catch (Exception e) {
            throw new IOException("Failed to save interview response", e);
        }
    }

    private void handleSaveProfile(HttpServletRequest req, HttpServletResponse resp, User current) throws IOException {
        try {
            current.setName(trim(req.getParameter("name")));
            String gpaValue = trim(req.getParameter("gpa"));
            if (!gpaValue.isBlank()) {
                current.setGpa(Double.valueOf(gpaValue));
            }

            String skillText = trim(req.getParameter("skillTags"));
            List<String> skills = Arrays.stream(skillText.split("[,，\\s]+"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .distinct()
                    .collect(Collectors.toList());
            current.setSkillTags(skills);
            JobScheduleUtil.ParseResult availParse = JobScheduleUtil.parseSlotRows(
                    req.getParameterValues("availDay"),
                    req.getParameterValues("availStart"),
                    req.getParameterValues("availEnd"));
            if (availParse.isOk()) {
                current.setAvailableSlots(availParse.getSlots());
                current.setAvailableTime(JobScheduleUtil.formatSummary(availParse.getSlots()));
            } else {
                current.setAvailableTime(trim(req.getParameter("availableTime")));
            }

            current.getProfile().setSummary(trim(req.getParameter("summary")));
            current.getProfile().setEducation(trim(req.getParameter("education")));
            current.getProfile().setProjects(trim(req.getParameter("projects")));
            String awards = trim(req.getParameter("awards"));
            if (awards.isBlank()) {
                awards = trim(req.getParameter("certificates"));
            }
            current.getProfile().setCertificates(awards);
            precomputeAiForCurrentTa(current);
            userService.save(current);
            req.getSession().setAttribute("currentUser", current);
            req.getSession().removeAttribute("taProfileNeedsConfirm");
            flashProfile(req.getSession(false), "Profile saved successfully.", "success");
            resp.sendRedirect(req.getContextPath() + "/ta/profile");
        } catch (Exception e) {
            if (UploadLimits.isSizeLimitExceeded(e)) {
                flashProfile(req.getSession(false), UploadLimits.requestTooLargeMessage(), "error");
                resp.sendRedirect(req.getContextPath() + "/ta/profile");
                return;
            }
            flashProfile(req.getSession(false), "Could not save profile. Please try again.", "error");
            resp.sendRedirect(req.getContextPath() + "/ta/profile");
        }
    }

    private static void flashProfile(HttpSession session, String message, String type) {
        if (session == null) {
            return;
        }
        session.setAttribute("taProfileMessage", message);
        session.setAttribute("taProfileMessageType", type);
    }

    private void handleReparseCv(HttpServletRequest req, HttpServletResponse resp, User current) throws IOException {
        try {
            if (current.getCvPath() == null || current.getCvPath().isBlank()) {
                flashProfile(req.getSession(false), "Please upload a CV first.", "error");
                resp.sendRedirect(req.getContextPath() + "/ta/profile");
                return;
            }
            mergeProfileFromCv(current, Path.of(current.getCvPath()));
            userService.save(current);
            req.getSession().setAttribute("currentUser", current);
            req.getSession().setAttribute("taProfileNeedsConfirm", Boolean.TRUE);
            resp.sendRedirect(req.getContextPath() + "/ta/profile");
        } catch (Exception e) {
            throw new IOException("Failed to re-parse CV", e);
        }
    }

    private void mergeProfileFromCv(User current, Path cvPath) throws IOException {
        var parsed = cvParsingService.parseCvFile(cvPath);
        current.setProfile(parsed);
        String extractedName = parsed.getExtractedName();
        if (extractedName != null && !extractedName.isBlank()) {
            current.setName(extractedName);
        }
        if (parsed.getExtractedSkills() != null && !parsed.getExtractedSkills().isEmpty()) {
            Set<String> merged = new java.util.LinkedHashSet<>();
            if (current.getSkillTags() != null) merged.addAll(current.getSkillTags());
            merged.addAll(parsed.getExtractedSkills());
            current.setSkillTags(merged.stream().toList());
        }
    }

    private List<JobAdviceView> buildJobAdviceList(User current, boolean allowAiRefresh) {
        List<JobAdviceView> result = new ArrayList<>();
        List<Job> jobs = jobService.listOpenJobs();
        if (jobs == null || jobs.isEmpty()) {
            return result;
        }
        UserProfile profile = current.getProfile();
        List<String> studentSkills = current.getSkillTags() == null ? List.of() : current.getSkillTags();
        String summary = profile.getSummary() == null ? "" : profile.getSummary();
        String rawCv = profile.getRawCvText() == null ? "" : profile.getRawCvText();
        boolean canRefreshWithAi = allowAiRefresh && hasUploadedCv(current) && hasAiEvaluation(current);
        boolean cacheUpdated = false;
        JobScheduleUtil.materializeAvailabilitySlots(current);
        if (profile.getJobAiAdviceCaches() == null) {
            profile.setJobAiAdviceCaches(new ArrayList<>());
        }
        for (Job job : jobs) {
            if (job == null) continue;
            List<String> required = job.getRequiredSkills() == null ? List.of() : job.getRequiredSkills();
            String signature = JobAdviceSignatureUtil.build(job, current, profile);
            SkillMatchService.MatchResult match;
            UserProfile.JobAiAdviceCache cache = profile.findJobAiAdviceCache(job.getId(), signature);
            JobScheduleUtil.materializeJobScheduleSlots(job);
            if (isJobCacheValid(cache)) {
                match = skillMatchService.fromJobAdviceCache(cache, required, studentSkills);
                match = skillMatchService.mergeScheduleFit(match, job, current);
            } else {
                if (canRefreshWithAi) {
                    match = skillMatchService.match(required, studentSkills, summary, rawCv, true, job, current);
                    UserProfile.JobAiAdviceCache newCache = new UserProfile.JobAiAdviceCache();
                    newCache.setJobId(job.getId());
                    newCache.setSignature(signature);
                    newCache.setAiScore(match.getAiScore());
                    newCache.setAiAdvice(match.getAiAdvice());
                    newCache.setAiStrengths(match.getAiStrengths());
                    newCache.setAiGaps(match.getAiGaps());
                    newCache.setAiFitSummary(match.getAiFitSummary());
                    newCache.setAiGenerated(match.isAiGenerated());
                    newCache.setCachedAt(System.currentTimeMillis());
                    profile.getJobAiAdviceCaches().removeIf(item -> item != null && job.getId().equals(item.getJobId()));
                    profile.getJobAiAdviceCaches().add(newCache);
                    cacheUpdated = true;
                } else {
                    match = skillMatchService.match(required, studentSkills, summary, rawCv, false, job, current);
                }
            }
            result.add(new JobAdviceView(job, match));
        }
        if (cacheUpdated) {
            try {
                userService.save(current);
            } catch (Exception ignored) {
            }
        }
        return result;
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private SkillMatchService.MatchResult buildProfileAdvice(User current) {
        UserProfile profile = current.getProfile();
        List<String> extracted = profile.getExtractedSkills() == null
                ? List.of()
                : profile.getExtractedSkills();
        List<String> skillTags = current.getSkillTags() == null
                ? List.of()
                : current.getSkillTags();
        if (profile.isAiCacheValid()) {
            return new SkillMatchService.MatchResult(
                    extracted,
                    skillTags,
                    List.of(),
                    List.of(),
                    extracted.isEmpty() ? 0 : 1.0,
                    profile.getLastAiScore(),
                    profile.getLastAiAdvice(),
                    profile.getLastAiStrengths(),
                    profile.getLastAiGaps(),
                    profile.getLastAiFitSummary(),
                    true
            );
        }
        SkillMatchService.MatchResult result = skillMatchService.match(
                extracted,
                skillTags,
                profile.getSummary(),
                profile.getRawCvText(),
                false
        );
        // When AI refresh is not triggered, return display-only result without writing cache.
        return result;
    }

    private int estimateFitScore(Job job, User current) {
        List<String> required = job.getRequiredSkills() == null ? List.of() : job.getRequiredSkills();
        if (required.isEmpty()) return 100;
        Set<String> req = required.stream().map(s -> s == null ? "" : s.trim().toLowerCase()).filter(s -> !s.isEmpty()).collect(Collectors.toSet());
        if (req.isEmpty()) return 100;
        Set<String> have = (current.getSkillTags() == null ? List.<String>of() : current.getSkillTags())
                .stream().map(s -> s == null ? "" : s.trim().toLowerCase()).filter(s -> !s.isEmpty()).collect(Collectors.toSet());
        long matched = req.stream().filter(have::contains).count();
        double ratio = (double) matched / req.size();
        double gpaBoost = Math.min(Math.max(current.getGpa() == null ? 0.0 : current.getGpa(), 0.0), 4.0) / 4.0 * 0.15;
        int score = (int) Math.round(Math.min(1.0, ratio * 0.85 + gpaBoost) * 100.0);
        return Math.max(0, Math.min(100, score));
    }

    private boolean hasUploadedCv(User current) {
        if (current == null) return false;
        String cvPath = current.getCvPath();
        if (cvPath == null || cvPath.isBlank()) return false;
        String rawCv = current.getProfile() == null ? "" : current.getProfile().getRawCvText();
        return rawCv != null && !rawCv.isBlank();
    }

    private boolean isJobCacheValid(UserProfile.JobAiAdviceCache cache) {
        if (cache == null) return false;
        if (cache.getCachedAt() <= 0) return false;
        if (cache.getAiAdvice() == null || cache.getAiAdvice().isBlank()) return false;
        return (System.currentTimeMillis() - cache.getCachedAt()) < 30 * 60 * 1000L;
    }

    private void handleLoginProfileDecision(HttpServletRequest req, HttpServletResponse resp, User current) throws IOException {
        String decision = trim(req.getParameter("decision"));
        HttpSession session = req.getSession(false);
        if ("ai".equalsIgnoreCase(decision)) {
            if (current == null || !hasUploadedCv(current)) {
                if (session != null) {
                    session.setAttribute("taLoginPromptHint", "No CV uploaded yet. Please complete profile or upload a CV first.");
                }
                resp.sendRedirect(req.getContextPath() + "/ta/profile");
                return;
            }
            if (session != null) {
                session.setAttribute("taManualAiRefresh", Boolean.TRUE);
                session.setAttribute("taLoginPromptHint", "AI refresh started. Please wait for updated results.");
            }
        }
        resp.sendRedirect(req.getContextPath() + "/ta/profile");
    }

    private boolean hasAiEvaluation(User current) {
        if (current == null || current.getProfile() == null) return false;
        UserProfile profile = current.getProfile();
        boolean profileEvaluated = profile.getLastAiAdviceTime() > 0
                && profile.getLastAiAdvice() != null
                && !profile.getLastAiAdvice().isBlank();
        boolean jobEvaluated = profile.getJobAiAdviceCaches() != null && !profile.getJobAiAdviceCaches().isEmpty();
        return profileEvaluated || jobEvaluated;
    }

    private void precomputeAiForCurrentTa(User current) {
        if (current == null) return;
        UserProfile profile = current.getProfile();
        List<String> extracted = profile.getExtractedSkills() == null ? List.of() : profile.getExtractedSkills();
        List<String> skillTags = current.getSkillTags() == null ? List.of() : current.getSkillTags();
        boolean allowAi = hasUploadedCv(current);
        SkillMatchService.MatchResult profileResult = skillMatchService.match(
                extracted, skillTags, profile.getSummary(), profile.getRawCvText(), allowAi
        );
        profile.setLastAiScore(profileResult.getAiScore());
        profile.setLastAiAdvice(profileResult.getAiAdvice());
        profile.setLastAiStrengths(profileResult.getAiStrengths());
        profile.setLastAiGaps(profileResult.getAiGaps());
        profile.setLastAiFitSummary(profileResult.getAiFitSummary());
        profile.setLastAiAdviceTime(System.currentTimeMillis());

        List<Job> jobs = jobService.listOpenJobs();
        List<UserProfile.JobAiAdviceCache> caches = new ArrayList<>();
        for (Job job : jobs) {
            if (job == null) continue;
            List<String> required = job.getRequiredSkills() == null ? List.of() : job.getRequiredSkills();
            SkillMatchService.MatchResult match = skillMatchService.match(
                    required, skillTags, profile.getSummary(), profile.getRawCvText(), allowAi, job, current
            );
            UserProfile.JobAiAdviceCache cache = new UserProfile.JobAiAdviceCache();
            cache.setJobId(job.getId());
            cache.setSignature(JobAdviceSignatureUtil.build(job, current, profile));
            cache.setAiScore(match.getAiScore());
            cache.setAiAdvice(match.getAiAdvice());
            cache.setAiStrengths(match.getAiStrengths());
            cache.setAiGaps(match.getAiGaps());
            cache.setAiFitSummary(match.getAiFitSummary());
            cache.setAiGenerated(match.isAiGenerated());
            cache.setCachedAt(System.currentTimeMillis());
            caches.add(cache);
        }
        profile.setJobAiAdviceCaches(caches);
    }

    private boolean isProfileInitialized(User current) {
        if (current == null) return false;
        UserProfile profile = current.getProfile();
        return current.getGpa() != null
                || (current.getSkillTags() != null && !current.getSkillTags().isEmpty())
                || !trim(profile.getSummary()).isBlank()
                || !trim(profile.getEducation()).isBlank()
                || !trim(profile.getProjects()).isBlank()
                || !trim(profile.getCertificates()).isBlank()
                || !trim(current.getCvPath()).isBlank();
    }

    private int calculateProfileCompletion(User current) {
        if (current == null) return 0;
        UserProfile profile = current.getProfile();
        int total = 9;
        int done = 0;
        if (!trim(current.getName()).isBlank()) done++;
        if (current.getGpa() != null) done++;
        if (current.getSkillTags() != null && !current.getSkillTags().isEmpty()) done++;
        if (!trim(current.getAvailableTime()).isBlank()) done++;
        if (!trim(profile.getSummary()).isBlank()) done++;
        if (!trim(profile.getEducation()).isBlank()) done++;
        if (!trim(profile.getProjects()).isBlank()) done++;
        if (!trim(profile.getCertificates()).isBlank()) done++;
        if (!trim(current.getCvPath()).isBlank()) done++;
        return (int) Math.round(done * 100.0 / total);
    }

    private boolean hasPendingNewJobAnalysis(User current) {
        if (current == null) return false;
        if (!hasUploadedCv(current) || !hasAiEvaluation(current)) return false;
        UserProfile profile = current.getProfile();
        List<Job> jobs = jobService.listOpenJobs();
        if (jobs == null || jobs.isEmpty()) return false;
        for (Job job : jobs) {
            if (job == null) continue;
            String signature = JobAdviceSignatureUtil.build(job, current, profile);
            UserProfile.JobAiAdviceCache cache = profile.findJobAiAdviceCache(job.getId(), signature);
            if (!isJobCacheValid(cache)) {
                return true;
            }
        }
        return false;
    }

    private void handleRefreshNewJobsAi(HttpServletRequest req, HttpServletResponse resp, User current) throws IOException {
        HttpSession session = req.getSession(false);
        resp.setContentType("application/json;charset=UTF-8");
        if (session != null && Boolean.TRUE.equals(session.getAttribute("taAiRefreshInProgress"))) {
            resp.getWriter().write("{\"updated\":false,\"inProgress\":true}");
            return;
        }
        boolean updated = false;
        if (current != null && hasUploadedCv(current)) {
            if (session != null) {
                session.setAttribute("taAiRefreshInProgress", Boolean.TRUE);
            }
            try {
                boolean needFullCompute = !hasAiEvaluation(current);
                if (needFullCompute) {
                    precomputeAiForCurrentTa(current);
                    updated = true;
                } else {
                    updated = incrementalRefreshNewJobs(current);
                }
                if (updated) {
                    try {
                        userService.save(current);
                        if (session != null) {
                            session.setAttribute("currentUser", current);
                            session.removeAttribute("taProfileNeedsConfirm");
                        }
                    } catch (Exception ignored) {
                    }
                }
            } finally {
                if (session != null) {
                    session.removeAttribute("taAiRefreshInProgress");
                }
            }
        }
        resp.getWriter().write("{\"updated\":" + updated + ",\"inProgress\":false}");
    }

    private boolean needsBackgroundAiRefresh(User current) {
        if (current == null) {
            return false;
        }
        if (!hasUploadedCv(current)) {
            return false;
        }
        return !hasAiEvaluation(current) || hasPendingNewJobAnalysis(current);
    }

    private boolean incrementalRefreshNewJobs(User current) {
        UserProfile profile = current.getProfile();
        List<String> skillTags = current.getSkillTags() == null ? List.of() : current.getSkillTags();
        List<Job> jobs = jobService.listOpenJobs();
        if (jobs == null || jobs.isEmpty()) return false;
        if (profile.getJobAiAdviceCaches() == null) {
            profile.setJobAiAdviceCaches(new ArrayList<>());
        }
        boolean anyUpdated = false;
        for (Job job : jobs) {
            if (job == null) continue;
            String signature = JobAdviceSignatureUtil.build(job, current, profile);
            UserProfile.JobAiAdviceCache existing = profile.findJobAiAdviceCache(job.getId(), signature);
            if (isJobCacheValid(existing)) continue;
            List<String> required = job.getRequiredSkills() == null ? List.of() : job.getRequiredSkills();
            SkillMatchService.MatchResult match = skillMatchService.match(
                    required, skillTags, profile.getSummary(), profile.getRawCvText(), true, job, current
            );
            UserProfile.JobAiAdviceCache cache = new UserProfile.JobAiAdviceCache();
            cache.setJobId(job.getId());
            cache.setSignature(signature);
            cache.setAiScore(match.getAiScore());
            cache.setAiAdvice(match.getAiAdvice());
            cache.setAiStrengths(match.getAiStrengths());
            cache.setAiGaps(match.getAiGaps());
            cache.setAiFitSummary(match.getAiFitSummary());
            cache.setAiGenerated(match.isAiGenerated());
            cache.setCachedAt(System.currentTimeMillis());
            profile.getJobAiAdviceCaches().removeIf(item -> item != null && job.getId().equals(item.getJobId()));
            profile.getJobAiAdviceCaches().add(cache);
            anyUpdated = true;
        }
        return anyUpdated;
    }
}

