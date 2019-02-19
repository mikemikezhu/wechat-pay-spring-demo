package com.wechatpay.wechatpay.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class WeChatPayBilling {

    @NotNull
    private Double totalPrice;

    @NotEmpty
    private String productName;

    @NotEmpty
    private String code;

    // Setters

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setCode(String code) {
        this.code = code;
    }

    // Getters

    public Double getTotalPrice() {
        return totalPrice;
    }

    public String getProductName() {
        return productName;
    }

    public String getCode() {
        return code;
    }
}