package bupt.is.ta.service;

import bupt.is.ta.model.Config;
import bupt.is.ta.model.Job;
import bupt.is.ta.model.User;
import bupt.is.ta.model.UserProfile;
import bupt.is.ta.store.DataStore;
import bupt.is.ta.util.JobScheduleUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class SkillMatchService {
    private final AiAdviceService aiAdviceService = new AiAdviceService();
    private final ScheduleFitService scheduleFitService = new ScheduleFitService();

    public static class MatchResult {
        private final List<String> requiredSkills;
        private final List<String> studentSkills;
        private final List<String> matchedSkills;
        private final List<String> missingSkills;
        private final double score;
        private final double aiScore;
        private final String aiAdvice;
        private final List<String> aiStrengths;
        private final List<String> aiGaps;
        private final String aiFitSummary;
        private final boolean aiGenerated;
        private final Double scheduleCoverage;
        private final int scheduleScore;
        private final String scheduleSummary;
        private final double combinedScore;

        public MatchResult(List<String> requiredSkills,
                           List<String> studentSkills,
                           List<String> matchedSkills,
                           List<String> missingSkills,
                           double score,
                           double aiScore,
                           String aiAdvice,
                           List<String> aiStrengths,
                           List<String> aiGaps,
                           String aiFitSummary,
                           boolean aiGenerated) {
            this(requiredSkills, studentSkills, matchedSkills, missingSkills, score, aiScore, aiAdvice,
                    aiStrengths, aiGaps, aiFitSummary, aiGenerated, null, 0, "", score);
        }

        public MatchResult(List<String> requiredSkills,
                           List<String> studentSkills,
                           List<String> matchedSkills,
                           List<String> missingSkills,
                           double score,
                           double aiScore,
                           String aiAdvice,
                           List<String> aiStrengths,
                           List<String> aiGaps,
                           String aiFitSummary,
                           boolean aiGenerated,
                           Double scheduleCoverage,
                           int scheduleScore,
                           String scheduleSummary,
                           double combinedScore) {
            this.requiredSkills = requiredSkills;
            this.studentSkills = studentSkills;
            this.matchedSkills = matchedSkills;
            this.missingSkills = missingSkills;
            this.score = score;
            this.aiScore = aiScore;
            this.aiAdvice = aiAdvice;
            this.aiStrengths = aiStrengths == null ? List.of() : aiStrengths;
            this.aiGaps = aiGaps == null ? List.of() : aiGaps;
            this.aiFitSummary = aiFitSummary == null ? "" : aiFitSummary;
            this.aiGenerated = aiGenerated;
            this.scheduleCoverage = scheduleCoverage;
            this.scheduleScore = scheduleScore;
            this.scheduleSummary = scheduleSummary == null ? "" : scheduleSummary;
            this.combinedScore = combinedScore;
        }

        public List<String> getRequiredSkills() {
            return requiredSkills;
        }

        public List<String> getStudentSkills() {
            return studentSkills;
        }

        public List<String> getMatchedSkills() {
            return matchedSkills;
        }

        public List<String> getMissingSkills() {
            return missingSkills;
        }

        public double getScore() {
            return score;
        }

        public String getAiAdvice() {
            return aiAdvice;
        }

        public double getAiScore() {
            return aiScore;
        }

        public List<String> getAiStrengths() {
            return aiStrengths;
        }

        public List<String> getAiGaps() {
            return aiGaps;
        }

        public String getAiFitSummary() {
            return aiFitSummary;
        }

        public boolean isAiGenerated() {
            return aiGenerated;
        }

        public Double getScheduleCoverage() {
            return scheduleCoverage;
        }

        public int getScheduleScore() {
            return scheduleScore;
        }

        public String getScheduleSummary() {
            return scheduleSummary;
        }

        public double getCombinedScore() {
            return combinedScore;
        }

        public boolean hasScheduleFit() {
            return scheduleCoverage != null;
        }
    }

    /**
     * Rebuild match from cached AI advice (schedule fit is not stored in cache).
     */
    public MatchResult fromJobAdviceCache(UserProfile.JobAiAdviceCache cache,
                                          List<String> requiredSkills,
                                          List<String> studentSkills) {
        if (cache == null) {
            return null;
        }
        List<String> required = requiredSkills == null ? List.of() : requiredSkills;
        List<String> student = studentSkills == null ? List.of() : studentSkills;
        List<String> gaps = cache.getAiGaps() == null ? List.of() : cache.getAiGaps();
        double ruleScore = Math.max(0.0, Math.min(1.0, cache.getAiScore() / 100.0));
        return new MatchResult(
                required,
                student,
                List.of(),
                gaps,
                ruleScore,
                cache.getAiScore(),
                cache.getAiAdvice(),
                cache.getAiStrengths(),
                gaps,
                cache.getAiFitSummary(),
                cache.isAiGenerated()
        );
    }

    /**
     * Always compute schedule fit live (cheap) and attach to an existing match (e.g. from AI cache).
     */
    public MatchResult mergeScheduleFit(MatchResult base, Job job, User student) {
        if (base == null) {
            return null;
        }
        if (job == null || student == null) {
            return base;
        }
        JobScheduleUtil.materializeAvailabilitySlots(student);
        JobScheduleUtil.materializeJobScheduleSlots(job);
        ScheduleFitService.ScheduleFitResult scheduleFit = scheduleFitService.computeFit(job, student);
        if (!scheduleFit.isCalculable()) {
            return base;
        }
        String aiFitSummary = base.getAiFitSummary();
        if (aiFitSummary == null || aiFitSummary.isBlank()) {
            aiFitSummary = scheduleFit.getSummary();
        } else if (!aiFitSummary.contains(scheduleFit.getSummary())) {
            aiFitSummary = aiFitSummary + " " + scheduleFit.getSummary();
        }
        double combinedScore = base.getScore() * 0.7 + (scheduleFit.getScheduleScore() / 100.0) * 0.3;
        return new MatchResult(
                base.getRequiredSkills(),
                base.getStudentSkills(),
                base.getMatchedSkills(),
                base.getMissingSkills(),
                base.getScore(),
                base.getAiScore(),
                base.getAiAdvice(),
                base.getAiStrengths(),
                base.getAiGaps(),
                aiFitSummary,
                base.isAiGenerated(),
                scheduleFit.getCoverage(),
                scheduleFit.getScheduleScore(),
                scheduleFit.getSummary(),
                combinedScore
        );
    }

    public MatchResult match(List<String> required, List<String> student) {
        return match(required, student, "", "", true, null, null);
    }

    public MatchResult match(List<String> required, List<String> student, String profileSummary) {
        return match(required, student, profileSummary, "", true, null, null);
    }

    public MatchResult match(List<String> required, List<String> student, String profileSummary, String rawCvText) {
        return match(required, student, profileSummary, rawCvText, true, null, null);
    }

    public MatchResult match(List<String> required, List<String> student, String profileSummary, String rawCvText, boolean enableAi) {
        return match(required, student, profileSummary, rawCvText, enableAi, null, null);
    }

    public MatchResult match(List<String> required,
                           List<String> student,
                           String profileSummary,
                           String rawCvText,
                           boolean enableAi,
                           Job job,
                           User studentUser) {
        List<String> safeRequired = required == null ? List.of() : required;
        List<String> safeStudent = student == null ? List.of() : student;
        Map<String, String> requiredByKey = normalizeSkills(safeRequired);
        Map<String, String> studentByKey = normalizeSkills(safeStudent);

        Set<String> requiredSet = new HashSet<>(requiredByKey.keySet());
        Set<String> studentSet = new HashSet<>(studentByKey.keySet());

        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        for (Map.Entry<String, String> entry : requiredByKey.entrySet()) {
            if (studentSet.contains(entry.getKey())) {
                matched.add(entry.getValue());
            } else {
                missing.add(entry.getValue());
            }
        }

        double score = requiredSet.isEmpty()
                ? 1.0
                : (double) matched.size() / (double) requiredSet.size();

        String scheduleContext = "";
        ScheduleFitService.ScheduleFitResult scheduleFit = null;
        if (job != null && studentUser != null) {
            scheduleFit = scheduleFitService.computeFit(job, studentUser);
            if (scheduleFit.isCalculable()) {
                scheduleContext = scheduleFit.getSummary();
            }
        }

        AiAdviceService.AiAnalysisResult aiResult = enableAi
                ? aiAdviceService.analyzeJobFit(
                List.copyOf(requiredByKey.values()),
                List.copyOf(studentByKey.values()),
                List.copyOf(missing),
                profileSummary,
                rawCvText,
                scheduleContext)
                : new AiAdviceService.AiAnalysisResult(null, "", List.of(), List.of(), "", false);
        String aiAdvice = aiResult.getAdvice();
        List<String> aiStrengths = aiResult.getStrengths();
        List<String> aiGaps = aiResult.getGaps();
        String aiFitSummary = aiResult.getFitSummary();
        boolean aiGenerated = aiResult.isFromAi();
        double aiScore = aiResult.getMatchScore() != null
                ? aiResult.getMatchScore()
                : Math.round(score * 100.0);
        if (aiAdvice == null || aiAdvice.isBlank()) {
            boolean aiConfigured = isAiConfigured();
            aiAdvice = aiConfigured
                    ? buildAiUnavailableAdvice(List.copyOf(missing), score)
                    : buildAiNotConfiguredAdvice();
            aiStrengths = List.of();
            aiGaps = List.copyOf(missing);
            aiFitSummary = aiConfigured
                    ? "AI is configured, but this request failed. Current result is from local rule-based evaluation."
                    : "Current result is from local rule-based evaluation because AI real-time evaluation is not configured.";
            aiGenerated = false;
        }
        if (scheduleFit != null && scheduleFit.isCalculable() && (aiFitSummary == null || aiFitSummary.isBlank())) {
            aiFitSummary = scheduleFit.getSummary();
        } else if (scheduleFit != null && scheduleFit.isCalculable()) {
            aiFitSummary = aiFitSummary + " " + scheduleFit.getSummary();
        }

        double combinedScore = score;
        Double scheduleCoverage = null;
        int scheduleScore = 0;
        String scheduleSummary = "Time fit: N/A";
        if (scheduleFit != null && scheduleFit.isCalculable()) {
            scheduleCoverage = scheduleFit.getCoverage();
            scheduleScore = scheduleFit.getScheduleScore();
            scheduleSummary = scheduleFit.getSummary();
            combinedScore = score * 0.7 + (scheduleScore / 100.0) * 0.3;
        }

        return new MatchResult(
                List.copyOf(requiredByKey.values()),
                List.copyOf(studentByKey.values()),
                List.copyOf(matched),
                List.copyOf(missing),
                score,
                aiScore,
                aiAdvice,
                aiStrengths,
                aiGaps,
                aiFitSummary,
                aiGenerated,
                scheduleCoverage,
                scheduleScore,
                scheduleSummary,
                combinedScore
        );
    }

    private Map<String, String> normalizeSkills(List<String> skills) {
        Map<String, String> normalized = new LinkedHashMap<>();
        if (skills == null) {
            return normalized;
        }
        for (String skill : skills) {
            if (skill == null) continue;
            String display = skill.trim();
            if (display.isEmpty()) continue;
            String key = display.toLowerCase(Locale.ROOT);
            normalized.putIfAbsent(key, display);
        }
        return normalized;
    }

    private String buildAdvice(List<String> missingSkills, double score) {
        if (missingSkills.isEmpty()) {
            return "Your skills are highly aligned with this role. Add relevant project evidence in your resume and prepare measurable outcomes.";
        }

        StringBuilder advice = new StringBuilder();
        if (score >= 0.7) {
            advice.append("High match. Prioritize filling these gaps: ");
        } else if (score >= 0.4) {
            advice.append("Medium match. Improve in phases: ");
        } else {
            advice.append("Low match. Focus first on these key skills: ");
        }

        advice.append(String.join(", ", missingSkills)).append(". ");
        advice.append("You can improve through coursework labs, small projects, or open-source tasks, and explain your learning plan in the application note.");
        return advice.toString();
    }

    private boolean isAiConfigured() {
        Config cfg = DataStore.getInstance().getConfig();
        String apiKey = cfg != null ? cfg.getDashscopeApiKey() : "";
        if (apiKey == null || apiKey.isBlank()) {
            apiKey = System.getenv("DASHSCOPE_API_KEY");
        }
        return apiKey != null && !apiKey.isBlank();
    }

    private String buildAiNotConfiguredAdvice() {
        return "AI advice is unavailable: DashScope API Key is not configured. Please set it in Admin > System Configuration and retry.";
    }

    private String buildAiUnavailableAdvice(List<String> missingSkills, double score) {
        String base = buildAdvice(missingSkills, score);
        return "AI request failed this time (possible network/endpoint/model config issue). Switched to local rule-based advice. "
                + base;
    }
}
