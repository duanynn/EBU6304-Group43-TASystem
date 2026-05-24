package bupt.is.ta.service;

import bupt.is.ta.model.Job;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScheduleViewServiceTest {

    @Test
    void timeLabelsUse30MinuteSteps() {
        List<String> labels = ScheduleViewService.buildTimeLabels(8 * 60, 10 * 60);
        assertEquals(4, labels.size());
        assertEquals("08:00", labels.get(0));
        assertEquals("08:30", labels.get(1));
        assertEquals("09:00", labels.get(2));
    }

    @Test
    void blockGridRowMatches30MinuteSlots() {
        ScheduleViewService.TimetableBlock block = new ScheduleViewService.TimetableBlock(
                "b1", "j1", "Course", Job.JobType.MODULE_TA, ScheduleViewService.Layer.OPEN,
                1, 9 * 60, 11 * 60, ScheduleViewService.GRID_START_MINUTE);
        assertEquals(4, block.getGridRowStart());
        assertEquals(4, block.getGridRowSpan());
    }

    @Test
    void fixedGridIsEightToTwentyTwo() {
        assertEquals(8 * 60, ScheduleViewService.GRID_START_MINUTE);
        assertEquals(23 * 60, ScheduleViewService.GRID_END_MINUTE);
        List<String> labels = ScheduleViewService.buildTimeLabels(
                ScheduleViewService.GRID_START_MINUTE, ScheduleViewService.GRID_END_MINUTE);
        assertEquals(30, labels.size());
        assertEquals("08:00", labels.get(0));
        assertEquals("22:30", labels.get(labels.size() - 1));
    }

    @Test
    void percentLayoutForMidMorningBlock() {
        ScheduleViewService.TimetableBlock block = new ScheduleViewService.TimetableBlock(
                "b1", "j1", "Course", Job.JobType.MODULE_TA, ScheduleViewService.Layer.OPEN,
                1, 10 * 60, 12 * 60, ScheduleViewService.GRID_START_MINUTE);
        double top = block.getTopPercent(ScheduleViewService.GRID_START_MINUTE, ScheduleViewService.GRID_END_MINUTE);
        double height = block.getHeightPercent(ScheduleViewService.GRID_START_MINUTE, ScheduleViewService.GRID_END_MINUTE);
        assertTrue(top > 12 && top < 16);
        assertTrue(height > 12 && height < 16);
    }
}
