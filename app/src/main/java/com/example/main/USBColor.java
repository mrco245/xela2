package com.example.main;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.*;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import static com.example.main.WebAppInterface.data_in;
import static java.lang.Thread.sleep;

public class USBColor extends MainActivity{
    private static final int targetVendorID= 26214;
    private static final int targetProductID = 26215;
    static UsbDevice deviceFound = null;
    static UsbInterface usbInterfaceFound = null;
    static UsbEndpoint endpointIn = null;
    static UsbEndpoint endpointOut = null;
    public static boolean STOP = false;
    public static byte r, g, b;
    public byte[] message;
    public byte[] message2;

    public int usbResult;

    private static final String ACTION_USB_PERMISSION =
            "com.android.example.USB_PERMISSION";
    PendingIntent mPermissionIntent;

    UsbInterface usbInterface;
    UsbDeviceConnection usbDeviceConnection;

    /**
     @Override
     protected void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     setContentView(R.layout.activity_main);

     //textStatus = (TextView)findViewById(R.id.textstatus);
     //textDeviceName = (TextView)findViewById(R.id.textdevicename);
     //textInfo = (TextView) findViewById(R.id.info);
     // textSearchedEndpoint = (TextView)findViewById(R.id.searchedendpoint);

     //String message = "\0x16\0x05\0x00\0x00\0x01\0x00\0x00\0x00\0xFF";
     //System.out.println(message.getBytes());

     //register the broadcast receiver
     mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
     IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
     registerReceiver(mUsbReceiver, filter);

     registerReceiver(mUsbDeviceReceiver, new IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED));
     registerReceiver(mUsbDeviceReceiver, new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED));

     connectUsb();
     }

     **/


    public void connectUsb(){

        searchEndPoint();

        if(usbInterfaceFound != null){
            setupUsbComm(null);
        }

    }

    public void releaseUsb(){

        if(usbDeviceConnection != null){
            if(usbInterface != null){
                usbDeviceConnection.releaseInterface(usbInterface);
                usbInterface = null;
            }
            usbDeviceConnection.close();
            usbDeviceConnection = null;
        }

        deviceFound = null;
        usbInterfaceFound = null;
        endpointIn = null;
        endpointOut = null;
    }

    public boolean searchEndPoint(){

        boolean isConnected =  false;


        usbInterfaceFound = null;


        //Search device for targetVendorID and targetProductID
        if(deviceFound == null){
            // UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
            HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
            Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();

            while (deviceIterator.hasNext()) {
                UsbDevice device = deviceIterator.next();

                if(device.getVendorId()==targetVendorID){
                    if(device.getProductId()==targetProductID){
                        deviceFound = device;
                    }
                }
            }
        }

        if(deviceFound==null)
        {
            isConnected= false;

        }
        else{
            isConnected = true;
            usbInterfaceFound = deviceFound.getInterface(0);
            endpointOut = usbInterfaceFound.getEndpoint(1);
            endpointIn = usbInterfaceFound.getEndpoint(0);

        }
        return isConnected;
    }


    public static int[] ihex2rgb(String colorStr)
    {

        int r = Integer.parseInt( colorStr.substring(1,3));
        int g = Integer.parseInt(colorStr.substring(3,5));
        int b = Integer.parseInt(colorStr.substring(5,7));

        int[] data = {b,g,r};
        return data;
    }

    //UNFINISHED********************************************************
    public static byte rgb2hex(int rgb)
    {
        String hex = String.format("#%02x%02x%02x", r, g, b);
        byte bHex = (byte)Integer.parseInt(hex, 16);

        return bHex;
    }

    public static byte[] hex2rgb(String colorStr)
    {

        byte r = (byte)Integer.parseInt( colorStr.substring(1,3),16);
        byte g = (byte)Integer.parseInt(colorStr.substring(3,5),16);
        byte b = (byte)Integer.parseInt(colorStr.substring(5,7),16);

        byte[] data = {b,g,r};
        return data;
    }

    public boolean setupUsbComm(byte[] msg){

        //for more info, search SET_LINE_CODING and
        //SET_CONTROL_LINE_STATE in the document:
        //"Universal Serial Bus Class Definitions for Communication Devices"
        //at http://adf.ly/dppFt

        boolean success = false;

        // UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        Boolean permitToRead = manager.hasPermission(deviceFound);

        if(permitToRead){
            usbDeviceConnection = manager.openDevice(deviceFound);
            if(usbDeviceConnection != null){
                usbDeviceConnection.claimInterface(usbInterfaceFound, true);

                //byte[] message = {0x16, 0x05, 0x00, 0x00, 0x01, 0x00, colors[0], colors[1], colors[2]};
                //System.out.println(message);
                //String messageStr = "\0x15\0x04\0x00\0x00\0x00\0x00\0x00\0xFF";

                usbResult = usbDeviceConnection.bulkTransfer(
                        endpointOut,
                        msg,
                        msg.length,
                        0);
                success = true;
                System.out.println("Sent " + message);
            }

        }else{
            manager.requestPermission(deviceFound, mPermissionIntent);

        }

        return success;
    }

    public void specialMode()
    {
        String mode = "";
        try {
            mode = data_in.get("mode").toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        switch (mode)
        {
            case("wave"):
                waveMode();
                break;

            case("up-down"):
                upDown();
                break;
        }

    }

    public void waveMode()
    {
        searchEndPoint();
        int lightIndex = 0;
        int colorIndex = 0;
        int run = 0;
        int duration = 0;
        try {
            duration = Integer.parseInt(data_in.get("duration").toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        byte[] colors ={0x00, 0x00, (byte)0xFF, 0x00, 0x7F, (byte)0xFF, 0x00, (byte)0xFF, (byte)0xFF, 0x00, (byte)0xFF, 0x00, (byte)0xFF, 0x00, 0x00, (byte)0x82, 0x00, 0x4B, (byte)0xFF, 0x00, (byte)0x8B, 0x00, 0x00, (byte)0xFF, 0x00, 0x7F, (byte)0xFF, 0x00, (byte)0xFF, (byte)0xFF, 0x00, (byte)0xFF, 0x00, (byte)0xFF, 0x00, 0x00, (byte)0x82, 0x00, 0x4B, (byte)0xFF, 0x00, (byte)0x8B, 0x00, 0x00, (byte)0xFF, 0x00, 0x7F, (byte)0xFF, 0x00, (byte)0xFF, (byte)0xFF, 0x00, (byte)0xFF, 0x00, (byte)0xFF, 0x00, 0x00, (byte)0x82, 0x00, 0x4B, (byte)0xFF, 0x00, (byte)0x8B, 0x00, 0x00, (byte)0xFF, 0x00, 0x7F, (byte)0xFF, 0x00, (byte)0xFF, (byte)0xFF, 0x00, (byte)0xFF, 0x00, (byte)0xFF, 0x00, 0x00, (byte)0x82, 0x00, 0x4B, (byte)0xFF, 0x00, (byte)0x8B, 0x00, 0x00, (byte)0xFF, 0x00, 0x7F, (byte)0xFF, 0x00, (byte)0xFF, (byte)0xFF, 0x00, (byte)0xFF, 0x00, (byte)0xFF, 0x00, 0x00, (byte)0x82, 0x00, 0x4B, (byte)0xFF, 0x00, (byte)0x8B, 0x00, 0x00, (byte)0xFF, 0x00, 0x7F, (byte)0xFF, 0x00, (byte)0xFF, (byte)0xFF, 0x00, (byte)0xFF, 0x00, (byte)0xFF, 0x00, 0x00, (byte)0x82, 0x00, 0x4B, (byte)0xFF, 0x00, (byte)0x8B};

        try{
            while(run < duration) {
                while (lightIndex < 16) {
                    colors =new byte[]{0x00, 0x00, (byte)0xFF, 0x00, 0x7F, (byte)0xFF, 0x00, (byte)0xFF, (byte)0xFF, 0x00, (byte)0xFF, 0x00, (byte)0xFF, 0x00, 0x00, (byte)0x82, 0x00, 0x4B, (byte)0xFF, 0x00, (byte)0x8B, 0x00, 0x00, (byte)0xFF, 0x00, 0x7F, (byte)0xFF, 0x00, (byte)0xFF, (byte)0xFF, 0x00, (byte)0xFF, 0x00, (byte)0xFF, 0x00, 0x00, (byte)0x82, 0x00, 0x4B, (byte)0xFF, 0x00, (byte)0x8B, 0x00, 0x00, (byte)0xFF, 0x00, 0x7F, (byte)0xFF, 0x00, (byte)0xFF, (byte)0xFF, 0x00, (byte)0xFF, 0x00, (byte)0xFF, 0x00, 0x00, (byte)0x82, 0x00, 0x4B, (byte)0xFF, 0x00, (byte)0x8B, 0x00, 0x00, (byte)0xFF, 0x00, 0x7F, (byte)0xFF, 0x00, (byte)0xFF, (byte)0xFF, 0x00, (byte)0xFF, 0x00, (byte)0xFF, 0x00, 0x00, (byte)0x82, 0x00, 0x4B, (byte)0xFF, 0x00, (byte)0x8B, 0x00, 0x00, (byte)0xFF, 0x00, 0x7F, (byte)0xFF, 0x00, (byte)0xFF, (byte)0xFF, 0x00, (byte)0xFF, 0x00, (byte)0xFF, 0x00, 0x00, (byte)0x82, 0x00, 0x4B, (byte)0xFF, 0x00, (byte)0x8B, 0x00, 0x00, (byte)0xFF, 0x00, 0x7F, (byte)0xFF, 0x00, (byte)0xFF, (byte)0xFF, 0x00, (byte)0xFF, 0x00, (byte)0xFF, 0x00, 0x00, (byte)0x82, 0x00, 0x4B, (byte)0xFF, 0x00, (byte)0x8B};

                    String ind = Integer.toHexString(lightIndex);
                    byte bInd = Byte.parseByte(ind, 16);
                    message = new byte[]{0x11, 0x06, 0x00, 0x00, bInd, 0x01, 0x00, colors[colorIndex], colors[colorIndex + 1], colors[colorIndex + 2]};
                    message2 = new byte[]{0x11, 0x06, 0x00, 0x00, bInd, 0x04, 0x00, colors[colorIndex], colors[colorIndex + 1], colors[colorIndex + 2]};
                    setupUsbComm(message);

                    lightIndex++;
                    colorIndex = colorIndex + 3;
                    sleep(100);
                }
                lightIndex = 0;
                colorIndex = 0;
                while (lightIndex < 16) {
                    //alternate order shifted up 1
                    colors = new byte[]{0x00, 0x7F, (byte)0xFF, 0x00, (byte)0xFF, (byte)0xFF, 0x00, (byte)0xFF, 0x00, (byte)0xFF, 0x00, 0x00, (byte)0x82, 0x00, 0x4B, (byte)0xFF, 0x00, (byte)0x8B, 0x00, 0x00, (byte)0xFF, 0x00, 0x7F, (byte)0xFF, 0x00, (byte)0xFF, (byte)0xFF, 0x00, (byte)0xFF, 0x00, (byte)0xFF, 0x00, 0x00, (byte)0x82, 0x00, 0x4B, (byte)0xFF, 0x00, (byte)0x8B, 0x00, 0x00, (byte)0xFF, 0x00, 0x7F, (byte)0xFF, 0x00, (byte)0xFF, (byte)0xFF, 0x00, (byte)0xFF, 0x00, (byte)0xFF, 0x00, 0x00, (byte)0x82, 0x00, 0x4B, (byte)0xFF, 0x00, (byte)0x8B, 0x00, 0x00, (byte)0xFF, 0x00, 0x7F, (byte)0xFF, 0x00, (byte)0xFF, (byte)0xFF, 0x00, (byte)0xFF, 0x00, (byte)0xFF, 0x00, 0x00, (byte)0x82, 0x00, 0x4B, (byte)0xFF, 0x00, (byte)0x8B, 0x00, 0x00, (byte)0xFF};
                    String ind = Integer.toHexString(lightIndex);
                    byte bInd = Byte.parseByte(ind, 16);
                    message = new byte[]{0x11, 0x06, 0x00, 0x00, bInd, 0x01, 0x00, colors[colorIndex], colors[colorIndex + 1], colors[colorIndex + 2]};
                    message2 = new byte[]{0x11, 0x06, 0x00, 0x00, bInd, 0x04, 0x00, colors[colorIndex], colors[colorIndex + 1], colors[colorIndex + 2]};
                    setupUsbComm(message);

                    lightIndex++;
                    colorIndex = colorIndex + 3;
                    sleep(100);
                }
                lightIndex = 0;
                colorIndex = 0;
                run++;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


        if(usbInterfaceFound != null){
            setupUsbComm(message);
        }
    }

    public void upDown()
    {
        int run = 0;
        int duration = 0;
        try {
            duration = Integer.parseInt(data_in.get("duration").toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        searchEndPoint();
        int lightIndex = 0;
        int colorIndex = 0;
        byte[] colors ={0x00, 0x00, (byte)0xFF, 0x00, 0x7F, (byte)0xFF, 0x00, (byte)0xFF, (byte)0xFF, 0x00, (byte)0xFF, 0x00, (byte)0xFF, 0x00, 0x00, (byte)0x82, 0x00, 0x4B, (byte)0xFF, 0x00, (byte)0x8B, 0x00, 0x00, (byte)0xFF, 0x00, 0x7F, (byte)0xFF, 0x00, (byte)0xFF, (byte)0xFF, 0x00, (byte)0xFF, 0x00, (byte)0xFF, 0x00, 0x00, (byte)0x82, 0x00, 0x4B, (byte)0xFF, 0x00, (byte)0x8B, 0x00, 0x00, (byte)0xFF, 0x00, 0x7F, (byte)0xFF, 0x00, (byte)0xFF, (byte)0xFF, 0x00, (byte)0xFF, 0x00, (byte)0xFF, 0x00, 0x00, (byte)0x82, 0x00, 0x4B, (byte)0xFF, 0x00, (byte)0x8B, 0x00, 0x00, (byte)0xFF, 0x00, 0x7F, (byte)0xFF, 0x00, (byte)0xFF, (byte)0xFF, 0x00, (byte)0xFF, 0x00, (byte)0xFF, 0x00, 0x00, (byte)0x82, 0x00, 0x4B, (byte)0xFF, 0x00, (byte)0x8B, 0x00, 0x00, (byte)0xFF, 0x00, 0x7F, (byte)0xFF, 0x00, (byte)0xFF, (byte)0xFF, 0x00, (byte)0xFF, 0x00, (byte)0xFF, 0x00, 0x00, (byte)0x82, 0x00, 0x4B, (byte)0xFF, 0x00, (byte)0x8B, 0x00, 0x00, (byte)0xFF, 0x00, 0x7F, (byte)0xFF, 0x00, (byte)0xFF, (byte)0xFF, 0x00, (byte)0xFF, 0x00, (byte)0xFF, 0x00, 0x00, (byte)0x82, 0x00, 0x4B, (byte)0xFF, 0x00, (byte)0x8B};

        try{
            while(run < duration) {
                while (lightIndex < 16) {
                    //colors =new byte[]{0x00, 0x00, (byte)0xFF, 0x00, 0x7F, (byte)0xFF, 0x00, (byte)0xFF, (byte)0xFF, 0x00, (byte)0xFF, 0x00, (byte)0xFF, 0x00, 0x00, (byte)0x82, 0x00, 0x4B, (byte)0xFF, 0x00, (byte)0x8B, 0x00, 0x00, (byte)0xFF, 0x00, 0x7F, (byte)0xFF, 0x00, (byte)0xFF, (byte)0xFF, 0x00, (byte)0xFF, 0x00, (byte)0xFF, 0x00, 0x00, (byte)0x82, 0x00, 0x4B, (byte)0xFF, 0x00, (byte)0x8B, 0x00, 0x00, (byte)0xFF, 0x00, 0x7F, (byte)0xFF, 0x00, (byte)0xFF, (byte)0xFF, 0x00, (byte)0xFF, 0x00, (byte)0xFF, 0x00, 0x00, (byte)0x82, 0x00, 0x4B, (byte)0xFF, 0x00, (byte)0x8B, 0x00, 0x00, (byte)0xFF, 0x00, 0x7F, (byte)0xFF, 0x00, (byte)0xFF, (byte)0xFF, 0x00, (byte)0xFF, 0x00, (byte)0xFF, 0x00, 0x00, (byte)0x82, 0x00, 0x4B, (byte)0xFF, 0x00, (byte)0x8B, 0x00, 0x00, (byte)0xFF, 0x00, 0x7F, (byte)0xFF, 0x00, (byte)0xFF, (byte)0xFF, 0x00, (byte)0xFF, 0x00, (byte)0xFF, 0x00, 0x00, (byte)0x82, 0x00, 0x4B, (byte)0xFF, 0x00, (byte)0x8B, 0x00, 0x00, (byte)0xFF, 0x00, 0x7F, (byte)0xFF, 0x00, (byte)0xFF, (byte)0xFF, 0x00, (byte)0xFF, 0x00, (byte)0xFF, 0x00, 0x00, (byte)0x82, 0x00, 0x4B, (byte)0xFF, 0x00, (byte)0x8B};

                    colors =new byte[]{(byte)0xFF, 0x00, 0x00, (byte)0xFF, 0x00, 0x00, (byte)0xFF, 0x00, 0x00, (byte)0xFF, 0x00, 0x00, 0x00, (byte)0xFF, 0x00, 0x00, (byte)0xFF, 0x00, 0x00, (byte)0xFF, 0x00, 0x00, (byte)0xFF, 0x00, 0x00, (byte)0xFF, (byte)0xFF, 0x00, (byte)0xFF, (byte)0xFF, 0x00, (byte)0xFF, (byte)0xFF, 0x00, (byte)0xFF, (byte)0xFF, 0x00, 0x00, (byte)0xFF, 0x00, 0x00, (byte)0xFF, 0x00, 0x00, (byte)0xFF, 0x00, 0x00, (byte)0xFF};

                    String ind = Integer.toHexString(lightIndex);
                    byte bInd = Byte.parseByte(ind, 16);
                    message = new byte[]{0x11, 0x06, 0x00, 0x00, bInd, 0x01, 0x00, colors[colorIndex], colors[colorIndex + 1], colors[colorIndex + 2]};
                    message2 = new byte[]{0x11, 0x06, 0x00, 0x00, bInd, 0x04, 0x00, colors[colorIndex], colors[colorIndex + 1], colors[colorIndex + 2]};
                    setupUsbComm(message);

                    lightIndex++;
                    colorIndex = colorIndex + 3;
                    sleep(100);
                }
                sleep(500);
                colorIndex = 0;
                lightIndex = 16;
                while (lightIndex >= 0) {
                    //alternate order shifted up 1
                    //colors = new byte[]{0x00, 0x7F, (byte)0xFF, 0x00, (byte)0xFF, (byte)0xFF, 0x00, (byte)0xFF, 0x00, (byte)0xFF, 0x00, 0x00, (byte)0x82, 0x00, 0x4B, (byte)0xFF, 0x00, (byte)0x8B, 0x00, 0x00, (byte)0xFF, 0x00, 0x7F, (byte)0xFF, 0x00, (byte)0xFF, (byte)0xFF, 0x00, (byte)0xFF, 0x00, (byte)0xFF, 0x00, 0x00, (byte)0x82, 0x00, 0x4B, (byte)0xFF, 0x00, (byte)0x8B, 0x00, 0x00, (byte)0xFF, 0x00, 0x7F, (byte)0xFF, 0x00, (byte)0xFF, (byte)0xFF, 0x00, (byte)0xFF, 0x00, (byte)0xFF, 0x00, 0x00, (byte)0x82, 0x00, 0x4B, (byte)0xFF, 0x00, (byte)0x8B, 0x00, 0x00, (byte)0xFF, 0x00, 0x7F, (byte)0xFF, 0x00, (byte)0xFF, (byte)0xFF, 0x00, (byte)0xFF, 0x00, (byte)0xFF, 0x00, 0x00, (byte)0x82, 0x00, 0x4B, (byte)0xFF, 0x00, (byte)0x8B, 0x00, 0x00, (byte)0xFF};
                    colors = new byte[]{0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00};
                    String ind = Integer.toHexString(lightIndex);
                    byte bInd = Byte.parseByte(ind, 16);
                    message = new byte[]{0x11, 0x06, 0x00, 0x00, bInd, 0x01, 0x00, colors[colorIndex], colors[colorIndex + 1], colors[colorIndex + 2]};
                    message2 = new byte[]{0x11, 0x06, 0x00, 0x00, bInd, 0x04, 0x00, colors[colorIndex], colors[colorIndex + 1], colors[colorIndex + 2]};
                    setupUsbComm(message);

                    lightIndex--;
                    sleep(100);
                }
                sleep(500);
                lightIndex = 0;
                colorIndex = 0;
                run++;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


        if(usbInterfaceFound != null){
            setupUsbComm(message);
        }
    }

    /*
    //fade from selected color to black(off)
    public void breathe()
    {
        searchEndPoint();
        int[] colors = {};
        int newVal = 0;

        try{
            colors = ihex2rgb((data_in.get("color").toString()));
            //RED
            int oldVal = colors[2];
            int steps = 10;

            int currentVal = oldVal;
            for (int i = 0; i < steps; i++) {
                currentVal = oldVal + ((i * (newVal - oldVal)) / (steps - 1));
                message = new byte[] {0x15, 0x04, 0x00, 0x00, 0x00, colors[0], colors[1], colors[2]};
                message2 = new byte[] {0x15, 0x01, 0x00, 0x00, 0x00, colors[0], colors[1], colors[2]};
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    */
/*
    public int fade(int oldVal, int newVal)
    {
        int steps = 10;
        int stepAmount = (newVal - oldVal) / steps;

        int currentVal = oldVal;
        for (int i = 0; i < steps; i++) {
            currentVal = oldVal + ((i * (newVal - oldVal)) / (steps - 1));
        }
    }
    */
    public void allStripAllColor()
    {
        searchEndPoint();
        byte[] colors ={};
        try{
            colors = hex2rgb(data_in.get("color").toString());
            String index = data_in.get("index").toString();
            if(index.equals("999"))
            {
                message = new byte[] {0x15, 0x04, 0x00, 0x00, 0x00, colors[0], colors[1], colors[2]};
            }
            else {
                byte ind = (byte)Integer.parseInt(index, 16);
                message = new byte[]{0x11, 0x06, 0x00, 0x00, ind, 0x01, 0x00, colors[0], colors[1], colors[2]};
                message2 = new byte[]{0x11, 0x06, 0x00, 0x00, ind, 0x04, 0x00, colors[0], colors[1], colors[2]};
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


        if(usbInterfaceFound != null){
            setupUsbComm(message);
        }
    }


    public void setLed(String index)
    {
        searchEndPoint();
        byte[] colors ={};
        try{
            colors = hex2rgb(data_in.get("color").toString());
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        byte ind = (byte)Integer.parseInt(index, 16);
        byte[] message = {0x11, 0x06, 0x00, 0x00, 0x01, ind, 0x00, colors[0], colors[1], colors[2]};

        if(usbInterfaceFound != null){
            setupUsbComm(message);
        }
    }



    public final BroadcastReceiver mUsbReceiver =
            new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (ACTION_USB_PERMISSION.equals(action)) {


                        synchronized (this) {
                            UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                            if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                                if(device != null){
                                    connectUsb();
                                }
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
                        connectUsb();

                    }else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {

                        UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                        if(device!=null){
                            if(device == deviceFound){
                                releaseUsb();
                            }
                        }

                    }
                }

            };
}
