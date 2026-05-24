package bupt.is.ta.service;

import bupt.is.ta.model.Application;
import bupt.is.ta.store.DataStore;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationServiceTest {

    private static DataStore store;
    private ApplicationService service;

    @BeforeAll
    static void initStore() throws IOException {
        store = DataStore.getInstance();
        Path tmp = Files.createTempDirectory("ta-app-svc-test");
        tmp.toFile().deleteOnExit();
        store.init(tmp);
    }

    @BeforeEach
    void setUp() {
        service = new ApplicationService();
    }

    // ── listByStudent ────────────────────────────────────────────────────

    @Test
    void listByStudent_returnsOnlyThatStudentsApplications() throws Exception {
        String stu1 = "STU-" + uid();
        String stu2 = "STU-" + uid();
        Application a1 = buildApp(uid(), stu1, "JOB-A");
        Application a2 = buildApp(uid(), stu2, "JOB-B");
        service.create(a1);
        service.create(a2);

        List<Application> list = service.listByStudent(stu1);
        assertTrue(list.stream().anyMatch(a -> a1.getId().equals(a.getId())));
        assertFalse(list.stream().anyMatch(a -> a2.getId().equals(a.getId())));
    }

    @Test
    void listByStudent_unknownStudent_returnsEmpty() {
        List<Application> list = service.listByStudent("NO_STUDENT_" + UUID.randomUUID());
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    // ── listByJob ─────────────────────────────────────────────────────────

    @Test
    void listByJob_returnsOnlyThatJobsApplications() throws Exception {
        String job1 = "JOB-" + uid();
        String job2 = "JOB-" + uid();
        Application a1 = buildApp(uid(), "STU-X", job1);
        Application a2 = buildApp(uid(), "STU-Y", job2);
        service.create(a1);
        service.create(a2);

        List<Application> list = service.listByJob(job1);
        assertTrue(list.stream().anyMatch(a -> a1.getId().equals(a.getId())));
        assertFalse(list.stream().anyMatch(a -> a2.getId().equals(a.getId())));
    }

    @Test
    void listByJob_unknownJob_returnsEmpty() {
        List<Application> list = service.listByJob("NO_JOB_" + UUID.randomUUID());
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    // ── findById ─────────────────────────────────────────────────────────

    @Test
    void findById_existingApplication_returnsIt() throws Exception {
        String id = uid();
        Application app = buildApp(id, "STU-FIND", "JOB-FIND");
        service.create(app);

        Optional<Application> found = service.findById(id);
        assertTrue(found.isPresent());
        assertEquals(id, found.get().getId());
        assertEquals("STU-FIND", found.get().getStudentId());
    }

    @Test
    void findById_nonExistent_returnsEmpty() {
        Optional<Application> found = service.findById("NO_SUCH_APP_" + UUID.randomUUID());
        assertTrue(found.isEmpty());
    }

    // ── findByStudentAndJob ───────────────────────────────────────────────

    @Test
    void findByStudentAndJob_matchingPair_returnsApplication() throws Exception {
        String stuId = "STU-PAIR-" + uid();
        String jobId = "JOB-PAIR-" + uid();
        Application app = buildApp(uid(), stuId, jobId);
        service.create(app);

        Optional<Application> found = service.findByStudentAndJob(stuId, jobId);
        assertTrue(found.isPresent());
    }

    @Test
    void findByStudentAndJob_wrongPair_returnsEmpty() throws Exception {
        String stuId = "STU-PAIR2-" + uid();
        String jobId = "JOB-PAIR2-" + uid();
        service.create(buildApp(uid(), stuId, jobId));

        // Different job for same student
        Optional<Application> found = service.findByStudentAndJob(stuId, "WRONG_JOB_" + uid());
        assertTrue(found.isEmpty());
    }

    // ── countAcceptedByStudent ───────────────────────────────────────────

    @Test
    void countAcceptedByStudent_countsOnlyAccepted() throws Exception {
        String stuId = "STU-COUNT-" + uid();

        Application accepted1 = buildApp(uid(), stuId, "JOB-C1");
        accepted1.setStatus(Application.Status.ACCEPTED);
        Application accepted2 = buildApp(uid(), stuId, "JOB-C2");
        accepted2.setStatus(Application.Status.ACCEPTED);
        Application pending = buildApp(uid(), stuId, "JOB-C3");
        pending.setStatus(Application.Status.PENDING);
        Application rejected = buildApp(uid(), stuId, "JOB-C4");
        rejected.setStatus(Application.Status.REJECTED);

        service.create(accepted1);
        service.create(accepted2);
        service.create(pending);
        service.create(rejected);

        long count = service.countAcceptedByStudent(stuId);
        assertEquals(2, count);
    }

    @Test
    void countAcceptedByStudent_noApplications_returnsZero() {
        long count = service.countAcceptedByStudent("NO_STU_COUNT_" + UUID.randomUUID());
        assertEquals(0, count);
    }

    @Test
    void countAcceptedByStudent_onlyNonAccepted_returnsZero() throws Exception {
        String stuId = "STU-NOACC-" + uid();
        Application app = buildApp(uid(), stuId, "JOB-NA");
        app.setStatus(Application.Status.INTERVIEWING);
        service.create(app);

        long count = service.countAcceptedByStudent(stuId);
        assertEquals(0, count);
    }

    // ── create ───────────────────────────────────────────────────────────

    @Test
    void create_assignsIdIfNull() throws Exception {
        Application app = new Application();
        app.setId(null);
        app.setStudentId("STU-CREATE-" + uid());
        app.setJobId("JOB-CREATE-" + uid());
        service.create(app);

        assertNotNull(app.getId());
        assertFalse(app.getId().isBlank());
    }

    @Test
    void create_defaultStatusIsPending() throws Exception {
        String id = uid();
        Application app = buildApp(id, "STU-DEFAULT-" + uid(), "JOB-DEFAULT-" + uid());
        service.create(app);

        Optional<Application> found = service.findById(id);
        assertTrue(found.isPresent());
        assertEquals(Application.Status.PENDING, found.get().getStatus());
    }

    // ── update ───────────────────────────────────────────────────────────

    @Test
    void update_changesStatus() throws Exception {
        String id = uid();
        Application app = buildApp(id, "STU-UPD-" + uid(), "JOB-UPD-" + uid());
        service.create(app);

        app.setStatus(Application.Status.ACCEPTED);
        service.update(app);

        Optional<Application> found = service.findById(id);
        assertTrue(found.isPresent());
        assertEquals(Application.Status.ACCEPTED, found.get().getStatus());
    }

    @Test
    void update_doesNotDuplicate() throws Exception {
        String id = uid();
        Application app = buildApp(id, "STU-DUP-" + uid(), "JOB-DUP-" + uid());
        service.create(app);
        service.update(app);

        long count = store.getApplications().stream()
                .filter(a -> id.equals(a.getId())).count();
        assertEquals(1, count);
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private String uid() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private Application buildApp(String id, String studentId, String jobId) {
        Application app = new Application();
        app.setId(id);
        app.setStudentId(studentId);
        app.setJobId(jobId);
        return app;
    }
}
