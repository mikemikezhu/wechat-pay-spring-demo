package com.wechatpay.wechatpay.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONObject;
import org.json.XML;

import java.lang.reflect.Type;
import java.util.Map;

public class XmlUtil {

    public static String convertToXml(Map<String, Object> map) {
        JSONObject json = new JSONObject(map);
        return "<xml>" + XML.toString(json) + "</xml>";
    }

    public static Map<String, Object> parseXml(String xml) {
        JSONObject json = XML.toJSONObject(xml);
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, Object>>() {
        }.getType();
        Map<String, Map<String, Object>> result = gson.fromJson(json.toString(), type);
        return result.get("xml");
    }
}
