package bupt.is.ta.web;

import bupt.is.ta.model.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class AuthFilterTest {

    private final AuthFilter filter = new AuthFilter();

    @Test
    void doFilter_redirectsAnonymousUserToLogin() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        when(req.getSession(false)).thenReturn(null);
        when(req.getContextPath()).thenReturn("/ta-recruitment-system");

        filter.doFilter(req, resp, chain);

        verify(resp).sendRedirect("/ta-recruitment-system/login.jsp");
        verifyNoInteractions(chain);
    }

    @Test
    void doFilter_allowsMatchingRolePath() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        FilterChain chain = mock(FilterChain.class);
        User ta = user(User.Role.TA);
        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("currentUser")).thenReturn(ta);
        when(req.getRequestURI()).thenReturn("/ta-recruitment-system/ta/jobs");
        when(req.getContextPath()).thenReturn("/ta-recruitment-system");

        filter.doFilter(req, resp, chain);

        verify(chain).doFilter(req, resp);
        verify(resp, never()).sendError(anyInt());
    }

    @Test
    void doFilter_blocksWrongRolePath() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        FilterChain chain = mock(FilterChain.class);
        User mo = user(User.Role.MO);
        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("currentUser")).thenReturn(mo);
        when(req.getRequestURI()).thenReturn("/ta-recruitment-system/ta/jobs");
        when(req.getContextPath()).thenReturn("/ta-recruitment-system");

        filter.doFilter(req, resp, chain);

        verify(resp).sendError(HttpServletResponse.SC_FORBIDDEN);
        verifyNoInteractions(chain);
    }

    private User user(User.Role role) {
        User user = new User();
        user.setId("role-" + role.name());
        user.setRole(role);
        return user;
    }
}
