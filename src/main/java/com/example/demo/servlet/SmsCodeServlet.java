package com.example.demo.servlet;

import com.example.demo.repository.BbsRepository;
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
        String purpose = TextUtils.trim(request.getParameter("purpose"));
        if (!"REGISTER".equals(purpose) && !"LOGIN".equals(purpose) && !"RESET".equals(purpose)) {
            purpose = "LOGIN";
        }

        try {
            BbsRepository repository = WebUtil.getRepository(getServletContext());
            String code = repository.createSmsCode(phone, purpose);
            response.getWriter().write("{\"ok\":true,\"code\":\"" + code + "\",\"message\":\"模拟短信验证码已生成\"}");
        } catch (IllegalArgumentException | SQLException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"ok\":false,\"message\":\"" + json(e.getMessage()) + "\"}");
        }
    }

    private String json(String text) {
        return text == null ? "" : text.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
