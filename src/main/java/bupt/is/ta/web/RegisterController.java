package bupt.is.ta.web;

import bupt.is.ta.model.User;
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
@MultipartConfig
public class RegisterController extends HttpServlet {

    private final UserService userService = new UserService();
    private final FileStorageService fileStorageService = new FileStorageService();
    private final CvParsingService cvParsingService = new CvParsingService();

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
        ta.setAvailableTime(availableTime);
        boolean uploadedCv = false;
        try {
            Part cvPart = req.getPart("cvFile");
            if (cvPart != null && cvPart.getSize() > 0) {
                if (cvPart.getSize() > 5 * 1024 * 1024L) {
                    req.setAttribute("error", "CV file size must be <= 5MB");
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
            req.setAttribute("error", "CV upload or parsing failed. Please check the file and retry.");
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
