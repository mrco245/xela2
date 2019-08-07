package com.example.main;

import android.os.AsyncTask;
import android.os.Build;
import android.util.Base64;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static com.example.main.MainActivity.json;

public class RequestAsync extends AsyncTask<String, String, String> {

    Encryption encryption;
    @Override
    protected String doInBackground(String... strings) {
        try {
            //GET Request
            String link = "https://web1-api.xela.cc/v2/log-sensor-data/EE8553FD3B5FD6EE/01e0fdca7d49906b887a8a61e1c6ca2649523de543910a0fed564f4699b167c0/";
            String sensData = encryption.encodeBase64(json.toString());

            System.out.println();
            System.out.print(json.toString());
            // System.out.println();
            //System.out.print(sensData);


            return RequestHandler.sendGet(link + sensData);

            // POST Request
                /*
                JSONObject postDataParams = new JSONObject();
                postDataParams.put("data", "10");
                postDataParams.put("num", "987");
                String encodedParams = encode(postDataParams.toString());
                return RequestHandler.sendPost("https://web1.xela.cc/week1/test1.php",encodedParams);*/
        } catch (Exception e) {

            return "Exception: " + e.getMessage();
        }
    }

    public String sendGet(String url) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        int responseCode = con.getResponseCode();
        System.out.println("Response Code :: " + responseCode);
        if (responseCode == HttpURLConnection.HTTP_OK) { // connection ok
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        } else {
            return "";
        }
    }
}