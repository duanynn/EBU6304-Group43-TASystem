package bupt.is.ta.service;

import bupt.is.ta.model.Config;
import bupt.is.ta.store.DataStore;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for FileStorageService — covers deleteCv and resolveCvBaseDir logic.
 * saveCv is not tested here because it requires a jakarta.servlet.http.Part,
 * which is a Servlet API interface and is not available outside a container.
 */
class FileStorageServiceTest {

    private final FileStorageService service = new FileStorageService();

    @TempDir
    Path tempDir;

    @BeforeAll
    static void initStore() throws IOException {
        Path tmp = Files.createTempDirectory("ta-file-svc-test");
        tmp.toFile().deleteOnExit();
        DataStore.getInstance().init(tmp);
    }

    // ── deleteCv ─────────────────────────────────────────────────────────

    @Test
    void deleteCv_existingFile_isDeleted() throws IOException {
        Path cv = tempDir.resolve("student123.pdf");
        Files.writeString(cv, "dummy cv content");
        assertTrue(Files.exists(cv));

        service.deleteCv(cv.toString());

        assertFalse(Files.exists(cv), "File should be deleted");
    }

    @Test
    void deleteCv_nonExistentPath_doesNotThrow() {
        Path missing = tempDir.resolve("does_not_exist.pdf");
        assertDoesNotThrow(() -> service.deleteCv(missing.toString()));
    }

    @Test
    void deleteCv_nullPath_doesNotThrow() {
        assertDoesNotThrow(() -> service.deleteCv(null));
    }

    @Test
    void deleteCv_blankPath_doesNotThrow() {
        assertDoesNotThrow(() -> service.deleteCv("   "));
    }

    // ── resolveCvBaseDir — USER_HOME mode ─────────────────────────────────

    @Test
    void resolveCvBaseDir_userHomeMode_returnsUserHomeSubPath() {
        Config cfg = new Config();
        cfg.setStorageMode("USER_HOME");
        DataStore.getInstance().updateConfig(cfg);

        // Use a mock-free check: just verify path is under user.home/ebu_data/cvs
        Path expected = Path.of(System.getProperty("user.home"), "ebu_data", "cvs");
        Path resolved = service.resolveCvBaseDir(null); // context is unused in USER_HOME mode

        assertEquals(expected, resolved);

        // Restore default config
        DataStore.getInstance().updateConfig(new Config());
    }
}
