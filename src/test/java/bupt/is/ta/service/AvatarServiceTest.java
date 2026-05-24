package bupt.is.ta.service;

import bupt.is.ta.model.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AvatarServiceTest {

    private final AvatarService avatarService = new AvatarService();

    @Test
    void moAlwaysUsesDefaultAvatar() {
        User mo = new User();
        mo.setRole(User.Role.MO);
        assertEquals(AvatarService.MO_DEFAULT_WEB_PATH, avatarService.resolveDisplayUrl(mo));
    }

    @Test
    void presetKeyResolvesWebPath() {
        User ta = new User();
        ta.setRole(User.Role.TA);
        ta.setAvatarType(User.AvatarType.PRESET);
        ta.setAvatarKey("preset/3.png");
        assertEquals("/assets/avatars/preset/3.png", avatarService.resolveDisplayUrl(ta));
    }

    @Test
    void uploadUsesAvatarServlet() {
        User ta = new User();
        ta.setId("2021001001");
        ta.setRole(User.Role.TA);
        ta.setAvatarType(User.AvatarType.UPLOAD);
        assertEquals("/avatar?userId=2021001001", avatarService.resolveDisplayUrl(ta));
    }

    @Test
    void applyPresetSetsTypeAndKey() {
        User ta = new User();
        ta.setRole(User.Role.TA);
        avatarService.applyPreset(ta, "2.png");
        assertEquals(User.AvatarType.PRESET, ta.getAvatarType());
        assertEquals("preset/2.png", ta.getAvatarKey());
    }
}
