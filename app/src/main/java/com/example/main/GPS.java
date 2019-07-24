package com.example.main;

import org.json.JSONException;
import org.json.JSONObject;

public class GPS extends MainActivity {

    public JSONObject setGPSdata() {
        try {
            data.put("GPS", gps);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data;
    }

    //Function that sends the current GPS location to the web server
    public JSONObject sendLocation() {

        GPSData = setGPSdata();

        try {
            json.put("data", GPSData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

}
