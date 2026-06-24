package com.example.demo.util;

import com.example.demo.model.User;

public final class ForumOptions {
    public static final String[] PROVINCES = {
            "北京", "天津", "河北", "山西", "内蒙古", "辽宁", "吉林", "黑龙江",
            "上海", "江苏", "浙江", "安徽", "福建", "江西", "山东", "河南",
            "湖北", "湖南", "广东", "广西", "海南", "重庆", "四川", "贵州",
            "云南", "西藏", "陕西", "甘肃", "青海", "宁夏", "新疆", "香港",
            "澳门", "台湾"
    };

    private ForumOptions() {
    }

    public static boolean isProvince(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        for (String province : PROVINCES) {
            if (province.equals(value.trim())) {
                return true;
            }
        }
        return false;
    }

    public static String roleLabel(String role) {
        if (User.ROLE_ADMIN.equals(role)) {
            return "系统管理员";
        }
        if (User.ROLE_OLD.equals(role)) {
            return "老东西";
        }
        return "新用户";
    }
}
