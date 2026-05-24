package bupt.is.ta.model;

import java.time.Instant;

public class Application {
    public enum Status {
        PENDING,
        INTERVIEWING,
        ACCEPTED,
        REJECTED
    }

    public enum InterviewResponse {
        NONE, PENDING, ACCEPTED, DECLINED
    }

    private String id;
    private String studentId;
    private String jobId;
    private Status status = Status.PENDING;
    private Instant appliedAt = Instant.now();
    private String note = "";
    private JobScheduleSlot interviewSlot;
    private String interviewMessage = "";
    private String interviewLocation = "";
    private boolean interviewRequiresWrittenTest;
    private String interviewScope = "";
    private InterviewResponse interviewResponse = InterviewResponse.NONE;
    private Instant interviewRespondedAt;
    private Instant interviewUpdatedAt;

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

    public String getNote() {
        return note == null ? "" : note;
    }

    public void setNote(String note) {
        this.note = note == null ? "" : note.trim();
    }

    public JobScheduleSlot getInterviewSlot() {
        return interviewSlot;
    }

    public void setInterviewSlot(JobScheduleSlot interviewSlot) {
        this.interviewSlot = interviewSlot;
    }

    public String getInterviewMessage() {
        return interviewMessage == null ? "" : interviewMessage;
    }

    public void setInterviewMessage(String interviewMessage) {
        this.interviewMessage = interviewMessage == null ? "" : interviewMessage.trim();
    }

    public Instant getInterviewUpdatedAt() {
        return interviewUpdatedAt;
    }

    public void setInterviewUpdatedAt(Instant interviewUpdatedAt) {
        this.interviewUpdatedAt = interviewUpdatedAt;
    }

    public String getInterviewLocation() {
        return interviewLocation == null ? "" : interviewLocation;
    }

    public void setInterviewLocation(String interviewLocation) {
        this.interviewLocation = interviewLocation == null ? "" : interviewLocation.trim();
    }

    public boolean isInterviewRequiresWrittenTest() {
        return interviewRequiresWrittenTest;
    }

    public void setInterviewRequiresWrittenTest(boolean interviewRequiresWrittenTest) {
        this.interviewRequiresWrittenTest = interviewRequiresWrittenTest;
    }

    public String getInterviewScope() {
        return interviewScope == null ? "" : interviewScope;
    }

    public void setInterviewScope(String interviewScope) {
        this.interviewScope = interviewScope == null ? "" : interviewScope.trim();
    }

    public InterviewResponse getInterviewResponse() {
        return interviewResponse == null ? InterviewResponse.NONE : interviewResponse;
    }

    public void setInterviewResponse(InterviewResponse interviewResponse) {
        this.interviewResponse = interviewResponse == null ? InterviewResponse.NONE : interviewResponse;
    }

    public Instant getInterviewRespondedAt() {
        return interviewRespondedAt;
    }

    public void setInterviewRespondedAt(Instant interviewRespondedAt) {
        this.interviewRespondedAt = interviewRespondedAt;
    }

    public boolean needsInterviewResponse() {
        return status == Status.INTERVIEWING && getInterviewResponse() == InterviewResponse.PENDING;
    }
}

