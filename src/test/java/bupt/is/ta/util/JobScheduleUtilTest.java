package bupt.is.ta.util;

import bupt.is.ta.model.Job;
import bupt.is.ta.model.JobScheduleSlot;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JobScheduleUtilTest {

    @Test
    void parseSlotRowsSuccess() {
        JobScheduleUtil.ParseResult result = JobScheduleUtil.parseSlotRows(
                new String[]{"1", "3"},
                new String[]{"09:00", "14:00"},
                new String[]{"11:00", "16:00"}
        );
        assertTrue(result.isOk());
        assertEquals(2, result.getSlots().size());
        assertEquals("Mon 09:00-11:00; Wed 14:00-16:00", JobScheduleUtil.formatSummary(result.getSlots()));
    }

    @Test
    void parseSlotRowsRequiresAtLeastOne() {
        JobScheduleUtil.ParseResult result = JobScheduleUtil.parseSlotRows(
                new String[]{""},
                new String[]{""},
                new String[]{""}
        );
        assertFalse(result.isOk());
    }

    @Test
    void parseSlotRowsEndMustBeAfterStart() {
        JobScheduleUtil.ParseResult result = JobScheduleUtil.parseSlotRows(
                new String[]{"MON"},
                new String[]{"12:00"},
                new String[]{"10:00"}
        );
        assertFalse(result.isOk());
    }

    @Test
    void slotsOverlapSameDay() {
        JobScheduleSlot a = new JobScheduleSlot(1, "09:00", "11:00");
        JobScheduleSlot b = new JobScheduleSlot(1, "10:00", "12:00");
        assertTrue(JobScheduleUtil.slotsOverlap(a, b));
    }

    @Test
    void slotsDoNotOverlapDifferentDays() {
        JobScheduleSlot a = new JobScheduleSlot(1, "09:00", "11:00");
        JobScheduleSlot b = new JobScheduleSlot(2, "09:00", "11:00");
        assertFalse(JobScheduleUtil.slotsOverlap(a, b));
    }

    @Test
    void jobsOverlapWhenAnySlotOverlaps() {
        Job jobA = new Job();
        jobA.setScheduleSlots(List.of(new JobScheduleSlot(1, "09:00", "11:00")));
        Job jobB = new Job();
        jobB.setScheduleSlots(List.of(new JobScheduleSlot(1, "10:30", "12:00")));
        assertTrue(JobScheduleUtil.jobsOverlap(jobA, jobB));
    }

    @Test
    void jobsWithoutSlotsDoNotOverlap() {
        Job jobA = new Job();
        jobA.setRequiredWorkTime("8 hrs weekly");
        Job jobB = new Job();
        jobB.setScheduleSlots(List.of(new JobScheduleSlot(1, "09:00", "11:00")));
        assertFalse(JobScheduleUtil.jobsOverlap(jobA, jobB));
    }

    @Test
    void parseSlotRowsRejectsBeforeEightAm() {
        JobScheduleUtil.ParseResult result = JobScheduleUtil.parseSlotRows(
                new String[]{"MON"},
                new String[]{"07:30"},
                new String[]{"09:00"}
        );
        assertFalse(result.isOk());
    }

    @Test
    void parseSlotRowsRejectsAfterElevenPm() {
        JobScheduleUtil.ParseResult result = JobScheduleUtil.parseSlotRows(
                new String[]{"FRI"},
                new String[]{"22:00"},
                new String[]{"23:30"}
        );
        assertFalse(result.isOk());
    }

    @Test
    void parseSlotRowsAllowsEightToTwentyThree() {
        JobScheduleUtil.ParseResult result = JobScheduleUtil.parseSlotRows(
                new String[]{"SAT"},
                new String[]{"08:00"},
                new String[]{"23:00"}
        );
        assertTrue(result.isOk());
        assertEquals("Sat 08:00-23:00", JobScheduleUtil.formatSummary(result.getSlots()));
    }

    @Test
    void clampMinutesToGrid() {
        assertEquals(JobScheduleUtil.GRID_DAY_START_MINUTE, JobScheduleUtil.clampStartMinute(0));
        assertEquals(JobScheduleUtil.GRID_DAY_END_MINUTE, JobScheduleUtil.clampEndMinute(24 * 60));
    }

    @Test
    void parseSummaryTextReadsSemicolonSeparatedSlots() {
        List<JobScheduleSlot> slots = JobScheduleUtil.parseSummaryText("Mon 08:00-12:00; Wed 14:00-18:00");
        assertEquals(2, slots.size());
        assertEquals(1, slots.get(0).getDayOfWeek());
        assertEquals("08:00", slots.get(0).getStartTime());
        assertEquals("12:00", slots.get(0).getEndTime());
    }

    @Test
    void materializeAvailabilitySlotsFromSummary() {
        bupt.is.ta.model.User user = new bupt.is.ta.model.User();
        user.setAvailableTime("Mon 09:00-11:00");
        assertTrue(JobScheduleUtil.materializeAvailabilitySlots(user));
        assertEquals(1, user.getAvailableSlots().size());
    }

    @Test
    void displayWorkTimePrefersStructuredSlots() {
        Job job = new Job();
        job.setRequiredWorkTime("legacy text");
        job.setScheduleSlots(List.of(new JobScheduleSlot(5, "08:00", "10:00")));
        assertEquals("Fri 08:00-10:00", JobScheduleUtil.displayWorkTime(job));
    }
}
