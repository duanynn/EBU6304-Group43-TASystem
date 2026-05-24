package bupt.is.ta.service;

import bupt.is.ta.model.Config;
import bupt.is.ta.store.DataStore;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class RecruitmentPolicyService {

    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    private final DataStore store = DataStore.getInstance();

    public boolean isApplicationOpen() {
        Config cfg = store.getConfig();
        if (cfg == null) {
            return true;
        }
        String deadline = cfg.getApplicationDeadline();
        if (deadline == null || deadline.isBlank()) {
            return true;
        }
        try {
            Instant end = Instant.parse(deadline.trim());
            return Instant.now().isBefore(end);
        } catch (Exception e) {
            return true;
        }
    }

    public String getCurrentSemester() {
        Config cfg = store.getConfig();
        if (cfg == null || cfg.getCurrentSemester() == null || cfg.getCurrentSemester().isBlank()) {
            return "2025-2026-S2";
        }
        return cfg.getCurrentSemester().trim();
    }

    public String formatDeadlineForDisplay() {
        Config cfg = store.getConfig();
        if (cfg == null) {
            return "";
        }
        String deadline = cfg.getApplicationDeadline();
        if (deadline == null || deadline.isBlank()) {
            return "No deadline (open)";
        }
        try {
            return DISPLAY_FORMAT.format(Instant.parse(deadline.trim()));
        } catch (Exception e) {
            return deadline.trim();
        }
    }
}
