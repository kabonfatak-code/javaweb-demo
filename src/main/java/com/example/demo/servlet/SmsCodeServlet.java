package com.example.demo.servlet;

import com.example.demo.repository.BbsRepository;
import com.example.demo.sms.AliyunSmsSender;
import com.example.demo.sms.SmsSendException;
import com.example.demo.util.TextUtils;
import com.example.demo.util.WebUtil;

import java.io.IOException;
import java.sql.SQLException;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/sms-code")
public class SmsCodeServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=UTF-8");
        String phone = TextUtils.trim(request.getParameter("phone"));
        String normalizedPhone = phone.replaceAll("\\s+", "");
        String purpose = TextUtils.trim(request.getParameter("purpose"));
        if (!"REGISTER".equals(purpose) && !"LOGIN".equals(purpose) && !"RESET".equals(purpose)) {
            purpose = "LOGIN";
        }

        try {
            BbsRepository repository = WebUtil.getRepository(getServletContext());
            String code = repository.createSmsCode(normalizedPhone, purpose);
            if (isDemoSmsEnabled()) {
                response.getWriter().write("{\"ok\":true,\"code\":\"" + code + "\",\"message\":\"演示验证码：" + code + "\"}");
                return;
            }
            AliyunSmsSender.sendCode(normalizedPhone, code, purpose);
            response.getWriter().write("{\"ok\":true,\"message\":\"验证码已发送，请查收短信\"}");
        } catch (IllegalArgumentException | SQLException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"ok\":false,\"message\":\"" + json(e.getMessage()) + "\"}");
        } catch (SmsSendException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"ok\":false,\"message\":\"" + json(e.getMessage()) + "\"}");
        }
    }

    private String json(String text) {
        return text == null ? "" : text.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private boolean isDemoSmsEnabled() {
        String property = System.getProperty("bbs.sms.demo");
        if (property == null || property.trim().isEmpty()) {
            property = System.getenv("BBS_SMS_DEMO");
        }
        return "true".equalsIgnoreCase(property) || "1".equals(property);
    }
}
