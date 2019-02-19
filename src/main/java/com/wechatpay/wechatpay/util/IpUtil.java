package com.wechatpay.wechatpay.util;

import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

public class IpUtil {

    private static final String REQUEST_HEADER_X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String REQUEST_HEADER_X_REAL_IP = "X-Real-IP";

    public static String findClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader(REQUEST_HEADER_X_FORWARDED_FOR);
        if (!StringUtils.isEmpty(ip)) {
            // The header value of reverse proxy request will contain:
            // (1) client IP address, (2) load balancer server, (3) reverse proxy server
            int index = ip.indexOf(",");
            if (index != -1) {
                return ip.substring(0, index);
            } else {
                return ip;
            }
        }

        ip = request.getHeader(REQUEST_HEADER_X_REAL_IP);
        if (!StringUtils.isEmpty(ip)) {
            return ip;
        }

        return request.getRemoteAddr();
    }
}