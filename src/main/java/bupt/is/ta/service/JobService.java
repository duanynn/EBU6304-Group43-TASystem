package bupt.is.ta.service;

import bupt.is.ta.model.Job;
import bupt.is.ta.store.DataStore;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class JobService {

    private static final Pattern TOKEN_PATTERN = Pattern.compile("[\\p{L}\\p{N}]+");
    private static final double BM25_K1 = 1.5;
    private static final double BM25_B = 0.75;

    private final DataStore store = DataStore.getInstance();

    public static class JobSearchResult {
        private final String query;
        private final boolean searched;
        private final int totalOpenJobs;
        private final List<Job> jobs;
        private final Map<String, Double> bm25Scores;
        private final Map<String, Integer> normalizedScores;

        public JobSearchResult(String query,
                               boolean searched,
                               int totalOpenJobs,
                               List<Job> jobs,
                               Map<String, Double> bm25Scores,
                               Map<String, Integer> normalizedScores) {
            this.query = query == null ? "" : query;
            this.searched = searched;
            this.totalOpenJobs = totalOpenJobs;
            this.jobs = jobs == null ? List.of() : List.copyOf(jobs);
            this.bm25Scores = bm25Scores == null ? Map.of() : Map.copyOf(bm25Scores);
            this.normalizedScores = normalizedScores == null ? Map.of() : Map.copyOf(normalizedScores);
        }

        public String getQuery() {
            return query;
        }

        public boolean isSearched() {
            return searched;
        }

        public int getTotalOpenJobs() {
            return totalOpenJobs;
        }

        public List<Job> getJobs() {
            return jobs;
        }

        public Map<String, Double> getBm25Scores() {
            return bm25Scores;
        }

        public Map<String, Integer> getNormalizedScores() {
            return normalizedScores;
        }
    }

    public List<Job> listOpenJobs() {
        return store.getJobs().stream()
                .filter(Job::isOpen)
                .collect(Collectors.toList());
    }

    public List<Job> listJobsByMo(String moId) {
        return store.getJobs().stream()
                .filter(j -> moId.equals(j.getMoId()))
                .collect(Collectors.toList());
    }

    public Optional<Job> findById(String id) {
        return store.getJobs().stream()
                .filter(j -> j.getId().equals(id))
                .findFirst();
    }

    public JobSearchResult searchOpenJobs(String query) {
        return searchOpenJobs(query, null);
    }

    public JobSearchResult searchOpenJobs(String query, Job.JobType jobTypeFilter) {
        String cleanQuery = query == null ? "" : query.trim();
        List<Job> openJobs = listOpenJobs();
        if (jobTypeFilter != null) {
            openJobs = openJobs.stream()
                    .filter(j -> jobTypeFilter == j.getJobType())
                    .collect(Collectors.toList());
        }
        List<String> queryTerms = tokenize(cleanQuery);
        if (queryTerms.isEmpty()) {
            List<Job> ordered = openJobs.stream()
                    .sorted(Comparator.comparing(JobService::createdAtOrEpoch).reversed())
                    .collect(Collectors.toList());
            return new JobSearchResult(cleanQuery, false, openJobs.size(), ordered, Map.of(), Map.of());
        }

        Map<String, List<String>> documentTokens = new HashMap<>();
        Map<String, Map<String, Integer>> termFrequency = new HashMap<>();
        Map<String, Integer> documentFrequency = new HashMap<>();
        int totalLength = 0;

        for (Job job : openJobs) {
            List<String> tokens = tokenizeJob(job);
            documentTokens.put(job.getId(), tokens);
            totalLength += tokens.size();
            Map<String, Integer> tf = new HashMap<>();
            for (String token : tokens) {
                tf.merge(token, 1, Integer::sum);
            }
            termFrequency.put(job.getId(), tf);
            for (String term : tf.keySet()) {
                documentFrequency.merge(term, 1, Integer::sum);
            }
        }

        double averageLength = openJobs.isEmpty() ? 0.0 : (double) totalLength / openJobs.size();
        Map<String, Double> scores = new LinkedHashMap<>();
        for (Job job : openJobs) {
            double score = scoreJob(queryTerms, documentTokens.get(job.getId()), termFrequency.get(job.getId()),
                    documentFrequency, openJobs.size(), averageLength);
            if (score > 0.0) {
                scores.put(job.getId(), score);
            }
        }

        List<Job> ranked = openJobs.stream()
                .filter(job -> scores.containsKey(job.getId()))
                .sorted(Comparator
                        .comparingDouble((Job job) -> scores.getOrDefault(job.getId(), 0.0)).reversed()
                        .thenComparing(Comparator.comparing(JobService::createdAtOrEpoch).reversed()))
                .collect(Collectors.toList());

        return new JobSearchResult(cleanQuery, true, openJobs.size(), ranked, scores, normalizeScores(scores));
    }

    public void save(Job job) throws Exception {
        synchronized (store) {
            if (job.getId() == null) {
                store.addJob(job);
            } else {
                store.updateJob(job);
            }
            store.saveAll();
        }
    }

    public void updateOpenState(String jobId, boolean open) throws Exception {
        Job job = findById(jobId).orElseThrow(() -> new IllegalArgumentException("Job not found"));
        job.setOpen(open);
        save(job);
    }

    private static double scoreJob(List<String> queryTerms,
                                   List<String> documentTokens,
                                   Map<String, Integer> termFrequency,
                                   Map<String, Integer> documentFrequency,
                                   int totalDocuments,
                                   double averageLength) {
        if (documentTokens == null || documentTokens.isEmpty() || averageLength <= 0.0 || totalDocuments == 0) {
            return 0.0;
        }
        double score = 0.0;
        int documentLength = documentTokens.size();
        for (String term : queryTerms) {
            int tf = termFrequency == null ? 0 : termFrequency.getOrDefault(term, 0);
            if (tf == 0) continue;
            int df = documentFrequency.getOrDefault(term, 0);
            double idf = Math.log(1.0 + (totalDocuments - df + 0.5) / (df + 0.5));
            double denominator = tf + BM25_K1 * (1.0 - BM25_B + BM25_B * documentLength / averageLength);
            score += idf * (tf * (BM25_K1 + 1.0)) / denominator;
        }
        return score;
    }

    private static List<String> tokenizeJob(Job job) {
        List<String> tokens = new ArrayList<>();
        addTokens(tokens, job.getCourseName(), 3);
        addTokens(tokens, job.getDescription(), 2);
        addTokens(tokens, job.getRequiredWorkTime(), 1);
        addTokens(tokens, job.getJobType() == null ? "module" : job.getJobType().name().toLowerCase(Locale.ROOT).replace('_', ' '), 3);
        if (job.getRequiredSkills() != null) {
            for (String skill : job.getRequiredSkills()) {
                addTokens(tokens, skill, 4);
            }
        }
        return tokens;
    }

    private static void addTokens(List<String> target, String value, int weight) {
        if (value == null || value.isBlank()) {
            return;
        }
        List<String> tokens = tokenize(value);
        for (int i = 0; i < Math.max(1, weight); i++) {
            target.addAll(tokens);
        }
    }

    private static List<String> tokenize(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        List<String> tokens = new ArrayList<>();
        Matcher matcher = TOKEN_PATTERN.matcher(value.toLowerCase(Locale.ROOT));
        while (matcher.find()) {
            String token = matcher.group().trim();
            if (!token.isBlank()) {
                tokens.add(token);
            }
        }
        return tokens;
    }

    private static Map<String, Integer> normalizeScores(Map<String, Double> scores) {
        if (scores == null || scores.isEmpty()) {
            return Map.of();
        }
        double max = scores.values().stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        if (max <= 0.0) {
            return Map.of();
        }
        Map<String, Integer> normalized = new LinkedHashMap<>();
        for (Map.Entry<String, Double> entry : scores.entrySet()) {
            int pct = (int) Math.round(Math.max(0.0, Math.min(1.0, entry.getValue() / max)) * 100.0);
            normalized.put(entry.getKey(), pct);
        }
        return normalized;
    }

    private static Instant createdAtOrEpoch(Job job) {
        return job == null || job.getCreatedAt() == null ? Instant.EPOCH : job.getCreatedAt();
    }
}

