package com.example.main;

import android.annotation.SuppressLint;

import android.net.ConnectivityManager;
import android.os.Build;
import org.json.JSONObject;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class PhoneInfo extends MainActivity {

     boolean ismobile;


    public JSONObject displayPhoneInfo()
    {
        JSONObject phoneinfo = new JSONObject();
        try {
            phoneinfo.put("ssid", con.checkWifi());
            phoneinfo.put("wifi", isInternetAvailable());
            phoneinfo.put("device-info", deviceInfo());
            phoneinfo.put("sim", getSimSerial());
        }catch(Exception e)
        {
            e.printStackTrace();
        }

        return phoneinfo;
    }


    //checks for usable internet connection
    public boolean isInternetAvailable() {
        InetAddress inetAddress = null;
        try {
            Future<InetAddress> future = Executors.newSingleThreadExecutor().submit(new Callable<InetAddress>() {
                @Override
                public InetAddress call() {
                    try {
                        return InetAddress.getByName("google.com");
                    } catch (UnknownHostException e) {
                        return null;
                    }
                }
            });
            inetAddress = future.get(10000, TimeUnit.MILLISECONDS);
            future.cancel(true);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        return inetAddress != null && !inetAddress.equals("");
    }


    @SuppressLint("MissingPermission")
    public String getDeviceID() {
        // final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
        String devID = tm1.getDeviceId();
        if (devID.length() == 14) {
            System.out.println("Device is CDMA. MEID is " + devID);
        } else if (devID.length() == 15) {
            System.out.println("Device is GSM. IMEI is " + devID);
        } else
            System.out.println("Device ID not available");

        return devID;
    }

    @SuppressLint("MissingPermission")
    public String getSimSerial() {
        //final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
        return "" + tm1.getSimSerialNumber();
    }


    @SuppressLint("MissingPermission")
    public String deviceInfo() {
        final String tmDevice, tmSerial, androidId;

        //UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
        //String deviceId = deviceUuid.toString();

        String s = "Debug-infos:";
        s += "\n OS Version: " + System.getProperty("os.version") + "(" + android.os.Build.VERSION.INCREMENTAL + ")";
        s += "\n OS API Level: " + android.os.Build.VERSION.SDK_INT;
        s += "\n Device: " + android.os.Build.DEVICE;
        s += "\n Model (and Product): " + android.os.Build.MODEL + " (" + android.os.Build.PRODUCT + ")";
        System.out.println(s);

        return s;
    }
}
