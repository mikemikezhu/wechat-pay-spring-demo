package com.wechatpay.wechatpay.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.wechatpay.wechatpay.util.PaySignUtil;
import com.wechatpay.wechatpay.util.RandomNonceUtil;
import com.wechatpay.wechatpay.util.XmlUtil;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Component
public class WeChatPayAPIHandler {

    private static final String APP_ID_KEY = "appid";
    private static final String APP_SECRET_KEY = "secret";

    private static final String OPEN_ID_KEY = "openid";
    private static final String MCH_ID_KEY = "mch_id";
    private static final String PREPAY_ID_KEY = "prepay_id";

    private static final String NONCE_STR_KEY = "nonce_str";
    private static final String BODY_KEY = "body";
    private static final String OUT_TRADE_NO_KEY = "out_trade_no";
    private static final String TOTAL_FEE_KEY = "total_fee";
    private static final String SPBILL_CREATE_IP_KEY = "spbill_create_ip";
    private static final String NOTIFY_URL_KEY = "notify_url";
    private static final String TRADE_TYPE_KEY = "trade_type";

    private static final String PAY_SIGN_KEY = "sign";
    private static final String PACKAGE_KEY = "package";

    private static final String SIGN_TYPE_KEY = "signType";
    private static final String SIGN_TYPE_MD5 = "MD5";

    private static final String TIME_STAMP_KEY = "timeStamp";

    private static final String LOGIN_SESSION_CODE_KEY = "js_code";
    private static final String LOGIN_SESSION_GRANT_TYPE_KEY = "grant_type";

    private static final String ERROR_CODE_KEY = "errcode";
    private static final String ERROR_MESSAGE_KEY = "errmsg";

    private static final String RESULT_CODE_KEY = "result_code";
    private static final String RESULT_CODE_SUCCESS = "SUCCESS";
    private static final String RESULT_CODE_FAIL = "FAIL";

    private static final String RETURN_MSG_KEY = "return_msg";
    private static final String RESULT_CODE_OK = "OK";

    @Value("${wechat.id}")
    private String appId; // App id

    @Value("${wechat.secret}")
    private String appSecret; // App secret

    @Value("${wechat.session.url}")
    private String loginSessionUrl; // Login session URL

    @Value("${wechat.session.grant-type}")
    private String loginSessionGrantType; // Login session grant type

    @Value("${wechat.pay.mch-id}")
    private String mchId; // WeChat pay merchant id

    @Value("${wechat.pay.url}")
    private String requestPayUrl; // WeChat pay request URL

    @Value("${wechat.pay.notify}")
    private String notifyUrl; // WeChat pay notify URL

    @Value("${wechat.pay.type}")
    private String tradeType; // WeChat pay trade type. JSAPI for mini program

    @Value("${wechat.pay.key}")
    private String payKey; // WeChat pay sign key

    private RestTemplate restTemplate;

    private static final Logger LOGGER = LoggerFactory.getLogger(WeChatPayAPIHandler.class);

    // Constructor

    public WeChatPayAPIHandler() {
        this.restTemplate = new RestTemplate();
    }

    // Public methods

    public Map<String, Object> sendLoginSessionRequest(String code) throws RestClientException {

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(loginSessionUrl)
                .queryParam(APP_ID_KEY, appId)
                .queryParam(APP_SECRET_KEY, appSecret)
                .queryParam(LOGIN_SESSION_CODE_KEY, code)
                .queryParam(LOGIN_SESSION_GRANT_TYPE_KEY, loginSessionGrantType);

        ResponseEntity<String> responseEntity = restTemplate.getForEntity(builder.toUriString(), String.class);
        String responseBody = responseEntity.getBody();
        JSONObject jsonObject = new JSONObject(responseBody);

        Map<String, Object> result = new HashMap<>();

        if (jsonObject.has(OPEN_ID_KEY)) {
            // Get open id from WeChat server
            String openId = jsonObject.getString(OPEN_ID_KEY);
            result.put(OPEN_ID_KEY, openId);
            return result;

        } else if (jsonObject.has(ERROR_CODE_KEY) && jsonObject.has(ERROR_MESSAGE_KEY)) {
            Integer errorCode = jsonObject.getInt(ERROR_CODE_KEY);
            String errorMessage = jsonObject.getString(ERROR_MESSAGE_KEY);
            result.put(ERROR_CODE_KEY, errorCode);
            result.put(ERROR_MESSAGE_KEY, errorMessage);
            return result;

        } else {
            throw new RestClientException("Fail to parse login session response");
        }
    }

    public Map<String, Object> sendWeChatPayRequest(String openId,
                                                    String tradeNumber,
                                                    String productName,
                                                    String clientIpAddress,
                                                    Double totalPrice) throws RestClientException {

        String nonceStr = RandomNonceUtil.generateRandomNonce();
        Integer totalPriceInt = ((Double) (totalPrice * 100)).intValue();

        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put(APP_ID_KEY, appId);
        requestParams.put(MCH_ID_KEY, mchId);
        requestParams.put(NONCE_STR_KEY, nonceStr);
        requestParams.put(BODY_KEY, productName);
        requestParams.put(OUT_TRADE_NO_KEY, tradeNumber);
        requestParams.put(TOTAL_FEE_KEY, totalPriceInt.toString()); // Need to convert to string, otherwise pay sign will fail
        requestParams.put(SPBILL_CREATE_IP_KEY, clientIpAddress);
        requestParams.put(NOTIFY_URL_KEY, notifyUrl);
        requestParams.put(TRADE_TYPE_KEY, tradeType);
        requestParams.put(OPEN_ID_KEY, openId);

        // First sign
        String firstSign = PaySignUtil.createSign(requestParams, payKey);
        requestParams.put(PAY_SIGN_KEY, firstSign);

        Map<String, Object> orderedRequestParams = new TreeMap<>(requestParams);
        String xml = XmlUtil.convertToXml(orderedRequestParams);

        LOGGER.info("Request WeChat pay: " + xml);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);

        HttpEntity<String> entity = new HttpEntity<>(xml, headers);

        ResponseEntity<String> responseEntity = restTemplate.postForEntity(requestPayUrl, entity, String.class);
        String result = responseEntity.getBody();

        LOGGER.info("Receive WeChat pay response: " + result);

        Map<String, Object> response = XmlUtil.parseXml(result);

        if (response.get(RESULT_CODE_KEY) != null && response.get(RETURN_MSG_KEY) != null &&
                response.get(RESULT_CODE_KEY).toString().equals(RESULT_CODE_SUCCESS) &&
                response.get(RETURN_MSG_KEY).toString().equals(RESULT_CODE_OK)) {

            String prepayId = response.get(PREPAY_ID_KEY).toString();
            String responseNonceStr = response.get(NONCE_STR_KEY).toString();

            long timeStamp = System.currentTimeMillis() / 1000;
            String timeStampStr = Long.toString(timeStamp);

            Map<String, Object> responseParams = new HashMap<>();
            responseParams.put(APP_ID_KEY, appId);
            responseParams.put(NONCE_STR_KEY, responseNonceStr);
            responseParams.put(PACKAGE_KEY, PREPAY_ID_KEY + "=" + prepayId);
            responseParams.put(SIGN_TYPE_KEY, SIGN_TYPE_MD5);
            responseParams.put(TIME_STAMP_KEY, timeStampStr);

            // Second sign
            String secondSign = PaySignUtil.createSign(responseParams, payKey);

            Map<String, Object> updatedResponseParams = new HashMap<>();
            updatedResponseParams.put(PACKAGE_KEY, responseParams.get(PACKAGE_KEY));
            updatedResponseParams.put(NONCE_STR_KEY, responseParams.get(NONCE_STR_KEY));
            updatedResponseParams.put(TIME_STAMP_KEY, responseParams.get(TIME_STAMP_KEY));
            updatedResponseParams.put(SIGN_TYPE_KEY, responseParams.get(SIGN_TYPE_KEY));
            updatedResponseParams.put(PAY_SIGN_KEY, secondSign);

            return updatedResponseParams;

        } else {
            return response;
        }
    }

    public Map<String, Object> replyNotifyWeChatPay(Map<String, Object> response) {

        LOGGER.info("Receive notify WeChat pay response: " + response);

        Map<String, Object> result = new HashMap<>();
        if (response.get(RESULT_CODE_KEY) != null &&
                response.get(RESULT_CODE_KEY).toString().equals(RESULT_CODE_SUCCESS)) {

            // Verify pay sign
            boolean isValid = PaySignUtil.verify(response, response.get(PAY_SIGN_KEY).toString(), payKey);
            if (isValid) {
                result.put(RESULT_CODE_KEY, RESULT_CODE_SUCCESS);
                result.put(RETURN_MSG_KEY, RESULT_CODE_OK);
                return result;
            }
        }

        result.put(RESULT_CODE_KEY, RESULT_CODE_FAIL);
        result.put(RETURN_MSG_KEY, "Fail to verify pay sign");
        return result;
    }
}
