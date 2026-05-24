package bupt.is.ta.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigTest {

    @Test
    void defaultValues() {
        Config cfg = new Config();
        assertEquals(2, cfg.getMaxCoursesPerTA());
        assertEquals("/WEB-INF/data/cvs", cfg.getCvRelativePath());
        assertEquals("WEBAPP", cfg.getStorageMode());
        assertEquals("", cfg.getDashscopeApiKey());
        assertEquals("https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions",
                cfg.getDashscopeEndpoint());
        assertEquals("qwen-plus", cfg.getDashscopeModel());
        assertEquals("2025-2026-S2", cfg.getCurrentSemester());
        assertEquals("", cfg.getApplicationDeadline());
    }

    @Test
    void settersAndGetters() {
        Config cfg = new Config();
        cfg.setMaxCoursesPerTA(5);
        cfg.setCvRelativePath("/data/cvs");
        cfg.setStorageMode("USER_HOME");
        cfg.setDashscopeApiKey("sk-abc123");
        cfg.setDashscopeEndpoint("https://example.com/v1/chat");
        cfg.setDashscopeModel("qwen-max");
        cfg.setCurrentSemester("2026-2027-S1");
        cfg.setApplicationDeadline("2026-12-31T00:00:00Z");

        assertEquals(5, cfg.getMaxCoursesPerTA());
        assertEquals("/data/cvs", cfg.getCvRelativePath());
        assertEquals("USER_HOME", cfg.getStorageMode());
        assertEquals("sk-abc123", cfg.getDashscopeApiKey());
        assertEquals("https://example.com/v1/chat", cfg.getDashscopeEndpoint());
        assertEquals("qwen-max", cfg.getDashscopeModel());
        assertEquals("2026-2027-S1", cfg.getCurrentSemester());
        assertEquals("2026-12-31T00:00:00Z", cfg.getApplicationDeadline());
    }

    @Test
    void maxCoursesPerTACanBeOne() {
        Config cfg = new Config();
        cfg.setMaxCoursesPerTA(1);
        assertEquals(1, cfg.getMaxCoursesPerTA());
    }
}
