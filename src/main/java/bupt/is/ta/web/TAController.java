package bupt.is.ta.web;

import bupt.is.ta.model.Application;
import bupt.is.ta.model.Job;
import bupt.is.ta.model.User;
import bupt.is.ta.service.ApplicationService;
import bupt.is.ta.service.JobService;
import bupt.is.ta.service.SkillMatchService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

@WebServlet({"/ta/dashboard", "/ta/jobs", "/ta/apply", "/ta/confirmApply", "/ta/applications"})
@MultipartConfig
public class TAController extends HttpServlet {

    private final JobService jobService = new JobService();
    private final ApplicationService applicationService = new ApplicationService();
    private final SkillMatchService skillMatchService = new SkillMatchService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        HttpSession session = req.getSession(false);
        User current = (User) session.getAttribute("currentUser");

        switch (path) {
            case "/ta/dashboard", "/ta/jobs" -> {
                List<Job> jobs = jobService.listOpenJobs();
                req.setAttribute("jobs", jobs);
                req.getRequestDispatcher("/ta/jobBoard.jsp").forward(req, resp);
            }
            case "/ta/applications" -> {
                List<Application> apps = applicationService.listByStudent(current.getId());
                req.setAttribute("applications", apps);
                req.getRequestDispatcher("/ta/myApplications.jsp").forward(req, resp);
            }
            default -> resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        HttpSession session = req.getSession(false);
        User current = (User) session.getAttribute("currentUser");

        if ("/ta/apply".equals(path)) {
            handleApply(req, resp, current);
        } else if ("/ta/confirmApply".equals(path)) {
            handleConfirmApply(req, resp, current);
        } else {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }

    private void handleConfirmApply(HttpServletRequest req, HttpServletResponse resp, User current) throws IOException {
        String jobId = req.getParameter("jobId");
        Job job = jobService.findById(jobId).orElse(null);
        if (job == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Job not found");
            return;
        }
        Application app = new Application();
        app.setStudentId(current.getId());
        app.setJobId(jobId);
        try {
            applicationService.create(app);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        resp.sendRedirect(req.getContextPath() + "/ta/applications");
    }

    private void handleApply(HttpServletRequest req, HttpServletResponse resp, User current)
            throws IOException, ServletException {
        String jobId = req.getParameter("jobId");
        Job job = jobService.findById(jobId).orElse(null);
        if (job == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Job not found");
            return;
        }

        SkillMatchService.MatchResult matchResult =
                skillMatchService.match(job.getRequiredSkills(), current.getSkillTags());

        req.setAttribute("job", job);
        req.setAttribute("match", matchResult);
        req.getRequestDispatcher("/ta/applyConfirm.jsp").forward(req, resp);
    }
}

