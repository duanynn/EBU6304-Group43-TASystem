package bupt.is.ta.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UploadLimitsTest {

    @Test
    void detectsSizeLimitInCauseChain() {
        Exception inner = new Exception("the request was rejected because its size (6000000) exceeds the configured maximum (5242880)");
        Exception outer = new IllegalStateException("UT000036", inner);
        assertTrue(UploadLimits.isSizeLimitExceeded(outer));
    }

    @Test
    void ignoresUnrelatedErrors() {
        assertFalse(UploadLimits.isSizeLimitExceeded(new IllegalArgumentException("bad id")));
    }
}
