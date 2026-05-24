package bupt.is.ta.web;

import bupt.is.ta.util.CaptchaUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/captcha")
public class CaptchaController extends HttpServlet {

    public static final String SESSION_ATTR = "captchaCode";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String code = CaptchaUtil.generateCode(5);
        HttpSession session = req.getSession(true);
        session.setAttribute(SESSION_ATTR, code);
        byte[] png = CaptchaUtil.renderPng(code);
        resp.setContentType("image/png");
        resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        resp.getOutputStream().write(png);
    }
}
