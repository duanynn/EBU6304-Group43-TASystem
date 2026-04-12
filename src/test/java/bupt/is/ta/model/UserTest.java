package bupt.is.ta.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void settersAndGetters() {
        User u = new User();
        u.setId("2021001001");
        u.setPassword("secret");
        u.setRole(User.Role.TA);
        u.setName("Alice");
        u.setGpa(3.9);
        u.setSkillTags(List.of("Java", "Python"));
        u.setAvailableTime("Mon-Fri 9am-5pm");
        u.setCvPath("/data/cvs/2021001001.pdf");

        assertEquals("2021001001", u.getId());
        assertEquals("secret", u.getPassword());
        assertEquals(User.Role.TA, u.getRole());
        assertEquals("Alice", u.getName());
        assertEquals(3.9, u.getGpa());
        assertEquals(List.of("Java", "Python"), u.getSkillTags());
        assertEquals("Mon-Fri 9am-5pm", u.getAvailableTime());
        assertEquals("/data/cvs/2021001001.pdf", u.getCvPath());
    }

    @Test
    void availableTimeNullIsNormalizedToEmpty() {
        User u = new User();
        u.setAvailableTime(null);
        assertEquals("", u.getAvailableTime());
    }

    @Test
    void rolesExist() {
        assertNotNull(User.Role.TA);
        assertNotNull(User.Role.MO);
        assertNotNull(User.Role.ADMIN);
    }

    @Test
    void defaultSkillTagsIsEmptyList() {
        User u = new User();
        assertNotNull(u.getSkillTags());
        assertTrue(u.getSkillTags().isEmpty());
    }

    @Test
    void defaultProfileIsNotNull() {
        User u = new User();
        assertNotNull(u.getProfile());
    }

    @Test
    void moUser() {
        User u = new User();
        u.setId("0000000001");
        u.setRole(User.Role.MO);
        u.setName("Prof. Zhang");
        u.setEmployeeId("EMP001");

        assertEquals(User.Role.MO, u.getRole());
        assertEquals("EMP001", u.getEmployeeId());
    }
}
