package bupt.is.ta.service;

import bupt.is.ta.model.Job;
import bupt.is.ta.model.JobScheduleSlot;
import bupt.is.ta.model.User;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScheduleFitServiceTest {

    private final ScheduleFitService service = new ScheduleFitService();

    @Test
    void containedWiderAvailabilityCapsAt100() {
        Job job = new Job();
        job.setScheduleSlots(List.of(new JobScheduleSlot(1, "10:00", "12:00")));
        User user = new User();
        user.setAvailableSlots(List.of(new JobScheduleSlot(1, "09:00", "12:00")));
        assertEquals(100, service.computeFit(job, user).getScheduleScore());
    }

    @Test
    void availStartsBeforeJobStartScoresZero() {
        Job job = new Job();
        job.setScheduleSlots(List.of(new JobScheduleSlot(1, "10:00", "12:00")));
        User user = new User();
        user.setAvailableSlots(List.of(new JobScheduleSlot(1, "09:00", "11:00")));
        assertEquals(0, service.computeFit(job, user).getScheduleScore());
    }

    @Test
    void leftAlignedPartialOverlapScores50() {
        Job job = new Job();
        job.setScheduleSlots(List.of(new JobScheduleSlot(1, "10:00", "12:00")));
        User user = new User();
        user.setAvailableSlots(List.of(new JobScheduleSlot(1, "10:00", "11:00")));
        assertEquals(50, service.computeFit(job, user).getScheduleScore());
    }

    @Test
    void misalignedOverlapScoresZero() {
        Job job = new Job();
        job.setScheduleSlots(List.of(new JobScheduleSlot(2, "10:00", "12:00")));
        User user = new User();
        user.setAvailableSlots(List.of(new JobScheduleSlot(2, "11:00", "13:00")));
        assertEquals(0, service.computeFit(job, user).getScheduleScore());
    }

    @Test
    void weightedAcrossMultipleSlots() {
        Job job = new Job();
        job.setScheduleSlots(List.of(
                new JobScheduleSlot(1, "09:00", "10:00"),
                new JobScheduleSlot(2, "14:00", "16:00")
        ));
        User user = new User();
        user.setAvailableSlots(List.of(new JobScheduleSlot(1, "08:00", "12:00")));
        ScheduleFitService.ScheduleFitResult result = service.computeFit(job, user);
        assertEquals(33, result.getScheduleScore());
    }

    @Test
    void noSlotsReturnsNa() {
        Job job = new Job();
        User user = new User();
        assertFalse(service.computeFit(job, user).isCalculable());
    }

    @Test
    void parsesAvailabilitySummaryText() {
        Job job = new Job();
        job.setScheduleSlots(List.of(new JobScheduleSlot(1, "10:00", "12:00")));
        User user = new User();
        user.setAvailableTime("Mon 08:00-12:00");
        assertEquals(100, service.computeFit(job, user).getScheduleScore());
    }

    @Test
    void parsesJobWorkTimeSummaryText() {
        Job job = new Job();
        job.setRequiredWorkTime("Mon 10:00-12:00");
        User user = new User();
        user.setAvailableSlots(List.of(new JobScheduleSlot(1, "08:00", "12:00")));
        assertEquals(100, service.computeFit(job, user).getScheduleScore());
    }
}
