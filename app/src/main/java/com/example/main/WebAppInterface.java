package com.example.main;

import android.content.Context;
import android.webkit.JavascriptInterface;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static java.lang.Thread.sleep;

public class WebAppInterface extends MainActivity{
    public Context mContext;

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
    Text2Speech t2s = new Text2Speech();
    public static  Speech2Text s2t = new Speech2Text();
    public static  String text2beSpoken;
    public static  String spokenText;

    @JavascriptInterface
    public String xelaHandler(String json)
    {
        data_out = new JSONObject();
        rawdata = new JSONObject();
        try {
            data_in = new JSONObject(json);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        String module = "PHONE";
        String action = "get-time";
        try {
             module = data_in.get("module").toString();
             action = data_in.get("action").toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(module.equals("PHONE"))
        {
            switch(action) {

                case "get-ssid":
                    rawdata = con.sendSSID();
                    break;
                case "scan-ssids":
                    rawdata = con.scanSSID();
                    break;
                case "connect-phone":
                    String ssid = "";
                    String password = "";
                    try {
                        ssid = data_in.get("ssid").toString();
                        password = data_in.get("password").toString();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    String log = con.connectToWiFi(1, ssid, password).toString();

                    try {
                        rawdata.put("Wifi", log);
                        rawdata.put("data", data);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                    break;



                case "text2speech":
                    try {
                        text2beSpoken = data_in.get("Text2Speech").toString();
                        t2s.ConvertTextToSpeech(text2beSpoken);
                        rawdata.put("Speech Spoken", true);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;

                case "speech2text":
                    //need to implement
                    break;

                case "set-speakervoice":
                    String language = null;
                    try {
                        language = data_in.get("Language").toString();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    t2s.setSpeechVoice(language);

                    try {
                        rawdata.put("Language", language);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case "all-data":
                    //sends all the sensor and gps data to the web server
                    rawdata = sData.sendALL();
                    break;
                case "get-phoneinfo":
                    //gathers the phone info from the users device
                    rawdata = phoneInfo.displayPhoneInfo();
                    break;
                case "get-time":
                    //gets the current date and time in mm/dd/yyy hh:mm:ss:nnnn
                    rawdata = time.currentDateTime();
                    break;
                case "get-gps":
                    //gets the users current gps location in latitude and longitude
                    rawdata = gps.sendLocation();
                    break;
                case "get-sensor":
                    //gets the users sensor data which includes the acceleromter, magnometer, gyroscope, temperature, humidity, and pressure if supported
                    rawdata = sData.sendSensor();
                    break;
                case "get-temperature":
                case "get-weather":
                    //gets the users temperature, humidity and pressure data if supported
                    rawdata = sData.sendWeather();
                    break;
                case "get-acceleramoter":
                case "get-acc":
                case "get-9dof":
                    //Just gets the accelormeter, magnometer and gyroscope data
                    rawdata = sData.sendAccelerometer();
                    break;
                default:

            }

            try
            {
                //data_out = null;
                //Puts the action that was called in the json object that will be sent back to the javascript handler
                data_out.put("module", module);
                data_out.put("action", action);
                //Puts the current time into the json object that will be sent back to the javascript handler
                data_out.put("time", time.currentDateTime().get("Time"));

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
                System.out.println(data_out.toString());

            }catch (Exception e)
            {
                e.printStackTrace();
            }

            //returns the json object back to the javascript handler on the webpage
            return data_out.toString();
        }

        //need a how -- wifi or USB?
        else
        {
            //if the users device is connected thru usb
            if(usbColor.searchEndPoint())
            {
                data_in.remove("module");
                data_in.remove("action");
                rawdata = data_in;

                data_out = getDataOut(module, action);
                byte[] prepareMessage = prepareJSON(data_out);
                sendJsonOverUSB(prepareMessage);
            }

            //if the user is connected through wifi
            else
            {
                data_in.remove("module");
                data_in.remove("action");
                rawdata = data_in;
                data_out = getDataOut(module, action);
                byte[] prepareMessage = prepareJSON(data_out);
                //call send json over wifi
                //sendJsonOverUSB(prepareMessage);
            }
        }
        //returns the json object back to the javascript handler on the webpage
        return data_out.toString();
    }



    public JSONObject getDataOut (String module, String action)
    {
        data_out = new JSONObject();
        try
        {
            //Puts the action that was called in the json object that will be sent back to the javascript handler
            data_out.put("module", module);
            data_out.put("action", action);
            data_out.put("data", rawdata);
            //Puts the current time into the json object that will be sent back to the javascript handler
            //data_out.put("Time", time.currentDateTime().get("Time"));

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
            //data_out.put("Status", status);
            //puts the raw data collected into the data_out json object


        }catch (Exception e)
        {
            e.printStackTrace();
        }

        //json that has the action, time, status, and raw data
        System.out.println(data_out.toString());

        return data_out;
    }

    public byte[] prepareJSON(JSONObject data)
    {
        System.out.println(data);

        byte[] preparedMsg = {};
        byte[] msgPtTwo = data.toString().getBytes();
        byte[] msgPtOne = new byte[]{0x33, (byte)msgPtTwo.length, 0x00};

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try {
            output.write(msgPtOne);
            output.write(msgPtTwo);
        } catch (IOException e) {
            e.printStackTrace();
        }

        preparedMsg = output.toByteArray();

        return preparedMsg;
    }

    public boolean sendJsonOverUSB(byte[] preparedMsg)
    {
        boolean success = false;
        boolean isConnected;

        isConnected = usbColor.searchEndPoint();

        if(isConnected)
        {
            usbColor.setupUsbComm(preparedMsg);
        }


        if(usbColor.usbResult != 0)
            success = true;

        return success;
    }

/**
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

            case "text-to-speech":
                try {
                    data_in = new JSONObject(json);
                }catch(Exception e)
                {
                    e.printStackTrace();
                }
                System.out.println(data_in);

                try {
                    text2beSpoken = data_in.get("Text2Speech").toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                t2s.ConvertTextToSpeech(text2beSpoken);
                //tts.speak(text2beSpoken, TextToSpeech.QUEUE_FLUSH, null);
                try {
                    rawdata.put("Speech Spoken",true);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;

            case "Set-Voice":

                   //rawdata= t2s.getVoices();
                t2s.setSpeechVoice("USA");

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
        System.out.println(rawdata.toString());

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
        System.out.println(data_out.toString());

        //returns the json object back to the javascript handler on the webpage
        return data_out.toString();
    }**/

}