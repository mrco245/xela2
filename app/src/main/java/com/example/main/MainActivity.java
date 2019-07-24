package com.example.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity{

    public WebView webView;
    public static final int RequestPermissionCode = 1;
    public static WifiConnector con;

    //For Phone INFO
    public static NetworkInfo mWifi;
    public static NetworkInfo mMobile;
    public static TelephonyManager tm;
    public static TelephonyManager tm1;

    public static SensorManager sensorManager;
    public static Sensor accelerometer, mGyro, mMagno, mPressure, mTemp, mHumi;

    //Had to change to static so data is saved and can be used across classes
    public static JSONObject json = new JSONObject();

    public static JSONObject GPSData = new JSONObject();
    public static JSONObject SensorData = new JSONObject();
    public static JSONObject data = new JSONObject();

    public static JSONObject weather = new JSONObject();
    public static JSONObject Pressure = new JSONObject();
    public static JSONObject Temp = new JSONObject();
    public static JSONObject humidity = new JSONObject();

    public static JSONObject Accelerometer = new JSONObject();
    public static JSONObject acceleration = new JSONObject();
    public static JSONObject gyro = new JSONObject();
    public static JSONObject magnometer = new JSONObject();

    //for GPS
    public static LocationManager locationManager;
    public static LocationListener listener;
    public static JSONObject gps = new JSONObject();
    public static  Sensors sensors = new Sensors();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        IntentFilter filter = new IntentFilter();
        filter.addAction(getPackageName() + "android.net.conn.CONNECTIVITY_CHANGE");
       WifiConnector.WiFiConnectorListener list = new WifiConnector.WiFiConnectorListener() {
            @Override
            public void onWiFiStateUpdate(WifiInfo wifiInfo, NetworkInfo.DetailedState detailedState) {

            }
        };

        con = new WifiConnector(this, list);
        System.out.println(con.checkWifi());

        webView = (WebView) findViewById(R.id.WebView1);

        //open browser inside application
        webView.setWebViewClient(new MyBrowser());
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowFileAccess(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            webView.getSettings().setAllowFileAccessFromFileURLs(true);
        }

        webView.getSettings().setAllowContentAccess(true);
        webView.addJavascriptInterface(new WebAppInterface(this), "xelaHandler");
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        do {
            RequestPermission();
        }while (!CheckPermission());

        if (CheckPermission())
        {
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                //Log.d(TAG, "No SDCARD");
            } else {

                try {
                    //webView.loadUrl("file:///sdcard/phone/welcome.html");
                    webView.loadUrl("file:///android_asset/madisons.html");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


            StartSensors();
            startLocationListener();
            checkInternet();
        }



    }

    @Override
    public void onBackPressed() {
        if(webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    public class MyBrowser extends WebViewClient
    {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if(Uri.parse(url).getHost().endsWith("html")) {
                return false;
            }

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            view.getContext().startActivity(intent);
            return true;
        }
    }
    private void RequestPermission()
    {
        ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE, ACCESS_NETWORK_STATE, ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION, READ_PHONE_STATE, READ_CONTACTS}, RequestPermissionCode);
    }

    @Override
    public void onRequestPermissionsResult ( int requestCode, @NonNull String[] permissions,
                                             @NonNull int[] grantResults){
        switch (requestCode) {
            case RequestPermissionCode:
                if (grantResults.length > 0) {
                    boolean StoragePermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean ReadPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean NetworkPermission = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                    boolean FinePermission = grantResults[3] == PackageManager.PERMISSION_GRANTED;
                    boolean CoursePermission = grantResults[4] == PackageManager.PERMISSION_GRANTED;
                    boolean PhonePermission = grantResults[5] == PackageManager.PERMISSION_GRANTED;
                    boolean AccountPermission = grantResults[6] == PackageManager.PERMISSION_GRANTED;

                    if (StoragePermission && ReadPermission && NetworkPermission && FinePermission && CoursePermission && PhonePermission &&  AccountPermission) {
                        Toast.makeText(this, "Permission is Granted", Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(this, "Permission is Denied", Toast.LENGTH_SHORT).show();
                    }

                }
        }
    }

    public boolean CheckPermission ()
    {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
        int result2 = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_NETWORK_STATE);
        int result3 = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION);
        int result4 = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_COARSE_LOCATION);
        int result5 = ContextCompat.checkSelfPermission(getApplicationContext(), READ_PHONE_STATE);
        int result6 = ContextCompat.checkSelfPermission(getApplicationContext(), READ_CONTACTS);


        return result == PackageManager.PERMISSION_GRANTED &&
                result1 == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED &&
                result3 == PackageManager.PERMISSION_GRANTED &&
                result4 == PackageManager.PERMISSION_GRANTED && result5==PackageManager.PERMISSION_GRANTED && result6==PackageManager.PERMISSION_GRANTED;
    }

    //Starts and gets the current network state
    public void checkInternet() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        mMobile = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //cursor = getContentResolver().query(uriContact, null, null, null, null);
        tm1 = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
    }

    //functions for GPS location
    public void startLocationListener() {

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                try {
                    gps.put("latitude", location.getLatitude());
                    gps.put("longituide", location.getLongitude());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
            }
        };
        //configure_button();
        try {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, listener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Starts the individual sensors by calling their individual functions.
    public void StartSensors() {

        //Log.d(TAG, "OnCreate: Starting Sensor Services");
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        sensors.getAcceleration();
        sensors.getGyro();
        sensors.getMagnometer();
        sensors.getPressure();
        sensors.getTemp();
        sensors.getHumidity();
    }


}
