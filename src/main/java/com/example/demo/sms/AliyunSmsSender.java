package com.example.demo.sms;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public final class AliyunSmsSender {
    private static final String DEFAULT_ENDPOINT = "https://dysmsapi.aliyuncs.com/";
    private static final String REGION_ID = "cn-hangzhou";
    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC);

    private AliyunSmsSender() {
    }

    public static void sendCode(String phone, String code, String purpose) {
        Config config = Config.load(purpose);
        Map<String, String> params = new TreeMap<>();
        params.put("AccessKeyId", config.accessKeyId);
        params.put("Action", "SendSms");
        params.put("Format", "JSON");
        params.put("PhoneNumbers", phone);
        params.put("RegionId", REGION_ID);
        params.put("SignName", config.signName);
        params.put("SignatureMethod", "HMAC-SHA1");
        params.put("SignatureNonce", UUID.randomUUID().toString());
        params.put("SignatureVersion", "1.0");
        params.put("TemplateCode", config.templateCode);
        params.put("TemplateParam", "{\"" + json(config.templateParamName) + "\":\"" + json(code) + "\"}");
        params.put("Timestamp", TIMESTAMP_FORMATTER.format(Instant.now()));
        params.put("Version", "2017-05-25");

        try {
            String canonicalizedQuery = canonicalize(params);
            String signature = sign(canonicalizedQuery, config.accessKeySecret);
            String requestUrl = config.endpoint + "?Signature=" + percentEncode(signature) + "&" + canonicalizedQuery;
            String response = get(requestUrl);
            String responseCode = readJsonValue(response, "Code");
            if (!"OK".equals(responseCode)) {
                String message = readJsonValue(response, "Message");
                throw new SmsSendException("短信发送失败：" + (message.isEmpty() ? responseCode : message));
            }
        } catch (IOException | GeneralSecurityException e) {
            throw new SmsSendException("短信发送失败：" + e.getMessage(), e);
        }
    }

    private static String canonicalize(Map<String, String> params) throws UnsupportedEncodingException {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (builder.length() > 0) {
                builder.append('&');
            }
            builder.append(percentEncode(entry.getKey()));
            builder.append('=');
            builder.append(percentEncode(entry.getValue()));
        }
        return builder.toString();
    }

    private static String sign(String canonicalizedQuery, String accessKeySecret)
            throws GeneralSecurityException, UnsupportedEncodingException {
        String stringToSign = "GET&%2F&" + percentEncode(canonicalizedQuery);
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec((accessKeySecret + "&").getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
        return Base64.getEncoder().encodeToString(mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8)));
    }

    private static String get(String requestUrl) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(requestUrl).openConnection();
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.setRequestMethod("GET");
        int status = connection.getResponseCode();
        InputStream stream = status >= 200 && status < 300 ? connection.getInputStream() : connection.getErrorStream();
        String body = readAll(stream);
        if (status < 200 || status >= 300) {
            throw new IOException("HTTP " + status + " " + body);
        }
        return body;
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

    private static String readJsonValue(String json, String key) {
        if (json == null) {
            return "";
        }
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? matcher.group(1) : "";
    }

    private static String percentEncode(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value == null ? "" : value, "UTF-8")
                .replace("+", "%20")
                .replace("*", "%2A")
                .replace("%7E", "~");
    }

    private static String json(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static final class Config {
        private final String accessKeyId;
        private final String accessKeySecret;
        private final String signName;
        private final String templateCode;
        private final String templateParamName;
        private final String endpoint;

        private Config(String accessKeyId, String accessKeySecret, String signName,
                       String templateCode, String templateParamName, String endpoint) {
            this.accessKeyId = accessKeyId;
            this.accessKeySecret = accessKeySecret;
            this.signName = signName;
            this.templateCode = templateCode;
            this.templateParamName = templateParamName;
            this.endpoint = endpoint.endsWith("/") ? endpoint : endpoint + "/";
        }

        private static Config load(String purpose) {
            String templateKey = purpose == null ? "" : purpose.trim().toLowerCase();
            String specificTemplate = readOptional("bbs.sms.aliyun.template." + templateKey,
                    "BBS_SMS_ALIYUN_TEMPLATE_" + templateKey.toUpperCase());
            String commonTemplate = readOptional("bbs.sms.aliyun.templateCode", "BBS_SMS_ALIYUN_TEMPLATE_CODE");
            String templateCode = specificTemplate.isEmpty() ? commonTemplate : specificTemplate;

            return new Config(
                    readRequired("bbs.sms.aliyun.accessKeyId", "BBS_SMS_ALIYUN_ACCESS_KEY_ID", "阿里云 AccessKeyId"),
                    readRequired("bbs.sms.aliyun.accessKeySecret", "BBS_SMS_ALIYUN_ACCESS_KEY_SECRET", "阿里云 AccessKeySecret"),
                    readRequired("bbs.sms.aliyun.signName", "BBS_SMS_ALIYUN_SIGN_NAME", "阿里云短信签名"),
                    requireValue(templateCode, "阿里云短信模板 code"),
                    readOptional("bbs.sms.aliyun.templateParamName", "BBS_SMS_ALIYUN_TEMPLATE_PARAM_NAME", "code"),
                    readOptional("bbs.sms.aliyun.endpoint", "BBS_SMS_ALIYUN_ENDPOINT", DEFAULT_ENDPOINT)
            );
        }

        private static String readRequired(String property, String env, String label) {
            return requireValue(readOptional(property, env), label);
        }

        private static String requireValue(String value, String label) {
            String clean = value == null ? "" : value.trim();
            if (clean.isEmpty()) {
                throw new SmsSendException("短信服务未配置：" + label);
            }
            return clean;
        }

        private static String readOptional(String property, String env) {
            return readOptional(property, env, "");
        }

        private static String readOptional(String property, String env, String fallback) {
            String value = System.getProperty(property);
            if (value == null || value.trim().isEmpty()) {
                value = System.getenv(env);
            }
            return value == null || value.trim().isEmpty() ? fallback : value.trim();
        }
    }
}
