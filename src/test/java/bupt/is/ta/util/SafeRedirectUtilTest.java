package bupt.is.ta.util;

import bupt.is.ta.model.User;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SafeRedirectUtilTest {

  private static final String CTX = "/ta-recruitment-system";

    @Test
    void sanitizeRejectsChangePasswordPath() {
        assertNull(SafeRedirectUtil.sanitizeReturnPath("/ta/changePassword", User.Role.TA, CTX));
    }

    @Test
    void sanitizeRejectsPathTraversal() {
        assertNull(SafeRedirectUtil.sanitizeReturnPath("/ta/../admin/overview", User.Role.TA, CTX));
    }

    @Test
    void sanitizeRejectsWrongRolePrefix() {
        assertNull(SafeRedirectUtil.sanitizeReturnPath("/mo/home", User.Role.TA, CTX));
    }

    @Test
    void sanitizeAcceptsValidTaPath() {
        assertEquals("/ta/jobs", SafeRedirectUtil.sanitizeReturnPath("/ta/jobs", User.Role.TA, CTX));
    }

    @Test
    void extractPathStripsContextFromFullReferer() {
        String path = SafeRedirectUtil.extractPathFromReferer(CTX,
                "http://localhost:8080/ta-recruitment-system/ta/jobs");
        assertEquals("/ta/jobs", path);
    }

    @Test
    void extractPathFromRelativeRefererStyle() {
        assertEquals("/ta/profile",
                SafeRedirectUtil.extractPathFromReferer(CTX, "/ta-recruitment-system/ta/profile"));
    }

    @Test
    void resolveRedirectUsesReturnUrlWithContext() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getContextPath()).thenReturn(CTX);
        User ta = new User();
        ta.setRole(User.Role.TA);
        String url = SafeRedirectUtil.resolveRedirectUrl(req, ta, "/ta/jobs", null);
        assertEquals(CTX + "/ta/jobs", url);
    }

    @Test
    void resolveRedirectFallsBackWhenRefererIsUnsafe() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getContextPath()).thenReturn(CTX);
        User ta = new User();
        ta.setRole(User.Role.TA);
        String url = SafeRedirectUtil.resolveRedirectUrl(req, ta, null, "/ta/changePassword");
        assertEquals(CTX + "/ta/profile", url);
    }

    @Test
    void resolveRedirectDefaultsForMo() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getContextPath()).thenReturn(CTX);
        User mo = new User();
        mo.setRole(User.Role.MO);
        assertEquals(CTX + "/mo/home", SafeRedirectUtil.resolveRedirectUrl(req, mo, null, null));
    }
}
