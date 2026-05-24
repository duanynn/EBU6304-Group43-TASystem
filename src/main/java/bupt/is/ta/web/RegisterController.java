package bupt.is.ta.web;

import bupt.is.ta.model.User;
import bupt.is.ta.util.UploadLimits;
import bupt.is.ta.service.CvParsingService;
import bupt.is.ta.service.FileStorageService;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet("/register")
@MultipartConfig(
        maxFileSize = UploadLimits.CV_MAX_BYTES,
        maxRequestSize = UploadLimits.MULTIPART_MAX_REQUEST_BYTES,
        fileSizeThreshold = 1024 * 1024
)
public class RegisterController extends HttpServlet {

    private final UserService userService = new UserService();
    private final FileStorageService fileStorageService = new FileStorageService();
    private final CvParsingService cvParsingService = new CvParsingService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            Object err = session.getAttribute("registerError");
            if (err instanceof String s && !s.isBlank()) {
                req.setAttribute("error", s);
                session.removeAttribute("registerError");
            }
        }
        req.getRequestDispatcher("/register.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            handleRegisterPost(req, resp);
        } catch (IllegalStateException e) {
            if (UploadLimits.isSizeLimitExceeded(e)) {
                req.setAttribute("error", UploadLimits.cvSizeMessage());
                req.getRequestDispatcher("/register.jsp").forward(req, resp);
                return;
            }
            throw e;
        }
    }

    private void handleRegisterPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = req.getParameter("id");
        String password = req.getParameter("password");
        String name = req.getParameter("name");
        String gpaStr = req.getParameter("gpa");
        String skillsStr = req.getParameter("skillTags");
        String availableTime = req.getParameter("availableTime");

        if (id == null || id.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            req.setAttribute("error", "Student ID and password are required");
            req.getRequestDispatcher("/register.jsp").forward(req, resp);
            return;
        }
        id = id.trim();
        if (!id.matches("\\d{10}")) {
            req.setAttribute("error", "Student ID must be 10 digits");
            req.getRequestDispatcher("/register.jsp").forward(req, resp);
            return;
        }
        if (userService.findById(id).isPresent()) {
            req.setAttribute("error", "This student ID is already registered");
            req.getRequestDispatcher("/register.jsp").forward(req, resp);
            return;
        }
        String idCardSuffix = req.getParameter("idCardSuffix");
        if (idCardSuffix == null || !idCardSuffix.trim().matches("\\d{6}")) {
            req.setAttribute("error", "ID card last 6 digits are required (used only for password recovery).");
            req.getRequestDispatcher("/register.jsp").forward(req, resp);
            return;
        }

        User ta = new User();
        ta.setId(id);
        ta.setPassword(password.trim());
        ta.setName(name != null ? name.trim() : id);
        ta.setRole(User.Role.TA);
        ta.setIdCardSuffix(idCardSuffix.trim());
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
        bupt.is.ta.util.JobScheduleUtil.ParseResult availParse = bupt.is.ta.util.JobScheduleUtil.parseSlotRows(
                req.getParameterValues("availDay"),
                req.getParameterValues("availStart"),
                req.getParameterValues("availEnd"));
        if (availParse.isOk()) {
            ta.setAvailableSlots(availParse.getSlots());
            ta.setAvailableTime(bupt.is.ta.util.JobScheduleUtil.formatSummary(availParse.getSlots()));
        } else {
            req.setAttribute("error", availParse.getError());
            req.getRequestDispatcher("/register.jsp").forward(req, resp);
            return;
        }
        boolean uploadedCv = false;
        try {
            Part cvPart = req.getPart("cvFile");
            if (cvPart != null && cvPart.getSize() > 0) {
                if (cvPart.getSize() > UploadLimits.CV_MAX_BYTES) {
                    req.setAttribute("error", UploadLimits.cvSizeMessage());
                    req.getRequestDispatcher("/register.jsp").forward(req, resp);
                    return;
                }
                String submittedName = cvPart.getSubmittedFileName();
                String lower = submittedName == null ? "" : submittedName.toLowerCase();
                if (!(lower.endsWith(".pdf") || lower.endsWith(".doc") || lower.endsWith(".docx"))) {
                    req.setAttribute("error", "Only PDF/DOC/DOCX CV files are supported");
                    req.getRequestDispatcher("/register.jsp").forward(req, resp);
                    return;
                }
                String cvPath = fileStorageService.saveCv(getServletContext(), ta.getId(), cvPart);
                ta.setCvPath(cvPath);
                ta.setProfile(cvParsingService.parseCvFile(java.nio.file.Path.of(cvPath)));
                uploadedCv = true;
            }
        } catch (Exception e) {
            if (UploadLimits.isSizeLimitExceeded(e)) {
                req.setAttribute("error", UploadLimits.cvSizeMessage());
            } else {
                req.setAttribute("error", "CV upload or parsing failed. Please check the file and retry.");
            }
            req.getRequestDispatcher("/register.jsp").forward(req, resp);
            return;
        }

        try {
            userService.save(ta);
        } catch (Exception e) {
            req.setAttribute("error", "Registration failed. Please try again later.");
            req.getRequestDispatcher("/register.jsp").forward(req, resp);
            return;
        }

        HttpSession session = req.getSession(true);
        session.setAttribute("currentUser", ta);
        session.setAttribute("taProfilePromptPending", Boolean.TRUE);
        if (uploadedCv) {
            session.setAttribute("taProfileNeedsConfirm", Boolean.TRUE);
        }
        resp.sendRedirect(req.getContextPath() + "/ta/jobs");
    }
}
