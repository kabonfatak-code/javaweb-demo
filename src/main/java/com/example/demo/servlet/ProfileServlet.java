package com.example.demo.servlet;

import com.example.demo.model.User;
import com.example.demo.repository.BbsRepository;
import com.example.demo.util.TextUtils;
import com.example.demo.util.WebUtil;

import java.io.IOException;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/profile")
public class ProfileServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = WebUtil.getLoginUser(request);
        if (user == null) {
            WebUtil.setFlash(request, "请先登录");
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        try {
            BbsRepository repository = WebUtil.getRepository(getServletContext());
            forwardProfile(request, response, repository, user, TextUtils.trim(request.getParameter("tab")));
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        User user = WebUtil.getLoginUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        try {
            BbsRepository repository = WebUtil.getRepository(getServletContext());
            String action = TextUtils.trim(request.getParameter("action"));
            if ("account".equals(action)) {
                User freshUser = repository.updateProfile(
                        user.getId(),
                        request.getParameter("username"),
                        request.getParameter("phone"),
                        request.getParameter("password")
                );
                request.getSession().setAttribute(WebUtil.LOGIN_USER_KEY, freshUser);
                WebUtil.setFlash(request, "账号资料已更新");
                response.sendRedirect(request.getContextPath() + "/profile?tab=edit");
            } else {
                boolean enabled = "on".equals(request.getParameter("historyEnabled"));
                repository.setHistoryEnabled(user.getId(), enabled);
                request.getSession().setAttribute(WebUtil.LOGIN_USER_KEY, repository.findUserById(user.getId()));
                WebUtil.setFlash(request, enabled ? "已开启历史浏览记录" : "已关闭历史浏览记录并清空历史");
                response.sendRedirect(request.getContextPath() + "/profile?tab=account");
            }
        } catch (IllegalArgumentException e) {
            try {
                BbsRepository repository = WebUtil.getRepository(getServletContext());
                request.setAttribute("error", e.getMessage());
                request.setAttribute("editUsername", TextUtils.trim(request.getParameter("username")));
                request.setAttribute("editPhone", TextUtils.trim(request.getParameter("phone")));
                forwardProfile(request, response, repository, user, "edit");
            } catch (SQLException sqlException) {
                throw new ServletException(sqlException);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    private void forwardProfile(HttpServletRequest request, HttpServletResponse response, BbsRepository repository, User user, String tab)
            throws ServletException, IOException, SQLException {
        request.setAttribute("tab", tab);
        request.setAttribute("history", repository.findHistory(user.getId()));
        request.setAttribute("favorites", repository.findFavorites(user.getId()));
        request.setAttribute("myPosts", repository.findMyPosts(user.getId()));
        request.getRequestDispatcher("/WEB-INF/views/profile.jsp").forward(request, response);
    }
}
