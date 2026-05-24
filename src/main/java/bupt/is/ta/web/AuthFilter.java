package bupt.is.ta.web;

import bupt.is.ta.model.User;
import bupt.is.ta.service.UserService;
import bupt.is.ta.util.JobScheduleUtil;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebFilter(urlPatterns = {"/ta/*", "/mo/*", "/admin/*"})
public class AuthFilter implements Filter {

    private final UserService userService = new UserService();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);

        User current = session != null ? (User) session.getAttribute("currentUser") : null;
        if (current == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }

        String path = req.getRequestURI();
        String contextPath = req.getContextPath();
        String relative = path.substring(contextPath.length());

        boolean allowed = false;
        switch (current.getRole()) {
            case TA -> allowed = relative.startsWith("/ta/");
            case MO -> allowed = relative.startsWith("/mo/");
            case ADMIN -> allowed = relative.startsWith("/admin/");
        }
        if (!allowed) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        userService.findById(current.getId()).ifPresent(fresh -> {
            JobScheduleUtil.materializeAvailabilitySlots(fresh);
            session.setAttribute("currentUser", fresh);
        });

        chain.doFilter(request, response);
    }
}

