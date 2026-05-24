package bupt.is.ta.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserProfileTest {

    @Test
    void defaultFieldsAreEmptyStrings() {
        UserProfile p = new UserProfile();
        assertEquals("", p.getExtractedName());
        assertEquals("", p.getSummary());
        assertEquals("", p.getEducation());
        assertEquals("", p.getProjects());
        assertEquals("", p.getCertificates());
        assertEquals("", p.getRawCvText());
        assertEquals("", p.getLastParsedAt());
    }

    @Test
    void defaultSkillsListIsEmpty() {
        UserProfile p = new UserProfile();
        assertNotNull(p.getExtractedSkills());
        assertTrue(p.getExtractedSkills().isEmpty());
    }

    @Test
    void defaultAiScoreIsNegativeOne() {
        UserProfile p = new UserProfile();
        assertEquals(-1.0, p.getLastAiScore());
    }

    @Test
    void settersAndGetters() {
        UserProfile p = new UserProfile();
        p.setExtractedName("Alice");
        p.setSummary("Strong in Java");
        p.setEducation("BUPT, BSc CS, 2024");
        p.setProjects("TA system, 2024");
        p.setCertificates("CET-6");
        p.setRawCvText("Raw CV text here");
        p.setLastParsedAt("2026-04-01T00:00:00Z");
        p.setExtractedSkills(List.of("Java", "Python"));
        p.setLastAiScore(85.0);
        p.setLastAiAdvice("Focus on SQL");
        p.setLastAiStrengths(List.of("Java"));
        p.setLastAiGaps(List.of("SQL"));
        p.setLastAiFitSummary("Good fit overall");
        p.setLastAiAdviceTime(1000L);

        assertEquals("Alice", p.getExtractedName());
        assertEquals("Strong in Java", p.getSummary());
        assertEquals("BUPT, BSc CS, 2024", p.getEducation());
        assertEquals("TA system, 2024", p.getProjects());
        assertEquals("CET-6", p.getCertificates());
        assertEquals("Raw CV text here", p.getRawCvText());
        assertEquals("2026-04-01T00:00:00Z", p.getLastParsedAt());
        assertEquals(List.of("Java", "Python"), p.getExtractedSkills());
        assertEquals(85.0, p.getLastAiScore());
        assertEquals("Focus on SQL", p.getLastAiAdvice());
        assertEquals(List.of("Java"), p.getLastAiStrengths());
        assertEquals(List.of("SQL"), p.getLastAiGaps());
        assertEquals("Good fit overall", p.getLastAiFitSummary());
        assertEquals(1000L, p.getLastAiAdviceTime());
    }

    @Test
    void jobAiAdviceCacheGettersAndSetters() {
        UserProfile.JobAiAdviceCache cache = new UserProfile.JobAiAdviceCache();
        cache.setJobId("job-001");
        cache.setSignature("sig-abc");
        cache.setAiScore(72.0);
        cache.setAiAdvice("Improve SQL skills");
        cache.setAiStrengths(List.of("Java"));
        cache.setAiGaps(List.of("SQL"));
        cache.setAiFitSummary("Moderate fit");
        cache.setAiGenerated(true);
        cache.setCachedAt(9999L);

        assertEquals("job-001", cache.getJobId());
        assertEquals("sig-abc", cache.getSignature());
        assertEquals(72.0, cache.getAiScore());
        assertEquals("Improve SQL skills", cache.getAiAdvice());
        assertEquals(List.of("Java"), cache.getAiStrengths());
        assertEquals(List.of("SQL"), cache.getAiGaps());
        assertEquals("Moderate fit", cache.getAiFitSummary());
        assertTrue(cache.isAiGenerated());
        assertEquals(9999L, cache.getCachedAt());
    }

    @Test
    void setJobAiAdviceCachesNullBecomesEmptyList() {
        UserProfile p = new UserProfile();
        p.setJobAiAdviceCaches(null);
        assertNotNull(p.getJobAiAdviceCaches());
        assertTrue(p.getJobAiAdviceCaches().isEmpty());
    }
}
