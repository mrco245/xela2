package com.example.main;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES256Util {
        private static final String STRING_FORMAT = "utf-8";

        public static byte[] decrypt(String key, String iv, byte[] needDecrypt) {
            try {
                return decrypt(key.getBytes(STRING_FORMAT), iv.getBytes(STRING_FORMAT), needDecrypt);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return null;
        }

        public static byte[] decrypt(byte[] key_bytes, byte[] iv_bytes, byte[] needDecrypt) {
            try {
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                cipher.init(
                        Cipher.DECRYPT_MODE,
                        new SecretKeySpec(key_bytes, "AES"),
                        new IvParameterSpec(iv_bytes));
                return cipher.doFinal(needDecrypt);
            } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
                e.printStackTrace();
            }
            return null;
        }

        public static byte[] encrypt(String key, String iv, byte[] needEncrypt) {
            try {
                return encrypt(key.getBytes(STRING_FORMAT), iv.getBytes(STRING_FORMAT), needEncrypt);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return null;
        }

        public static byte[] encrypt(byte[] key_bytes, byte[] iv_bytes, byte[] needEncrypt) {
            try {
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                cipher.init(
                        Cipher.ENCRYPT_MODE,
                        new SecretKeySpec(key_bytes, "AES"),
                        new IvParameterSpec(iv_bytes));
                return cipher.doFinal(needEncrypt);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

