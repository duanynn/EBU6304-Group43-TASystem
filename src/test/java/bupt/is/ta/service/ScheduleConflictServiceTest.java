package bupt.is.ta.service;

import bupt.is.ta.model.Application;
import bupt.is.ta.model.Job;
import bupt.is.ta.model.JobScheduleSlot;
import bupt.is.ta.store.DataStore;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ScheduleConflictServiceTest {

    private static final ScheduleConflictService conflictService = new ScheduleConflictService();
    private static final ApplicationService applicationService = new ApplicationService();
    private static final JobService jobService = new JobService();

    @BeforeAll
    static void initStore() throws Exception {
        DataStore.getInstance().init(Files.createTempDirectory("schedule-conflict-test"));
    }

    @Test
    void findConflictingApplicationsDetectsOverlap() throws Exception {
        Job jobA = saveJob("Course A", List.of(new JobScheduleSlot(1, "09:00", "11:00")));
        Job jobB = saveJob("Course B", List.of(new JobScheduleSlot(1, "10:00", "12:00")));

        Application appA = new Application();
        appA.setId(UUID.randomUUID().toString());
        appA.setStudentId("stu-conflict-" + UUID.randomUUID());
        appA.setJobId(jobA.getId());
        appA.setStatus(Application.Status.PENDING);
        applicationService.create(appA);

        List<Application> conflicts = conflictService.findConflictingApplications(appA.getStudentId(), jobB);
        assertEquals(1, conflicts.size());
        assertEquals(jobA.getId(), conflicts.get(0).getJobId());
    }

    @Test
    void rejectOverlappingApplicationsOnAccept() throws Exception {
        Job jobA = saveJob("Course A", List.of(new JobScheduleSlot(2, "14:00", "16:00")));
        Job jobB = saveJob("Course B", List.of(new JobScheduleSlot(2, "15:00", "17:00")));

        Application appA = new Application();
        appA.setId(UUID.randomUUID().toString());
        String studentId = "stu-reject-" + UUID.randomUUID();
        appA.setStudentId(studentId);
        appA.setJobId(jobA.getId());
        appA.setStatus(Application.Status.ACCEPTED);
        applicationService.create(appA);

        Application appB = new Application();
        appB.setId(UUID.randomUUID().toString());
        appB.setStudentId(studentId);
        appB.setJobId(jobB.getId());
        appB.setStatus(Application.Status.PENDING);
        applicationService.create(appB);

        int rejected = conflictService.rejectOverlappingApplications(appA);
        assertEquals(1, rejected);

        Application updatedB = applicationService.findById(appB.getId()).orElseThrow();
        assertEquals(Application.Status.REJECTED, updatedB.getStatus());
    }

    private Job saveJob(String name, List<JobScheduleSlot> slots) throws Exception {
        Job job = new Job();
        job.setId(UUID.randomUUID().toString());
        job.setCourseName(name);
        job.setMoId("mo1");
        job.setRequiredCount(1);
        job.setScheduleSlots(slots);
        job.setRequiredWorkTime(bupt.is.ta.util.JobScheduleUtil.formatSummary(slots));
        job.setOpen(true);
        jobService.save(job);
        return job;
    }
}
