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

class LoginControllerTest {

    private final LoginController controller = new LoginController();

    @BeforeAll
    static void initStore() throws Exception {
        DataStore.getInstance().init(Files.createTempDirectory("ta-login-controller-test"));
        DataStore.getInstance().upsertUser(user("2021001001", "123", User.Role.TA));
        DataStore.getInstance().upsertUser(user("0000000001", "123", User.Role.MO));
        DataStore.getInstance().upsertUser(user("admin", "admin", User.Role.ADMIN));
    }

    @Test
    void doPost_invalidCredentialsForwardsToLogin() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        when(req.getParameter("id")).thenReturn("missing");
        when(req.getParameter("password")).thenReturn("bad");
        when(req.getRequestDispatcher("/login.jsp")).thenReturn(dispatcher);

        controller.doPost(req, resp);

        verify(req).setAttribute("error", "Invalid id or password");
        verify(dispatcher).forward(req, resp);
    }

    @Test
    void doPost_taLoginStoresSessionAndRedirects() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        when(req.getParameter("id")).thenReturn("2021001001");
        when(req.getParameter("password")).thenReturn("123");
        when(req.getSession(true)).thenReturn(session);
        when(req.getContextPath()).thenReturn("/ta-recruitment-system");

        controller.doPost(req, resp);

        verify(session).setAttribute(eq("currentUser"), any(User.class));
        verify(session).setAttribute("taProfilePromptPending", Boolean.TRUE);
        verify(resp).sendRedirect("/ta-recruitment-system/ta/dashboard");
    }

    @Test
    void doGet_invalidatesExistingSessionAndRedirects() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        when(req.getSession(false)).thenReturn(session);
        when(req.getContextPath()).thenReturn("/ta-recruitment-system");

        controller.doGet(req, resp);

        verify(session).invalidate();
        verify(resp).sendRedirect("/ta-recruitment-system/login.jsp");
    }

    private static User user(String id, String password, User.Role role) {
        User user = new User();
        user.setId(id);
        user.setPassword(password);
        user.setRole(role);
        user.setName(id);
        return user;
    }
}
