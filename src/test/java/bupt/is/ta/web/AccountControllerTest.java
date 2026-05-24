package bupt.is.ta.web;

import bupt.is.ta.model.User;
import bupt.is.ta.store.DataStore;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;

import static org.mockito.Mockito.*;

class AccountControllerTest {

    private final AccountController controller = new AccountController();

    @BeforeAll
    static void initStore() throws Exception {
        DataStore store = DataStore.getInstance();
        store.init(Files.createTempDirectory("ta-account-test"));
        User ta = new User();
        ta.setId("2021001888");
        ta.setPassword("secret");
        ta.setRole(User.Role.TA);
        store.upsertUser(ta);
    }

    @Test
    void changePasswordRejectsWrongOldPassword() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        User current = new User();
        current.setId("2021001888");
        current.setPassword("secret");
        current.setRole(User.Role.TA);

        when(req.getServletPath()).thenReturn("/ta/changePassword");
        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("currentUser")).thenReturn(current);
        when(req.getParameter("oldPassword")).thenReturn("wrong");
        when(req.getParameter("newPassword")).thenReturn("newone");
        when(req.getParameter("confirmPassword")).thenReturn("newone");
        when(req.getHeader("Referer")).thenReturn("/ta/profile");

        controller.doPost(req, resp);

        verify(session).setAttribute(eq("accountMessage"), contains("incorrect"));
        verify(resp).sendRedirect("/ta/profile");
    }
}
