package bupt.is.ta.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for AiAdviceService inner data classes and offline (no API key) behaviour.
 * No real HTTP calls are made — all API calls short-circuit when key is absent.
 */
class AiAdviceServiceTest {

    private final AiAdviceService service = new AiAdviceService();

    // ── AiAnalysisResult data class ───────────────────────────────────────

    @Test
    void aiAnalysisResult_allFieldsRetained() {
        var result = new AiAdviceService.AiAnalysisResult(
                82.0,
                "Improve SQL skills",
                List.of("Java", "Spring"),
                List.of("SQL", "Docker"),
                "Strong Java developer",
                true);

        assertEquals(82.0, result.getMatchScore());
        assertEquals("Improve SQL skills", result.getAdvice());
        assertEquals(List.of("Java", "Spring"), result.getStrengths());
        assertEquals(List.of("SQL", "Docker"), result.getGaps());
        assertEquals("Strong Java developer", result.getFitSummary());
        assertTrue(result.isFromAi());
    }

    @Test
    void aiAnalysisResult_nullListsBecomEmpty() {
        var result = new AiAdviceService.AiAnalysisResult(
                null, "", null, null, null, false);

        assertNull(result.getMatchScore());
        assertNotNull(result.getStrengths());
        assertNotNull(result.getGaps());
        assertTrue(result.getStrengths().isEmpty());
        assertTrue(result.getGaps().isEmpty());
        assertEquals("", result.getFitSummary());
        assertFalse(result.isFromAi());
    }

    @Test
    void aiAnalysisResult_fromAiFalse_whenNotFromAi() {
        var result = new AiAdviceService.AiAnalysisResult(
                null, "", List.of(), List.of(), "", false);
        assertFalse(result.isFromAi());
    }

    // ── CvParseResult data class ──────────────────────────────────────────

    @Test
    void cvParseResult_allFieldsRetained() {
        var result = new AiAdviceService.CvParseResult(
                "Alice",
                "Strong in Java and Python",
                List.of("Java", "Python"),
                "BUPT BSc CS 2024",
                "TA System 2024",
                "CET-6 2023",
                true);

        assertEquals("Alice", result.getName());
        assertEquals("Strong in Java and Python", result.getSummary());
        assertEquals(List.of("Java", "Python"), result.getSkills());
        assertEquals("BUPT BSc CS 2024", result.getEducation());
        assertEquals("TA System 2024", result.getProjects());
        assertEquals("CET-6 2023", result.getAwards());
        assertTrue(result.isFromAi());
    }

    @Test
    void cvParseResult_nullsNormalizedToEmpty() {
        var result = new AiAdviceService.CvParseResult(
                null, null, null, null, null, null, false);

        assertEquals("", result.getName());
        assertEquals("", result.getSummary());
        assertNotNull(result.getSkills());
        assertTrue(result.getSkills().isEmpty());
        assertEquals("", result.getEducation());
        assertEquals("", result.getProjects());
        assertEquals("", result.getAwards());
        assertFalse(result.isFromAi());
    }

    @Test
    void cvParseResult_whitespaceInNameAndSummaryTrimmed() {
        var result = new AiAdviceService.CvParseResult(
                "  Bob  ", "  Great developer  ", List.of(), "", "", "", true);

        assertEquals("Bob", result.getName());
        assertEquals("Great developer", result.getSummary());
    }

    // ── generateAdvice — result is a string (non-null) in all cases ──────

    @Test
    void generateAdvice_returnsNonNull() {
        // May return real AI advice (if API key in env) or empty string (no key).
        // In both cases the method must not throw and must return a String.
        String advice = service.generateAdvice(
                List.of("Java", "SQL"),
                List.of("Java"),
                List.of("SQL"),
                "Good profile");

        assertNotNull(advice);
    }

    // ── analyzeJobFit — always returns a non-null result ─────────────────

    @Test
    void analyzeJobFit_alwaysReturnsNonNullResult() {
        var result = service.analyzeJobFit(
                List.of("Java"),
                List.of("Python"),
                List.of("Java"),
                "profile",
                "raw cv");

        assertNotNull(result);
        assertNotNull(result.getAdvice());
        assertNotNull(result.getStrengths());
        assertNotNull(result.getGaps());
        assertNotNull(result.getFitSummary());
        // matchScore is null when no AI result, or a 0-100 double when AI responded
        if (result.getMatchScore() != null) {
            assertTrue(result.getMatchScore() >= 0 && result.getMatchScore() <= 100,
                    "matchScore out of range: " + result.getMatchScore());
        }
    }

    // ── analyzeCvStructure — no API key → empty fallback ─────────────────

    @Test
    void analyzeCvStructure_noApiKey_returnsFallback() {
        var result = service.analyzeCvStructure("Some CV text here");

        assertNotNull(result);
        assertFalse(result.isFromAi());
    }

    // ── analyzeCvStructuredFields — no API key → empty fields ────────────

    @Test
    void analyzeCvStructuredFields_noApiKey_returnsFallback() {
        var result = service.analyzeCvStructuredFields("Some CV text here");

        assertNotNull(result);
        assertFalse(result.isFromAi());
        assertEquals("", result.getName());
    }
}
