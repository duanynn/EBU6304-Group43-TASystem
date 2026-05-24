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

class ForgotPasswordControllerTest {

    private final ForgotPasswordController controller = new ForgotPasswordController();

    @BeforeAll
    static void initStore() throws Exception {
        DataStore store = DataStore.getInstance();
        store.init(Files.createTempDirectory("ta-forgot-pw-test"));
        User ta = new User();
        ta.setId("2021001999");
        ta.setPassword("oldpass");
        ta.setName("Test TA");
        ta.setRole(User.Role.TA);
        ta.setIdCardSuffix("123456");
        store.upsertUser(ta);
    }

    @Test
    void postWrongSuffixForwardsWithError() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        when(req.getMethod()).thenReturn("POST");
        when(req.getSession(true)).thenReturn(session);
        when(session.getAttribute(CaptchaController.SESSION_ATTR)).thenReturn("ABCDE");
        when(req.getParameter("id")).thenReturn("2021001999");
        when(req.getParameter("idCardSuffix")).thenReturn("000000");
        when(req.getParameter("captcha")).thenReturn("abcde");
        when(req.getParameter("newPassword")).thenReturn("newpass");
        when(req.getParameter("confirmPassword")).thenReturn("newpass");
        when(req.getRequestDispatcher("/forgotPassword.jsp")).thenReturn(dispatcher);

        controller.doPost(req, resp);

        verify(req).setAttribute(eq("error"), anyString());
        verify(dispatcher).forward(req, resp);
        verify(resp, never()).sendRedirect(contains("login.jsp"));
    }
}
