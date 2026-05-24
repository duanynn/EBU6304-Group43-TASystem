package bupt.is.ta.service;

import bupt.is.ta.model.Application;
import bupt.is.ta.model.Job;
import bupt.is.ta.model.JobScheduleSlot;
import bupt.is.ta.model.User;
import bupt.is.ta.store.DataStore;
import bupt.is.ta.util.JobScheduleUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ScheduleViewService {

    public static final int GRID_START_MINUTE = JobScheduleUtil.GRID_DAY_START_MINUTE;
    public static final int GRID_END_MINUTE = JobScheduleUtil.GRID_DAY_END_MINUTE;
    /** @deprecated use {@link #GRID_END_MINUTE} */
    public static final int DEFAULT_GRID_END_MINUTE = GRID_END_MINUTE;
    public static final int SLOT_MINUTES = 30;

    public enum Layer {
        AVAILABILITY,
        OPEN,
        PENDING,
        INTERVIEWING,
        ACCEPTED
    }

    public static class TimetableBlock {
        private final String blockId;
        private final String jobId;
        private final String courseName;
        private final Job.JobType jobType;
        private final Layer layer;
        private final int dayOfWeek;
        private final int startMinute;
        private final int endMinute;
        private final int gridStartMinute;
        private boolean conflict;
        private boolean interviewInvite;
        private int laneIndex;
        private int laneCount = 1;

        public TimetableBlock(String blockId,
                              String jobId,
                              String courseName,
                              Job.JobType jobType,
                              Layer layer,
                              int dayOfWeek,
                              int startMinute,
                              int endMinute,
                              int gridStartMinute) {
            this.blockId = blockId;
            this.jobId = jobId;
            this.courseName = courseName;
            this.jobType = jobType == null ? Job.JobType.MODULE_TA : jobType;
            this.layer = layer == null ? Layer.OPEN : layer;
            this.dayOfWeek = dayOfWeek;
            this.startMinute = startMinute;
            this.endMinute = endMinute;
            this.gridStartMinute = gridStartMinute;
        }

        public String getBlockId() {
            return blockId;
        }

        public String getJobId() {
            return jobId;
        }

        public String getCourseName() {
            return courseName;
        }

        public Job.JobType getJobType() {
            return jobType;
        }

        public Layer getLayer() {
            return layer;
        }

        public int getDayOfWeek() {
            return dayOfWeek;
        }

        public int getStartMinute() {
            return startMinute;
        }

        public int getEndMinute() {
            return endMinute;
        }

        public int getGridRowStart() {
            return (startMinute - gridStartMinute) / SLOT_MINUTES + 2;
        }

        public int getGridRowSpan() {
            int span = (int) Math.ceil((endMinute - startMinute) / (double) SLOT_MINUTES);
            return Math.max(1, span);
        }

        public int getGridColumn() {
            return dayOfWeek + 1;
        }

        public boolean isConflict() {
            return conflict;
        }

        public void setConflict(boolean conflict) {
            this.conflict = conflict;
        }

        public boolean isInterviewInvite() {
            return interviewInvite;
        }

        public void setInterviewInvite(boolean interviewInvite) {
            this.interviewInvite = interviewInvite;
        }

        public int getLaneIndex() {
            return laneIndex;
        }

        public void setLaneIndex(int laneIndex) {
            this.laneIndex = laneIndex;
        }

        public int getLaneCount() {
            return laneCount;
        }

        public void setLaneCount(int laneCount) {
            this.laneCount = Math.max(1, laneCount);
        }

        public String formatTimeRange() {
            return formatMinute(startMinute) + "–" + formatMinute(endMinute);
        }

        private static String formatMinute(int minute) {
            return String.format("%02d:%02d", minute / 60, minute % 60);
        }

        public String getLayerCssClass() {
            return switch (layer) {
                case AVAILABILITY -> "slot-availability";
                case OPEN -> "slot-open";
                case PENDING -> "slot-pending";
                case INTERVIEWING -> "slot-interviewing";
                case ACCEPTED -> "slot-accepted";
            };
        }

        public double getTopPercent(int gridStart, int gridEnd) {
            double span = gridEnd - gridStart;
            if (span <= 0) {
                return 0;
            }
            return (startMinute - gridStart) * 100.0 / span;
        }

        public double getHeightPercent(int gridStart, int gridEnd) {
            double span = gridEnd - gridStart;
            if (span <= 0) {
                return 0;
            }
            return Math.max((endMinute - startMinute) * 100.0 / span, 2.5);
        }
    }

    public static class TimetableView {
        private final List<TimetableBlock> blocks;
        private final List<String> unstructuredJobs;
        private final int gridStartMinute;
        private final int gridEndMinute;
        private final int gridRowCount;
        private final int gridHeightPx;
        private final List<String> timeLabels;

        public TimetableView(List<TimetableBlock> blocks,
                             List<String> unstructuredJobs,
                             int gridStartMinute,
                             int gridEndMinute) {
            this.blocks = blocks == null ? List.of() : List.copyOf(blocks);
            this.unstructuredJobs = unstructuredJobs == null ? List.of() : List.copyOf(unstructuredJobs);
            this.gridStartMinute = gridStartMinute;
            this.gridEndMinute = gridEndMinute;
            this.gridRowCount = (gridEndMinute - gridStartMinute) / SLOT_MINUTES;
            this.gridHeightPx = this.gridRowCount * 32;
            this.timeLabels = buildTimeLabels(gridStartMinute, gridEndMinute);
        }

        public List<TimetableBlock> getBlocks() {
            return blocks;
        }

        public List<String> getUnstructuredJobs() {
            return unstructuredJobs;
        }

        public int getGridStartMinute() {
            return gridStartMinute;
        }

        public int getGridEndMinute() {
            return gridEndMinute;
        }

        public int getGridRowCount() {
            return gridRowCount;
        }

        public List<String> getTimeLabels() {
            return timeLabels;
        }

        public int getGridHeightPx() {
            return gridHeightPx;
        }

        public boolean isHourLabel(int index) {
            if (index < 0 || index >= timeLabels.size()) {
                return false;
            }
            int minute = gridStartMinute + index * SLOT_MINUTES;
            return minute % 60 == 0;
        }
    }

    private final JobService jobService = new JobService();
    private final ApplicationService applicationService = new ApplicationService();
    private final DataStore store = DataStore.getInstance();

    public TimetableView buildForStudent(String studentId) {
        List<TimetableBlock> blocks = new ArrayList<>();
        List<String> unstructured = new ArrayList<>();
        java.util.Set<String> appliedJobIds = new java.util.HashSet<>();

        User student = store.getUsers().stream()
                .filter(u -> studentId != null && studentId.equals(u.getId()))
                .findFirst()
                .orElse(null);
        if (student != null) {
            addAvailabilityBlocks(blocks, student);
        }

        for (Application app : applicationService.listByStudent(studentId)) {
            if (app.getStatus() == Application.Status.REJECTED) {
                continue;
            }
            appliedJobIds.add(app.getJobId());
            jobService.findById(app.getJobId()).ifPresent(job -> {
                Layer layer = switch (app.getStatus()) {
                    case PENDING -> Layer.PENDING;
                    case INTERVIEWING -> Layer.INTERVIEWING;
                    case ACCEPTED -> Layer.ACCEPTED;
                    default -> Layer.PENDING;
                };
                if (app.getStatus() == Application.Status.INTERVIEWING && app.getInterviewSlot() != null) {
                    addInterviewBlock(blocks, app, job);
                } else {
                    addJobBlocks(blocks, unstructured, job, layer, false);
                }
            });
        }

        for (Job job : jobService.listOpenJobs()) {
            if (job.getId() != null && appliedJobIds.contains(job.getId())) {
                continue;
            }
            addJobBlocks(blocks, unstructured, job, Layer.OPEN, false);
        }

        int gridEnd = GRID_END_MINUTE;
        int gridStart = GRID_START_MINUTE;
        markConflicts(blocks);
        assignLanes(blocks);
        blocks.sort(Comparator.comparingInt(TimetableBlock::getDayOfWeek)
                .thenComparingInt(TimetableBlock::getStartMinute)
                .thenComparing(b -> b.getLayer().ordinal()));
        return new TimetableView(blocks, unstructured, gridStart, gridEnd);
    }

    private void addAvailabilityBlocks(List<TimetableBlock> blocks, User student) {
        List<JobScheduleSlot> slots = JobScheduleUtil.resolveAvailabilitySlots(student);
        if (slots.isEmpty()) {
            return;
        }
        int gs = GRID_START_MINUTE;
        for (JobScheduleSlot slot : slots) {
            int start = clampBlockStart(JobScheduleUtil.toMinutes(slot.getStartTime()));
            int end = clampBlockEnd(JobScheduleUtil.toMinutes(slot.getEndTime()));
            if (end <= start) {
                continue;
            }
            TimetableBlock block = new TimetableBlock(
                    "avail-" + slot.getDayOfWeek() + "-" + start,
                    null,
                    "My availability",
                    Job.JobType.OTHER,
                    Layer.AVAILABILITY,
                    slot.getDayOfWeek(),
                    start,
                    end,
                    gs
            );
            blocks.add(block);
        }
    }

    private void addInterviewBlock(List<TimetableBlock> blocks, Application app, Job job) {
        JobScheduleSlot slot = app.getInterviewSlot();
        if (slot == null) {
            addJobBlocks(blocks, new ArrayList<>(), job, Layer.INTERVIEWING, true);
            return;
        }
        int start = clampBlockStart(JobScheduleUtil.toMinutes(slot.getStartTime()));
        int end = clampBlockEnd(JobScheduleUtil.toMinutes(slot.getEndTime()));
        if (end <= start) {
            return;
        }
        TimetableBlock block = new TimetableBlock(
                "interview-" + app.getId(),
                job.getId(),
                job.getCourseName(),
                job.getJobType(),
                Layer.INTERVIEWING,
                slot.getDayOfWeek(),
                start,
                end,
                GRID_START_MINUTE
        );
        block.setInterviewInvite(true);
        blocks.add(block);
    }

    private void addJobBlocks(List<TimetableBlock> blocks,
                              List<String> unstructured,
                              Job job,
                              Layer layer,
                              boolean interviewFallback) {
        if (!JobScheduleUtil.hasStructuredSlots(job)) {
            String name = job.getCourseName() == null ? "Unknown" : job.getCourseName();
            if (!unstructured.contains(name)) {
                unstructured.add(name);
            }
            return;
        }
        for (JobScheduleSlot slot : job.getScheduleSlots()) {
            int start = clampBlockStart(JobScheduleUtil.toMinutes(slot.getStartTime()));
            int end = clampBlockEnd(JobScheduleUtil.toMinutes(slot.getEndTime()));
            if (end <= start) {
                continue;
            }
            TimetableBlock block = new TimetableBlock(
                    job.getId() + "-" + slot.getDayOfWeek() + "-" + start,
                    job.getId(),
                    job.getCourseName(),
                    job.getJobType(),
                    layer,
                    slot.getDayOfWeek(),
                    start,
                    end,
                    GRID_START_MINUTE
            );
            if (interviewFallback) {
                block.setInterviewInvite(true);
            }
            blocks.add(block);
        }
    }

    private void markConflicts(List<TimetableBlock> blocks) {
        for (TimetableBlock block : blocks) {
            if (block.getLayer() == Layer.AVAILABILITY) {
                continue;
            }
            for (TimetableBlock other : blocks) {
                if (block == other || other.getLayer() == Layer.AVAILABILITY) {
                    continue;
                }
                if (block.getDayOfWeek() != other.getDayOfWeek()) {
                    continue;
                }
                if (rangesOverlap(block.getStartMinute(), block.getEndMinute(),
                        other.getStartMinute(), other.getEndMinute())) {
                    block.setConflict(true);
                    other.setConflict(true);
                }
            }
        }
    }

    private void assignLanes(List<TimetableBlock> blocks) {
        List<TimetableBlock> jobBlocks = blocks.stream()
                .filter(b -> b.getLayer() != Layer.AVAILABILITY)
                .toList();
        for (int day = 1; day <= 7; day++) {
            List<TimetableBlock> dayBlocks = new ArrayList<>();
            for (TimetableBlock b : jobBlocks) {
                if (b.getDayOfWeek() == day) {
                    dayBlocks.add(b);
                }
            }
            dayBlocks.sort(Comparator.comparingInt(TimetableBlock::getStartMinute));
            for (int i = 0; i < dayBlocks.size(); i++) {
                TimetableBlock current = dayBlocks.get(i);
                List<TimetableBlock> cluster = new ArrayList<>();
                cluster.add(current);
                for (int j = i + 1; j < dayBlocks.size(); j++) {
                    TimetableBlock other = dayBlocks.get(j);
                    boolean overlapsCluster = cluster.stream().anyMatch(c ->
                            rangesOverlap(c.getStartMinute(), c.getEndMinute(),
                                    other.getStartMinute(), other.getEndMinute()));
                    if (overlapsCluster) {
                        cluster.add(other);
                    }
                }
                if (cluster.size() > 1) {
                    for (int lane = 0; lane < cluster.size(); lane++) {
                        TimetableBlock b = cluster.get(lane);
                        b.setLaneIndex(lane);
                        b.setLaneCount(cluster.size());
                    }
                }
            }
        }
    }

    private static boolean rangesOverlap(int aStart, int aEnd, int bStart, int bEnd) {
        return aStart < bEnd && bStart < aEnd;
    }

    private static int clampBlockStart(int minute) {
        return JobScheduleUtil.clampStartMinute(minute);
    }

    private static int clampBlockEnd(int minute) {
        return JobScheduleUtil.clampEndMinute(minute);
    }

    public static List<String> buildTimeLabels(int gridStartMinute, int gridEndMinute) {
        List<String> labels = new ArrayList<>();
        for (int m = gridStartMinute; m < gridEndMinute; m += SLOT_MINUTES) {
            labels.add(String.format("%02d:%02d", m / 60, m % 60));
        }
        return labels;
    }

    /** @deprecated use TimetableView.getTimeLabels() */
    public static List<String> buildTimeLabels() {
        return buildTimeLabels(GRID_START_MINUTE, DEFAULT_GRID_END_MINUTE);
    }

    /** @deprecated use TimetableView.getGridRowCount() */
    public static int gridRowCount() {
        return (GRID_END_MINUTE - GRID_START_MINUTE) / SLOT_MINUTES;
    }
}
