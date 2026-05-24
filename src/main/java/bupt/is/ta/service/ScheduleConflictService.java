package bupt.is.ta.service;

import bupt.is.ta.model.Application;
import bupt.is.ta.model.Job;
import bupt.is.ta.util.JobScheduleUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ScheduleConflictService {

    private final ApplicationService applicationService = new ApplicationService();
    private final JobService jobService = new JobService();

    public List<Application> findConflictingApplications(String studentId, Job targetJob) {
        if (studentId == null || targetJob == null || !JobScheduleUtil.hasStructuredSlots(targetJob)) {
            return List.of();
        }
        List<Application> conflicts = new ArrayList<>();
        for (Application app : applicationService.listByStudent(studentId)) {
            if (app.getStatus() == Application.Status.REJECTED) {
                continue;
            }
            if (targetJob.getId() != null && targetJob.getId().equals(app.getJobId())) {
                continue;
            }
            Optional<Job> otherJob = jobService.findById(app.getJobId());
            if (otherJob.isEmpty() || !JobScheduleUtil.hasStructuredSlots(otherJob.get())) {
                continue;
            }
            if (JobScheduleUtil.jobsOverlap(targetJob, otherJob.get())) {
                conflicts.add(app);
            }
        }
        return conflicts;
    }

    public String conflictMessage(Job targetJob, List<Application> conflicts) {
        if (conflicts == null || conflicts.isEmpty()) {
            return "";
        }
        List<String> names = new ArrayList<>();
        for (Application app : conflicts) {
            jobService.findById(app.getJobId()).ifPresent(j -> names.add(j.getCourseName()));
        }
        String list = names.isEmpty() ? "another position" : String.join(", ", names);
        return "This job overlaps with " + list + " you already applied for. "
                + "You cannot hold two positions at the same weekly time.";
    }

    /**
     * After accepting one application, auto-reject other non-rejected applications
     * from the same student that overlap in schedule with the accepted job.
     */
    public int rejectOverlappingApplications(Application acceptedApp) throws Exception {
        if (acceptedApp == null || acceptedApp.getStudentId() == null) {
            return 0;
        }
        Optional<Job> acceptedJob = jobService.findById(acceptedApp.getJobId());
        if (acceptedJob.isEmpty() || !JobScheduleUtil.hasStructuredSlots(acceptedJob.get())) {
            return 0;
        }
        int count = 0;
        for (Application other : applicationService.listByStudent(acceptedApp.getStudentId())) {
            if (other.getId() != null && other.getId().equals(acceptedApp.getId())) {
                continue;
            }
            if (other.getStatus() == Application.Status.REJECTED) {
                continue;
            }
            Optional<Job> otherJob = jobService.findById(other.getJobId());
            if (otherJob.isEmpty() || !JobScheduleUtil.hasStructuredSlots(otherJob.get())) {
                continue;
            }
            if (JobScheduleUtil.jobsOverlap(acceptedJob.get(), otherJob.get())) {
                other.setStatus(Application.Status.REJECTED);
                applicationService.update(other);
                count++;
            }
        }
        return count;
    }
}
