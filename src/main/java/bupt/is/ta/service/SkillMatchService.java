package bupt.is.ta.service;

import bupt.is.ta.model.Config;
import bupt.is.ta.store.DataStore;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SkillMatchService {
    private final AiAdviceService aiAdviceService = new AiAdviceService();

    public static class MatchResult {
        private final List<String> requiredSkills;
        private final List<String> studentSkills;
        private final List<String> matchedSkills;
        private final List<String> missingSkills;
        private final double score; // 0.0 - 1.0
        private final double aiScore; // 0 - 100
        private final String aiAdvice; // AI natural-language advice, may be empty
        private final List<String> aiStrengths;
        private final List<String> aiGaps;
        private final String aiFitSummary;
        private final boolean aiGenerated;

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
    }

    public MatchResult match(List<String> required, List<String> student) {
        return match(required, student, "", "", true);
    }

    public MatchResult match(List<String> required, List<String> student, String profileSummary) {
        return match(required, student, profileSummary, "", true);
    }

    public MatchResult match(List<String> required, List<String> student, String profileSummary, String rawCvText) {
        return match(required, student, profileSummary, rawCvText, true);
    }

    public MatchResult match(List<String> required, List<String> student, String profileSummary, String rawCvText, boolean enableAi) {
        Set<String> requiredSet = new HashSet<>(required);
        Set<String> studentSet = new HashSet<>(student);

        Set<String> matched = new HashSet<>(requiredSet);
        matched.retainAll(studentSet);

        Set<String> missing = new HashSet<>(requiredSet);
        missing.removeAll(studentSet);

        double score = requiredSet.isEmpty()
                ? 1.0
                : (double) matched.size() / (double) requiredSet.size();

        AiAdviceService.AiAnalysisResult aiResult = enableAi
                ? aiAdviceService.analyzeJobFit(List.copyOf(requiredSet), List.copyOf(studentSet), List.copyOf(missing), profileSummary, rawCvText)
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

        return new MatchResult(
                List.copyOf(requiredSet),
                List.copyOf(studentSet),
                List.copyOf(matched),
                List.copyOf(missing),
                score,
                aiScore,
                aiAdvice,
                aiStrengths,
                aiGaps,
                aiFitSummary,
                aiGenerated
        );
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

