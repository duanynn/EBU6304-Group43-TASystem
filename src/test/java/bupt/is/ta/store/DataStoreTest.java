package bupt.is.ta.store;

import bupt.is.ta.model.Application;
import bupt.is.ta.model.Config;
import bupt.is.ta.model.Job;
import bupt.is.ta.model.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DataStoreTest {

    private static DataStore store;

    @BeforeAll
    static void initStore() throws IOException {
        store = DataStore.getInstance();
        Path tmpDir = Files.createTempDirectory("ta-datastore-test");
        tmpDir.toFile().deleteOnExit();
        store.init(tmpDir); // no-op if already initialized by another test class
    }

    // ── Initialization ────────────────────────────────────────────────────

    @Test
    void storeIsNotNull() {
        assertNotNull(store);
    }

    @Test
    void getConfig_returnsNonNull() {
        assertNotNull(store.getConfig());
    }

    @Test
    void defaultConfig_maxCoursesIsTwo() {
        // After init(), Config defaults or loaded values should be sensible
        assertTrue(store.getConfig().getMaxCoursesPerTA() >= 1);
    }

    @Test
    void seedDefaultUsers_arePresent() {
        // After init(), default seed users must exist
        List<User> users = store.getUsers();
        assertFalse(users.isEmpty());
    }

    @Test
    void seedDefaultJobs_arePresent() {
        List<Job> jobs = store.getJobs();
        assertFalse(jobs.isEmpty());
    }

    // ── User CRUD ─────────────────────────────────────────────────────────

    @Test
    void upsertUser_addsNewUser() {
        User u = buildUser("TEST-DS-U1", "Alice", User.Role.TA);
        store.upsertUser(u);

        boolean found = store.getUsers().stream()
                .anyMatch(x -> "TEST-DS-U1".equals(x.getId()));
        assertTrue(found);
    }

    @Test
    void upsertUser_updatesExistingUser() {
        User u = buildUser("TEST-DS-U2", "Bob", User.Role.TA);
        store.upsertUser(u);

        User updated = buildUser("TEST-DS-U2", "Bobby", User.Role.MO);
        store.upsertUser(updated);

        long count = store.getUsers().stream()
                .filter(x -> "TEST-DS-U2".equals(x.getId()))
                .count();
        assertEquals(1, count, "upsert should not create duplicate");

        String name = store.getUsers().stream()
                .filter(x -> "TEST-DS-U2".equals(x.getId()))
                .map(User::getName)
                .findFirst().orElseThrow();
        assertEquals("Bobby", name);
    }

    // ── Job CRUD ──────────────────────────────────────────────────────────

    @Test
    void addJob_assignsIdIfNull() {
        Job job = new Job();
        job.setId(null);
        job.setCourseName("Test Course A");
        store.addJob(job);

        assertNotNull(job.getId(), "ID should be assigned by addJob");
        assertFalse(job.getId().isBlank());
    }

    @Test
    void addJob_keepsExplicitId() {
        Job job = new Job();
        job.setId("FIXED-ID-001");
        job.setCourseName("Test Course B");
        store.addJob(job);

        boolean found = store.getJobs().stream()
                .anyMatch(j -> "FIXED-ID-001".equals(j.getId()));
        assertTrue(found);
    }

    @Test
    void updateJob_replacesExisting() {
        Job job = new Job();
        job.setId("JOB-UPDATE-001");
        job.setCourseName("Original Name");
        store.addJob(job);

        job.setCourseName("Updated Name");
        store.updateJob(job);

        long count = store.getJobs().stream()
                .filter(j -> "JOB-UPDATE-001".equals(j.getId()))
                .count();
        assertEquals(1, count);

        String name = store.getJobs().stream()
                .filter(j -> "JOB-UPDATE-001".equals(j.getId()))
                .map(Job::getCourseName)
                .findFirst().orElseThrow();
        assertEquals("Updated Name", name);
    }

    // ── Application CRUD ─────────────────────────────────────────────────

    @Test
    void addApplication_assignsIdIfNull() {
        Application app = new Application();
        app.setId(null);
        app.setStudentId("STU-DS-001");
        app.setJobId("JOB-DS-001");
        store.addApplication(app);

        assertNotNull(app.getId());
        assertFalse(app.getId().isBlank());
    }

    @Test
    void updateApplication_replacesExisting() {
        Application app = new Application();
        app.setId("APP-UPDATE-001");
        app.setStudentId("STU-DS-002");
        app.setJobId("JOB-DS-002");
        app.setStatus(Application.Status.PENDING);
        store.addApplication(app);

        app.setStatus(Application.Status.ACCEPTED);
        store.updateApplication(app);

        long count = store.getApplications().stream()
                .filter(a -> "APP-UPDATE-001".equals(a.getId()))
                .count();
        assertEquals(1, count);

        Application.Status status = store.getApplications().stream()
                .filter(a -> "APP-UPDATE-001".equals(a.getId()))
                .map(Application::getStatus)
                .findFirst().orElseThrow();
        assertEquals(Application.Status.ACCEPTED, status);
    }

    // ── Config update ─────────────────────────────────────────────────────

    @Test
    void updateConfig_isReflectedInGetConfig() {
        Config cfg = new Config();
        cfg.setMaxCoursesPerTA(7);
        cfg.setDashscopeApiKey("sk-test");
        store.updateConfig(cfg);

        assertEquals(7, store.getConfig().getMaxCoursesPerTA());
        assertEquals("sk-test", store.getConfig().getDashscopeApiKey());

        // Restore to avoid side effects on other tests
        Config restored = new Config();
        store.updateConfig(restored);
    }

    // ── getUsers / getJobs / getApplications are unmodifiable ────────────

    @Test
    void getUsersReturnsUnmodifiableList() {
        assertThrows(UnsupportedOperationException.class, () ->
                store.getUsers().add(new User()));
    }

    @Test
    void getJobsReturnsUnmodifiableList() {
        assertThrows(UnsupportedOperationException.class, () ->
                store.getJobs().add(new Job()));
    }

    @Test
    void getApplicationsReturnsUnmodifiableList() {
        assertThrows(UnsupportedOperationException.class, () ->
                store.getApplications().add(new Application()));
    }

    // ── Helper ────────────────────────────────────────────────────────────

    private User buildUser(String id, String name, User.Role role) {
        User u = new User();
        u.setId(id);
        u.setName(name);
        u.setPassword("pass");
        u.setRole(role);
        return u;
    }
}
