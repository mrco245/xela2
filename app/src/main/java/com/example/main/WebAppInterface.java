package com.example.main;

import android.content.Context;
import android.webkit.JavascriptInterface;

import org.json.JSONObject;

public class WebAppInterface{
    Context mContext;

    WebAppInterface(Context c) {
        mContext = c;
    }

    //defines the instances of the other classes to be used
    Sensors sData = new Sensors();
    PhoneInfo phoneInfo = new PhoneInfo();
    Time time = new Time();
    GPS gps = new GPS();

    //creates the json objects that are used in the javascript handler
    JSONObject data_out = new JSONObject();
    JSONObject rawdata = new JSONObject();
    JSONObject data_in;
    String status = "";


    @JavascriptInterface
    public String xelaHandler(String action, String json)
    {

        //build action json

        //build time

        //build status - pass/fail

        //build data

        //data_out is all of those combined


        switch(action) {
            case "set-color":
                try {
                    data_in = new JSONObject(json);
                }catch(Exception e)
                {
                    e.printStackTrace();
                }
                System.out.println(data_in);
                rawdata = data_in;

                break;
            case "all-data":
                rawdata = sData.sendALL();
                break;
            case "phone-info":
                rawdata = phoneInfo.displayPhoneInfo();
                break;
            case "time":
                rawdata = time.currentDateTime();
                break;
            case "GPS":
                rawdata = gps.sendLocation();
                break;
            case "SENSOR":
                rawdata = sData.sendSensor();
                break;
            case "temperature":
            case "weather":
                rawdata = sData.sendWeather();
                break;
            case "acceleramoter":
            case "acc":
            case "9DOF":
                rawdata = sData.sendAccelerometer();
                break;
            default:

        }

        //the json object that has the raw data from the sensors, gps location, time, etc.
        System.out.println(rawdata.toString());

        try
        {
            data_out.put("Action", action);
            data_out.put("Time", time.currentDateTime().get("Time"));

            if(rawdata.length() != 0)
            {
                 status = "Pass: data was successful";
            }
            else
            {
                status = "Failure: data was unsuccessful";
            }

            data_out.put("Status", status);
            data_out.put("rawdata", rawdata);

        }catch (Exception e)
        {
            e.printStackTrace();
        }

        //json that has the action, time, status, and raw data
        System.out.println(data_out.toString());

        return data_out.toString();
    }
}