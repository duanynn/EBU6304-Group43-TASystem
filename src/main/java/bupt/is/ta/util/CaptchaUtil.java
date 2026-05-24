package bupt.is.ta.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;

public final class CaptchaUtil {

    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private CaptchaUtil() {
    }

    public static String generateCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    public static byte[] renderPng(String code) throws IOException {
        int width = 120;
        int height = 40;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(new Color(245, 246, 248));
        g.fillRect(0, 0, width, height);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        for (int i = 0; i < 6; i++) {
            g.setColor(new Color(100 + RANDOM.nextInt(100), 100 + RANDOM.nextInt(100), 100 + RANDOM.nextInt(100)));
            int x1 = RANDOM.nextInt(width);
            int y1 = RANDOM.nextInt(height);
            int x2 = RANDOM.nextInt(width);
            int y2 = RANDOM.nextInt(height);
            g.drawLine(x1, y1, x2, y2);
        }
        int x = 12;
        for (int i = 0; i < code.length(); i++) {
            g.setColor(new Color(30 + RANDOM.nextInt(80), 30 + RANDOM.nextInt(80), 30 + RANDOM.nextInt(80)));
            g.drawString(String.valueOf(code.charAt(i)), x, 28 + RANDOM.nextInt(6));
            x += 20;
        }
        g.dispose();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        return out.toByteArray();
    }

    public static boolean matches(String expected, String actual) {
        if (expected == null || actual == null) {
            return false;
        }
        return expected.trim().equalsIgnoreCase(actual.trim());
    }
}
