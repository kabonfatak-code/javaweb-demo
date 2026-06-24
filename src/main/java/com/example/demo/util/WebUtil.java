package com.example.demo.util;

import com.example.demo.model.User;
import com.example.demo.repository.BbsRepository;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public final class WebUtil {
    public static final String REPOSITORY_KEY = "bbsRepository";
    public static final String LOGIN_USER_KEY = "loginUser";
    public static final String FLASH_KEY = "flashMessage";

    private WebUtil() {
    }

    public static BbsRepository getRepository(ServletContext context) {
        synchronized (context) {
            BbsRepository repository = (BbsRepository) context.getAttribute(REPOSITORY_KEY);
            if (repository == null) {
                repository = new BbsRepository();
                context.setAttribute(REPOSITORY_KEY, repository);
            }
            return repository;
        }
    }

    public static User getLoginUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return (User) session.getAttribute(LOGIN_USER_KEY);
    }

    public static boolean isLoggedIn(HttpServletRequest request) {
        return getLoginUser(request) != null;
    }

    public static boolean isAdmin(HttpServletRequest request) {
        User user = getLoginUser(request);
        return user != null && user.isAdmin();
    }

    public static long parseLong(String text, long defaultValue) {
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static int parseInt(String text, int defaultValue) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static void setFlash(HttpServletRequest request, String message) {
        request.getSession().setAttribute(FLASH_KEY, message);
    }

    public static String consumeFlash(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }

        String message = (String) session.getAttribute(FLASH_KEY);
        session.removeAttribute(FLASH_KEY);
        return message;
    }
}
