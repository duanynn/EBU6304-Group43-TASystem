package bupt.is.ta.service;

import bupt.is.ta.model.Config;
import bupt.is.ta.store.DataStore;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class AiAdviceService {
    private static final Gson GSON = new Gson();
    private static final String DEFAULT_ENDPOINT = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";
    private static final String DEFAULT_MODEL = "qwen-plus";
    /** Reuse connections to improve stability. */
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static void applyStableSampling(JsonObject payload, double temperature, double topP) {
        payload.addProperty("temperature", temperature);
        payload.addProperty("top_p", topP);
        payload.addProperty("frequency_penalty", 0.0);
        payload.addProperty("presence_penalty", 0.0);
    }
    public static class AiAnalysisResult {
        private final Double matchScore;
        private final String advice;
        private final List<String> strengths;
        private final List<String> gaps;
        private final String fitSummary;
        private final boolean fromAi;

        public AiAnalysisResult(Double matchScore, String advice, List<String> strengths, List<String> gaps, String fitSummary, boolean fromAi) {
            this.matchScore = matchScore;
            this.advice = advice;
            this.strengths = strengths == null ? List.of() : strengths;
            this.gaps = gaps == null ? List.of() : gaps;
            this.fitSummary = fitSummary == null ? "" : fitSummary;
            this.fromAi = fromAi;
        }

        public Double getMatchScore() {
            return matchScore;
        }

        public String getAdvice() {
            return advice;
        }

        public List<String> getStrengths() {
            return strengths;
        }

        public List<String> getGaps() {
            return gaps;
        }

        public String getFitSummary() {
            return fitSummary;
        }

        public boolean isFromAi() {
            return fromAi;
        }
    }

    public static class CvParseResult {
        private final String name;
        private final String summary;
        private final List<String> skills;
        private final String education;
        private final String projects;
        private final String awards;
        private final boolean fromAi;

        public CvParseResult(String name, String summary, List<String> skills, String education, String projects, String awards, boolean fromAi) {
            this.name = name == null ? "" : name.trim();
            this.summary = summary == null ? "" : summary.trim();
            this.skills = skills == null ? List.of() : skills;
            this.education = education == null ? "" : education.trim();
            this.projects = projects == null ? "" : projects.trim();
            this.awards = awards == null ? "" : awards.trim();
            this.fromAi = fromAi;
        }

        public String getName() { return name; }
        public String getSummary() { return summary; }
        public List<String> getSkills() { return skills; }
        public String getEducation() { return education; }
        public String getProjects() { return projects; }
        public String getAwards() { return awards; }
        public boolean isFromAi() { return fromAi; }
    }

    public String generateAdvice(List<String> requiredSkills,
                                 List<String> studentSkills,
                                 List<String> missingSkills,
                                 String profileSummary) {
        Config cfg = DataStore.getInstance().getConfig();
        String apiKey = cfg != null ? cfg.getDashscopeApiKey() : "";
        if (apiKey == null || apiKey.isBlank()) {
            apiKey = System.getenv("DASHSCOPE_API_KEY");
        }
        if (apiKey == null || apiKey.isBlank()) {
            return "";
        }

        try {
            String endpoint = cfg != null ? cfg.getDashscopeEndpoint() : "";
            if (endpoint == null || endpoint.isBlank()) {
                endpoint = System.getenv("DASHSCOPE_ENDPOINT");
            }
            if (endpoint == null || endpoint.isBlank()) {
                endpoint = DEFAULT_ENDPOINT;
            }
            String model = cfg != null ? cfg.getDashscopeModel() : "";
            if (model == null || model.isBlank()) {
                model = System.getenv("DASHSCOPE_MODEL");
            }
            if (model == null || model.isBlank()) {
                model = DEFAULT_MODEL;
            }

            JsonObject payload = new JsonObject();
            payload.addProperty("model", model);

            JsonArray messages = new JsonArray();
            JsonObject system = new JsonObject();
            system.addProperty("role", "system");
            system.addProperty("content", "You are a career assistant in a TA recruitment system. Provide concise, actionable skill-gap suggestions.");
            messages.add(system);

            JsonObject user = new JsonObject();
            user.addProperty("role", "user");
            user.addProperty("content", buildPrompt(requiredSkills, studentSkills, missingSkills, profileSummary));
            messages.add(user);
            payload.add("messages", messages);
            applyStableSampling(payload, 0.2, 0.75);

            HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .timeout(Duration.ofSeconds(15))
                    .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(payload)))
                    .build();

            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return "";
            }
            return extractContent(response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "";
        } catch (IOException e) {
            return "";
        } catch (Exception e) {
            return "";
        }
    }

    public AiAnalysisResult analyzeJobFit(List<String> requiredSkills,
                                          List<String> studentSkills,
                                          List<String> missingSkills,
                                          String profileSummary,
                                          String rawCvText) {
        Config cfg = DataStore.getInstance().getConfig();
        String apiKey = cfg != null ? cfg.getDashscopeApiKey() : "";
        if (apiKey == null || apiKey.isBlank()) {
            apiKey = System.getenv("DASHSCOPE_API_KEY");
        }
        if (apiKey == null || apiKey.isBlank()) {
            return new AiAnalysisResult(null, "", List.of(), List.of(), "", false);
        }
        try {
            String endpoint = cfg != null ? cfg.getDashscopeEndpoint() : "";
            if (endpoint == null || endpoint.isBlank()) endpoint = DEFAULT_ENDPOINT;
            String model = cfg != null ? cfg.getDashscopeModel() : "";
            if (model == null || model.isBlank()) model = DEFAULT_MODEL;

            JsonObject payload = new JsonObject();
            payload.addProperty("model", model);
            JsonArray messages = new JsonArray();
            JsonObject system = new JsonObject();
            system.addProperty("role", "system");
            system.addProperty("content",
                    "You are a TA recruitment evaluator. You must return only one valid JSON object."
                    + " Do not return Markdown, code fences, explanations, or any non-JSON content."
                    + " Keep outputs factual and consistent with resume evidence."
                    + " matchScore must be an integer from 0 to 100; strengths and gaps must be short phrases.");
            messages.add(system);
            JsonObject user = new JsonObject();
            user.addProperty("role", "user");
            user.addProperty("content", buildStructuredPrompt(requiredSkills, studentSkills, missingSkills, profileSummary, rawCvText));
            messages.add(user);
            payload.add("messages", messages);
            applyStableSampling(payload, 0.0, 0.1);
            JsonObject responseFormat = new JsonObject();
            responseFormat.addProperty("type", "json_object");
            payload.add("response_format", responseFormat);

            HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .timeout(Duration.ofSeconds(45))
                    .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(payload)))
                    .build();
            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return new AiAnalysisResult(null, "", List.of(), List.of(), "", false);
            }
            String content = extractContent(response.body());
            return parseAiResult(content);
        } catch (Exception e) {
            return new AiAnalysisResult(null, "", List.of(), List.of(), "", false);
        }
    }

    private String buildPrompt(List<String> requiredSkills,
                               List<String> studentSkills,
                               List<String> missingSkills,
                               String profileSummary) {
        return "Required skills: " + requiredSkills + "\n"
                + "Student skills: " + studentSkills + "\n"
                + "Missing skills: " + missingSkills + "\n"
                + "Profile summary: " + (profileSummary == null ? "" : profileSummary) + "\n\n"
                + "Please provide:\n"
                + "1) 2-3 prioritized improvement suggestions;\n"
                + "2) a short rationale for each suggestion;\n"
                + "3) concise English within 120 words.";
    }

    private String extractContent(String body) {
        JsonObject root = GSON.fromJson(body, JsonObject.class);
        if (root == null || !root.has("choices")) {
            return "";
        }
        JsonArray choices = root.getAsJsonArray("choices");
        if (choices == null || choices.isEmpty()) {
            return "";
        }
        JsonObject first = choices.get(0).getAsJsonObject();
        if (first == null || !first.has("message")) {
            return "";
        }
        JsonObject message = first.getAsJsonObject("message");
        if (message == null || !message.has("content")) {
            return "";
        }
        return message.get("content").getAsString().trim();
    }

    /** Strip markdown code fences for robust parsing. */
    private static String stripMarkdownCodeFence(String raw) {
        if (raw == null) {
            return "";
        }
        String s = raw.trim();
        if (s.startsWith("\uFEFF")) {
            s = s.substring(1).trim();
        }
        if (!s.startsWith("```")) {
            return s;
        }
        int firstNl = s.indexOf('\n');
        if (firstNl < 0) {
            return s.replace("```", "").trim();
        }
        s = s.substring(firstNl + 1);
        int fence = s.lastIndexOf("```");
        if (fence >= 0) {
            s = s.substring(0, fence);
        }
        return s.trim();
    }

    /**
     * Extract a balanced JSON object from text with prefixes/suffixes.
     */
    private static String extractBalancedJsonObject(String s) {
        if (s == null) {
            return "";
        }
        int start = s.indexOf('{');
        if (start < 0) {
            return s.trim();
        }
        int depth = 0;
        boolean inString = false;
        boolean escape = false;
        for (int i = start; i < s.length(); i++) {
            char c = s.charAt(i);
            if (escape) {
                escape = false;
                continue;
            }
            if (inString) {
                if (c == '\\') {
                    escape = true;
                } else if (c == '"') {
                    inString = false;
                }
                continue;
            }
            if (c == '"') {
                inString = true;
                continue;
            }
            if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0) {
                    return s.substring(start, i + 1);
                }
            }
        }
        int end = s.lastIndexOf('}');
        if (end > start) {
            return s.substring(start, end + 1);
        }
        return s.substring(start);
    }

    /** Repair common trailing-comma glitches from model output. */
    private static String repairCommonJsonGlitches(String json) {
        if (json == null || json.isEmpty()) {
            return json;
        }
        String t = json;
        String prev;
        int guard = 0;
        do {
            prev = t;
            t = t.replaceAll(",\\s*}", "}").replaceAll(",\\s*]", "]");
            guard++;
        } while (!t.equals(prev) && guard < 32);
        return t;
    }

    private JsonObject parseJsonObjectLenient(String rawContent) {
        if (rawContent == null || rawContent.isBlank()) {
            return null;
        }
        String s = stripMarkdownCodeFence(rawContent.trim());
        String candidate = extractBalancedJsonObject(s);
        if (candidate.isBlank()) {
            return null;
        }
        try {
            JsonElement el = JsonParser.parseString(candidate);
            if (el != null && el.isJsonObject()) {
                return el.getAsJsonObject();
            }
        } catch (Exception ignored) {
        }
        try {
            String fixed = repairCommonJsonGlitches(candidate);
            JsonElement el = JsonParser.parseString(fixed);
            if (el != null && el.isJsonObject()) {
                return el.getAsJsonObject();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static Double parseMatchScoreField(JsonObject json) {
        if (json == null || !json.has("matchScore")) {
            return null;
        }
        JsonElement el = json.get("matchScore");
        if (el == null || el.isJsonNull()) {
            return null;
        }
        try {
            if (el.isJsonPrimitive()) {
                JsonPrimitive p = el.getAsJsonPrimitive();
                if (p.isNumber()) {
                    double v = p.getAsDouble();
                    v = Math.round(v);
                    return Math.max(0, Math.min(100, v));
                }
                if (p.isString()) {
                    String s = p.getAsString().trim().replace("%", "").trim();
                    double v = Double.parseDouble(s);
                    v = Math.round(v);
                    return Math.max(0, Math.min(100, v));
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static String getStringFlexible(JsonObject json, String key, String defaultVal) {
        if (json == null || !json.has(key)) {
            return defaultVal;
        }
        JsonElement el = json.get(key);
        if (el == null || el.isJsonNull()) {
            return defaultVal;
        }
        try {
            if (el.isJsonPrimitive()) {
                JsonPrimitive p = el.getAsJsonPrimitive();
                if (p.isString()) {
                    return p.getAsString().trim();
                }
                if (p.isNumber()) {
                    return String.valueOf(p.getAsDouble());
                }
                if (p.isBoolean()) {
                    return p.getAsBoolean() ? "true" : "false";
                }
            }
            if (el.isJsonArray() || el.isJsonObject()) {
                return el.toString();
            }
        } catch (Exception ignored) {
        }
        return defaultVal;
    }

    private List<String> toStringListLenient(JsonObject json, String key) {
        if (json == null || !json.has(key)) {
            return List.of();
        }
        JsonElement el = json.get(key);
        if (el == null || el.isJsonNull()) {
            return List.of();
        }
        if (el.isJsonArray()) {
            List<String> out = new ArrayList<>();
            JsonArray arr = el.getAsJsonArray();
            for (int i = 0; i < arr.size(); i++) {
                JsonElement item = arr.get(i);
                if (item == null || item.isJsonNull()) {
                    continue;
                }
                if (item.isJsonPrimitive()) {
                    String s = item.getAsString().trim();
                    if (!s.isEmpty()) {
                        out.add(s);
                    }
                } else {
                    String s = item.toString().trim();
                    if (!s.isEmpty()) {
                        out.add(s);
                    }
                }
            }
            return out;
        }
        if (el.isJsonPrimitive()) {
            String s = el.getAsString().trim();
            if (s.startsWith("[") && s.endsWith("]")) {
                try {
                    JsonElement inner = JsonParser.parseString(s);
                    if (inner.isJsonArray()) {
                        return toStringListLenientFromArray(inner.getAsJsonArray());
                    }
                } catch (Exception ignored) {
                }
            }
            List<String> out = new ArrayList<>();
            for (String part : s.split("[,，、;；\n]+")) {
                String t = part.trim();
                if (!t.isEmpty()) {
                    out.add(t);
                }
            }
            return out;
        }
        return List.of();
    }

    private static List<String> toStringListLenientFromArray(JsonArray arr) {
        List<String> out = new ArrayList<>();
        for (int i = 0; i < arr.size(); i++) {
            JsonElement item = arr.get(i);
            if (item != null && item.isJsonPrimitive()) {
                String s = item.getAsString().trim();
                if (!s.isEmpty()) {
                    out.add(s);
                }
            }
        }
        return out;
    }

    private String buildStructuredPrompt(List<String> requiredSkills,
                                         List<String> studentSkills,
                                         List<String> missingSkills,
                                         String profileSummary,
                                         String rawCvText) {
        String cvSnippet = rawCvText == null ? "" : rawCvText.substring(0, Math.min(rawCvText.length(), 1500));
        return "Evaluate candidate-job fit using the details below.\n\n"
                + "=== Required Skills ===\n" + requiredSkills + "\n\n"
                + "=== Candidate Skills ===\n" + studentSkills + "\n\n"
                + "=== Missing Skills ===\n" + missingSkills + "\n\n"
                + "=== Resume Summary ===\n" + (profileSummary == null ? "" : profileSummary) + "\n\n"
                + "=== Resume Text ===\n" + cvSnippet + "\n\n"
                + "Return strictly in this JSON format (no extra content):\n"
                + "{\n"
                + "  \"matchScore\": 75,\n"
                + "  \"fitSummary\": \"Strong programming fundamentals with limited database experience\",\n"
                + "  \"strengths\": [\"Proficient in Python and Java\", \"Hands-on project development experience\"],\n"
                + "  \"gaps\": [\"Limited database optimization experience\", \"No distributed systems experience yet\"],\n"
                + "  \"advice\": \"Strengthen database fundamentals through focused exercises on SQL tuning, and gain distributed systems exposure through a small open-source contribution project.\"\n"
                + "}\n\n"
                + "Field rules (must follow):\n"
                + "- matchScore: integer 0-100 (no decimals)\n"
                + "- fitSummary: one-sentence summary (15-40 words)\n"
                + "- strengths: 2-4 items, each evidence-based from resume/skills\n"
                + "- gaps: 1-3 items, or [] if no obvious gaps\n"
                + "- advice: specific and actionable suggestions (80-200 words)";
    }

    private AiAnalysisResult parseAiResult(String content) {
        if (content == null || content.isBlank()) {
            return new AiAnalysisResult(null, "", List.of(), List.of(), "", false);
        }
        String raw = content.trim();
        JsonObject json = parseJsonObjectLenient(raw);
        if (json == null) {
            return new AiAnalysisResult(null, raw, List.of(), List.of(), "", true);
        }
        Double score = parseMatchScoreField(json);
        String advice = getStringFlexible(json, "advice", raw);
        String fitSummary = getStringFlexible(json, "fitSummary", "");
        List<String> strengths = toStringListLenient(json, "strengths");
        List<String> gaps = toStringListLenient(json, "gaps");
        return new AiAnalysisResult(score, advice, strengths, gaps, fitSummary, true);
    }

    public AiAnalysisResult analyzeCvStructure(String rawCvText) {
        Config cfg = DataStore.getInstance().getConfig();
        String apiKey = cfg != null ? cfg.getDashscopeApiKey() : "";
        if (apiKey == null || apiKey.isBlank()) return new AiAnalysisResult(null, "", List.of(), List.of(), "", false);
        try {
            String endpoint = cfg.getDashscopeEndpoint() == null || cfg.getDashscopeEndpoint().isBlank() ? DEFAULT_ENDPOINT : cfg.getDashscopeEndpoint();
            String model = cfg.getDashscopeModel() == null || cfg.getDashscopeModel().isBlank() ? DEFAULT_MODEL : cfg.getDashscopeModel();
            JsonObject payload = new JsonObject();
            payload.addProperty("model", model);
            JsonArray messages = new JsonArray();
            JsonObject system = new JsonObject();
            system.addProperty("role", "system");
            system.addProperty("content", "You are a resume parsing assistant. Return JSON only.");
            messages.add(system);
            JsonObject user = new JsonObject();
            user.addProperty("role", "user");
            user.addProperty("content",
                    "Extract structured information from the resume text below. Return JSON only:"
                            + "{\"fitSummary\":\"summary\",\"strengths\":[\"skills\"],\"gaps\":[\"education\",\"projects\",\"certificates\"],\"advice\":\"project details\"}\n"
                            + "Resume text: " + (rawCvText == null ? "" : rawCvText.substring(0, Math.min(rawCvText.length(), 3000))));
            messages.add(user);
            payload.add("messages", messages);
            applyStableSampling(payload, 0.05, 0.2);
            JsonObject responseFormatCv = new JsonObject();
            responseFormatCv.addProperty("type", "json_object");
            payload.add("response_format", responseFormatCv);
            HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .timeout(Duration.ofSeconds(45))
                    .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(payload)))
                    .build();
            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return new AiAnalysisResult(null, "", List.of(), List.of(), "", false);
            }
            return parseAiResult(extractContent(response.body()));
        } catch (Exception e) {
            return new AiAnalysisResult(null, "", List.of(), List.of(), "", false);
        }
    }

    public CvParseResult analyzeCvStructuredFields(String rawCvText) {
        Config cfg = DataStore.getInstance().getConfig();
        String apiKey = cfg != null ? cfg.getDashscopeApiKey() : "";
        if (apiKey == null || apiKey.isBlank()) return new CvParseResult("", "", List.of(), "", "", "", false);
        try {
            String endpoint = cfg.getDashscopeEndpoint() == null || cfg.getDashscopeEndpoint().isBlank() ? DEFAULT_ENDPOINT : cfg.getDashscopeEndpoint();
            String model = cfg.getDashscopeModel() == null || cfg.getDashscopeModel().isBlank() ? DEFAULT_MODEL : cfg.getDashscopeModel();
            String cvSnippet = rawCvText == null ? "" : rawCvText.substring(0, Math.min(rawCvText.length(), 3500));
            JsonObject payload = new JsonObject();
            payload.addProperty("model", model);
            JsonArray messages = new JsonArray();
            JsonObject system = new JsonObject();
            system.addProperty("role", "system");
            system.addProperty("content",
                    "You are a resume information extraction expert. Return only one valid JSON object."
                    + " Do not return Markdown, code fences, or any non-JSON content."
                    + " Use English for all fields. If uncertain, return an empty string or empty array.");
            messages.add(system);
            JsonObject user = new JsonObject();
            user.addProperty("role", "user");
            user.addProperty("content",
                    "Extract structured information from the resume text below.\n\n"
                    + "=== Resume Text ===\n" + cvSnippet + "\n\n"
                    + "Return strictly in the following JSON format (no extra content):\n"
                    + "{\n"
                    + "  \"name\": \"Alex Zhang\",\n"
                    + "  \"summary\": \"Undergraduate student in Computer Science with Python and Java development experience and AI research exposure\",\n"
                    + "  \"skills\": [\"Python\", \"Java\", \"PyTorch\"],\n"
                    + "  \"education\": \"BUPT, Computer Science, Bachelor's Degree, 2022-2026\",\n"
                    + "  \"projects\": \"1. OVON navigation research using UniGoal with algorithm improvements\\n2. Pneumonia image detection using ResNet50 + self-attention for multi-class classification\",\n"
                    + "  \"awards\": \"National Second Prize in Statistical Modeling Competition; Huawei ICT Competition Beijing Regional Second Prize; University Scholarship\"\n"
                    + "}\n\n"
                    + "Field rules:\n"
                    + "- name: candidate's real name\n"
                    + "- summary: profile summary (50-100 words)\n"
                    + "- skills: skill tags array\n"
                    + "- education: education details (school + major + degree + period)\n"
                    + "- projects: project experience (numbered if possible)\n"
                    + "- awards: awards separated by semicolons\n"
                    + "If uncertain, return empty string or empty array. Do not return placeholders like \"N/A\".");
            messages.add(user);
            payload.add("messages", messages);
            applyStableSampling(payload, 0.0, 0.15);
            JsonObject responseFormat = new JsonObject();
            responseFormat.addProperty("type", "json_object");
            payload.add("response_format", responseFormat);
            HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .timeout(Duration.ofSeconds(45))
                    .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(payload)))
                    .build();
            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return new CvParseResult("", "", List.of(), "", "", "", false);
            }
            String content = extractContent(response.body());
            return parseCvParseResult(content);
        } catch (Exception e) {
            return new CvParseResult("", "", List.of(), "", "", "", false);
        }
    }

    private CvParseResult parseCvParseResult(String content) {
        if (content == null || content.isBlank()) {
            return new CvParseResult("", "", List.of(), "", "", "", false);
        }
        JsonObject json = parseJsonObjectLenient(content.trim());
        if (json == null) {
            return new CvParseResult("", "", List.of(), "", "", "", false);
        }
        String name = getStringFlexible(json, "name", "");
        String summary = getStringFlexible(json, "summary", "");
        List<String> skills = toStringListLenient(json, "skills");
        String education = getStringFlexible(json, "education", "");
        String projects = getStringFlexible(json, "projects", "");
        String awards = getStringFlexible(json, "awards", "");
        if (awards.isBlank()) {
            awards = getStringFlexible(json, "certificates", "");
        }
        return new CvParseResult(name, summary, skills, education, projects, awards, true);
    }
}
