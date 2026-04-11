package bupt.is.ta.service;

import bupt.is.ta.model.UserProfile;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CvParsingService {
    private final AiAdviceService aiAdviceService = new AiAdviceService();
    private static final Pattern SKILL_PATTERN = Pattern.compile(
            "(java|python|c\\+\\+|javascript|typescript|sql|html|css|spring|servlet|jsp|git|linux|docker|tensorflow|pytorch)",
            Pattern.CASE_INSENSITIVE
    );
    private static final List<String> EDUCATION_HEADERS = List.of("education", "academic", "学历", "教育背景", "学习经历");
    private static final List<String> PROJECT_HEADERS = List.of("project", "projects", "experience", "项目", "项目经历", "实践经历");
    private static final List<String> CERT_HEADERS = List.of("certificate", "certification", "证书", "资格");
    private static final List<String> SKILL_HEADERS = List.of("skills", "technical skills", "技能", "专业技能");
    private static final Pattern NAME_PATTERN = Pattern.compile("(?:姓名|Name)\\s*[:：]\\s*([\\u4e00-\\u9fa5A-Za-z·\\s]{2,30})");

    public UserProfile parseCvFile(Path cvPath) throws IOException {
        String lower = cvPath.getFileName().toString().toLowerCase(Locale.ROOT);
        String text;
        if (lower.endsWith(".pdf")) {
            text = parsePdf(cvPath);
        } else if (lower.endsWith(".docx")) {
            text = parseDocx(cvPath);
        } else if (lower.endsWith(".doc")) {
            text = parseDoc(cvPath);
        } else {
            throw new IOException("Unsupported CV file type: " + lower);
        }
        return extractProfile(text);
    }

    private String parsePdf(Path path) throws IOException {
        try (PDDocument doc = Loader.loadPDF(path.toFile())) {
            return new PDFTextStripper().getText(doc);
        }
    }

    private String parseDoc(Path path) throws IOException {
        try (FileInputStream in = new FileInputStream(path.toFile());
             HWPFDocument doc = new HWPFDocument(in);
             WordExtractor extractor = new WordExtractor(doc)) {
            return String.join("\n", extractor.getParagraphText());
        }
    }

    private String parseDocx(Path path) throws IOException {
        try (FileInputStream in = new FileInputStream(path.toFile());
             XWPFDocument doc = new XWPFDocument(in);
             XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
            return extractor.getText();
        }
    }

    private UserProfile extractProfile(String text) {
        String normalized = text == null ? "" : text.replace("\r", "\n").trim();
        List<String> lines = Arrays.stream(normalized.split("\n"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();

        UserProfile profile = new UserProfile();
        profile.setExtractedName(extractName(normalized));
        profile.setRawCvText(limit(normalized, 8000));
        profile.setSummary(buildSummary(lines));
        String education = extractSectionByHeading(lines, EDUCATION_HEADERS, 8);
        String projects = extractSectionByHeading(lines, PROJECT_HEADERS, 10);
        String certificates = extractSectionByHeading(lines, CERT_HEADERS, 6);
        List<String> skills = extractSkills(normalized, lines);

        // AI structured parse first; fallback to heuristic sections.
        AiAdviceService.CvParseResult structured = aiAdviceService.analyzeCvStructuredFields(normalized);
        if (structured.isFromAi()) {
            if (isMeaningful(structured.getName())) {
                profile.setExtractedName(limit(structured.getName(), 30));
            }
            if (isMeaningful(structured.getSummary())) {
                profile.setSummary(limit(structured.getSummary(), 280));
            }
            if (structured.getSkills() != null && !structured.getSkills().isEmpty()) {
                skills = structured.getSkills().stream().filter(this::isMeaningful).toList();
            }
            if (isMeaningful(structured.getEducation())) {
                education = structured.getEducation();
            }
            if (isMeaningful(structured.getProjects())) {
                projects = structured.getProjects();
            }
            if (isMeaningful(structured.getAwards())) {
                certificates = structured.getAwards();
            }
        }
        profile.setEducation(limit(formatBullets(education), 700));
        profile.setProjects(limit(formatBullets(projects), 900));
        profile.setCertificates(limit(formatBullets(certificates), 700));
        profile.setExtractedSkills(skills);
        profile.setLastParsedAt(Instant.now().toString());
        return profile;
    }

    private List<String> extractSkills(String text, List<String> lines) {
        Set<String> skills = new LinkedHashSet<>();
        String skillSection = extractSectionByHeading(lines, SKILL_HEADERS, 8);
        String skillSource = (skillSection == null || skillSection.isBlank())
                ? text
                : skillSection + "\n" + text;
        Matcher matcher = SKILL_PATTERN.matcher(skillSource == null ? "" : skillSource);
        while (matcher.find()) {
            String skill = matcher.group(1).toLowerCase(Locale.ROOT);
            if ("c++".equals(skill)) {
                skills.add("C++");
            } else if ("sql".equals(skill)) {
                skills.add("SQL");
            } else {
                skills.add(skill.substring(0, 1).toUpperCase(Locale.ROOT) + skill.substring(1));
            }
        }
        return new ArrayList<>(skills);
    }

    private String extractSectionByHeading(List<String> lines, List<String> headings, int maxLines) {
        if (lines == null || lines.isEmpty()) {
            return "";
        }
        int start = -1;
        for (int i = 0; i < lines.size(); i++) {
            String lineLower = lines.get(i).toLowerCase(Locale.ROOT);
            for (String head : headings) {
                if (lineLower.contains(head.toLowerCase(Locale.ROOT))) {
                    start = i;
                    break;
                }
            }
            if (start >= 0) {
                break;
            }
        }
        if (start < 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int captured = 0;
        for (int i = start + 1; i < lines.size() && captured < maxLines; i++) {
            String line = lines.get(i);
            String lineLower = line.toLowerCase(Locale.ROOT);
            if (isHeadingLike(lineLower)) {
                break;
            }
            sb.append(line).append(" ");
            captured++;
        }
        if (sb.isEmpty()) {
            return "";
        }
        return limit(sb.toString().replaceAll("\\s+", " ").trim(), 700);
    }

    private boolean isHeadingLike(String lineLower) {
        if (lineLower.length() > 32) {
            return false;
        }
        return EDUCATION_HEADERS.stream().anyMatch(lineLower::contains)
                || PROJECT_HEADERS.stream().anyMatch(lineLower::contains)
                || CERT_HEADERS.stream().anyMatch(lineLower::contains)
                || SKILL_HEADERS.stream().anyMatch(lineLower::contains);
    }

    private String buildSummary(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return "";
        }
        String compact = lines.stream()
                .filter(line -> !isHeadingLike(line.toLowerCase(Locale.ROOT)))
                .limit(5)
                .reduce("", (a, b) -> a + " " + b)
                .replaceAll("\\s+", " ")
                .trim();
        return limit(compact, 280);
    }

    private String limit(String text, int maxLen) {
        if (text == null) {
            return "";
        }
        if (text.length() <= maxLen) {
            return text;
        }
        return text.substring(0, maxLen);
    }

    private String extractName(String text) {
        if (text == null || text.isBlank()) return "";
        Matcher matcher = NAME_PATTERN.matcher(text);
        if (!matcher.find()) return "";
        String name = matcher.group(1) == null ? "" : matcher.group(1).trim();
        return limit(name.replaceAll("\\s+", ""), 30);
    }

    private boolean isMeaningful(String text) {
        if (text == null) return false;
        String value = text.trim();
        if (value.isBlank()) return false;
        return !("暂无".equals(value) || "无".equals(value) || "N/A".equalsIgnoreCase(value));
    }

    private String formatBullets(String text) {
        if (!isMeaningful(text)) return "";
        String normalized = text.replace("\r", "\n").trim();
        if (normalized.contains("\n")) {
            List<String> lines = Arrays.stream(normalized.split("\n"))
                    .map(String::trim)
                    .filter(this::isMeaningful)
                    .toList();
            if (lines.isEmpty()) return "";
            return lines.stream()
                    .map(line -> line.startsWith("-") || line.startsWith("•") ? line : "• " + line)
                    .collect(java.util.stream.Collectors.joining("\n"));
        }
        String[] segments = normalized.split("[；;。]");
        List<String> items = new ArrayList<>();
        for (String segment : segments) {
            String s = segment == null ? "" : segment.trim();
            if (isMeaningful(s)) items.add("• " + s);
        }
        if (items.isEmpty()) return normalized;
        return String.join("\n", items);
    }
}
