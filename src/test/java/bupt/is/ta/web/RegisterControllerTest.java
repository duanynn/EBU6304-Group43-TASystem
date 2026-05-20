package bupt.is.ta.web;

import bupt.is.ta.model.User;
import bupt.is.ta.store.DataStore;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;

import static org.mockito.Mockito.*;

class RegisterControllerTest {

    private final RegisterController controller = new RegisterController();

    @BeforeAll
    static void initStore() throws Exception {
        DataStore.getInstance().init(Files.createTempDirectory("ta-register-controller-test"));
        DataStore.getInstance().upsertUser(user("2021001001", User.Role.TA));
    }

    @Test
    void doGet_forwardsToRegisterPage() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        when(req.getRequestDispatcher("/register.jsp")).thenReturn(dispatcher);

        controller.doGet(req, resp);

        verify(dispatcher).forward(req, resp);
    }

    @Test
    void doPost_rejectsMissingId() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        when(req.getParameter("id")).thenReturn(" ");
        when(req.getParameter("password")).thenReturn("123");
        when(req.getRequestDispatcher("/register.jsp")).thenReturn(dispatcher);

        controller.doPost(req, resp);

        verify(req).setAttribute("error", "Student ID and password are required");
        verify(dispatcher).forward(req, resp);
    }

    @Test
    void doPost_rejectsDuplicateId() throws Exception {
        HttpServletRequest req = baseRequest("2021001001");
        HttpServletResponse resp = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        when(req.getRequestDispatcher("/register.jsp")).thenReturn(dispatcher);

        controller.doPost(req, resp);

        verify(req).setAttribute("error", "This student ID is already registered");
        verify(dispatcher).forward(req, resp);
    }

    @Test
    void doPost_createsStudentAndRedirectsWithoutCv() throws Exception {
        String id = uniqueTenDigitId();
        HttpServletRequest req = baseRequest(id);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        when(req.getParameter("name")).thenReturn("New Student");
        when(req.getParameter("gpa")).thenReturn("3.6");
        when(req.getParameter("skillTags")).thenReturn("Java, SQL");
        when(req.getParameter("availableTime")).thenReturn("Friday");
        when(req.getPart("cvFile")).thenReturn(null);
        when(req.getSession(true)).thenReturn(session);
        when(req.getContextPath()).thenReturn("/ta-recruitment-system");

        controller.doPost(req, resp);

        verify(session).setAttribute(eq("currentUser"), any(User.class));
        verify(session).setAttribute("taProfilePromptPending", Boolean.TRUE);
        verify(resp).sendRedirect("/ta-recruitment-system/ta/jobs");
    }

    private HttpServletRequest baseRequest(String id) {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getParameter("id")).thenReturn(id);
        when(req.getParameter("password")).thenReturn("123456");
        return req;
    }

    private static String uniqueTenDigitId() {
        long suffix = Math.abs(System.nanoTime() % 1_000_000_000L);
        return "8" + String.format("%09d", suffix);
    }

    private static User user(String id, User.Role role) {
        User user = new User();
        user.setId(id);
        user.setPassword("123");
        user.setName(id);
        user.setRole(role);
        return user;
    }
}
