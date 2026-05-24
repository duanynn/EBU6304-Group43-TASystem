package bupt.is.ta.web;

import bupt.is.ta.model.User;
import bupt.is.ta.service.AvatarService;
import bupt.is.ta.store.DataStore;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@WebServlet("/avatar")
public class AvatarController extends HttpServlet {

    private final AvatarService avatarService = new AvatarService();
    private final DataStore store = DataStore.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User viewer = session != null ? (User) session.getAttribute("currentUser") : null;
        if (viewer == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        String rawUserId = trim(req.getParameter("userId"));
        final String targetUserId = rawUserId.isBlank() ? viewer.getId() : rawUserId;
        if (!avatarService.canViewAvatar(viewer, targetUserId)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        User target = store.getUsers().stream()
                .filter(u -> targetUserId.equals(u.getId()))
                .findFirst()
                .orElse(viewer);
        Path file = avatarService.resolveAvatarFile(getServletContext(), target);
        if (file == null || !Files.exists(file)) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        resp.setContentType(avatarService.contentTypeFor(file));
        resp.setHeader("Cache-Control", "private, max-age=300");
        try (OutputStream out = resp.getOutputStream()) {
            Files.copy(file, out);
        }
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
