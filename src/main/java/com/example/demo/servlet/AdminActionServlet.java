package com.example.demo.servlet;

import com.example.demo.model.User;
import com.example.demo.repository.BbsRepository;
import com.example.demo.util.TextUtils;
import com.example.demo.util.WebUtil;

import java.io.IOException;
import java.sql.SQLException;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/admin/action")
public class AdminActionServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = WebUtil.getLoginUser(request);
        if (user == null || !user.isAdmin()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String action = TextUtils.trim(request.getParameter("action"));
        try {
            BbsRepository repository = WebUtil.getRepository(getServletContext());
            if ("user".equals(action)) {
                long userId = WebUtil.parseLong(request.getParameter("userId"), -1L);
                repository.adminUpdateUser(
                        user.getId(),
                        userId,
                        request.getParameter("phone"),
                        TextUtils.trim(request.getParameter("role")),
                        "1".equals(request.getParameter("banned")),
                        WebUtil.parseInt(request.getParameter("banDays"), 0)
                );
                WebUtil.setFlash(request, "用户信息已更新");
            } else if ("pin".equals(action)) {
                repository.setPostPinned(WebUtil.parseLong(request.getParameter("postId"), -1L), "1".equals(request.getParameter("pinned")));
                WebUtil.setFlash(request, "帖子置顶状态已更新");
            } else if ("report".equals(action)) {
                repository.handleReport(
                        TextUtils.trim(request.getParameter("targetType")),
                        WebUtil.parseLong(request.getParameter("targetId"), -1L),
                        user.getId(),
                        WebUtil.parseInt(request.getParameter("banDays"), 0)
                );
                WebUtil.setFlash(request, "举报已标记处理");
            }
        } catch (IllegalArgumentException | SQLException e) {
            WebUtil.setFlash(request, e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/admin");
    }
}
