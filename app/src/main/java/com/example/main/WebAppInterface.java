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
    USBColor usbColor = new USBColor();

    //creates the json objects that are used in the javascript handler
    JSONObject data_out = new JSONObject();
    JSONObject rawdata = new JSONObject();
    public static JSONObject data_in;
    String status = "";


    @JavascriptInterface
    public String xelaHandler(String action, String json)
    {
        //Gets the action the user chose on the webpage and this switch case statement handles that
        switch(action) {
            //Will be used to help set the color of the led light, but right now just displays the color value to the screen
            case "set-color":
                try {
                    data_in = new JSONObject(json);
                }catch(Exception e)
                {
                    e.printStackTrace();
                }
                System.out.println(data_in);

                rawdata = data_in;

                usbColor.connectUsb();


                break;
            case "all-data":
                //sends all the sensor and gps data to the web server
                rawdata = sData.sendALL();
                break;
            case "phone-info":
                //gathers the phone info from the users device
                rawdata = phoneInfo.displayPhoneInfo();
                break;
            case "time":
                //gets the current date and time in mm/dd/yyy hh:mm:ss:nnnn
                rawdata = time.currentDateTime();
                break;
            case "GPS":
                //gets the users current gps location in latitude and longitude
                rawdata = gps.sendLocation();
                break;
            case "SENSOR":
                //gets the users sensor data which includes the acceleromter, magnometer, gyroscope, temperature, humidity, and pressure if supported
                rawdata = sData.sendSensor();
                break;
            case "temperature":
            case "weather":
                //gets the users temperature, humidity and pressure data if supported
                rawdata = sData.sendWeather();
                break;
            case "acceleramoter":
            case "acc":
            case "9DOF":
                //Just gets the accelormeter, magnometer and gyroscope data
                rawdata = sData.sendAccelerometer();
                break;
            default:

        }

        //the json object that has the raw data from the sensors, gps location, time, etc.
        //System.out.println(rawdata.toString());

        try
        {
            //Puts the action that was called in the json object that will be sent back to the javascript handler
            data_out.put("Action", action);
            //Puts the current time into the json object that will be sent back to the javascript handler
            data_out.put("Time", time.currentDateTime().get("Time"));

            //sets the status of the function, the the raw data is filled the the status is a pass and if no data then the status is fail.
            if(rawdata.length() != 0)
            {
                 status = "Successful: data was successfully collected";
            }
            else
            {
                status = "Failure: there was no data collected";
            }
            //puts the status into the json object data_out
            data_out.put("Status", status);
            //puts the raw data collected into the data_out json object
            data_out.put("rawdata", rawdata);

        }catch (Exception e)
        {
            e.printStackTrace();
        }

        //json that has the action, time, status, and raw data
       // System.out.println(data_out.toString());

        //returns the json object back to the javascript handler on the webpage
        return data_out.toString();
    }
}