<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.example.demo.util.TextUtils,com.example.demo.util.ForumOptions" %>
<%
    String error = (String) request.getAttribute("error");
    String savedTopic = (String) request.getAttribute("topic");
    String savedRegion = (String) request.getAttribute("region");
    String savedContent = (String) request.getAttribute("content");
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>发表留言 - BBS 论坛</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/style.css">
</head>
<body>
<%@ include file="/WEB-INF/views/fragments/header-v2.jspf" %>
<main class="page">
    <section class="form-card wide">
        <h1>发表留言</h1>
        <% if (error != null) { %><div class="error"><%= TextUtils.escapeHtml(error) %></div><% } %>
        <form method="post" action="<%= ctx %>/post/new" accept-charset="UTF-8">
            <div class="form-grid">
                <label>主题
                    <input type="text" name="topic" value="<%= TextUtils.escapeHtml(savedTopic) %>" maxlength="30" placeholder="例如：考研交流、二手交易" required>
                </label>
                <label>省份
                    <select name="region" required>
                        <option value="">请选择省份</option>
                        <% for (String province : ForumOptions.PROVINCES) { %>
                            <option value="<%= province %>" <%= province.equals(savedRegion) ? "selected" : "" %>><%= province %></option>
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
