package com.wechatpay.wechatpay.util;

import org.springframework.util.DigestUtils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PaySignUtil {

    private static final String KEY = "key";

    public static String createSign(Map<String, Object> params, String key) {
        // Create WeChat pay sign
        String linkString = createLinkString(params);
        linkString = linkString + "&" + KEY + "=" + key;
        return DigestUtils.md5DigestAsHex(getContentBytes(linkString, "utf-8")).toUpperCase();
    }

    public static boolean verify(Map<String, Object> params, String sign, String key) {
        // Verify WeChat pay sign
        String linkString = createLinkString(params);
        linkString = linkString + "&" + KEY + "=" + key;
        String expectedSign = DigestUtils.md5DigestAsHex(getContentBytes(linkString, "utf-8"));
        return sign.equals(expectedSign);
    }

    // Private methods

    private static String createLinkString(Map<String, Object> params) {
        // Create link string key-value pair
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);

        String linkString = "";
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = params.get(key).toString();
            if (i == keys.size() - 1) {
                // Last value shall not contain "&"
                linkString = linkString + key + "=" + value;
            } else {
                linkString = linkString + key + "=" + value + "&";
            }
        }

        return linkString;
    }

    private static byte[] getContentBytes(String content, String charset) {
        if (charset == null || "".equals(charset)) {
            return content.getBytes();
        }
        try {
            return content.getBytes(charset);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Incorrect charset: " + charset);
        }
    }
}
