package bupt.is.ta.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationTest {

    @Test
    void defaultStatusIsPending() {
        Application app = new Application();
        assertEquals(Application.Status.PENDING, app.getStatus());
    }

    @Test
    void defaultAppliedAtIsNotNull() {
        Application app = new Application();
        assertNotNull(app.getAppliedAt());
    }

    @Test
    void settersAndGetters() {
        Application app = new Application();
        Instant now = Instant.now();

        app.setId("app-001");
        app.setStudentId("2021001001");
        app.setJobId("job-001");
        app.setStatus(Application.Status.ACCEPTED);
        app.setAppliedAt(now);

        assertEquals("app-001", app.getId());
        assertEquals("2021001001", app.getStudentId());
        assertEquals("job-001", app.getJobId());
        assertEquals(Application.Status.ACCEPTED, app.getStatus());
        assertEquals(now, app.getAppliedAt());
    }

    @Test
    void allStatusValuesExist() {
        assertNotNull(Application.Status.PENDING);
        assertNotNull(Application.Status.INTERVIEWING);
        assertNotNull(Application.Status.ACCEPTED);
        assertNotNull(Application.Status.REJECTED);
        assertEquals(4, Application.Status.values().length);
    }

    @Test
    void statusTransitions() {
        Application app = new Application();
        app.setStatus(Application.Status.INTERVIEWING);
        assertEquals(Application.Status.INTERVIEWING, app.getStatus());

        app.setStatus(Application.Status.REJECTED);
        assertEquals(Application.Status.REJECTED, app.getStatus());
    }
}
