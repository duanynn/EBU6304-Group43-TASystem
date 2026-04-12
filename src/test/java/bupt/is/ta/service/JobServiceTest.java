package bupt.is.ta.service;

import bupt.is.ta.model.Job;
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

class JobServiceTest {

    private static DataStore store;
    private JobService service;

    @BeforeAll
    static void initStore() throws IOException {
        store = DataStore.getInstance();
        Path tmp = Files.createTempDirectory("ta-job-svc-test");
        tmp.toFile().deleteOnExit();
        store.init(tmp);
    }

    @BeforeEach
    void setUp() {
        service = new JobService();
    }

    // ── listOpenJobs ──────────────────────────────────────────────────────

    @Test
    void listOpenJobs_returnsOnlyOpenJobs() throws Exception {
        String openId = uid();
        String closedId = uid();
        service.save(buildJob(openId, "MO-LIST-OPEN", true));
        service.save(buildJob(closedId, "MO-LIST-OPEN", false));

        List<Job> open = service.listOpenJobs();

        assertTrue(open.stream().anyMatch(j -> openId.equals(j.getId())));
        assertFalse(open.stream().anyMatch(j -> closedId.equals(j.getId())));
    }

    @Test
    void listOpenJobs_allContentsAreOpen() {
        List<Job> open = service.listOpenJobs();
        open.forEach(j -> assertTrue(j.isOpen(), "Non-open job in listOpenJobs: " + j.getId()));
    }

    // ── listJobsByMo ──────────────────────────────────────────────────────

    @Test
    void listJobsByMo_returnsOnlyJobsForThatMo() throws Exception {
        String moA = "MO-JST-" + uid();
        String moB = "MO-JST-" + uid();
        String idA = uid();
        String idB = uid();

        service.save(buildJob(idA, moA, true));
        service.save(buildJob(idB, moB, true));

        List<Job> forA = service.listJobsByMo(moA);
        assertTrue(forA.stream().anyMatch(j -> idA.equals(j.getId())));
        assertFalse(forA.stream().anyMatch(j -> idB.equals(j.getId())));
    }

    @Test
    void listJobsByMo_unknownMo_returnsEmptyOrFiltered() {
        List<Job> jobs = service.listJobsByMo("UNKNOWN_MO_" + UUID.randomUUID());
        assertNotNull(jobs);
        assertTrue(jobs.stream().noneMatch(j -> "UNKNOWN_MO".equals(j.getMoId())));
    }

    // ── findById ──────────────────────────────────────────────────────────

    @Test
    void findById_existingJob_returnsJob() throws Exception {
        String id = uid();
        service.save(buildJob(id, "MO-FIND", true));

        Optional<Job> found = service.findById(id);
        assertTrue(found.isPresent());
        assertEquals(id, found.get().getId());
    }

    @Test
    void findById_nonExistentJob_returnsEmpty() {
        Optional<Job> found = service.findById("NON_EXISTENT_JOB_" + UUID.randomUUID());
        assertTrue(found.isEmpty());
    }

    // ── save ─────────────────────────────────────────────────────────────

    @Test
    void save_withNullId_assignsId() throws Exception {
        Job job = new Job();
        job.setId(null);
        job.setCourseName("Auto ID Course");
        job.setMoId("MO-AUTO");
        service.save(job);

        assertNotNull(job.getId(), "save should assign ID when null");
        assertFalse(job.getId().isBlank());

        Optional<Job> found = service.findById(job.getId());
        assertTrue(found.isPresent());
    }

    @Test
    void save_withExplicitId_updatesJob() throws Exception {
        String id = uid();
        Job job = buildJob(id, "MO-UPDATE", true);
        job.setCourseName("Original");
        service.save(job);

        job.setCourseName("Updated");
        service.save(job);

        Optional<Job> found = service.findById(id);
        assertTrue(found.isPresent());
        assertEquals("Updated", found.get().getCourseName());

        long count = store.getJobs().stream().filter(j -> id.equals(j.getId())).count();
        assertEquals(1, count, "save update should not duplicate the job");
    }

    @Test
    void save_closedJob_persistsClosedState() throws Exception {
        String id = uid();
        Job job = buildJob(id, "MO-CLOSED", false);
        service.save(job);

        Optional<Job> found = service.findById(id);
        assertTrue(found.isPresent());
        assertFalse(found.get().isOpen());
    }

    @Test
    void save_jobWithSkills_persistsSkills() throws Exception {
        String id = uid();
        Job job = buildJob(id, "MO-SKILLS", true);
        job.setRequiredSkills(List.of("Java", "Docker", "SQL"));
        service.save(job);

        Optional<Job> found = service.findById(id);
        assertTrue(found.isPresent());
        assertTrue(found.get().getRequiredSkills().containsAll(List.of("Java", "Docker", "SQL")));
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private String uid() {
        return "JST-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private Job buildJob(String id, String moId, boolean open) {
        Job job = new Job();
        job.setId(id);
        job.setCourseName("Course-" + id);
        job.setMoId(moId);
        job.setRequiredCount(2);
        job.setOpen(open);
        return job;
    }
}
