package com.example.wangjie.systemblueteeth.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.example.wangjie.systemblueteeth.entity.BlueTooThDevice;
import com.example.wangjie.systemblueteeth.util.CommonsUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static android.content.ContentValues.TAG;

/**
 * Created by wangjie on 2017/6/13.
 */

public class BlueTeethService extends Service {
    public boolean threadFlag = true;
    MyThread myThread;
    CommandReceiver cmdReceiver;//继承自BroadcastReceiver对象，用于得到Activity发送过来的命令

    /**************service 命令*********/

    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;
    private InputStream inStream = null;
    public  boolean bluetoothFlag  = true;//连接状态
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static String address = "00:19:5D:EE:9B:8F"; // <==要连接的蓝牙设备MAC地址

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        cmdReceiver = new CommandReceiver();
        IntentFilter filter = new IntentFilter();//创建IntentFilter对象
        //注册一个广播，用于接收Activity传送过来的命令，控制Service的行为，如：发送数据，停止服务等
        filter.addAction("android.intent.action.cmd");
        //注册Broadcast Receiver
        registerReceiver(cmdReceiver, filter);

    }



    //前台Activity调用startService时，该方法自动执行
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        doJob();
        init();
        return super.onStartCommand(intent, flags, startId);

    }

    public void init(){
        sendDevices();
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        this.unregisterReceiver(cmdReceiver);//取消注册的CommandReceiver
        threadFlag = false;
        boolean retry = true;
        while(retry){
            try{
                myThread.join();
                retry = false;
            }catch(Exception e){
                e.printStackTrace();
            }

        }
    }

    public class MyThread extends Thread{
        @Override
        public void run() {
            // TODO Auto-generated method stub
            super.run();
            connectDevice();//连接蓝牙设备
        }
    }

    public void doJob(){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            DisplayToast("蓝牙设备不可用，请打开蓝牙！");
            bluetoothFlag  = false;
            return;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            DisplayToast("请打开蓝牙并重新运行程序！");
            bluetoothFlag  = false;
            showToast("请打开蓝牙并重新运行程序！");
            return;
        }
        threadFlag = true;
    }
    public  void connectDevice(){
        DisplayToast("正在尝试连接蓝牙设备，请稍后····");
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

        try {
            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            DisplayToast("套接字创建失败！");
            bluetoothFlag = false;
        }
        mBluetoothAdapter.cancelDiscovery();
        try {
            Thread.sleep(1000);
            btSocket.connect();

            DisplayToast("成功连接蓝牙设备！");

            sendCmd(CommonsUtils.CONNECT_SUCCESS,"");

            DisplayToast("连接成功建立，可以开始操控了!");
            showToast("连接成功建立，可以开始操控了!");
            bluetoothFlag = true;
        } catch (Exception e) {
            try {
                btSocket.close();
                bluetoothFlag = false;

                connnetError();

                e.printStackTrace();
            } catch (IOException e2) {
                showToast("连接失败");
                DisplayToast("连接没有建立，无法关闭套接字！");
            }
        }

        if(bluetoothFlag){
            try {
                inStream = btSocket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            } //绑定读接口

            try {
                outStream = btSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            } //绑定写接口

        }
    }
    public void writeSerial(String value) {
        if(!bluetoothFlag){
            showToast("请先连接蓝牙小车！");
            return;
        }

        try {
            outStream.write(value.getBytes());
            Log.d(TAG,value);
            outStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int readSerial() {
        int ret = 0;
        byte[] rsp = null;

        if (!bluetoothFlag ) {
            return -1;
        }

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
        }
        if (inStream == null) {
            return -1;
        }

        try {
            rsp = new byte[inStream.available()];
            ret = inStream.read(rsp);
            DisplayToast(new String(rsp));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ret;
    }

    public void stopService(){
        if(btSocket == null){
            return;
        }

        threadFlag = false;//停止线程
        bluetoothFlag = false;//断开连接
        try {
            inStream.close();
            outStream.close();
            btSocket.close();
            btSocket = null;
        } catch (IOException e) {
            e.printStackTrace();
        }

//        stopSelf();//停止服务
    }

    public void showToast(String str){//显示提示信息
        Intent intent = new Intent();
        intent.putExtra("cmd", CommonsUtils.CMD_SHOW_TOAST);
        intent.putExtra("str", str);
        intent.setAction("android.intent.action.lxx");
        sendBroadcast(intent);
    }

    public void DisplayToast(String str)
    {
        Log.i("Season",str);
    }

    /**
     * 获取已配对设备
     * @return
     */
    public List<BlueTooThDevice> getmBtDevices(){
        Set<BluetoothDevice> mBtDevices = mBluetoothAdapter.getBondedDevices();

        List<BlueTooThDevice>  devices = new ArrayList<BlueTooThDevice>();

        BluetoothDevice bluetoothDevice = null;
        BlueTooThDevice bt = null;

        for (Iterator<BluetoothDevice> iterator = mBtDevices.iterator();
             iterator.hasNext(); ) {
            bt = new BlueTooThDevice();
            bluetoothDevice = (BluetoothDevice)iterator.next();
            bt.setAddress(bluetoothDevice.getAddress());
            bt.setName(bluetoothDevice.getName());
            devices.add(bt);
        }
        return devices;
    }

    private static BluetoothManager mBluetoothManager;

    /**
     * 清理本地的BluetoothGatt 的缓存，以保证在蓝牙连接设备的时候，设备的服务、特征是最新的
     * @return
     */
    public void refreshDeviceCache() {

    //通过BluetoothManager来获取BluetoothAdapter
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
    //一个Android系统只有一个BluetoothAdapter ，通过BluetoothManager 来获取
        mBluetoothAdapter = mBluetoothManager.getAdapter();

    //关闭蓝牙
        mBluetoothAdapter.disable();

        //打开蓝牙
        mBluetoothAdapter.enable();
    }

    //接收Activity传送过来的命令
    private class CommandReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("android.intent.action.cmd")){
                int cmd = intent.getIntExtra("cmd", -1);//获取Extra信息
                if(cmd == CommonsUtils.STOP_SERVICE){
                    stopService();
                }

                if(cmd == CommonsUtils.SEND_DATA)
                {
                    Bundle bundle = intent.getExtras();
                    writeSerial(bundle.getString("value"));
                }
                if(cmd == CommonsUtils.CONNECT_DEVICE){
                    Bundle bundle = intent.getExtras();
                    address = bundle.getString("address");
                    myThread = new MyThread();
                    myThread.start();
                }

                if(cmd == CommonsUtils.GET_DEVICES){
                    sendDevices();
                }
            }
        }
    }
    public void sendCmd(int cmd,String action){
        Intent intent = new Intent();//创建Intent对象
        intent.setAction("android.intent.action.lxx");
        intent.putExtra("cmd", cmd);
        sendBroadcast(intent);//发送广播
    }

    /**
     * 发送配对设备
     */
    public void sendDevices(){
        Intent intent = new Intent();
        intent.setAction("android.intent.action.lxx");
        Bundle bundle = new Bundle();
        bundle.putSerializable("devices",(Serializable)getmBtDevices());
        intent.putExtras(bundle);
        intent.putExtra("cmd", CommonsUtils.SET_DEVICES);
        sendBroadcast(intent);
    }

    /**
     * 连接异常
     */
    public void connnetError(){
        Intent intent = new Intent();
        intent.setAction("android.intent.action.lxx");
        intent.putExtra("cmd", CommonsUtils.CONNET_ERROR);
        sendBroadcast(intent);
    }
}
