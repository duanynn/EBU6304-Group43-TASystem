package bupt.is.ta.web;

import bupt.is.ta.model.Job;
import bupt.is.ta.model.User;
import bupt.is.ta.store.DataStore;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MOControllerTest {

    private final MOController controller = new MOController();

    @BeforeAll
    static void initStore() throws Exception {
        DataStore store = DataStore.getInstance();
        store.init(Files.createTempDirectory("ta-mo-controller-test"));
        store.getConfig().setDashscopeApiKey("");
        store.upsertUser(user("0000000001", User.Role.MO));
    }

    @Test
    void doPost_generateJobDescriptionReturnsJsonFallback() throws Exception {
        HttpServletRequest req = requestWithMoSession();
        HttpServletResponse resp = mock(HttpServletResponse.class);
        StringWriter body = new StringWriter();
        when(req.getServletPath()).thenReturn("/mo/generateJobDescription");
        when(req.getParameter("courseName")).thenReturn("Software Engineering");
        when(req.getParameter("requiredCount")).thenReturn("2");
        when(req.getParameterValues("requiredSkills")).thenReturn(new String[]{"Java, Git"});
        when(req.getParameter("requiredWorkTime")).thenReturn("8 hrs weekly");
        when(resp.getWriter()).thenReturn(new PrintWriter(body));

        controller.doPost(req, resp);

        verify(resp).setContentType("application/json;charset=UTF-8");
        assertTrue(body.toString().contains("description"));
        assertTrue(body.toString().contains("Software Engineering"));
    }

    @Test
    void doPost_postJobPersistsDescriptionAndRedirects() throws Exception {
        HttpServletRequest req = requestWithMoSession();
        HttpServletResponse resp = mock(HttpServletResponse.class);
        String courseName = "Controller Course " + System.nanoTime();
        when(req.getServletPath()).thenReturn("/mo/postJob");
        when(req.getParameter("courseName")).thenReturn(courseName);
        when(req.getParameter("requiredCount")).thenReturn("2");
        when(req.getParameterValues("requiredSkills")).thenReturn(new String[]{"Java, SQL"});
        when(req.getParameter("requiredWorkTime")).thenReturn("Friday");
        when(req.getParameter("description")).thenReturn("Support labs and office hours.");
        when(req.getContextPath()).thenReturn("/ta-recruitment-system");

        controller.doPost(req, resp);

        Job saved = DataStore.getInstance().getJobs().stream()
                .filter(job -> courseName.equals(job.getCourseName()))
                .findFirst()
                .orElseThrow();
        assertEquals("Support labs and office hours.", saved.getDescription());
        assertEquals("0000000001", saved.getMoId());
        verify(resp).sendRedirect("/ta-recruitment-system/mo/dashboard");
    }

    private HttpServletRequest requestWithMoSession() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("currentUser")).thenReturn(user("0000000001", User.Role.MO));
        return req;
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
