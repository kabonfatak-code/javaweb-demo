package com.example.demo.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class IpLocationUtil {
    public static final String LOCAL_REGION = "本地";
    public static final String UNKNOWN_REGION = "未知";

    private static final int TIMEOUT_MS = 800;
    private static final String DEFAULT_API = "http://ip-api.com/json/%s?fields=status,country,regionName,message&lang=zh-CN";
    private static final Pattern JSON_VALUE = Pattern.compile("\"%s\"\\s*:\\s*\"([^\"]*)\"");
    private static final Map<String, String> CACHE = new ConcurrentHashMap<>();

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
                return LOCAL_REGION;
            }

            String province = lookupByApi(ip);
            return province.isEmpty() ? UNKNOWN_REGION : province;
        } catch (RuntimeException e) {
            return UNKNOWN_REGION;
        }
    }

    private static String lookupByApi(String ip) {
        if (!locationLookupEnabled()) {
            return "";
        }
        String api = System.getProperty("bbs.ip.location.api");
        if (api == null || api.trim().isEmpty()) {
            api = DEFAULT_API;
        }

        String encodedIp = urlEncode(TextUtils.trim(ip));
        String url = api.contains("%s") ? String.format(api, encodedIp) : api;
        if (encodedIp.isEmpty()) {
            url = url.replace("/%s", "").replace("%s", "");
        }

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(timeoutMs());
            connection.setReadTimeout(timeoutMs());
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");
            int status = connection.getResponseCode();
            if (status < 200 || status >= 300) {
                return "";
            }
            return provinceFromJson(readAll(connection.getInputStream()));
        } catch (IOException | IllegalArgumentException e) {
            return "";
        }
    }

    private static String readAll(InputStream stream) throws IOException {
        if (stream == null) {
            return "";
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = stream.read(buffer)) != -1) {
            output.write(buffer, 0, length);
        }
        return new String(output.toByteArray(), StandardCharsets.UTF_8);
    }

    private static String urlEncode(String value) {
        try {
            return URLEncoder.encode(value == null ? "" : value, "UTF-8");
        } catch (IOException e) {
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

    private static boolean locationLookupEnabled() {
        String configured = System.getProperty("bbs.ip.location.enabled");
        if (configured == null || configured.trim().isEmpty()) {
            configured = System.getenv("BBS_IP_LOCATION_ENABLED");
        }
        return configured == null || configured.trim().isEmpty()
                || "true".equalsIgnoreCase(configured)
                || "1".equals(configured.trim());
    }
}
