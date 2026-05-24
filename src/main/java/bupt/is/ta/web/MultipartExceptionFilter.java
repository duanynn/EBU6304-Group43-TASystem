package bupt.is.ta.web;

import bupt.is.ta.util.UploadLimits;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Converts Tomcat multipart size errors into redirects with session flash messages.
 */
@WebFilter(urlPatterns = {"/ta/uploadCv", "/ta/profile", "/register"})
public class MultipartExceptionFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        try {
            chain.doFilter(request, response);
        } catch (Exception e) {
            if (UploadLimits.isSizeLimitExceeded(e)) {
                handleSizeError(req, resp);
                return;
            }
            if (e instanceof IOException io) {
                throw io;
            }
            if (e instanceof ServletException se) {
                throw se;
            }
            throw new ServletException(e);
        }
    }

    private void handleSizeError(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getServletPath();
        String ctx = req.getContextPath();
        HttpSession session = req.getSession(true);
        String message = UploadLimits.requestTooLargeMessage();

        if ("/register".equals(path)) {
            session.setAttribute("registerError", message);
            resp.sendRedirect(ctx + "/register?uploadError=1");
            return;
        }
        session.setAttribute("taProfileMessage", message);
        session.setAttribute("taProfileMessageType", "error");
        resp.sendRedirect(ctx + "/ta/profile");
    }
}
