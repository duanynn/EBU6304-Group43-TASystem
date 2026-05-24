package bupt.is.ta.service;

import bupt.is.ta.model.Job;
import bupt.is.ta.model.JobScheduleSlot;
import bupt.is.ta.model.User;
import bupt.is.ta.util.JobScheduleUtil;

import java.util.List;

/**
 * Per job slot: full containment uses min(availMinutes/jobMinutes, 1);
 * left-aligned partial overlap uses overlapMinutes/jobMinutes; otherwise 0.
 * Overall score is job-minute weighted average, capped at 100%.
 */
public class ScheduleFitService {

    public static class ScheduleFitResult {
        private final Double coverage;
        private final int scheduleScore;
        private final String summary;

        public ScheduleFitResult(Double coverage, int scheduleScore, String summary) {
            this.coverage = coverage;
            this.scheduleScore = scheduleScore;
            this.summary = summary == null ? "" : summary;
        }

        public Double getCoverage() {
            return coverage;
        }

        public int getScheduleScore() {
            return scheduleScore;
        }

        public String getSummary() {
            return summary;
        }

        public boolean isCalculable() {
            return coverage != null;
        }
    }

    public ScheduleFitResult computeFit(Job job, User student) {
        if (job == null || student == null) {
            return new ScheduleFitResult(null, 0, "Time fit: N/A");
        }
        List<JobScheduleSlot> jobSlots = JobScheduleUtil.resolveJobScheduleSlots(job);
        List<JobScheduleSlot> userSlots = JobScheduleUtil.resolveAvailabilitySlots(student);
        if (jobSlots == null || jobSlots.isEmpty() || userSlots == null || userSlots.isEmpty()) {
            return new ScheduleFitResult(null, 0, "Time fit: N/A (add structured availability in profile)");
        }
        SlotCoverageAggregate agg = computeWeightedCoverage(jobSlots, userSlots);
        if (agg.totalJobMinutes <= 0) {
            return new ScheduleFitResult(0.0, 0, "Time fit: N/A");
        }
        double coverage = Math.min(1.0, agg.weightedScoreSum / agg.totalJobMinutes);
        int score = (int) Math.round(coverage * 100.0);
        String summary = "Covers " + score + "% of required job hours";
        if (agg.slotCount > 1) {
            summary += " (" + agg.fittingSlots + " of " + agg.slotCount + " slot(s) with partial or full fit)";
        }
        return new ScheduleFitResult(coverage, score, summary);
    }

    public double computeWeightedCoverageRatio(List<JobScheduleSlot> jobSlots, List<JobScheduleSlot> userSlots) {
        SlotCoverageAggregate agg = computeWeightedCoverage(jobSlots, userSlots);
        if (agg.totalJobMinutes <= 0) {
            return 0.0;
        }
        return Math.min(1.0, agg.weightedScoreSum / agg.totalJobMinutes);
    }

    /** @deprecated use {@link #computeWeightedCoverageRatio} */
    public double computeSubsetCoverage(List<JobScheduleSlot> jobSlots, List<JobScheduleSlot> userSlots) {
        return computeWeightedCoverageRatio(jobSlots, userSlots);
    }

    /** @deprecated use {@link #computeWeightedCoverageRatio} */
    public double computeCoverage(List<JobScheduleSlot> jobSlots, List<JobScheduleSlot> userSlots) {
        return computeWeightedCoverageRatio(jobSlots, userSlots);
    }

    public static boolean isJobSlotWithinAvailability(JobScheduleSlot jobSlot, JobScheduleSlot userSlot) {
        if (jobSlot == null || userSlot == null || jobSlot.getDayOfWeek() != userSlot.getDayOfWeek()) {
            return false;
        }
        int jStart = JobScheduleUtil.toMinutes(jobSlot.getStartTime());
        int jEnd = JobScheduleUtil.toMinutes(jobSlot.getEndTime());
        int uStart = JobScheduleUtil.toMinutes(userSlot.getStartTime());
        int uEnd = JobScheduleUtil.toMinutes(userSlot.getEndTime());
        if (jEnd <= jStart || uEnd <= uStart) {
            return false;
        }
        return uStart <= jStart && jEnd <= uEnd;
    }

    /**
     * Score for one job slot in [0, 1], using the best matching availability on the same day.
     */
    public static double scoreJobSlot(JobScheduleSlot jobSlot, List<JobScheduleSlot> userSlots) {
        int jStart = JobScheduleUtil.toMinutes(jobSlot.getStartTime());
        int jEnd = JobScheduleUtil.toMinutes(jobSlot.getEndTime());
        if (jEnd <= jStart) {
            return 0.0;
        }
        int jobMinutes = jEnd - jStart;
        double best = 0.0;
        for (JobScheduleSlot userSlot : userSlots) {
            if (jobSlot.getDayOfWeek() != userSlot.getDayOfWeek()) {
                continue;
            }
            int uStart = JobScheduleUtil.toMinutes(userSlot.getStartTime());
            int uEnd = JobScheduleUtil.toMinutes(userSlot.getEndTime());
            if (uEnd <= uStart) {
                continue;
            }
            int uMinutes = uEnd - uStart;
            if (uStart <= jStart && jEnd <= uEnd) {
                best = Math.max(best, Math.min(1.0, (double) uMinutes / jobMinutes));
            } else if (uStart == jStart) {
                int overlap = overlapMinutes(jStart, jEnd, uStart, uEnd);
                if (overlap > 0) {
                    best = Math.max(best, (double) overlap / jobMinutes);
                }
            }
        }
        return Math.min(1.0, best);
    }

    private static SlotCoverageAggregate computeWeightedCoverage(List<JobScheduleSlot> jobSlots,
                                                                 List<JobScheduleSlot> userSlots) {
        SlotCoverageAggregate agg = new SlotCoverageAggregate();
        for (JobScheduleSlot jobSlot : jobSlots) {
            int jStart = JobScheduleUtil.toMinutes(jobSlot.getStartTime());
            int jEnd = JobScheduleUtil.toMinutes(jobSlot.getEndTime());
            if (jEnd <= jStart) {
                continue;
            }
            int jobMinutes = jEnd - jStart;
            double slotScore = scoreJobSlot(jobSlot, userSlots);
            agg.totalJobMinutes += jobMinutes;
            agg.weightedScoreSum += slotScore * jobMinutes;
            agg.slotCount++;
            if (slotScore > 0.001) {
                agg.fittingSlots++;
            }
        }
        return agg;
    }

    private static int overlapMinutes(int aStart, int aEnd, int bStart, int bEnd) {
        int start = Math.max(aStart, bStart);
        int end = Math.min(aEnd, bEnd);
        return Math.max(0, end - start);
    }

    private static class SlotCoverageAggregate {
        private int totalJobMinutes;
        private double weightedScoreSum;
        private int slotCount;
        private int fittingSlots;
    }
}
