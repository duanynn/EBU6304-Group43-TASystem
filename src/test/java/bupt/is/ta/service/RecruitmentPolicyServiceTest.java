package bupt.is.ta.service;

import bupt.is.ta.model.Config;
import bupt.is.ta.store.DataStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class RecruitmentPolicyServiceTest {

    @TempDir
    Path tmpDir;

    private RecruitmentPolicyService service;

    @BeforeEach
    void setUp() throws Exception {
        DataStore store = DataStore.getInstance();
        store.init(tmpDir);
        Config cfg = store.getConfig();
        cfg.setApplicationDeadline("");
        cfg.setCurrentSemester("2025-2026-S2");
        store.updateConfig(cfg);
        service = new RecruitmentPolicyService();
    }

    @Test
    void isApplicationOpen_whenDeadlineEmpty_returnsTrue() {
        assertTrue(service.isApplicationOpen());
    }

    @Test
    void isApplicationOpen_whenDeadlineInFuture_returnsTrue() {
        DataStore.getInstance().getConfig().setApplicationDeadline(
                Instant.now().plusSeconds(3600).toString());
        assertTrue(service.isApplicationOpen());
    }

    @Test
    void isApplicationOpen_whenDeadlinePassed_returnsFalse() {
        DataStore.getInstance().getConfig().setApplicationDeadline(
                Instant.now().minusSeconds(3600).toString());
        assertFalse(service.isApplicationOpen());
    }

    @Test
    void getCurrentSemester_returnsConfiguredValue() {
        assertEquals("2025-2026-S2", service.getCurrentSemester());
    }

    @Test
    void formatDeadlineForDisplay_whenEmpty_returnsOpenMessage() {
        assertEquals("No deadline (open)", service.formatDeadlineForDisplay());
    }
}
