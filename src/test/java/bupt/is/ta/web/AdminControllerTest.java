package bupt.is.ta.web;

import bupt.is.ta.model.Application;
import bupt.is.ta.model.Config;
import bupt.is.ta.model.User;
import bupt.is.ta.store.DataStore;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AdminControllerTest {

    private final AdminController controller = new AdminController();

    @BeforeAll
    static void initStore() throws Exception {
        DataStore store = DataStore.getInstance();
        store.init(Files.createTempDirectory("ta-admin-controller-test"));
        store.upsertUser(user("2021001001", User.Role.TA));
        store.upsertUser(user("0000000001", User.Role.MO));
        Application app = new Application();
        app.setId("admin-test-app");
        app.setStudentId("2021001001");
        app.setJobId("admin-test-job");
        app.setStatus(Application.Status.ACCEPTED);
        store.updateApplication(app);
    }

    @Test
    void doGet_overviewSetsMetricsAndForwards() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        when(req.getServletPath()).thenReturn("/admin/overview");
        when(req.getRequestDispatcher("/admin/overview.jsp")).thenReturn(dispatcher);

        controller.doGet(req, resp);

        verify(req).setAttribute(eq("totalUsers"), anyInt());
        verify(req).setAttribute(eq("totalJobs"), anyLong());
        verify(req).setAttribute(eq("totalApplications"), anyLong());
        verify(dispatcher).forward(req, resp);
    }

    @Test
    void doPost_usersRejectsInvalidStaffId() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        when(req.getServletPath()).thenReturn("/admin/users");
        when(req.getParameter("id")).thenReturn("abc");
        when(req.getRequestDispatcher("/admin/users.jsp")).thenReturn(dispatcher);

        controller.doPost(req, resp);

        verify(req).setAttribute("error", "Staff ID must be 10 digits");
        verify(req).setAttribute(eq("users"), any());
        verify(dispatcher).forward(req, resp);
    }

    @Test
    void doPost_resetMoPasswordUpdatesMoAndRedirects() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        when(req.getServletPath()).thenReturn("/admin/users");
        when(req.getParameter("action")).thenReturn("resetMoPassword");
        when(req.getParameter("moId")).thenReturn("0000000001");
        when(req.getContextPath()).thenReturn("/ta-recruitment-system");
        when(req.getSession(false)).thenReturn(session);

        controller.doPost(req, resp);

        verify(session).setAttribute(eq("adminMessage"), contains("111"));
        verify(session).setAttribute(eq("adminMessageType"), eq("success"));
        verify(resp).sendRedirect("/ta-recruitment-system/admin/users");
        assertEquals(AdminController.DEFAULT_MO_RESET_PASSWORD, DataStore.getInstance().getUsers().stream()
                .filter(u -> "0000000001".equals(u.getId()))
                .findFirst()
                .orElseThrow()
                .getPassword());
    }

    @Test
    void doPost_configNormalizesValuesAndRedirects() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(req.getServletPath()).thenReturn("/admin/config");
        when(req.getParameter("maxCoursesPerTA")).thenReturn("-3");
        when(req.getParameter("cvRelativePath")).thenReturn(null);
        when(req.getParameter("storageMode")).thenReturn(null);
        when(req.getParameter("dashscopeApiKey")).thenReturn(" ");
        when(req.getParameter("dashscopeEndpoint")).thenReturn(" ");
        when(req.getParameter("dashscopeModel")).thenReturn(" ");
        when(req.getContextPath()).thenReturn("/ta-recruitment-system");

        controller.doPost(req, resp);

        Config config = DataStore.getInstance().getConfig();
        verify(resp).sendRedirect("/ta-recruitment-system/admin/config");
        org.junit.jupiter.api.Assertions.assertEquals(1, config.getMaxCoursesPerTA());
        org.junit.jupiter.api.Assertions.assertEquals("/WEB-INF/data/cvs", config.getCvRelativePath());
        org.junit.jupiter.api.Assertions.assertEquals("WEBAPP", config.getStorageMode());
        org.junit.jupiter.api.Assertions.assertEquals("", config.getDashscopeApiKey());
        org.junit.jupiter.api.Assertions.assertEquals("qwen-plus", config.getDashscopeModel());
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
