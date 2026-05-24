package bupt.is.ta.util;

/**
 * Upload size limits and helpers for user-facing validation messages.
 */
public final class UploadLimits {

    public static final long CV_MAX_BYTES = 5L * 1024 * 1024;
    public static final long AVATAR_MAX_BYTES = 2L * 1024 * 1024;
    /** Slightly above CV limit to allow profile form fields + one file. */
    public static final long MULTIPART_MAX_REQUEST_BYTES = 8L * 1024 * 1024;

    private UploadLimits() {
    }

    public static String cvSizeMessage() {
        return "CV file is too large. Maximum size is 5 MB.";
    }

    public static String avatarSizeMessage() {
        return "Profile photo is too large. Maximum size is 2 MB.";
    }

    public static String requestTooLargeMessage() {
        return "Upload is too large. CV max 5 MB, profile photo max 2 MB.";
    }

    public static boolean isSizeLimitExceeded(Throwable error) {
        Throwable current = error;
        while (current != null) {
            String name = current.getClass().getName();
            if (name.contains("SizeLimitExceededException")
                    || name.contains("FileUploadException")
                    || name.contains("SizeException")) {
                return true;
            }
            String msg = current.getMessage();
            if (msg != null) {
                String lower = msg.toLowerCase();
                if (lower.contains("size") && (lower.contains("exceed") || lower.contains("larger") || lower.contains("max"))) {
                    return true;
                }
                if (lower.contains("multipart") && lower.contains("exceed")) {
                    return true;
                }
            }
            current = current.getCause();
        }
        return false;
    }
}
