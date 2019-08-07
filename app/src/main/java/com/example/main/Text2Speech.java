package com.example.main;

import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

/*
TEXT
TO
SPEECH
 */
public class Text2Speech {

        public static String text;
        public static TextToSpeech tts;


    public void ConvertTextToSpeech(String mytext) {
        // TODO Auto-generated method stub
        text = mytext;
        if(text==null||"".equals(text))
        {
            text = "Please input something";
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }else
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    public void setSpeechVoice(String lang)
    {
        if( lang.equals("CanadianFrench"))
        {
            tts.setLanguage(Locale.CANADA_FRENCH);
        }
        if( lang.equals("English(UK)"))
        {
            tts.setLanguage(Locale.UK);
        }
        if( lang.equals("English(US)"))
        {
            Locale mylocale  = new Locale("ru");
            tts.setLanguage(mylocale);
        }
        if( lang.equals("French"))
        {
            tts.setLanguage(Locale.FRANCE);
        }
        if( lang.equals("German"))
        {
            tts.setLanguage(Locale.GERMANY);
        }
        if( lang.equals("Italian"))
        {
            tts.setLanguage(Locale.ITALY);
        }
        if( lang.equals("Japanese"))
        {
            tts.setLanguage(Locale.JAPAN);
        }



    }

    public JSONObject getVoices()
    {
      JSONObject voiceSet = new JSONObject();
      JSONObject names = new JSONObject();
        int size = 0;
        Set<Voice> voiceSet1 = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
           voiceSet1 = tts.getVoices();
        }

        Iterator<Voice> it = voiceSet1.iterator();

        //hashCode i = 0;

        while(it.hasNext())
        {
            Voice i = it.next();

            String s = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                s = i.getName();
            }

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {

            }
            try {
                names.put("name", s);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        try {
            voiceSet.put("Voices", names);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return voiceSet;
    }

}

