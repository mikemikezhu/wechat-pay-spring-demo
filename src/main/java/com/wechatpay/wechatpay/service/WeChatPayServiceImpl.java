package com.wechatpay.wechatpay.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.wechatpay.wechatpay.api.WeChatPayAPIHandler;
import com.wechatpay.wechatpay.dto.WeChatPayBilling;
import com.wechatpay.wechatpay.util.XmlUtil;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Random;

@Service
public class WeChatPayServiceImpl implements WeChatPayService {

    @Autowired
    private WeChatPayAPIHandler weChatPayAPIHandler;

    private static final String OPEN_ID_KEY = "openid";
    private static final String ERROR_CODE_KEY = "errcode";
    private static final String ERROR_MESSAGE_KEY = "errmsg";

    private static final Logger LOGGER = LoggerFactory.getLogger(WeChatPayServiceImpl.class);

    // Public methods

    @Override
    public Map<String, Object> requestWeChatPay(WeChatPayBilling billing,
                                                String clientIpAddress) {
        // Request pay
        String code = billing.getCode();
        String openId = loginSession(code);

        if (openId != null) {

            String tradeNumber = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());

            Random random = new Random();
            Integer randomInteger = random.nextInt(900) + 100; // Random number from 100 - 999
            tradeNumber = tradeNumber + randomInteger.toString();

            return weChatPayAPIHandler.sendWeChatPayRequest(openId,
                    tradeNumber,
                    billing.getProductName(),
                    clientIpAddress,
                    billing.getTotalPrice());
        }

        return null;
    }

    @Override
    public void notifyWeChatPay(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ServletInputStream inputStream = request.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        StringBuilder stringBuilder = new StringBuilder();
        while (bufferedReader.readLine() != null) {
            String line = bufferedReader.readLine();
            stringBuilder.append(line);
        }

        bufferedReader.close();
        String notificationXml = stringBuilder.toString();

        Map<String, Object> notification = XmlUtil.parseXml(notificationXml);
        Map<String, Object> result = weChatPayAPIHandler.replyNotifyWeChatPay(notification);
        String resultXml = XmlUtil.convertToXml(result);

        ServletOutputStream outputStream = response.getOutputStream();
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
        bufferedOutputStream.write(resultXml.getBytes());
        bufferedOutputStream.flush();
        bufferedOutputStream.close();
    }

    // Private methods

    @Nullable
    private String loginSession(String code) {
        // Login session to find openId
        try {
            Map<String, Object> result = weChatPayAPIHandler.sendLoginSessionRequest(code);
            if (result.get(OPEN_ID_KEY) != null) {
                return result.get(OPEN_ID_KEY).toString();
            } else if (result.get(ERROR_CODE_KEY) != null && result.get(ERROR_MESSAGE_KEY) != null) {
                LOGGER.error("Fail to login session with error: " + result.get(ERROR_MESSAGE_KEY).toString());
            } else {
                LOGGER.warn("Unable to find open id");
            }
            return null;
        } catch (Exception e) {
            LOGGER.error("Fail to login session with exception: " + e.getMessage());
            return null;
        }
    }
}