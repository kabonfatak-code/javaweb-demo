package com.example.demo.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class IpLocationUtil {
    public static final String LOCAL_REGION = "本地";
    public static final String UNKNOWN_REGION = "未知";

    private static final int TIMEOUT_MS = 1500;
    private static final String DEFAULT_API = "http://ip-api.com/json/%s?fields=status,country,regionName,message&lang=zh-CN";
    private static final Pattern JSON_VALUE = Pattern.compile("\"%s\"\\s*:\\s*\"([^\"]*)\"");
    private static final Map<String, String> CACHE = new ConcurrentHashMap<>();
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(TIMEOUT_MS))
            .build();

    private IpLocationUtil() {
    }

    public static String resolveProvince(String ip) {
        String cleanIp = TextUtils.trim(ip);
        if (cleanIp.isEmpty()) {
            return UNKNOWN_REGION;
        }
        return CACHE.computeIfAbsent(cleanIp, IpLocationUtil::lookupProvince);
    }

    private static String lookupProvince(String ip) {
        try {
            if (isLocalOrPrivate(ip)) {
                String publicProvince = lookupByApi("");
                return publicProvince.isEmpty() ? LOCAL_REGION : publicProvince;
            }

            String province = lookupByApi(ip);
            return province.isEmpty() ? UNKNOWN_REGION : province;
        } catch (RuntimeException e) {
            return UNKNOWN_REGION;
        }
    }

    private static String lookupByApi(String ip) {
        String api = System.getProperty("bbs.ip.location.api");
        if (api == null || api.trim().isEmpty()) {
            api = DEFAULT_API;
        }

        String encodedIp = URLEncoder.encode(TextUtils.trim(ip), StandardCharsets.UTF_8);
        String url = api.contains("%s") ? String.format(api, encodedIp) : api;
        if (encodedIp.isEmpty()) {
            url = url.replace("/%s", "").replace("%s", "");
        }

        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofMillis(timeoutMs()))
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return "";
            }
            return provinceFromJson(response.body());
        } catch (IOException | InterruptedException | IllegalArgumentException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return "";
        }
    }

    private static String provinceFromJson(String json) {
        String regionName = readJsonString(json, "regionName");
        String province = ForumOptions.normalizeProvince(regionName);
        if (!province.isEmpty()) {
            return province;
        }

        String region = readJsonString(json, "region");
        province = ForumOptions.normalizeProvince(region);
        if (!province.isEmpty()) {
            return province;
        }

        String country = readJsonString(json, "country");
        return "中国".equals(country) ? UNKNOWN_REGION : "";
    }

    private static String readJsonString(String json, String key) {
        Matcher matcher = Pattern.compile(String.format(JSON_VALUE.pattern(), Pattern.quote(key))).matcher(json == null ? "" : json);
        if (!matcher.find()) {
            return "";
        }
        return unescapeJson(matcher.group(1));
    }

    private static String unescapeJson(String text) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char current = text.charAt(i);
            if (current == '\\' && i + 1 < text.length()) {
                char next = text.charAt(++i);
                if (next == 'u' && i + 4 < text.length()) {
                    String hex = text.substring(i + 1, i + 5);
                    try {
                        result.append((char) Integer.parseInt(hex, 16));
                        i += 4;
                    } catch (NumberFormatException e) {
                        result.append("\\u").append(hex);
                        i += 4;
                    }
                } else {
                    result.append(next);
                }
            } else {
                result.append(current);
            }
        }
        return result.toString();
    }

    private static boolean isLocalOrPrivate(String ip) {
        try {
            InetAddress address = InetAddress.getByName(ip);
            if (address.isAnyLocalAddress() || address.isLoopbackAddress()
                    || address.isLinkLocalAddress() || address.isSiteLocalAddress()) {
                return true;
            }
        } catch (IOException e) {
            return false;
        }

        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            return false;
        }
        try {
            int first = Integer.parseInt(parts[0]);
            int second = Integer.parseInt(parts[1]);
            return first == 10
                    || (first == 172 && second >= 16 && second <= 31)
                    || (first == 192 && second == 168)
                    || (first == 100 && second >= 64 && second <= 127)
                    || (first == 198 && (second == 18 || second == 19));
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static int timeoutMs() {
        String configured = System.getProperty("bbs.ip.location.timeout.ms");
        if (configured == null || configured.trim().isEmpty()) {
            return TIMEOUT_MS;
        }
        try {
            return Math.max(300, Integer.parseInt(configured.trim()));
        } catch (NumberFormatException e) {
            return TIMEOUT_MS;
        }
    }
}
