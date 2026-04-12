package bupt.is.ta.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JobTest {

    @Test
    void settersAndGetters() {
        Job job = new Job();
        job.setId("job-001");
        job.setCourseName("Software Engineering");
        job.setMoId("0000000001");
        job.setRequiredCount(3);
        job.setRequiredSkills(List.of("Java", "Git", "SQL"));
        job.setRequiredWorkTime("Mon/Wed 14:00-16:00");
        job.setOpen(true);

        assertEquals("job-001", job.getId());
        assertEquals("Software Engineering", job.getCourseName());
        assertEquals("0000000001", job.getMoId());
        assertEquals(3, job.getRequiredCount());
        assertEquals(List.of("Java", "Git", "SQL"), job.getRequiredSkills());
        assertEquals("Mon/Wed 14:00-16:00", job.getRequiredWorkTime());
        assertTrue(job.isOpen());
    }

    @Test
    void defaultOpenIsTrue() {
        Job job = new Job();
        assertTrue(job.isOpen());
    }

    @Test
    void defaultRequiredSkillsIsEmptyList() {
        Job job = new Job();
        assertNotNull(job.getRequiredSkills());
        assertTrue(job.getRequiredSkills().isEmpty());
    }

    @Test
    void defaultCreatedAtIsNotNull() {
        Job job = new Job();
        assertNotNull(job.getCreatedAt());
    }

    @Test
    void requiredWorkTimeNullIsNormalizedToEmpty() {
        Job job = new Job();
        job.setRequiredWorkTime(null);
        assertEquals("", job.getRequiredWorkTime());
    }

    @Test
    void canBeClosed() {
        Job job = new Job();
        job.setOpen(false);
        assertFalse(job.isOpen());
    }

    @Test
    void createdAtCanBeOverridden() {
        Job job = new Job();
        Instant now = Instant.now();
        job.setCreatedAt(now);
        assertEquals(now, job.getCreatedAt());
    }
}
