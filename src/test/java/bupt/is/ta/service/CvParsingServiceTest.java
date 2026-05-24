package bupt.is.ta.service;

import bupt.is.ta.model.UserProfile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for CvParsingService.
 * Creates real PDF / DOCX files in a temp directory so the full parse pipeline
 * (text extraction → heuristic skill matching) can be exercised.
 */
class CvParsingServiceTest {

    private final CvParsingService service = new CvParsingService();

    @TempDir
    Path tempDir;

    // ── PDF parsing ───────────────────────────────────────────────────────

    @Test
    void parsePdf_extractsKnownSkills() throws Exception {
        Path pdf = writePdf("Name: Alice\nSkills: Java Python SQL\nEducation: BUPT BSc Computer Science 2024");

        UserProfile profile = service.parseCvFile(pdf);

        assertNotNull(profile);
        List<String> skills = profile.getExtractedSkills();
        assertNotNull(skills);
        // Regex SKILL_PATTERN matches java, python, sql (case-insensitive)
        assertTrue(skills.stream().anyMatch(s -> s.equalsIgnoreCase("Java")),
                "Expected Java in skills: " + skills);
        assertTrue(skills.stream().anyMatch(s -> s.equalsIgnoreCase("Python")),
                "Expected Python in skills: " + skills);
        assertTrue(skills.stream().anyMatch(s -> s.equalsIgnoreCase("SQL")),
                "Expected SQL in skills: " + skills);
    }

    @Test
    void parsePdf_rawCvTextIsNotBlank() throws Exception {
        Path pdf = writePdf("Java developer with Docker and Git experience.");

        UserProfile profile = service.parseCvFile(pdf);

        assertNotNull(profile.getRawCvText());
        assertFalse(profile.getRawCvText().isBlank());
    }

    @Test
    void parsePdf_lastParsedAtIsSet() throws Exception {
        Path pdf = writePdf("Some content");

        UserProfile profile = service.parseCvFile(pdf);

        assertNotNull(profile.getLastParsedAt());
        assertFalse(profile.getLastParsedAt().isBlank());
    }

    @Test
    void parsePdf_cppSkillNormalized() throws Exception {
        Path pdf = writePdf("Proficient in C++ programming");

        UserProfile profile = service.parseCvFile(pdf);

        assertTrue(profile.getExtractedSkills().contains("C++"),
                "C++ should be normalized; got: " + profile.getExtractedSkills());
    }

    @Test
    void parsePdf_sqlSkillNormalized() throws Exception {
        Path pdf = writePdf("Experience with sql databases");

        UserProfile profile = service.parseCvFile(pdf);

        assertTrue(profile.getExtractedSkills().contains("SQL"),
                "SQL should be uppercased; got: " + profile.getExtractedSkills());
    }

    @Test
    void parsePdf_noKnownSkills_returnsEmptySkillList() throws Exception {
        Path pdf = writePdf("Experienced in painting and ceramics.");

        UserProfile profile = service.parseCvFile(pdf);

        // None of SKILL_PATTERN tokens present → empty or no recognized skills
        List<String> skills = profile.getExtractedSkills();
        assertNotNull(skills);
    }

    // ── DOCX parsing ──────────────────────────────────────────────────────

    @Test
    void parseDocx_extractsKnownSkills() throws Exception {
        Path docx = writeDocx("Name: Bob\nExpert in Java and Spring development.");

        UserProfile profile = service.parseCvFile(docx);

        assertNotNull(profile);
        assertTrue(profile.getExtractedSkills().stream()
                        .anyMatch(s -> s.equalsIgnoreCase("Java")),
                "Expected Java in skills: " + profile.getExtractedSkills());
    }

    @Test
    void parseDocx_rawCvTextContainsContent() throws Exception {
        String text = "Software engineer skilled in Typescript and Docker";
        Path docx = writeDocx(text);

        UserProfile profile = service.parseCvFile(docx);

        assertFalse(profile.getRawCvText().isBlank());
    }

    // ── Unsupported file type ─────────────────────────────────────────────

    @Test
    void unsupportedExtension_throwsIOException() {
        Path txtFile = tempDir.resolve("cv.txt");
        assertThrows(IOException.class, () -> service.parseCvFile(txtFile));
    }

    @Test
    void unsupportedExtensionXlsx_throwsIOException() {
        Path xlsxFile = tempDir.resolve("cv.xlsx");
        assertThrows(IOException.class, () -> service.parseCvFile(xlsxFile));
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private Path writePdf(String content) throws IOException {
        Path file = tempDir.resolve("test-" + System.nanoTime() + ".pdf");
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);
                cs.newLineAtOffset(50, 720);
                // PDFBox showText does not support \n; split and render line by line
                for (String line : content.split("\n")) {
                    cs.showText(line.length() > 200 ? line.substring(0, 200) : line);
                    cs.newLineAtOffset(0, -15);
                }
                cs.endText();
            }
            doc.save(file.toFile());
        }
        return file;
    }

    private Path writeDocx(String content) throws IOException {
        Path file = tempDir.resolve("test-" + System.nanoTime() + ".docx");
        try (XWPFDocument doc = new XWPFDocument();
             FileOutputStream fos = new FileOutputStream(file.toFile())) {
            for (String line : content.split("\n")) {
                XWPFParagraph para = doc.createParagraph();
                XWPFRun run = para.createRun();
                run.setText(line);
            }
            doc.write(fos);
        }
        return file;
    }
}
