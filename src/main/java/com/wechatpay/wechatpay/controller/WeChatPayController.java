package com.wechatpay.wechatpay.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import com.wechatpay.wechatpay.service.WeChatPayService;
import com.wechatpay.wechatpay.util.IpUtil;
import com.wechatpay.wechatpay.dto.WeChatPayBilling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
public class WeChatPayController {

    @Autowired
    private WeChatPayService weChatPayService;

    private static final String RESULT_KEY = "result";

    private static final Logger LOGGER = LoggerFactory.getLogger(WeChatPayController.class);

    // Public methods

    @PostMapping("/request_wechat_pay")
    public ResponseEntity<Map<String, Object>> requestWeChatPay(@RequestBody @Valid WeChatPayBilling billing,
                                                                HttpServletRequest request) {

        String clientIpAddress = IpUtil.findClientIpAddress(request);
        Map<String, Object> result = weChatPayService.requestWeChatPay(billing, clientIpAddress);

        Map<String, Object> body = new HashMap<>();
        if (result != null) {
            body.put(RESULT_KEY, result);
            return new ResponseEntity<>(body, HttpStatus.OK);
        } else {
            body.put(RESULT_KEY, "Open id not found");
            return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "/wechat_pay_notify")
    public void notifyWeChatPay(HttpServletRequest request,
                                HttpServletResponse response) {
        try {
            weChatPayService.notifyWeChatPay(request, response);
        } catch (IOException exception) {
            LOGGER.error("Fail to notify with exception: " + exception);
        }
    }
}