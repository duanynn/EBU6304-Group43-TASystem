package bupt.is.ta.util;

import bupt.is.ta.model.Job;
import bupt.is.ta.model.User;
import bupt.is.ta.model.UserProfile;
import java.util.List;
import java.util.stream.Collectors;

public final class JobAdviceSignatureUtil {

    private JobAdviceSignatureUtil() {
    }

    public static String build(Job job, User student, UserProfile profile) {
        String required = (job.getRequiredSkills() == null ? List.<String>of() : job.getRequiredSkills()).stream()
                .map(s -> s == null ? "" : s.trim().toLowerCase())
                .collect(Collectors.joining("|"));
        String studentSkills = (student.getSkillTags() == null ? List.<String>of() : student.getSkillTags()).stream()
                .map(s -> s == null ? "" : s.trim().toLowerCase())
                .collect(Collectors.joining("|"));
        String parsedAt = profile.getLastParsedAt() == null ? "" : profile.getLastParsedAt();
        String summary = profile.getSummary() == null ? "" : profile.getSummary();
        String jobType = job.getJobType() == null ? Job.JobType.MODULE_TA.name() : job.getJobType().name();
        String availability = JobScheduleUtil.formatSummary(JobScheduleUtil.resolveAvailabilitySlots(student));
        String jobSchedule = JobScheduleUtil.formatSummary(JobScheduleUtil.resolveJobScheduleSlots(job));
        return required + "::" + studentSkills + "::" + parsedAt + "::" + summary.hashCode()
                + "::" + jobType + "::" + availability + "::" + jobSchedule;
    }
}
