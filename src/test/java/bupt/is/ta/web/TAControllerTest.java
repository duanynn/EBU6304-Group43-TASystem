package bupt.is.ta.web;

import bupt.is.ta.model.Job;
import bupt.is.ta.model.User;
import bupt.is.ta.store.DataStore;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.util.List;

import static org.mockito.Mockito.*;

class TAControllerTest {

    private final TAController controller = new TAController();

    @BeforeEach
    void initServlet() throws Exception {
        ServletConfig config = mock(ServletConfig.class);
        ServletContext context = mock(ServletContext.class);
        when(config.getServletContext()).thenReturn(context);
        when(context.getRealPath(anyString())).thenReturn(null);
        controller.init(config);
    }

    @BeforeAll
    static void initStore() throws Exception {
        DataStore store = DataStore.getInstance();
        store.init(Files.createTempDirectory("ta-ta-controller-test"));
        User ta = user("2021001001", User.Role.TA);
        ta.setGpa(3.7);
        ta.setSkillTags(List.of("Java", "Git"));
        store.upsertUser(ta);

        Job job = new Job();
        job.setId("ta-controller-job");
        job.setCourseName("Software Engineering Controller Test");
        job.setMoId("0000000001");
        job.setRequiredCount(2);
        job.setRequiredSkills(List.of("Java", "Git"));
        job.setOpen(true);
        store.updateJob(job);
    }

    @Test
    void doGet_jobsSetsRecommendationAttributesAndForwards() throws Exception {
        HttpServletRequest req = requestWithTaSession();
        HttpServletResponse resp = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        when(req.getServletPath()).thenReturn("/ta/jobs");
        when(req.getParameter("q")).thenReturn("software java");
        when(req.getParameter("sort")).thenReturn("fit");
        when(req.getRequestDispatcher("/ta/jobBoard.jsp")).thenReturn(dispatcher);

        controller.doGet(req, resp);

        verify(req).setAttribute(eq("jobs"), any());
        verify(req).setAttribute(eq("fitScores"), any());
        verify(req).setAttribute(eq("jobAdviceByJobId"), any());
        verify(req).setAttribute("searchPerformed", true);
        verify(dispatcher).forward(req, resp);
    }

    @Test
    void doGet_profileSetsProfileAttributesWithoutJobAdviceList() throws Exception {
        HttpServletRequest req = requestWithTaSession();
        HttpServletResponse resp = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        when(req.getServletPath()).thenReturn("/ta/profile");
        when(req.getRequestDispatcher("/ta/profile.jsp")).thenReturn(dispatcher);

        controller.doGet(req, resp);

        verify(req).setAttribute(eq("profileMatch"), any());
        verify(req).setAttribute(eq("profileInitialized"), anyBoolean());
        verify(req).setAttribute(eq("profileCompletion"), anyInt());
        verify(req, never()).setAttribute(eq("jobAdviceList"), any());
        verify(dispatcher).forward(req, resp);
    }

    private HttpServletRequest requestWithTaSession() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        User current = user("2021001001", User.Role.TA);
        current.setGpa(3.7);
        current.setSkillTags(List.of("Java", "Git"));
        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("currentUser")).thenReturn(current);
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
