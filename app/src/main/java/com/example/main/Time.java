package com.example.main;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

import static android.text.format.DateFormat.format;

public class Time {

    //Function that gets the current date and time including milliseconds
    public JSONObject currentDateTime()
    {
        Calendar calendar = Calendar.getInstance();
        String currentDate = format("yyyy-MM-dd hh:mm:ss", calendar).toString();
        int milli = calendar.get(Calendar.MILLISECOND);
        currentDate += ":" + milli;

        JSONObject DateTime = new JSONObject();
        try {
            DateTime.put("Time",currentDate);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return DateTime;
    }
}
