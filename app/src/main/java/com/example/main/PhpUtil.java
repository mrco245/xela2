package com.example.main;

import android.os.Build;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class PhpUtil {
        /**
         * 实现php的
         * $s = hash_hmac('sha256', 'text', 'key', true);
         *
         * @return
         */
        public static byte[] hash_hmac_sha256(String key, String text) {
            try {
                SecretKeySpec secretKeySpec = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
                }
                Mac mac = Mac.getInstance("HmacSHA256");
                mac.init(secretKeySpec);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    return mac.doFinal(text.getBytes(StandardCharsets.UTF_8));
                }
            } catch (NoSuchAlgorithmException | InvalidKeyException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

