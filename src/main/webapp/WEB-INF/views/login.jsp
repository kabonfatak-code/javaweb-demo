<%--
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.example.demo.util.TextUtils" %>
<%
    String error = (String) request.getAttribute("error");
    String savedUsername = (String) request.getAttribute("username");
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>用户登录 - 小型论坛 BBS</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/style.css">
</head>
<body>
<%@ include file="/WEB-INF/views/fragments/header.jspf" %>
<main class="page narrow">
    <section class="form-card">
        <h1>用户登录</h1>
        <% if (error != null) { %>
            <div class="error"><%= TextUtils.escapeHtml(error) %></div>
        <% } %>
        <form method="post" action="<%= ctx %>/login" accept-charset="UTF-8">
            <label>
                用户名
                <input type="text" name="username" value="<%= TextUtils.escapeHtml(savedUsername) %>" required>
            </label>
            <label>
                密码
                <input type="password" name="password" required>
            </label>
            <button class="button primary full" type="submit">登录</button>
        </form>
        <p class="form-link">没有账号？<a href="<%= ctx %>/register">立即注册</a></p>
    </section>
</main>
</body>
</html>
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.example.demo.util.TextUtils" %>
<%
    String error = (String) request.getAttribute("error");
    String savedUsername = (String) request.getAttribute("username");
    String savedPhone = (String) request.getAttribute("phone");
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>登录 - BBS 论坛</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/style.css">
</head>
<body>
<%@ include file="/WEB-INF/views/fragments/header.jspf" %>
<main class="page auth-grid">
    <section class="form-card">
        <h1>用户名密码登录</h1>
        <% if (error != null) { %><div class="error"><%= TextUtils.escapeHtml(error) %></div><% } %>
        <form method="post" action="<%= ctx %>/login" accept-charset="UTF-8">
            <input type="hidden" name="mode" value="password">
            <label>用户名
                <input type="text" name="username" value="<%= TextUtils.escapeHtml(savedUsername) %>" required>
            </label>
            <label>密码
                <input type="password" name="password" required>
            </label>
            <button class="button primary full" type="submit">登录</button>
        </form>
        <p class="form-link"><a href="<%= ctx %>/password/reset">忘记密码？</a></p>
    </section>

    <section class="form-card">
        <h1>电话验证码登录</h1>
        <form method="post" action="<%= ctx %>/login" accept-charset="UTF-8">
            <input type="hidden" name="mode" value="sms">
            <label>电话
                <div class="inline-control">
                    <input id="loginPhone" type="text" name="phone" value="<%= TextUtils.escapeHtml(savedPhone) %>" required>
                    <button class="button" type="button" data-sms-purpose="LOGIN" data-phone-input="loginPhone">获取验证码</button>
                </div>
            </label>
            <label>短信验证码
                <input type="text" name="smsCode" required>
            </label>
            <div class="sms-result" id="smsResult"></div>
            <button class="button primary full" type="submit">验证码登录</button>
        </form>
        <p class="form-link">没有账号？<a href="<%= ctx %>/register">立即注册</a></p>
    </section>
</main>
<script src="<%= ctx %>/assets/app.js"></script>
</body>
</html>
