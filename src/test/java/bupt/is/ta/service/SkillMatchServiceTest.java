package bupt.is.ta.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SkillMatchService — pure computation, no external I/O.
 * AI is disabled (enableAi=false) or uses the no-API-key fallback path
 * so these tests run fully offline.
 */
class SkillMatchServiceTest {

    private final SkillMatchService service = new SkillMatchService();

    // ── Score calculation ──────────────────────────────────────────────────

    @Test
    void fullMatch_scoreIsOne() {
        var result = service.match(
                List.of("Java", "Python", "SQL"),
                List.of("Java", "Python", "SQL"),
                "", "", false);

        assertEquals(1.0, result.getScore(), 1e-9);
        assertTrue(result.getMissingSkills().isEmpty());
    }

    @Test
    void partialMatch_scoreIsProportional() {
        // required: 4 skills, student has 2 → score = 0.5
        var result = service.match(
                List.of("Java", "Python", "SQL", "Linux"),
                List.of("Java", "Python"),
                "", "", false);

        assertEquals(0.5, result.getScore(), 1e-9);
    }

    @Test
    void noMatch_scoreIsZero() {
        var result = service.match(
                List.of("Java", "Python"),
                List.of("C++", "Rust"),
                "", "", false);

        assertEquals(0.0, result.getScore(), 1e-9);
    }

    @Test
    void emptyRequired_scoreIsOne() {
        var result = service.match(
                List.of(),
                List.of("Java"),
                "", "", false);

        assertEquals(1.0, result.getScore(), 1e-9);
    }

    @Test
    void bothEmpty_scoreIsOne() {
        var result = service.match(List.of(), List.of(), "", "", false);
        assertEquals(1.0, result.getScore(), 1e-9);
    }

    // ── Matched and missing skill sets ────────────────────────────────────

    @Test
    void matchedSkillsAreCorrect() {
        var result = service.match(
                List.of("Java", "Git", "Docker"),
                List.of("Java", "Docker", "Python"),
                "", "", false);

        assertTrue(result.getMatchedSkills().containsAll(List.of("Java", "Docker")));
        assertEquals(2, result.getMatchedSkills().size());
    }

    @Test
    void missingSkillsAreCorrect() {
        var result = service.match(
                List.of("Java", "Git", "Docker"),
                List.of("Java"),
                "", "", false);

        assertTrue(result.getMissingSkills().containsAll(List.of("Git", "Docker")));
        assertEquals(2, result.getMissingSkills().size());
    }

    @Test
    void studentExtraSkillsNotCountedAsMissingOrMatched() {
        // Student has skills not required — they just don't affect score
        var result = service.match(
                List.of("Java"),
                List.of("Java", "Python", "Rust"),
                "", "", false);

        assertEquals(1.0, result.getScore(), 1e-9);
        assertTrue(result.getMissingSkills().isEmpty());
    }

    // ── Required / student skill lists returned intact ───────────────────

    @Test
    void requiredSkillsAreReturnedInResult() {
        var req = List.of("Java", "SQL");
        var result = service.match(req, List.of("Java"), "", "", false);
        assertTrue(result.getRequiredSkills().containsAll(req));
    }

    @Test
    void studentSkillsAreReturnedInResult() {
        var student = List.of("Java", "Docker");
        var result = service.match(List.of("Java"), student, "", "", false);
        assertTrue(result.getStudentSkills().containsAll(student));
    }

    // ── AI disabled path ─────────────────────────────────────────────────

    @Test
    void aiDisabled_aiGeneratedIsFalse() {
        var result = service.match(
                List.of("Java"), List.of("Java"), "", "", false);
        assertFalse(result.isAiGenerated());
    }

    @Test
    void aiDisabled_aiScoreFallsBackToRuleScore() {
        // enableAi=false with full match → aiScore should be 100
        var result = service.match(
                List.of("Java", "Python"),
                List.of("Java", "Python"),
                "", "", false);

        // rule score = 1.0 → expected aiScore in [99, 101] range due to rounding
        assertTrue(result.getAiScore() >= 99.0 && result.getAiScore() <= 101.0,
                "Expected aiScore ~100 but got: " + result.getAiScore());
    }

    @Test
    void aiDisabled_aiAdviceIsNotBlank() {
        // Fallback advice should always be present
        var result = service.match(
                List.of("Java"), List.of("Python"), "", "", false);

        assertNotNull(result.getAiAdvice());
        assertFalse(result.getAiAdvice().isBlank());
    }

    // ── Advice is always present (AI or fallback) ───────────────────────

    @Test
    void match_adviceIsAlwaysNonBlank() {
        // Whether AI is configured in the environment or not, advice must be present.
        // If AI is active, it returns real advice. If not, fallback text is returned.
        var result = service.match(
                List.of("Java"), List.of("Python"));

        assertNotNull(result.getAiAdvice());
        assertFalse(result.getAiAdvice().isBlank(),
                "Advice must never be blank, got empty string");
    }

    // ── Convenience overloads delegate correctly ──────────────────────────

    @Test
    void twoArgOverload_returnsResult() {
        var result = service.match(List.of("Java"), List.of("Java"));
        assertNotNull(result);
        assertEquals(1.0, result.getScore(), 1e-9);
    }

    @Test
    void threeArgOverload_returnsResult() {
        var result = service.match(List.of("Python"), List.of("Python"), "Good profile");
        assertNotNull(result);
        assertEquals(1.0, result.getScore(), 1e-9);
    }

    @Test
    void fourArgOverload_returnsResult() {
        var result = service.match(List.of("SQL"), List.of("SQL"), "profile", "raw cv text");
        assertNotNull(result);
        assertEquals(1.0, result.getScore(), 1e-9);
    }

    // ── Duplicate skills in required are de-duplicated ────────────────────

    @Test
    void duplicateRequiredSkillsDeduplicatedInScore() {
        // If required has duplicates, Set semantics mean score is based on unique count
        var result = service.match(
                List.of("Java", "Java"),
                List.of("Java"),
                "", "", false);

        assertEquals(1.0, result.getScore(), 1e-9);
    }
}
