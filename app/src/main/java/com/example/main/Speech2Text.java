package com.example.main;


import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognitionService;
import android.speech.RecognizerIntent;
import android.speech.RecognizerResultsIntent;
import android.speech.SpeechRecognizer;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;


public class Speech2Text extends AppCompatActivity {
    public static int REQ_CODE = 100;
    public static String SpokenText;
    public static Intent recognizerIntent;
    public static RecognitionService.Callback callback;
    public static SpeechRecognizer mSpeech;

    public void speech2text() {

        try {

            Intent recognizerIntentResults = new Intent(RecognizerResultsIntent.ACTION_VOICE_SEARCH_RESULTS);
            SpokenText= recognizerIntentResults.getDataString();

        } catch (ActivityNotFoundException a) {
            a.printStackTrace();
        }

    }
}