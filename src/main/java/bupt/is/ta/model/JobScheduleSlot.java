package bupt.is.ta.model;

public class JobScheduleSlot {
    /** 1 = Monday … 7 = Sunday */
    private int dayOfWeek;
    /** 24h format, e.g. "09:00" */
    private String startTime;
    /** 24h format, e.g. "11:00" */
    private String endTime;

    public JobScheduleSlot() {
    }

    public JobScheduleSlot(int dayOfWeek, String startTime, String endTime) {
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime == null ? "" : startTime.trim();
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime == null ? "" : endTime.trim();
    }
}
