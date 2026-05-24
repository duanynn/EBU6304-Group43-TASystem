package bupt.is.ta.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Job {
    public enum JobType {
        MODULE_TA,
        INVIGILATION,
        OTHER
    }

    private String id;
    private String courseName;
    private JobType jobType = JobType.MODULE_TA;
    private String moId;               // publisher (MO) id
    private int requiredCount;
    private List<String> requiredSkills = new ArrayList<>();
    private String requiredWorkTime = ""; // human-readable summary of scheduleSlots
    private List<JobScheduleSlot> scheduleSlots = new ArrayList<>();
    private String description = "";
    private boolean open = true;
    private Instant createdAt = Instant.now();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public JobType getJobType() {
        return jobType == null ? JobType.MODULE_TA : jobType;
    }

    public void setJobType(JobType jobType) {
        this.jobType = jobType == null ? JobType.MODULE_TA : jobType;
    }

    public String getMoId() {
        return moId;
    }

    public void setMoId(String moId) {
        this.moId = moId;
    }

    public int getRequiredCount() {
        return requiredCount;
    }

    public void setRequiredCount(int requiredCount) {
        this.requiredCount = requiredCount;
    }

    public List<String> getRequiredSkills() {
        return requiredSkills;
    }

    public void setRequiredSkills(List<String> requiredSkills) {
        this.requiredSkills = requiredSkills;
    }

    public String getRequiredWorkTime() {
        return requiredWorkTime;
    }

    public void setRequiredWorkTime(String requiredWorkTime) {
        this.requiredWorkTime = requiredWorkTime == null ? "" : requiredWorkTime.trim();
    }

    public List<JobScheduleSlot> getScheduleSlots() {
        return scheduleSlots == null ? List.of() : scheduleSlots;
    }

    public void setScheduleSlots(List<JobScheduleSlot> scheduleSlots) {
        this.scheduleSlots = scheduleSlots == null ? new ArrayList<>() : new ArrayList<>(scheduleSlots);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? "" : description.trim();
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

