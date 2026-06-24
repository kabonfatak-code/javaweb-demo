<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.time.format.DateTimeFormatter,java.util.List,com.example.demo.model.Notification,com.example.demo.util.TextUtils" %>
<%
    List<Notification> notifications = (List<Notification>) request.getAttribute("notifications");
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>信息通知 - BBS 论坛</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/style.css">
</head>
<body>
<%@ include file="/WEB-INF/views/fragments/header.jspf" %>
<main class="page">
    <section class="page-heading">
        <div>
            <h1>信息通知</h1>
            <p>你的帖子或评论收到评论、点赞、收藏时会出现在这里。</p>
        </div>
    </section>
    <% if (notifications == null || notifications.isEmpty()) { %>
        <section class="empty-state">暂无通知</section>
    <% } else { %>
        <section class="post-list">
            <% for (Notification notification : notifications) { %>
                <article class="post-card">
                    <div class="post-card-head">
                        <strong><%= TextUtils.escapeHtml(notification.getMessage()) %></strong>
                        <time><%= formatter.format(notification.getCreatedAt()) %></time>
                    </div>
                    <p class="post-meta">关联帖子：<%= TextUtils.escapeHtml(notification.getPostTitle()) %></p>
                </article>
            <% } %>
        </section>
    <% } %>
</main>
</body>
</html>
