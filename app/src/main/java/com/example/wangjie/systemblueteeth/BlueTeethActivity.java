package com.example.wangjie.systemblueteeth;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wangjie.systemblueteeth.db.DatabaseHelper;
import com.example.wangjie.systemblueteeth.entity.BlueTooThDevice;
import com.example.wangjie.systemblueteeth.myView.LoadingDialog;
import com.example.wangjie.systemblueteeth.service.BlueTeethService;
import com.example.wangjie.systemblueteeth.util.CommonsUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by wangjie on 2017/6/12.
 */

public class BlueTeethActivity extends Activity {

    private List<BlueTooThDevice>  devices = new ArrayList<BlueTooThDevice>();

    private ListView listView;

    private  List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

    private String connectName = "";

    private Intent intent;

    private MyReceiver receiver;

    private Button button;

    private DatabaseHelper databaseHelper = null;

    private LoadingDialog loadingDialog = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.blue_teeth_list);

        //自定义bar
        ActionBar actionBar=getActionBar();
        actionBar.setCustomView(R.layout.search_activity_bar);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

        listView = (ListView)this.findViewById(R.id.list);
        button = (Button) this.findViewById(R.id.connectNew);

        receiver = new MyReceiver();
        IntentFilter filter=new IntentFilter();
        filter.addAction("android.intent.action.lxx");
        BlueTeethActivity.this.registerReceiver(receiver,filter);

        loadingDialog = new LoadingDialog(this);

//        myStartService();
//        listView.setAdapter(new ListViewAdapter(mBtDevices));

        AppApplication context = (AppApplication)this.getApplicationContext();
        databaseHelper = context.getDatabaseHelper();

        intent = new Intent(BlueTeethActivity.this,BlueTeethService.class);
        startService(intent);


        //处理Item的点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                createAlertDialog(position);
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =  new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
                startActivity(intent);
            }
        });
        Log.i("BLUETEETH","onstart");
    }

    //AlertDialog对话框
    public void createAlertDialog(final int position){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("是否连接该设备？");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                loadingDialog.setMessage("正在连接.....").show();
                BlueTooThDevice blueTooThDevice = null;
                if(devices!=null && devices.size()>0){
                    blueTooThDevice = devices.get(position);
                }

                try {
                    sendCmd(CommonsUtils.STOP_SERVICE,"");//先关闭上个连接
                }catch (NullPointerException e){
                    Log.e("BlueTeeth",e.toString());
                }

                connectName = blueTooThDevice.getName();
                databaseHelper.deleteAll();
                sendCmd(CommonsUtils.CONNECT_DEVICE,blueTooThDevice.getAddress());
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), "取消成功", Toast.LENGTH_SHORT).show();
            }
        });
        builder.create().show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(keyCode == KeyEvent.KEYCODE_BACK){
            Intent myIntent = new Intent();
            myIntent.setClass(getApplicationContext(),MainActivity.class);
            this.setResult(2, myIntent);
            this.finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();

        if(receiver!=null){
            BlueTeethActivity.this.unregisterReceiver(receiver);
        }
        //sendCmd(CommonsUtils.STOP_SERVICE,"");
    }




    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        sendCmd(CommonsUtils.GET_DEVICES,"");
        Log.i("BLUETEETH","onrsume");
    }

    public class ListViewAdapter extends BaseAdapter {
        View[] itemViews;

        public ListViewAdapter(List<BlueTooThDevice>  devices) {
            // TODO Auto-generated constructor stub
            if(devices == null){
                return;
            }
            itemViews = new View[devices.size()];

            BlueTooThDevice bluetoothDevice = null;
            if ( devices.size() > 0 ) {

                for(int i=0;i<devices.size();i++){
                    bluetoothDevice = devices.get(i);
                    itemViews[i]=makeItemView(
                            bluetoothDevice.getName(),bluetoothDevice.getAddress(),bluetoothDevice.getConnect_status()
                    );
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
        private View makeItemView(String strTitle, String strText,String connectStatus) {
            LayoutInflater inflater = (LayoutInflater) BlueTeethActivity.this
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            // 使用View的对象itemView与R.layout.item关联
            View itemView = inflater.inflate(R.layout.list_item, null);

            // 通过findViewById()方法实例R.layout.item内各组件
            TextView title = (TextView) itemView.findViewById(R.id.blueTeethName);
            title.setText(strTitle);    //填入相应的值
            TextView text = (TextView) itemView.findViewById(R.id.blueTeethAddress);
            text.setText(strText);
            TextView statues = (TextView) itemView.findViewById(R.id.connect_status);
            statues.setText(connectStatus);


            return itemView;
        }


        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                return itemViews[position];
            return convertView;
        }
    }

    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            if(intent.getAction().equals("android.intent.action.lxx")){
                Bundle bundle = intent.getExtras();
                int cmd = bundle.getInt("cmd");


                if(cmd == CommonsUtils.SET_DEVICES){
                    devices = handle(bundle);
                    listView.setAdapter(new ListViewAdapter(devices));
                }

                if(cmd == CommonsUtils.CONNECT_SUCCESS){
                    junpTo();
                }

                if(cmd == CommonsUtils.CMD_SHOW_TOAST){
                    showToast(intent.getStringExtra("str"));
                    Log.i("BlueTeeTh",intent.getStringExtra("str"));
                }

                if(cmd == CommonsUtils.CONNET_ERROR){
                    connectName = "";
                    loadingDialog.dismiss();
                    showToast("连接失败，请检查设备蓝牙是否开启！");
                }
            }
        }
    }

    public void sendCmd(int cmd,String value){
        Intent intent = new Intent();//创建Intent对象
        intent.setAction("android.intent.action.cmd");
        intent.putExtra("cmd", cmd);
        Bundle bundle = new Bundle();
        bundle.putString("address", value);
        intent.putExtras(bundle);
        sendBroadcast(intent);//发送广播
    }

    public void showToast(String str){
        Toast.makeText(BlueTeethActivity.this, str,Toast.LENGTH_SHORT).show();
    }

    public void junpTo(){
        Intent myIntent = new Intent();
        myIntent.setClass(getApplicationContext(),MainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("connectName",connectName);
        myIntent.putExtras(bundle);
        this.setResult(1, myIntent);
        this.finish();
    }

    public List<BlueTooThDevice> handle(Bundle bundle){
        List<BlueTooThDevice>  devicesList = new ArrayList<BlueTooThDevice>();
        devicesList = (List<BlueTooThDevice>)bundle.getSerializable("devices");
        for(int i=0;i<devicesList.size();i++){
            BlueTooThDevice d = devicesList.get(i);
            connectName = databaseHelper.getConnectName();
            if(connectName !=null
                    && connectName.equals(d.getName())){
                d.setConnect_status("已连接");
            }else {
                d.setConnect_status("未连接");
            }
        }
        return devicesList;
    }
}
