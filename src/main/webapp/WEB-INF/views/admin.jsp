<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.time.format.DateTimeFormatter,java.util.List,com.example.demo.model.User,com.example.demo.model.Report,com.example.demo.util.TextUtils,com.example.demo.util.ForumOptions" %>
<%
    List<User> users = (List<User>) request.getAttribute("users");
    List<Report> reports = (List<Report>) request.getAttribute("reports");
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>管理员后台 - BBS 论坛</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/style.css">
</head>
<body>
<%@ include file="/WEB-INF/views/fragments/header.jspf" %>
<main class="page">
    <section class="page-heading">
        <div>
            <h1>管理员后台</h1>
            <p>查看举报、封号/解封、修改用户角色和电话。</p>
        </div>
    </section>

    <section class="admin-section">
        <h2>举报情况</h2>
        <% if (reports == null || reports.isEmpty()) { %>
            <div class="empty-state small">暂无举报</div>
        <% } else { %>
            <% for (Report report : reports) { %>
                <article class="post-card">
                    <div class="post-card-head">
                        <strong><%= TextUtils.escapeHtml(report.getPostTitle() == null ? "评论举报" : report.getPostTitle()) %></strong>
                        <span class="tag"><%= report.isHandled() ? "已处理" : "待处理" %></span>
                    </div>
                    <p>举报人：<%= TextUtils.escapeHtml(report.getReporterUsername()) %> · 权重：<%= report.getWeight() %> · <%= formatter.format(report.getCreatedAt()) %></p>
                    <p><%= TextUtils.escapeHtml(report.getReason()) %></p>
                    <% if (!report.isHandled()) { %>
                        <form method="post" action="<%= ctx %>/admin/action">
                            <input type="hidden" name="action" value="report">
                            <input type="hidden" name="reportId" value="<%= report.getId() %>">
                            <button class="button" type="submit">标记已处理</button>
                        </form>
                    <% } %>
                </article>
            <% } %>
        <% } %>
    </section>

    <section class="admin-section">
        <h2>用户管理</h2>
        <div class="table-wrap">
            <table>
                <thead>
                <tr>
                    <th>用户</th>
                    <th>电话</th>
                    <th>角色</th>
                    <th>状态</th>
                    <th>操作</th>
                </tr>
                </thead>
                <tbody>
                <% for (User item : users) { %>
                    <tr>
                        <form method="post" action="<%= ctx %>/admin/action">
                            <td><%= TextUtils.escapeHtml(item.getUsername()) %></td>
                            <td><input type="text" name="phone" value="<%= TextUtils.escapeHtml(item.getPhone()) %>"></td>
                            <td>
                                <select name="role">
                                    <option value="<%= User.ROLE_NEW %>" <%= User.ROLE_NEW.equals(item.getRole()) ? "selected" : "" %>>新用户</option>
                                    <option value="<%= User.ROLE_OLD %>" <%= User.ROLE_OLD.equals(item.getRole()) ? "selected" : "" %>>老东西</option>
                                    <option value="<%= User.ROLE_ADMIN %>" <%= User.ROLE_ADMIN.equals(item.getRole()) ? "selected" : "" %>>系统管理员</option>
                                </select>
                            </td>
                            <td>
                                <select name="banned">
                                    <option value="0" <%= item.isBanned() ? "" : "selected" %>>正常</option>
                                    <option value="1" <%= item.isBanned() ? "selected" : "" %>>封禁</option>
                                </select>
                            </td>
                            <td>
                                <input type="hidden" name="action" value="user">
                                <input type="hidden" name="userId" value="<%= item.getId() %>">
                                <button class="button" type="submit">保存</button>
                            </td>
                        </form>
                    </tr>
                <% } %>
                </tbody>
            </table>
        </div>
    </section>
</main>
</body>
</html>
