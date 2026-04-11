package bupt.is.ta.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UserProfile {
    public static class JobAiAdviceCache {
        private String jobId = "";
        private String signature = "";
        private double aiScore = -1;
        private String aiAdvice = "";
        private List<String> aiStrengths = new ArrayList<>();
        private List<String> aiGaps = new ArrayList<>();
        private String aiFitSummary = "";
        private boolean aiGenerated = false;
        private long cachedAt = 0;

        public String getJobId() { return jobId; }
        public void setJobId(String jobId) { this.jobId = jobId; }
        public String getSignature() { return signature; }
        public void setSignature(String signature) { this.signature = signature; }
        public double getAiScore() { return aiScore; }
        public void setAiScore(double aiScore) { this.aiScore = aiScore; }
        public String getAiAdvice() { return aiAdvice; }
        public void setAiAdvice(String aiAdvice) { this.aiAdvice = aiAdvice; }
        public List<String> getAiStrengths() { return aiStrengths; }
        public void setAiStrengths(List<String> aiStrengths) { this.aiStrengths = aiStrengths; }
        public List<String> getAiGaps() { return aiGaps; }
        public void setAiGaps(List<String> aiGaps) { this.aiGaps = aiGaps; }
        public String getAiFitSummary() { return aiFitSummary; }
        public void setAiFitSummary(String aiFitSummary) { this.aiFitSummary = aiFitSummary; }
        public boolean isAiGenerated() { return aiGenerated; }
        public void setAiGenerated(boolean aiGenerated) { this.aiGenerated = aiGenerated; }
        public long getCachedAt() { return cachedAt; }
        public void setCachedAt(long cachedAt) { this.cachedAt = cachedAt; }
    }

    private String extractedName = "";
    private String summary = "";
    private String education = "";
    private String projects = "";
    private String certificates = "";
    private List<String> extractedSkills = new ArrayList<>();
    private String rawCvText = "";
    private String lastParsedAt = "";

    private double lastAiScore = -1;
    private String lastAiAdvice = "";
    private List<String> lastAiStrengths = new ArrayList<>();
    private List<String> lastAiGaps = new ArrayList<>();
    private String lastAiFitSummary = "";
    private long lastAiAdviceTime = 0;
    private List<JobAiAdviceCache> jobAiAdviceCaches = new ArrayList<>();

    public String getExtractedName() { return extractedName; }
    public void setExtractedName(String extractedName) { this.extractedName = extractedName; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }

    public String getProjects() { return projects; }
    public void setProjects(String projects) { this.projects = projects; }

    public String getCertificates() { return certificates; }
    public void setCertificates(String certificates) { this.certificates = certificates; }

    public List<String> getExtractedSkills() { return extractedSkills; }
    public void setExtractedSkills(List<String> extractedSkills) { this.extractedSkills = extractedSkills; }

    public String getRawCvText() { return rawCvText; }
    public void setRawCvText(String rawCvText) { this.rawCvText = rawCvText; }

    public String getLastParsedAt() { return lastParsedAt; }
    public void setLastParsedAt(String lastParsedAt) { this.lastParsedAt = lastParsedAt; }

    public double getLastAiScore() { return lastAiScore; }
    public void setLastAiScore(double lastAiScore) { this.lastAiScore = lastAiScore; }

    public String getLastAiAdvice() { return lastAiAdvice; }
    public void setLastAiAdvice(String lastAiAdvice) { this.lastAiAdvice = lastAiAdvice; }

    public List<String> getLastAiStrengths() { return lastAiStrengths; }
    public void setLastAiStrengths(List<String> lastAiStrengths) { this.lastAiStrengths = lastAiStrengths; }

    public List<String> getLastAiGaps() { return lastAiGaps; }
    public void setLastAiGaps(List<String> lastAiGaps) { this.lastAiGaps = lastAiGaps; }

    public String getLastAiFitSummary() { return lastAiFitSummary; }
    public void setLastAiFitSummary(String lastAiFitSummary) { this.lastAiFitSummary = lastAiFitSummary; }

    public long getLastAiAdviceTime() { return lastAiAdviceTime; }
    public void setLastAiAdviceTime(long lastAiAdviceTime) { this.lastAiAdviceTime = lastAiAdviceTime; }
    public List<JobAiAdviceCache> getJobAiAdviceCaches() { return jobAiAdviceCaches; }
    public void setJobAiAdviceCaches(List<JobAiAdviceCache> jobAiAdviceCaches) {
        this.jobAiAdviceCaches = jobAiAdviceCaches == null ? new ArrayList<>() : jobAiAdviceCaches;
    }

    public boolean isAiCacheValid() {
        if (lastAiAdviceTime <= 0 || lastAiAdvice == null || lastAiAdvice.isBlank()) return false;
        return (System.currentTimeMillis() - lastAiAdviceTime) < 30 * 60 * 1000L;
    }

    public JobAiAdviceCache findJobAiAdviceCache(String jobId, String signature) {
        if (jobAiAdviceCaches == null || jobAiAdviceCaches.isEmpty()) return null;
        for (JobAiAdviceCache item : jobAiAdviceCaches) {
            if (item == null) continue;
            if (Objects.equals(jobId, item.getJobId()) && Objects.equals(signature, item.getSignature())) {
                return item;
            }
        }
        return null;
    }
}
