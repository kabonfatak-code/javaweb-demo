package com.example.demo.servlet;

import com.example.demo.model.Comment;
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

@WebServlet("/comment/action")
public class CommentServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = WebUtil.getLoginUser(request);
        if (user == null) {
            if (isAjax(request)) {
                writeJson(response, false, "请先登录");
            } else {
                WebUtil.setFlash(request, "请先登录");
                response.sendRedirect(request.getContextPath() + "/login");
            }
            return;
        }

        String action = TextUtils.trim(request.getParameter("action"));
        long postId = WebUtil.parseLong(request.getParameter("postId"), -1L);
        long commentId = WebUtil.parseLong(request.getParameter("commentId"), -1L);
        String anchor = "#comments";
        String message = "操作已完成";

        try {
            BbsRepository repository = WebUtil.getRepository(getServletContext());
            if ("add".equals(action)) {
                Comment comment = repository.addComment(user, postId, request.getParameter("content"));
                anchor = "#comment-" + comment.getId();
                message = "评论已发表，刷新后可见";
            } else if ("edit".equals(action)) {
                repository.updateComment(user, commentId, request.getParameter("content"));
                anchor = "#comment-" + commentId;
                message = "评论已更新";
            } else if ("delete".equals(action)) {
                Comment comment = repository.findComment(commentId);
                repository.deleteComment(user, commentId);
                postId = comment == null ? postId : comment.getPostId();
                anchor = "#comments";
                message = "评论已删除，刷新后不再显示";
            } else if ("like".equals(action)) {
                repository.voteComment(user, commentId, 1);
                anchor = "#comment-" + commentId;
                message = user.isOldUser() ? "已按老东西权限双倍点赞评论" : "已点赞评论";
            } else if ("dislike".equals(action)) {
                repository.voteComment(user, commentId, -1);
                anchor = "#comment-" + commentId;
                message = user.isOldUser() ? "已按老东西权限双倍踩评论" : "已踩评论";
            } else if ("report".equals(action)) {
                repository.reportComment(user, commentId, request.getParameter("reason"));
                anchor = "#comment-" + commentId;
                message = user.isOldUser() ? "评论举报已提交，举报人数 +2" : "评论举报已提交";
            } else {
                throw new IllegalArgumentException("未知操作");
            }
        } catch (IllegalArgumentException | SQLException e) {
            if (isAjax(request)) {
                writeJson(response, false, e.getMessage());
            } else {
                WebUtil.setFlash(request, e.getMessage());
                redirectWithFragment(response, request.getContextPath() + "/post/detail?id=" + postId + anchor);
            }
            return;
        }

        if (isAjax(request)) {
            writeJson(response, true, message);
        } else {
            WebUtil.setFlash(request, message);
            redirectWithFragment(response, request.getContextPath() + "/post/detail?id=" + postId + anchor);
        }
    }

    private void redirectWithFragment(HttpServletResponse response, String location) {
        response.setStatus(HttpServletResponse.SC_SEE_OTHER);
        response.setHeader("Location", location);
    }

    private boolean isAjax(HttpServletRequest request) {
        return "fetch".equalsIgnoreCase(request.getHeader("X-Requested-With"));
    }

    private void writeJson(HttpServletResponse response, boolean ok, String message) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"ok\":" + ok + ",\"message\":\"" + jsonEscape(message) + "\"}");
    }

    private String jsonEscape(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "\\r").replace("\n", "\\n");
    }
}
