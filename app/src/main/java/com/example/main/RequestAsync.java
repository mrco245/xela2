package com.example.main;

import android.os.AsyncTask;
import android.util.Base64;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.example.main.MainActivity.json;

public class RequestAsync extends AsyncTask<String, String, String> {
    @Override
    protected String doInBackground(String... strings) {
        try {
            //GET Request
            String link = "https://web1-api.xela.cc/v2/log-sensor-data/EE8553FD3B5FD6EE/01e0fdca7d49906b887a8a61e1c6ca2649523de543910a0fed564f4699b167c0/";
            String sensData = encode(json.toString());

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

            return new String("Exception: " + e.getMessage());
        }
    }

    @Override
    protected void onPostExecute(String s) {
        // if (s != null) {

        //}

        //http test
        //textView.append(s + "\n\n");
        //textView.append(decode(s));

        //aes test
                /*
                EncryptionMain cipher = new EncryptionMain();
                try {
                    String hash = cipher.encrypt("{\"timestamp\":\"2019-Jun-12 10:06:31\",\"data\":{\"GPS\":{\"latitude\":23.34,\"longituide\":123.34},\"WEATHER\":{\"temperature\":{\"units\":\"C\",\"value\":38.8},\"humidity\":{\"units\":\"%\",\"value\":31.1},\"pressure\":{\"units\":\"kPa\",\"value\":38.8}},\"ACCELEROMETER\":{\"acceleration\":{\"x\":13,\"y\":33,\"z\":35},\"gyroscope\":{\"x\":13,\"y\":33,\"z\":35},\"magnometer\":{\"x\":13,\"y\":33,\"z\":35}}}}");
                    textView.append(hash);
                    textView.append("\n" + cipher.decrypt(hash));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                */

        //base64 test
                /*
                String encodedData = encode("{\\\"timestamp\\\":\\\"2019-Jun-12 10:06:31\\\",\\\"data\\\":{\\\"GPS\\\":{\\\"latitude\\\":23.34,\\\"longituide\\\":123.34},\\\"WEATHER\\\":{\\\"temperature\\\":{\\\"units\\\":\\\"C\\\",\\\"value\\\":38.8},\\\"humidity\\\":{\\\"units\\\":\\\"%\\\",\\\"value\\\":31.1},\\\"pressure\\\":{\\\"units\\\":\\\"kPa\\\",\\\"value\\\":38.8}},\\\"ACCELEROMETER\\\":{\\\"acceleration\\\":{\\\"x\\\":13,\\\"y\\\":33,\\\"z\\\":35},\\\"gyroscope\\\":{\\\"x\\\":13,\\\"y\\\":33,\\\"z\\\":35},\\\"magnometer\\\":{\\\"x\\\":13,\\\"y\\\":33,\\\"z\\\":35}}}}\");\n");
                textView.append(encodedData + "\n\n");

                String decodedData = decode(encodedData);
                textView.append(decodedData);
                */

    }

    public String encode(String data) {
        String encodedData = Base64.encodeToString(data.getBytes(), Base64.DEFAULT);
        encodedData = encodedData.replace('+', '_');
        encodedData = encodedData.replace('/', '-');
        encodedData = encodedData.replace('=', '.');

        return encodedData;
    }

    public String decode(String encodedData) {
        byte[] decodedJson = Base64.decode(encodedData.getBytes(), Base64.DEFAULT);
        String decodedString = null;
        try {
            decodedString = new String(decodedJson, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        decodedString = decodedString.replace('_', '+');
        decodedString = decodedString.replace('-', '/');
        decodedString = decodedString.replace('.', '=');

        return decodedString;
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
            StringBuffer response = new StringBuffer();

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