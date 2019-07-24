package com.example.main;

import android.content.Context;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

//import android.support.annotation.CheckResult;

public class WifiConnector {

    private static final String TAG = WifiConnector.class.getSimpleName();
    public static final String NO_WIFI = quoted("<unknown ssid>");

    private Context mContext;
    private boolean mIsWifiBroadcastRegistered;
    private IntentFilter mWifiIntentFilter;
    private WifiManager mWifiManager;
    private WiFiConnectorListener mWifiConnectorListener;

    public WifiConnector(Context context, WiFiConnectorListener wifiConnectorListener) {
        mContext = context;
        mWifiIntentFilter = new IntentFilter();
        mWifiIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mWifiIntentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        mWifiIntentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        mWifiIntentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mWifiManager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        mWifiConnectorListener = wifiConnectorListener;
    }

    public String checkWifi()
    {
        String ssid = "";
        ssid = getActiveConnection().getSSID();

        if(ssid != null)
        {
            System.out.println("Wifi is connected. SSID is " + ssid);
        }
        else
        {
            System.out.println("Wifi is disconnected");
        }
        return ssid;
    }

    public NetworkInfo.DetailedState connectToWiFi(int securityType, String ssid, String key) {
        Log.d(TAG, "connectToWiFi() called with: securityType = [" + securityType + "], ssid = [" + ssid + "], key = [" + key + "]");
        /* Check if already connected to that wifi */
        String currentSsid = getActiveConnection().getSSID();
        Log.d(TAG, "Current Ssid " + currentSsid);
        NetworkInfo.DetailedState currentState = WifiInfo.getDetailedStateOf(getActiveConnection().getSupplicantState()); //todo check this
        if (currentState == NetworkInfo.DetailedState.CONNECTED && currentSsid.equals(quoted(ssid))) {
            Log.d(TAG, "Already connected");
            mWifiConnectorListener.onWiFiStateUpdate(getActiveConnection(), NetworkInfo.DetailedState.CONNECTED);
            return NetworkInfo.DetailedState.CONNECTED;
        }

        int highestPriorityNumber = 0;
        WifiConfiguration selectedConfig = null;
        /* Check if not connected but has connected to that wifi in the past */
        for (WifiConfiguration config : mWifiManager.getConfiguredNetworks()) {
            if (config.priority > highestPriorityNumber) highestPriorityNumber = config.priority;
            if (config.SSID.equals(quoted(ssid)) && config.allowedKeyManagement.get(securityType)) {
                Log.d(TAG, "Saved preshared key is " + config.preSharedKey);
                if (securityType == WifiConfiguration.KeyMgmt.WPA_PSK
                        && config.preSharedKey != null && config.preSharedKey.equals(key))
                    selectedConfig = config;
                else if (securityType == WifiConfiguration.KeyMgmt.NONE)
                    selectedConfig = config;
            }
        }

        if (selectedConfig != null) {
            selectedConfig.priority = highestPriorityNumber + 1;
            mWifiManager.updateNetwork(selectedConfig);
            // mWifiManager.disconnect(); /* disconnect from whichever wifi you're connected to */
            mWifiManager.enableNetwork(selectedConfig.networkId, true);
            mWifiManager.reconnect();
            Log.d(TAG, "Connection exists in past, enabling and connecting priority = " + highestPriorityNumber);
            return NetworkInfo.DetailedState.CONNECTING;
        }

        /* Make new connection */
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = quoted(ssid);
        config.priority = highestPriorityNumber + 1;
        config.status = WifiConfiguration.Status.ENABLED;
        if (securityType == WifiConfiguration.KeyMgmt.WPA_PSK) {
            config.preSharedKey = quoted(key);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        } else {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        }
        Log.d(TAG, "Attempting new wifi connection, setting priority number to, connecting " + config.priority);

        int netId = mWifiManager.addNetwork(config);
        // mWifiManager.disconnect(); /* disconnect from whichever wifi you're connected to */
        mWifiManager.enableNetwork(netId, true);
        mWifiManager.reconnect(); // todo?
        return NetworkInfo.DetailedState.CONNECTING;
    }

   // @CheckResult
  //  public boolean startScan() {
   //     return mWifiManager.startScan();
   // }

    public WifiInfo getActiveConnection() {
        WifiInfo currentInfo = mWifiManager.getConnectionInfo();
        return currentInfo;
    }


    public void onStateUpdate(NetworkInfo.DetailedState detailedState) {
        mWifiConnectorListener.onWiFiStateUpdate(getActiveConnection(), detailedState);
    }







    public static String quoted(String s) {
        return "\"" + s + "\"";
    }

    public interface WiFiConnectorListener {
        void onWiFiStateUpdate(WifiInfo wifiInfo, NetworkInfo.DetailedState detailedState);
    }
}
