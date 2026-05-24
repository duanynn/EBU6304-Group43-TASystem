package bupt.is.ta.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CaptchaUtilTest {

    @Test
    void generateCodeHasExpectedLength() {
        assertEquals(5, CaptchaUtil.generateCode(5).length());
    }

    @Test
    void matchesIgnoresCase() {
        assertTrue(CaptchaUtil.matches("AbCdE", "abcde"));
    }

    @Test
    void renderPngProducesBytes() throws Exception {
        byte[] png = CaptchaUtil.renderPng("TEST1");
        assertNotNull(png);
        assertTrue(png.length > 100);
    }
}
