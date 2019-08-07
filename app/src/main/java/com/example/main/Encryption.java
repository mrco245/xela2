package com.example.main;

import android.os.Build;
import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Encryption {

    public String encodeBase64(String data) {
        String encodedData = Base64.encodeToString(data.getBytes(), Base64.DEFAULT);
        encodedData = encodedData.replace('+', '_');
        encodedData = encodedData.replace('/', '-');
        encodedData = encodedData.replace('=', '.');

        return encodedData;
    }

    public String decodeBase64(String encodedData) {
        byte[] decodedJson = Base64.decode(encodedData.getBytes(), Base64.DEFAULT);
        String decodedString = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            decodedString = new String(decodedJson, StandardCharsets.UTF_8);
        }
        decodedString = decodedString.replace('_', '+');
        decodedString = decodedString.replace('-', '/');
        decodedString = decodedString.replace('.', '=');

        return decodedString;
    }


    private String encryptAES(String data, String secretKey) {
        try {

            data = data.replace('+', '_');
            data = data.replace('/', '-');
            data = data.replace('=', '.');

            String initVector = UUID.randomUUID().toString().substring(0, 16);
            byte[] bytePass = secretKey.getBytes("utf-8");
            byte[] byteV = initVector.getBytes("utf-8");

            byte[] byteKey = Arrays.copyOf(bytePass, 32);
            byte[] byteIV = Arrays.copyOf(byteV, 16);

            SecretKeySpec skeySpec = new SecretKeySpec(byteKey, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(byteIV);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivSpec);

            data = initVector + data;
            byte[] byteText = data.getBytes("utf-8");
            byte[] buf = cipher.doFinal(byteText);

            byte[] byteBase64 = Base64.encode(buf, Base64.DEFAULT);
            String finalData = new String(byteBase64);

            return finalData;
        }
        catch(Exception ex) {
            return ex.getMessage();
        }
    }

    private String decryptAES(String data, String secretKey) {
        try {
            String initVector = "";
            data = data.replace('_', '+');
            data = data.replace('-', '/');
            data = data.replace('.', '=');

            byte[] byteData = Base64.decode(data.getBytes(), Base64.DEFAULT);
            byte[] bodyData = {};
            for(int i=0; i<16;i++) {
                initVector += byteData[i];
            }

            byte[] bytePass = secretKey.getBytes("utf-8");
            byte[] byteV = initVector.getBytes("utf-8");

            byte[] byteKey = Arrays.copyOf(bytePass, 32);
            byte[] byteIV = Arrays.copyOf(byteV, 16);

            SecretKeySpec skeySpec = new SecretKeySpec(byteKey, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(byteIV);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivSpec);

            byte[] byteText = cipher.doFinal(byteData);
            String clearText = new String(byteText);
            clearText = clearText.substring(16);

            return clearText;
        }
        catch(Exception ex) {
            return ex.getMessage();
        }
    }

}

