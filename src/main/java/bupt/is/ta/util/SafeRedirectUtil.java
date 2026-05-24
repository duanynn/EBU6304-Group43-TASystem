package bupt.is.ta.util;

import bupt.is.ta.model.User;
import jakarta.servlet.http.HttpServletRequest;

import java.net.URI;

/**
 * Builds safe in-app redirect URLs (context path + validated relative path).
 */
public final class SafeRedirectUtil {

    private SafeRedirectUtil() {
    }

    public static String resolveRedirectUrl(HttpServletRequest req, User user, String returnUrlParam, String refererHeader) {
        String contextPath = req.getContextPath();
        String path = sanitizeReturnPath(returnUrlParam, user.getRole(), contextPath);
        if (path == null) {
            path = sanitizeReturnPath(extractPathFromReferer(contextPath, refererHeader), user.getRole(), contextPath);
        }
        if (path == null) {
            path = defaultHomePath(user.getRole());
        }
        return contextPath + path;
    }

    public static String sanitizeReturnPath(String raw, User.Role role) {
        return sanitizeReturnPath(raw, role, "");
    }

    public static String sanitizeReturnPath(String raw, User.Role role, String contextPath) {
        if (raw == null || raw.isBlank() || role == null) {
            return null;
        }
        String path = raw.trim();
        if (path.startsWith("http://") || path.startsWith("https://")) {
            path = extractPathFromReferer(contextPath, path);
            if (path == null) {
                return null;
            }
        } else if (contextPath != null && !contextPath.isBlank() && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
            if (path.isEmpty()) {
                path = "/";
            }
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (path.contains("..") || path.contains("\\") || path.contains("\r") || path.contains("\n")) {
            return null;
        }
        if (path.contains("/changePassword") || path.endsWith("/login") || path.contains("/login.jsp")) {
            return null;
        }
        String requiredPrefix = rolePrefix(role);
        if (!path.startsWith(requiredPrefix)) {
            return null;
        }
        return path;
    }

    public static String extractPathFromReferer(String contextPath, String referer) {
        if (referer == null || referer.isBlank()) {
            return null;
        }
        try {
            URI uri = URI.create(referer.trim());
            String path = uri.getPath();
            if (path == null || path.isBlank()) {
                return null;
            }
            if (contextPath != null && !contextPath.isBlank() && path.startsWith(contextPath)) {
                path = path.substring(contextPath.length());
                if (path.isEmpty()) {
                    path = "/";
                }
            }
            String query = uri.getQuery();
            if (query != null && !query.isBlank()) {
                path = path + "?" + query;
            }
            return path;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static String defaultHomePath(User.Role role) {
        return switch (role) {
            case TA -> "/ta/profile";
            case MO -> "/mo/home";
            case ADMIN -> "/admin/overview";
        };
    }

    private static String rolePrefix(User.Role role) {
        return switch (role) {
            case TA -> "/ta/";
            case MO -> "/mo/";
            case ADMIN -> "/admin/";
        };
    }
}
