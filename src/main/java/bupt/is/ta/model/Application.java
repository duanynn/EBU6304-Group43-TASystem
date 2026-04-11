package bupt.is.ta.model;

import java.time.Instant;

public class Application {
    public enum Status {
        PENDING,
        INTERVIEWING,
        ACCEPTED,
        REJECTED
    }

    private String id;
    private String studentId;
    private String jobId;
    private Status status = Status.PENDING;
    private Instant appliedAt = Instant.now();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Instant getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(Instant appliedAt) {
        this.appliedAt = appliedAt;
    }
}

