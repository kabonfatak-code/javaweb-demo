<%--
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.example.demo.util.TextUtils" %>
<%
    String error = (String) request.getAttribute("error");
    String savedTitle = (String) request.getAttribute("title");
    String savedContent = (String) request.getAttribute("content");
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>发表留言 - 小型论坛 BBS</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/style.css">
</head>
<body>
<%@ include file="/WEB-INF/views/fragments/header.jspf" %>
<main class="page">
    <section class="form-card wide">
        <h1>发表留言</h1>
        <% if (error != null) { %>
            <div class="error"><%= TextUtils.escapeHtml(error) %></div>
        <% } %>
        <form method="post" action="<%= ctx %>/post/new" accept-charset="UTF-8">
            <label>
                留言主题
                <input type="text" name="title" value="<%= TextUtils.escapeHtml(savedTitle) %>" maxlength="100" required>
            </label>
            <label>
                留言内容
                <textarea name="content" rows="12" maxlength="4000" required><%= TextUtils.escapeHtml(savedContent) %></textarea>
            </label>
            <div class="form-actions">
                <a class="button" href="<%= ctx %>/posts">返回列表</a>
                <button class="button primary" type="submit">发表</button>
            </div>
        </form>
    </section>
</main>
</body>
</html>
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.example.demo.util.TextUtils,com.example.demo.util.ForumOptions" %>
<%
    String error = (String) request.getAttribute("error");
    String savedTitle = (String) request.getAttribute("title");
    String savedTopic = (String) request.getAttribute("topic");
    String savedRegion = (String) request.getAttribute("region");
    String savedContent = (String) request.getAttribute("content");
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>发表帖子 - BBS 论坛</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/style.css">
</head>
<body>
<%@ include file="/WEB-INF/views/fragments/header.jspf" %>
<main class="page">
    <section class="form-card wide">
        <h1>发表帖子</h1>
        <% if (error != null) { %><div class="error"><%= TextUtils.escapeHtml(error) %></div><% } %>
        <form method="post" action="<%= ctx %>/post/new" accept-charset="UTF-8">
            <label>标题
                <input type="text" name="title" value="<%= TextUtils.escapeHtml(savedTitle) %>" maxlength="120" required>
            </label>
            <div class="form-grid">
                <label>主题
                    <select name="topic" required>
                        <% for (String topic : ForumOptions.TOPICS) { %>
                            <option value="<%= topic %>" <%= topic.equals(savedTopic) ? "selected" : "" %>><%= topic %></option>
                        <% } %>
                    </select>
                </label>
                <label>地区
                    <select name="region" required>
                        <% for (String region : ForumOptions.REGIONS) { %>
                            <option value="<%= region %>" <%= region.equals(savedRegion) ? "selected" : "" %>><%= region %></option>
                        <% } %>
                    </select>
                </label>
            </div>
            <label>正文
                <textarea name="content" rows="14" maxlength="8000" required><%= TextUtils.escapeHtml(savedContent) %></textarea>
            </label>
            <div class="form-actions">
                <a class="button" href="<%= ctx %>/posts">返回主页</a>
                <button class="button primary" type="submit">发表</button>
            </div>
        </form>
    </section>
</main>
</body>
</html>
