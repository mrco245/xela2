package com.example.main;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.*;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Iterator;

import static com.example.main.MainActivity.manager;
import static com.example.main.WebAppInterface.data_in;

public class USBColor {
    private static final int targetVendorID= 26214;
    private static final int targetProductID = 26215;
    static UsbDevice deviceFound = null;
    static UsbInterface usbInterfaceFound = null;
    static UsbEndpoint endpointIn = null;
    static UsbEndpoint endpointOut = null;

    public static byte r, g, b;

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
     @Override
     protected void onDestroy() {
     releaseUsb();
     unregisterReceiver(mUsbReceiver);
     unregisterReceiver(mUsbDeviceReceiver);
     super.onDestroy();
     }

     **/

    public void connectUsb(){
/**
 Toast.makeText(Color.this,
 "connectUsb()",
 Toast.LENGTH_LONG).show();
 textStatus.setText("connectUsb()");**/

        searchEndPoint();

        if(usbInterfaceFound != null){
            setupUsbComm();
        }

    }

    public void releaseUsb(){
/**
 Toast.makeText(Color.this,
 "releaseUsb()",
 Toast.LENGTH_LONG).show();
 textStatus.setText("releaseUsb()");**/

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

    public void searchEndPoint(){

        //textInfo.setText("");
        //textSearchedEndpoint.setText("");

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

        if(deviceFound==null){

            // Toast.makeText(USBColor.this,
             //"device not found",
            // Toast.LENGTH_LONG).show();
             /**
             textStatus.setText("device not found");**/
             }
              else{

            /** String s = deviceFound.toString() + "\n" +
             "DeviceID: " + deviceFound.getDeviceId() + "\n" +
             "DeviceName: " + deviceFound.getDeviceName() + "\n" +
             "DeviceClass: " + deviceFound.getDeviceClass() + "\n" +
             "DeviceSubClass: " + deviceFound.getDeviceSubclass() + "\n" +
             "VendorID: " + deviceFound.getVendorId() + "\n" +
             "ProductID: " + deviceFound.getProductId() + "\n" +
             "InterfaceCount: " + deviceFound.getInterfaceCount();
             textInfo.setText(s);**/

            usbInterfaceFound = deviceFound.getInterface(0);
            endpointOut = usbInterfaceFound.getEndpoint(1);
            endpointIn = usbInterfaceFound.getEndpoint(0);

        }
    }


    public static byte[] hex2rgb(String colorStr)
    {

        byte r = (byte)Integer.parseInt( colorStr.substring(1,3),16);
        byte g = (byte)Integer.parseInt(colorStr.substring(3,5),16);
        byte b = (byte)Integer.parseInt(colorStr.substring(5,7),16);

       byte[] data = {b,g,r};
        return data;
    }

    public boolean setupUsbComm(){

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

                int usbResult;
                byte[] test ={};
                try{
                    test = hex2rgb(data_in.get("color").toString());
                    System.out.println(test);
                    System.out.println(test[0]);
                    System.out.println(test[1]);
                    System.out.println(test[2]);
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
                //hex2rgb(data_in.get("color"));

                byte[] message = {0x16, 0x05, 0x00, 0x00, 0x01, 0x00, test[0], test[1], test[2]};
                System.out.println(message);
                //String messageStr = "\0x15\0x04\0x00\0x00\0x00\0x00\0x00\0xFF";

                //byte message[] = new byte[1000];
                //message = toBinary(messageStr);
                //message = messageStr.getBytes();
                // textSearchedEndpoint.append("\n\n" + message);
                usbResult = usbDeviceConnection.bulkTransfer(
                        endpointOut,
                        message,
                        message.length,
                        0);

                 //Toast.makeText(USBColor.this,
                 //"bulkTransfer: " + usbResult,
                 //Toast.LENGTH_LONG).show();
                 //textSearchedEndpoint.append("\n\nTransferred " + usbResult + " bytes successfully");

                success = true;
            }

        }else{
            manager.requestPermission(deviceFound, mPermissionIntent);
            /**Toast.makeText(Color.this,
             "Permission: " + permitToRead,
             Toast.LENGTH_LONG).show();
             textStatus.setText("Permission: " + permitToRead);**/
        }

        return success;
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
                                    connectUsb();
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

                        connectUsb();

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
                                releaseUsb();
                            }
                        }

                    }
                }

            };

    byte[] toBinary(String message)
    {
        byte[] bytes = message.getBytes();
        StringBuilder binary = new StringBuilder();
        for (byte b : bytes)
        {
            int val = b;
            for (int i = 0; i < 8; i++)
            {
                binary.append((val & 128) == 0 ? 0 : 1);
                val <<= 1;
            }
            binary.append(' ');
        }
        String bins = binary.toString();
        return bins.getBytes();
    }
}
