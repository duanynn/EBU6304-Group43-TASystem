package bupt.is.ta.util;

import bupt.is.ta.model.Job;
import bupt.is.ta.model.JobScheduleSlot;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class JobScheduleUtil {

    public static final int GRID_DAY_START_MINUTE = 8 * 60;
    public static final int GRID_DAY_END_MINUTE = 23 * 60;
    public static final String HTML_TIME_MIN = "08:00";
    public static final String HTML_TIME_MAX = "23:00";
    private static final LocalTime GRID_DAY_START = LocalTime.of(8, 0);
    private static final LocalTime GRID_DAY_END = LocalTime.of(23, 0);

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("H:mm");
    private static final String[] DAY_LABELS = {"", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
    private static final Pattern SUMMARY_SLOT_PATTERN = Pattern.compile(
            "(?i)(Mon|Tue|Wed|Thu|Fri|Sat|Sun|Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday|\\d{1})"
                    + "\\s+(\\d{1,2}:\\d{2})\\s*[-–—~]\\s*(\\d{1,2}:\\d{2})");

    private JobScheduleUtil() {
    }

    public static class ParseResult {
        private final List<JobScheduleSlot> slots;
        private final String error;

        public ParseResult(List<JobScheduleSlot> slots, String error) {
            this.slots = slots == null ? List.of() : List.copyOf(slots);
            this.error = error;
        }

        public List<JobScheduleSlot> getSlots() {
            return slots;
        }

        public String getError() {
            return error;
        }

        public boolean isOk() {
            return error == null || error.isBlank();
        }
    }

    public static ParseResult parseSlotRows(String[] days, String[] starts, String[] ends) {
        if (days == null || starts == null || ends == null) {
            return new ParseResult(List.of(), "Add at least one weekly time slot.");
        }
        int rows = Math.min(days.length, Math.min(starts.length, ends.length));
        if (rows == 0) {
            return new ParseResult(List.of(), "Add at least one weekly time slot.");
        }
        List<JobScheduleSlot> slots = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            String dayRaw = days[i] == null ? "" : days[i].trim();
            String startRaw = starts[i] == null ? "" : starts[i].trim();
            String endRaw = ends[i] == null ? "" : ends[i].trim();
            if (dayRaw.isEmpty() && startRaw.isEmpty() && endRaw.isEmpty()) {
                continue;
            }
            int day = parseDayOfWeek(dayRaw);
            if (day < 1 || day > 7) {
                return new ParseResult(List.of(), "Invalid weekday on row " + (i + 1) + ".");
            }
            LocalTime start = parseTime(startRaw);
            LocalTime end = parseTime(endRaw);
            if (start == null || end == null) {
                return new ParseResult(List.of(), "Invalid time on row " + (i + 1) + ". Use HH:mm format.");
            }
            if (!start.isBefore(end)) {
                return new ParseResult(List.of(), "End time must be after start time on row " + (i + 1) + ".");
            }
            if (!isWithinDailyGrid(start) || !isWithinDailyGrid(end)) {
                return new ParseResult(List.of(),
                        "Times on row " + (i + 1) + " must be between 08:00 and 23:00.");
            }
            JobScheduleSlot slot = new JobScheduleSlot();
            slot.setDayOfWeek(day);
            slot.setStartTime(formatTime(start));
            slot.setEndTime(formatTime(end));
            slots.add(slot);
        }
        if (slots.isEmpty()) {
            return new ParseResult(List.of(), "Add at least one weekly time slot.");
        }
        slots.sort(Comparator.comparingInt(JobScheduleSlot::getDayOfWeek)
                .thenComparing(JobScheduleSlot::getStartTime));
        return new ParseResult(slots, null);
    }

    public static int parseDayOfWeek(String raw) {
        if (raw == null || raw.isBlank()) {
            return -1;
        }
        String v = raw.trim().toUpperCase(Locale.ROOT);
        try {
            int n = Integer.parseInt(v);
            if (n >= 1 && n <= 7) {
                return n;
            }
        } catch (NumberFormatException ignored) {
            // fall through
        }
        return switch (v) {
            case "MON", "MONDAY" -> 1;
            case "TUE", "TUESDAY" -> 2;
            case "WED", "WEDNESDAY" -> 3;
            case "THU", "THURSDAY" -> 4;
            case "FRI", "FRIDAY" -> 5;
            case "SAT", "SATURDAY" -> 6;
            case "SUN", "SUNDAY" -> 7;
            default -> -1;
        };
    }

    public static LocalTime parseTime(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String v = raw.trim();
        try {
            if (v.length() == 5 && v.charAt(2) == ':') {
                return LocalTime.parse(v, DateTimeFormatter.ofPattern("HH:mm"));
            }
            return LocalTime.parse(v, TIME_FMT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static String formatTime(LocalTime time) {
        return time == null ? "" : time.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public static String formatSummary(List<JobScheduleSlot> slots) {
        if (slots == null || slots.isEmpty()) {
            return "";
        }
        List<JobScheduleSlot> sorted = new ArrayList<>(slots);
        sorted.sort(Comparator.comparingInt(JobScheduleSlot::getDayOfWeek)
                .thenComparing(JobScheduleSlot::getStartTime));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sorted.size(); i++) {
            JobScheduleSlot s = sorted.get(i);
            if (i > 0) {
                sb.append("; ");
            }
            int d = s.getDayOfWeek();
            String label = d >= 1 && d <= 7 ? DAY_LABELS[d] : "Day" + d;
            sb.append(label).append(' ')
                    .append(s.getStartTime()).append('-').append(s.getEndTime());
        }
        return sb.toString();
    }

    public static String displayWorkTime(Job job) {
        if (job == null) {
            return "-";
        }
        List<JobScheduleSlot> slots = job.getScheduleSlots();
        if (slots != null && !slots.isEmpty()) {
            String summary = formatSummary(slots);
            if (!summary.isBlank()) {
                return summary;
            }
        }
        String text = job.getRequiredWorkTime();
        return text == null || text.isBlank() ? "-" : text;
    }

    public static boolean hasStructuredSlots(Job job) {
        return job != null && job.getScheduleSlots() != null && !job.getScheduleSlots().isEmpty();
    }

    public static boolean hasStructuredAvailability(bupt.is.ta.model.User user) {
        return user != null && !resolveAvailabilitySlots(user).isEmpty();
    }

    /**
     * Parse human-readable summaries such as "Mon 08:00-12:00; Wed 14:00-18:00".
     */
    public static List<JobScheduleSlot> parseSummaryText(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        List<JobScheduleSlot> slots = new ArrayList<>();
        for (String segment : text.split("[;\\n]+")) {
            String part = segment == null ? "" : segment.trim();
            if (part.isBlank()) {
                continue;
            }
            Matcher matcher = SUMMARY_SLOT_PATTERN.matcher(part);
            if (!matcher.find()) {
                continue;
            }
            int day = parseDayOfWeek(matcher.group(1));
            LocalTime start = parseTime(matcher.group(2));
            LocalTime end = parseTime(matcher.group(3));
            if (day < 1 || day > 7 || start == null || end == null || !end.isAfter(start)) {
                continue;
            }
            slots.add(new JobScheduleSlot(day, formatTime(start), formatTime(end)));
        }
        slots.sort(Comparator.comparingInt(JobScheduleSlot::getDayOfWeek)
                .thenComparing(JobScheduleSlot::getStartTime));
        return slots;
    }

    public static List<JobScheduleSlot> resolveAvailabilitySlots(bupt.is.ta.model.User user) {
        if (user == null) {
            return List.of();
        }
        List<JobScheduleSlot> slots = user.getAvailableSlots();
        if (slots != null && !slots.isEmpty()) {
            return slots;
        }
        return parseSummaryText(user.getAvailableTime());
    }

    public static List<JobScheduleSlot> resolveJobScheduleSlots(Job job) {
        if (job == null) {
            return List.of();
        }
        List<JobScheduleSlot> slots = job.getScheduleSlots();
        if (slots != null && !slots.isEmpty()) {
            return slots;
        }
        return parseSummaryText(job.getRequiredWorkTime());
    }

    /** Fill structured slots from summary text when missing; returns true if user was updated. */
    public static boolean materializeAvailabilitySlots(bupt.is.ta.model.User user) {
        if (user == null) {
            return false;
        }
        List<JobScheduleSlot> existing = user.getAvailableSlots();
        if (existing != null && !existing.isEmpty()) {
            return false;
        }
        List<JobScheduleSlot> parsed = parseSummaryText(user.getAvailableTime());
        if (parsed.isEmpty()) {
            return false;
        }
        user.setAvailableSlots(parsed);
        if (user.getAvailableTime() == null || user.getAvailableTime().isBlank()) {
            user.setAvailableTime(formatSummary(parsed));
        }
        return true;
    }

    /** Fill structured job slots from requiredWorkTime when missing; returns true if job was updated. */
    public static boolean materializeJobScheduleSlots(Job job) {
        if (job == null) {
            return false;
        }
        List<JobScheduleSlot> existing = job.getScheduleSlots();
        if (existing != null && !existing.isEmpty()) {
            return false;
        }
        List<JobScheduleSlot> parsed = parseSummaryText(job.getRequiredWorkTime());
        if (parsed.isEmpty()) {
            return false;
        }
        job.setScheduleSlots(parsed);
        return true;
    }

    public static String displayAvailability(bupt.is.ta.model.User user) {
        if (user == null) {
            return "-";
        }
        List<JobScheduleSlot> slots = resolveAvailabilitySlots(user);
        if (!slots.isEmpty()) {
            String summary = formatSummary(slots);
            if (!summary.isBlank()) {
                return summary;
            }
        }
        String text = user.getAvailableTime();
        return text == null || text.isBlank() ? "-" : text;
    }

    public static String formatInterviewSlot(JobScheduleSlot slot) {
        if (slot == null) {
            return "";
        }
        int d = slot.getDayOfWeek();
        String label = d >= 1 && d <= 7 ? DAY_LABELS[d] : "Day" + d;
        return label + " " + slot.getStartTime() + "-" + slot.getEndTime();
    }

    public static boolean jobsOverlap(Job a, Job b) {
        if (a == null || b == null) {
            return false;
        }
        List<JobScheduleSlot> slotsA = a.getScheduleSlots();
        List<JobScheduleSlot> slotsB = b.getScheduleSlots();
        if (slotsA == null || slotsA.isEmpty() || slotsB == null || slotsB.isEmpty()) {
            return false;
        }
        for (JobScheduleSlot sa : slotsA) {
            for (JobScheduleSlot sb : slotsB) {
                if (slotsOverlap(sa, sb)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean slotsOverlap(JobScheduleSlot a, JobScheduleSlot b) {
        if (a == null || b == null || a.getDayOfWeek() != b.getDayOfWeek()) {
            return false;
        }
        LocalTime aStart = parseTime(a.getStartTime());
        LocalTime aEnd = parseTime(a.getEndTime());
        LocalTime bStart = parseTime(b.getStartTime());
        LocalTime bEnd = parseTime(b.getEndTime());
        if (aStart == null || aEnd == null || bStart == null || bEnd == null) {
            return false;
        }
        return aStart.isBefore(bEnd) && bStart.isBefore(aEnd);
    }

    /** Minutes from midnight for grid positioning. */
    public static int toMinutes(String time) {
        LocalTime t = parseTime(time);
        return t == null ? 0 : t.getHour() * 60 + t.getMinute();
    }

    public static boolean isWithinDailyGrid(LocalTime time) {
        if (time == null) {
            return false;
        }
        return !time.isBefore(GRID_DAY_START) && !time.isAfter(GRID_DAY_END);
    }

    /** Clamp slot minutes to the fixed 08:00–23:00 timetable grid. */
    public static int clampStartMinute(int minute) {
        return Math.max(GRID_DAY_START_MINUTE, minute);
    }

    public static int clampEndMinute(int minute) {
        return Math.min(GRID_DAY_END_MINUTE, Math.max(GRID_DAY_START_MINUTE, minute));
    }
}
