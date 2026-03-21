package bupt.is.ta.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SkillMatchService {

    public static class MatchResult {
        private final List<String> requiredSkills;
        private final List<String> studentSkills;
        private final List<String> matchedSkills;
        private final List<String> missingSkills;
        private final double score; // 0.0 - 1.0
        private final String aiAdvice; // 预留给 AI，自然语言解释，可为空

        public MatchResult(List<String> requiredSkills,
                           List<String> studentSkills,
                           List<String> matchedSkills,
                           List<String> missingSkills,
                           double score,
                           String aiAdvice) {
            this.requiredSkills = requiredSkills;
            this.studentSkills = studentSkills;
            this.matchedSkills = matchedSkills;
            this.missingSkills = missingSkills;
            this.score = score;
            this.aiAdvice = aiAdvice;
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
    }

    public MatchResult match(List<String> required, List<String> student) {
        Set<String> requiredSet = new HashSet<>(required);
        Set<String> studentSet = new HashSet<>(student);

        Set<String> matched = new HashSet<>(requiredSet);
        matched.retainAll(studentSet);

        Set<String> missing = new HashSet<>(requiredSet);
        missing.removeAll(studentSet);

        double score = requiredSet.isEmpty()
                ? 1.0
                : (double) matched.size() / (double) requiredSet.size();

        // 这里暂时不接入大模型，aiAdvice 可以在将来扩展为调用外部 API。
        String aiAdvice = "";

        return new MatchResult(
                List.copyOf(requiredSet),
                List.copyOf(studentSet),
                List.copyOf(matched),
                List.copyOf(missing),
                score,
                aiAdvice
        );
    }
}

