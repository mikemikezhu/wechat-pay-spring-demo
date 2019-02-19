package com.wechatpay.wechatpay.service;

import com.wechatpay.wechatpay.dto.WeChatPayBilling;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public interface WeChatPayService {

    /**
     * Request WeChat pay
     *
     * @param billing         WeChat pay billing
     * @param clientIpAddress Client IP address
     * @return WeChat pay response
     */
    Map<String, Object> requestWeChatPay(WeChatPayBilling billing,
                                         String clientIpAddress);

    void notifyWeChatPay(HttpServletRequest request, HttpServletResponse response) throws IOException;
}