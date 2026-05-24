package bupt.is.ta.web;

import bupt.is.ta.model.User;
import bupt.is.ta.service.UserService;
import bupt.is.ta.store.DataStore;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@TestMethodOrder(MethodOrderer.MethodName.class)
class AccountControllerTest {

    private static final String TEST_ID = "2021001888";
    private final AccountController controller = new AccountController();

    @BeforeAll
    static void initStore() throws Exception {
        DataStore store = DataStore.getInstance();
        Path dataDir = Files.createTempDirectory("ta-account-test");
        store.init(dataDir);
    }

    @BeforeEach
    void resetUserPassword() throws Exception {
        User ta = new User();
        ta.setId(TEST_ID);
        ta.setPassword("secret");
        ta.setRole(User.Role.TA);
        new UserService().save(ta);
    }

    @Test
    void changePasswordRejectsMismatchedConfirm() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        User current = new User();
        current.setId(TEST_ID);
        current.setPassword("secret");
        current.setRole(User.Role.TA);

        when(req.getServletPath()).thenReturn("/ta/changePassword");
        when(req.getContextPath()).thenReturn("/ta-recruitment-system");
        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("currentUser")).thenReturn(current);
        when(req.getParameter("userId")).thenReturn(TEST_ID);
        when(req.getParameter("oldPassword")).thenReturn("secret");
        when(req.getParameter("newPassword")).thenReturn("newone");
        when(req.getParameter("confirmPassword")).thenReturn("different");
        when(req.getParameter("returnUrl")).thenReturn("/ta/jobs");

        controller.doPost(req, resp);

        verify(session).setAttribute(eq("changePasswordMessage"), contains("do not match"));
        verify(session).setAttribute(eq("changePasswordMessageType"), eq("error"));
        verify(session).setAttribute(eq("openChangePasswordModal"), eq(Boolean.TRUE));
        verify(resp).sendRedirect("/ta-recruitment-system/ta/jobs");
        verify(session, never()).setAttribute(eq("currentUser"), any());
    }

    @Test
    void changePasswordRejectsWrongOldPassword() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        User current = new User();
        current.setId(TEST_ID);
        current.setPassword("secret");
        current.setRole(User.Role.TA);

        when(req.getServletPath()).thenReturn("/ta/changePassword");
        when(req.getContextPath()).thenReturn("/ta-recruitment-system");
        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("currentUser")).thenReturn(current);
        when(req.getParameter("userId")).thenReturn(TEST_ID);
        when(req.getParameter("oldPassword")).thenReturn("wrong");
        when(req.getParameter("newPassword")).thenReturn("newone");
        when(req.getParameter("confirmPassword")).thenReturn("newone");
        when(req.getParameter("returnUrl")).thenReturn("/ta/jobs");
        when(req.getHeader("Referer")).thenReturn("/ta/profile");

        controller.doPost(req, resp);

        verify(session).setAttribute(eq("changePasswordMessage"), contains("incorrect"));
        verify(session).setAttribute(eq("changePasswordMessageType"), eq("error"));
        verify(session).setAttribute(eq("openChangePasswordModal"), eq(Boolean.TRUE));
        verify(resp).sendRedirect("/ta-recruitment-system/ta/jobs");
        assertEquals("secret", DataStore.getInstance().resolveStoredPassword(TEST_ID));
    }

    @Test
    void changePasswordUpdatesJsonWhenOldPasswordCorrect() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        User current = new User();
        current.setId(TEST_ID);
        current.setPassword("secret");
        current.setRole(User.Role.TA);

        when(req.getServletPath()).thenReturn("/ta/changePassword");
        when(req.getContextPath()).thenReturn("/ta-recruitment-system");
        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("currentUser")).thenReturn(current);
        when(req.getParameter("userId")).thenReturn(TEST_ID);
        when(req.getParameter("oldPassword")).thenReturn("secret");
        when(req.getParameter("newPassword")).thenReturn("newone");
        when(req.getParameter("confirmPassword")).thenReturn("newone");
        when(req.getParameter("returnUrl")).thenReturn("/ta/jobs");

        controller.doPost(req, resp);

        verify(session).setAttribute(eq("accountMessage"), contains("success"));
        assertEquals("newone", DataStore.getInstance().resolveStoredPassword(TEST_ID));
    }

    @Test
    void changePasswordAjaxWrongOldPasswordReturnsJsonWithoutRedirect() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        StringWriter body = new StringWriter();
        User current = new User();
        current.setId(TEST_ID);
        current.setPassword("secret");
        current.setRole(User.Role.TA);

        when(req.getServletPath()).thenReturn("/ta/changePassword");
        when(req.getContextPath()).thenReturn("/ta-recruitment-system");
        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("currentUser")).thenReturn(current);
        when(req.getHeader("X-Requested-With")).thenReturn("XMLHttpRequest");
        when(req.getParameter("userId")).thenReturn(TEST_ID);
        when(req.getParameter("oldPassword")).thenReturn("wrong");
        when(req.getParameter("newPassword")).thenReturn("newone");
        when(req.getParameter("confirmPassword")).thenReturn("newone");
        when(req.getParameter("returnUrl")).thenReturn("/ta/jobs");
        when(req.getParameter("ajax")).thenReturn("1");
        when(resp.getWriter()).thenReturn(new PrintWriter(body));

        controller.doPost(req, resp);

        verify(resp, never()).sendRedirect(anyString());
        verify(session, never()).setAttribute(eq("changePasswordMessage"), any());
        assertTrue(body.toString().contains("\"success\":false"));
        assertTrue(body.toString().contains("incorrect"));
        assertEquals("secret", DataStore.getInstance().resolveStoredPassword(TEST_ID));
    }

    @Test
    void changePasswordGetRedirectsToSafeHome() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        User current = new User();
        current.setRole(User.Role.ADMIN);

        when(req.getContextPath()).thenReturn("/ta-recruitment-system");
        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("currentUser")).thenReturn(current);

        controller.doGet(req, resp);

        verify(resp).sendRedirect("/ta-recruitment-system/admin/overview");
    }
}
