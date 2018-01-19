package com.example.wangjie.systemblueteeth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wangjie.systemblueteeth.util.BlueTeethUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Created by wangjie on 2017/6/12.
 */

public class BlueTeethActivity_bak extends Activity {
    private BluetoothAdapter mBtAdapter = null;
    private BluetoothSocket mBtSocket   = null;
    private OutputStream outStream = null;
    private InputStream inStream  = null;
    private boolean mBtFlag = true;

    private boolean threadFlag = false;

    private Thread mThread;


    private static final UUID HC_UUID   = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private String TAG = "BLUTHEE";

    private Set<BluetoothDevice> mBtDevices;

    private ListView listView;

    private  List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

    private String connectName = "";

    private BlueTeethUtils blueTeethUtils = new BlueTeethUtils();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blue_teeth_list);

        listView=(ListView)this.findViewById(R.id.list);

        mBtDevices = blueTeethUtils.myStartService();
        new MyThread().start();
//        myStartService();
//        listView.setAdapter(new ListViewAdapter(mBtDevices));

        //处理Item的点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                connectName = position+"";
                BluetoothDevice mBtDevice = null;
                if(mBtDevices !=null &&
                        mBtDevices.size() >= position+1){
                    int i = 0;
                    for (Iterator<BluetoothDevice> iterator = mBtDevices.iterator();
                         iterator.hasNext(); ) {

                        iterator.next();

                        if(i==position){
                            mBtDevice = (BluetoothDevice)iterator.next();
                            break;
                        }
                        i+=1;
                    }
                    new BlueTeethUtils.Connect(mBtDevice,getApplicationContext()).start();
                    //connectTo(mBtDevice);
                }
                //Toast显示测试
                Toast.makeText(BlueTeethActivity_bak.this, "点击第几个:"+position,Toast.LENGTH_SHORT).show();
            }
        });
    }

    public class MyThread extends Thread{
        @Override
        public void run() {
            mBtDevices = blueTeethUtils.myStartService();
            listView.setAdapter(new ListViewAdapter(mBtDevices));
        }
    }

    /**
     * Called by onStartCommand, initialize and start runtime thread
     */
//    private void myStartService() {
//        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
//        if ( mBtAdapter == null ) {
//            showToast("Bluetooth unused.");
//            mBtFlag  = false;
//            return;
//        }
//        if ( !mBtAdapter.isEnabled() ) {
//            mBtFlag  = false;
//            myStopService();
//            showToast("Open bluetoooth then restart program!!");
//            return;
//        }
//
//        showToast("Start searching!!");
//        threadFlag = true;
//        mThread = new MyThread();
//        mThread.start();
//    }
//
//    private void myStopService() {
//        mBtAdapter.cancelDiscovery();
//    }
//
//    /**
//     * Thread runtime
//     */
//    public class MyThread extends Thread {
//        @Override
//        public void run() {
//            super.run();
//            myBtConnect();
//        }
//    }
//
//    public void myBtConnect() {
//        showToast("Connecting...");
//         mBtDevices = mBtAdapter.getBondedDevices();
//
//        BluetoothDevice mBtDevice = null;
//        if(mBtDevices !=null &&
//                mBtDevices.size() >= 1+1) {
//            int i = 0;
//            for (Iterator<BluetoothDevice> iterator = mBtDevices.iterator();
//                 iterator.hasNext(); ) {
//
//                mBtDevice = (BluetoothDevice) iterator.next();
//
//                if ("GSCC_YM-V1.0".equals(mBtDevice.getName())) {
//                    break;
//                }
//                i += 1;
//            }
////                    new BlueTeethUtils.Connect(mBtDevice,getApplicationContext()).start();
//            try {
//                mBtSocket = mBtDevice.createRfcommSocketToServiceRecord(HC_UUID);
//            } catch (IOException e) {
//                e.printStackTrace();
//                mBtFlag = false;
//                showToast("Create bluetooth socket error");
//            }
//
//            mBtAdapter.cancelDiscovery();
//
//            try {
//                Method m = mBtDevice.getClass().getMethod(
//                        "createRfcommSocket", new Class[] { int.class });
//                mBtSocket = (BluetoothSocket) m.invoke(mBtDevice, 1);//这里端口为1
//            } catch (SecurityException e) {
//                e.printStackTrace();
//            } catch (NoSuchMethodException e) {
//                e.printStackTrace();
//            } catch (IllegalArgumentException e) {
//                e.printStackTrace();
//            } catch (IllegalAccessException e) {
//                e.printStackTrace();
//            } catch (InvocationTargetException e) {
//                e.printStackTrace();
//            }
//    /* Setup connection */
//            try {
//
//                mBtSocket.connect();
//                showToast("Connect bluetooth success");
////                Toast.makeText(getApplicationContext(), "Connect bluetooth success",Toast.LENGTH_SHORT).show();
//                Log.i(TAG, "Connect " + mBtDevice.getAddress() + " Success!");
//            } catch (IOException e) {
//                e.printStackTrace();
//                try {
//                    showToast("Connect error, close");
//                    Toast.makeText(getApplicationContext(), "Connect error, close",Toast.LENGTH_SHORT).show();
//                    mBtSocket.close();
//                    mBtFlag = false;
//                } catch (IOException e1) {
//                    e1.printStackTrace();
//                }
//            }
//
//            if ( mBtFlag ) {
//                try {
//                    inStream  = mBtSocket.getInputStream();
//                    outStream = mBtSocket.getOutputStream();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                writeSerial("hello");
//            }
//        }
//    }
//
//
//    public int readSerial() {
//        int ret = 0;
//        byte[] rsp = null;
//
//        if ( !mBtFlag ) {
//            return -1;
//        }
//        try {
//            rsp = new byte[inStream.available()];
//            ret = inStream.read(rsp);
//            showToast(new String(rsp));
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        return ret;
//    }
//
//    public void writeSerial(String value) {
//        String ha = value;
//        try {
//            outStream.write("123".getBytes());
//            Log.d(TAG,outStream != null?"1":"2");
//            outStream.flush();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void connectTo(BluetoothDevice mBtDevice){
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
//        try {
//            Method m = mBtDevice.getClass().getMethod(
//                    "createRfcommSocket", new Class[] { int.class });
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
//            Toast.makeText(getApplicationContext(), "Connect bluetooth success",Toast.LENGTH_SHORT).show();
//            Log.i(TAG, "Connect " + mBtDevice.getAddress() + " Success!");
//        } catch (IOException e) {
//            e.printStackTrace();
//            try {
//                showToast("Connect error, close");
//                Toast.makeText(getApplicationContext(), "Connect error, close",Toast.LENGTH_SHORT).show();
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
//
//    public void showToast(String str)
//    {
//        Log.d(TAG,str);
//    }

    private List<Map<String, Object>> getData() {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

        BluetoothDevice mBtDevice = null;

        Map<String, Object> map = null;
        if ( mBtDevices.size() > 0 ) {
            for (Iterator<BluetoothDevice> iterator = mBtDevices.iterator();
                 iterator.hasNext(); ) {
                map = new HashMap<String, Object>();
                mBtDevice = (BluetoothDevice)iterator.next();
                map.put("name", mBtDevice.getName());
                map.put("address", mBtDevice.getAddress());
                list.add(map);
            }
        }
        return list;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(keyCode == KeyEvent.KEYCODE_BACK){
            Intent myIntent = new Intent();
            myIntent.setClass(getApplicationContext(),MainActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("connectName",connectName);
            myIntent.putExtras(bundle);
            startActivityForResult(myIntent,1);
        }
        return super.onKeyDown(keyCode, event);
    }


    public class ListViewAdapter extends BaseAdapter {
        View[] itemViews;

        public ListViewAdapter(Set<BluetoothDevice> mBtDevices) {
            // TODO Auto-generated constructor stub
            if(mBtDevices == null){
                return;
            }
            itemViews = new View[mBtDevices.size()];

            BluetoothDevice bluetoothDevice = null;
            int i = 0;
            if ( mBtDevices.size() > 0 ) {
                for (Iterator<BluetoothDevice> iterator = mBtDevices.iterator();
                     iterator.hasNext(); ) {
                    bluetoothDevice = (BluetoothDevice)iterator.next();
                    //调用makeItemView，实例化一个Item
                    itemViews[i]=makeItemView(
                            bluetoothDevice.getName(),bluetoothDevice.getAddress()
                    );
                    i++;
                }
            }
        }

        public int getCount() {
            return itemViews!=null?itemViews.length:0;
        }

        public View getItem(int position) {
            return itemViews[position];
        }

        public long getItemId(int position) {
            return position;
        }

        //绘制Item的函数
        private View makeItemView(String strTitle, String strText) {
            LayoutInflater inflater = (LayoutInflater) BlueTeethActivity_bak.this
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            // 使用View的对象itemView与R.layout.item关联
            View itemView = inflater.inflate(R.layout.list_item, null);

            // 通过findViewById()方法实例R.layout.item内各组件
            TextView title = (TextView) itemView.findViewById(R.id.blueTeethName);
            title.setText(strTitle);    //填入相应的值
            TextView text = (TextView) itemView.findViewById(R.id.blueTeethAddress);
            text.setText(strText);


            return itemView;
        }


        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                return itemViews[position];
            return convertView;
        }
    }
}
