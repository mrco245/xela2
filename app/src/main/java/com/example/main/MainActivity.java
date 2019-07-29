package com.example.main;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.example.main.USBColor.deviceFound;

public class MainActivity extends AppCompatActivity{

    public WebView webView;
    public static final int RequestPermissionCode = 1;
    public static WifiConnector con;

    //For Phone INFO
    public static NetworkInfo mWifi;
    public static NetworkInfo mMobile;
    public static TelephonyManager tm;
    public static TelephonyManager tm1;
    public static ConnectivityManager connManager;
    public static UsbManager manager;

    private static final String ACTION_USB_PERMISSION =
            "com.android.example.USB_PERMISSION";
    public static PendingIntent mPermissionIntent;

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
    public static Sensors sensors = new Sensors();
    public static USBColor test = new USBColor();

    String url = "file://" +Environment.getExternalStorageDirectory().getPath() +"/phone/welcome.html";
    //String url = "file:///android_asset/madisons.html";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        IntentFilter filter = new IntentFilter();
        filter.addAction(getPackageName() + "android.net.conn.CONNECTIVITY_CHANGE");
       WifiConnector.WiFiConnectorListener list = new WifiConnector.WiFiConnectorListener() {
            @Override
            public void onWiFiStateUpdate(WifiInfo wifiInfo, DetailedState detailedState) {

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
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);

        //adds the javascipt functionality and names the handler
        webView.addJavascriptInterface(new WebAppInterface(this), "xelaHandler");

        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        //String url = "file://" +Environment.getExternalStorageDirectory().getPath() +"/phone/welcome.html";

        //Request the Permissions until all permissions are granted
        //Will check if the user disabled a permission, it will ask for only that permission
        do {
            RequestPermission();


        }while (!CheckPermission());


        //Starts and registers the sensor listener
        StartSensors();
        //Starts the gps location listener
        startLocationListener();
        //checks the network state
        checkInternet();


        //if the user has the permissions necessary for the project
        if (CheckPermission())
        {
            //Checks to see if the user has a SD card
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                //Log.d(TAG, "No SDCARD");
            } else {
                //Opens up the correct webpage
                try {
                    //opens up the webpage stored on the phones sd card
                    webView.loadUrl(url);

                    //opens up the webpage stored in the assets folder in this project
                    //webView.loadUrl("file:///android_asset/madisons.html");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }



    }

    @Override
    protected void onDestroy() {
        test.releaseUsb();
        unregisterReceiver(mUsbReceiver);
        unregisterReceiver(mUsbDeviceReceiver);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()){
            case R.id.action_home : webView.loadUrl(url); break;
            case R.id.action_refresh : webView.loadUrl(webView.getUrl()); break;
        }
        return super.onOptionsItemSelected(item);
    }

    //Allows the back button to be pressed and function properly as expected
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

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            view.getContext().startActivity(intent);
            return true;
        }
    }

    //Requests the required permissions from the user at runtime
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

    //Checks the permissions to see if they are allowed or not.
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
        connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        mMobile = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //cursor = getContentResolver().query(uriContact, null, null, null, null);
        tm1 = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);

        manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(mUsbReceiver, filter);

        registerReceiver(mUsbDeviceReceiver, new IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED));
        registerReceiver(mUsbDeviceReceiver, new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED));
    }
    public final BroadcastReceiver mUsbReceiver =
            new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (ACTION_USB_PERMISSION.equals(action)) {

                        /** Toast.makeText(Color.this,
                         "ACTION_USB_PERMISSION",
                         Toast.LENGTH_LONG).show();
                         textStatus.setText("ACTION_USB_PERMISSION");**/

                        synchronized (this) {
                            UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                            if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                                if(device != null){
                                    //connectUsb();
                                }
                            }
                            else {
                                /**Toast.makeText(Color.this,
                                 "permission denied for device " + device,
                                 Toast.LENGTH_LONG).show();
                                 textStatus.setText("permission denied for device " + device);**/
                            }
                        }
                    }
                }
            };

    public final BroadcastReceiver mUsbDeviceReceiver =
            new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {

                        deviceFound = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                        /**Toast.makeText(Color.this,
                         "ACTION_USB_DEVICE_ATTACHED: \n" +
                         deviceFound.toString(),
                         Toast.LENGTH_LONG).show();
                         textStatus.setText("ACTION_USB_DEVICE_ATTACHED: \n" +
                         deviceFound.toString());**/

                        //connectUsb();

                    }else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {

                        UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                        /** Toast.makeText(Color.this,
                         "ACTION_USB_DEVICE_DETACHED: \n" +
                         device.toString(),
                         Toast.LENGTH_LONG).show();
                         textStatus.setText("ACTION_USB_DEVICE_DETACHED: \n" +
                         device.toString());**/

                        if(device!=null){
                            if(device == deviceFound){
                                //releaseUsb();
                            }
                        }

                    }
                }

            };

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
        } catch (SecurityException e)
        {
            e.printStackTrace();
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

