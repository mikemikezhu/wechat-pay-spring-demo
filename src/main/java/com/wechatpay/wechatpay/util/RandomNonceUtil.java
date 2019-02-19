package com.wechatpay.wechatpay.util;

import java.util.Random;

public class RandomNonceUtil {

    private static final int RANDOM_NONCE_LENGTH = 32;

    public static String generateRandomNonce() {

        // Generate random nonce
        String base = "abcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < RANDOM_NONCE_LENGTH; i++) {
            int number = random.nextInt(base.length());
            stringBuilder.append(base.charAt(number));
        }

        return stringBuilder.toString();
    }
}