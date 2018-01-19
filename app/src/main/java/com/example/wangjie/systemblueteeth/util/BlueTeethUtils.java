package com.example.wangjie.systemblueteeth.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Created by wangjie on 2017/6/13.
 */

public class BlueTeethUtils {
    private static BluetoothAdapter mBtAdapter = null;
    private static BluetoothSocket mBtSocket   = null;
    private static OutputStream outStream = null;
    private static InputStream inStream  = null;
    private static boolean mBtFlag = true;

    private boolean threadFlag = false;


    private static final UUID HC_UUID   = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    private static String TAG = "BLUTHEE";

    private Set<BluetoothDevice> mBtDevices;

    private ListView listView;

    private List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

    private String connectName = "";

    /**
     * Called by onStartCommand, initialize and start runtime thread
     */
    public Set<BluetoothDevice>  myStartService() {
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if ( mBtAdapter == null ) {
            showToast("Bluetooth unused.");
            mBtFlag  = false;
        }
        if ( !mBtAdapter.isEnabled() ) {
            mBtFlag  = false;
            myStopService();
            showToast("Open bluetoooth then restart program!!");
        }

        mBtDevices = mBtAdapter.getBondedDevices();
        return mBtDevices;
    }

    private void myStopService() {
        mBtAdapter.cancelDiscovery();
    }

    /**
     * Thread runtime
     */
    public static class Connect extends Thread {
        private BluetoothDevice mBtDevice;
        private Context context;

        public Connect( BluetoothDevice mBtDevice, Context context) {
            this.mBtDevice = mBtDevice;
            this.context = context;
        }

        @Override
        public void run() {
            try {
                mBtSocket = mBtDevice.createRfcommSocketToServiceRecord(HC_UUID);
            } catch (IOException e) {
                e.printStackTrace();
                mBtFlag = false;
            }

            mBtAdapter.cancelDiscovery();

            try {
                Method m = mBtDevice.getClass().getMethod(
                        "createRfcommSocket", new Class[] { int.class });
                mBtSocket = (BluetoothSocket) m.invoke(mBtDevice, 1);//这里端口为1
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
    /* Setup connection */
            try {

                mBtSocket.connect();
                Toast.makeText(context, "Connect bluetooth success",Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Connect " + mBtDevice.getAddress() + " Success!");
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    Toast.makeText(context, "Connect error, close",Toast.LENGTH_SHORT).show();
                    mBtSocket.close();
                    mBtFlag = false;
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

            if ( mBtFlag ) {
                try {
                    inStream  = mBtSocket.getInputStream();
                    outStream = mBtSocket.getOutputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public int readSerial() {
        int ret = 0;
        byte[] rsp = null;

        if ( !mBtFlag ) {
            return -1;
        }
        try {
            rsp = new byte[inStream.available()];
            ret = inStream.read(rsp);
            showToast(new String(rsp));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ret;
    }

    public void writeSerial(String value) {
        String ha = value;
        try {
            outStream.write("123".getBytes());
            Log.d(TAG,outStream != null?"1":"2");
            outStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    public void connectTo(BluetoothDevice mBtDevice,Context context){
//        try {
//            mBtSocket = mBtDevice.createRfcommSocketToServiceRecord(HC_UUID);
//        } catch (IOException e) {
//            e.printStackTrace();
//            mBtFlag = false;
//            showToast("Create bluetooth socket error");
//        }
//
//        mBtAdapter.cancelDiscovery();
//
//        BluetoothSocket temp = null;
//        try {
//            Method m = mBtDevice.getClass().getMethod(
//                    "createRfcommSocket", new Class[] { int.class });
//
//            Method listenMethod = mBtDevice.getClass().getMethod("listenUsingRfcommOn", new Class[]{int.class});
//            BluetoothServerSocket returnValue = (BluetoothServerSocket) listenMethod.invoke(mBtAdapter, new Object[]{ 29});
//
//            mBtSocket = (BluetoothSocket) m.invoke(mBtDevice, 1);//这里端口为1
//        } catch (SecurityException e) {
//            e.printStackTrace();
//        } catch (NoSuchMethodException e) {
//            e.printStackTrace();
//        } catch (IllegalArgumentException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        } catch (InvocationTargetException e) {
//            e.printStackTrace();
//        }
//    /* Setup connection */
//        try {
//
//            mBtSocket.connect();
//            showToast("Connect bluetooth success");
//            Toast.makeText(context, "Connect bluetooth success",Toast.LENGTH_SHORT).show();
//            Log.i(TAG, "Connect " + mBtDevice.getAddress() + " Success!");
//        } catch (IOException e) {
//            e.printStackTrace();
//            try {
//                showToast("Connect error, close");
//                Toast.makeText(context, "Connect error, close",Toast.LENGTH_SHORT).show();
//                mBtSocket.close();
//                mBtFlag = false;
//            } catch (IOException e1) {
//                e1.printStackTrace();
//            }
//        }
//
//        if ( mBtFlag ) {
//            try {
//                inStream  = mBtSocket.getInputStream();
//                outStream = mBtSocket.getOutputStream();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    public void showToast(String str)
    {
        Log.d(TAG,str);
    }
}
