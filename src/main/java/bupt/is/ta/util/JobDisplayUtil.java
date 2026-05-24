package bupt.is.ta.util;

import bupt.is.ta.model.Job;

public final class JobDisplayUtil {

    private JobDisplayUtil() {
    }

    public static String jobTypeLabel(Job job) {
        if (job == null || job.getJobType() == null) {
            return "Module TA";
        }
        return switch (job.getJobType()) {
            case MODULE_TA -> "Module TA";
            case INVIGILATION -> "Invigilation";
            case OTHER -> "Other";
        };
    }

    public static String jobTypeCssClass(Job job) {
        if (job == null || job.getJobType() == null) {
            return "job-type-module";
        }
        return switch (job.getJobType()) {
            case MODULE_TA -> "job-type-module";
            case INVIGILATION -> "job-type-invigilation";
            case OTHER -> "job-type-other";
        };
    }
}
