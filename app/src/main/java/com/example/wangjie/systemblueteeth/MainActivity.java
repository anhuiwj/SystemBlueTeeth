package com.example.wangjie.systemblueteeth;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wangjie.systemblueteeth.db.DatabaseHelper;
import com.example.wangjie.systemblueteeth.myView.Rudder;
import com.example.wangjie.systemblueteeth.util.CommonsUtils;
import com.example.wangjie.systemblueteeth.util.DateUtils;
import com.example.wangjie.systemblueteeth.util.MsgUtils;
import com.pgyersdk.crash.PgyCrashManager;
import com.pgyersdk.feedback.PgyFeedbackShakeManager;
import com.pgyersdk.update.PgyUpdateManager;

public class MainActivity extends Activity {
    private TextView textView;

    private ImageButton goUp;

    private ImageButton goDown;

    private int offset;

    private String connnectName;

    private String connectNewName;//新连接名称

    private MyReceiver receiver;

    private ImageButton quitCar;

    private ImageButton stopMove;

    private String MSG = "请先连接蓝牙智能小车";

    private DatabaseHelper databaseHelper = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PgyUpdateManager.setIsForced(true); //设置是否强制更新。true为强制更新；false为不强制更新（默认值）。
        PgyUpdateManager.register(this);

        //自定义bar
        ActionBar actionBar=getActionBar();
        actionBar.setCustomView(R.layout.actionbar_title);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

        receiver = new MyReceiver();

        IntentFilter filter=new IntentFilter();
        filter.addAction("android.intent.action.main");
        MainActivity.this.registerReceiver(receiver,filter);


        textView = (TextView) findViewById(R.id.showLog);
        textView.setMovementMethod(ScrollingMovementMethod.getInstance());

        goUp = (ImageButton) findViewById(R.id.goUp);
        goDown = (ImageButton) findViewById(R.id.goDown);

        stopMove = (ImageButton)findViewById(R.id.stopMove);

        AppApplication context = (AppApplication)this.getApplicationContext();
        databaseHelper = context.getDatabaseHelper();

        databaseHelper.deleteAll();

        hasConnected();

        init();
    }
    public void init(){
        //初始化摇杆
        Rudder rudder = (Rudder) findViewById(R.id.rudder);
        rudder.setRudderListener(new Rudder.RudderListener() {
            @Override
            public void onSteeringWheelChanged(int action, int angle) {
                if(action == Rudder.ACTION_RUDDER) {
                    //TODO:事件实现
                    if(connnectName !=null &&
                            connnectName.length() >0 ){
                        sendCmd(CommonsUtils.FANG_XIANG,angle+"",CommonsUtils.SEND_DATA);

                        databaseHelper.insertLog("转向"+angle,connectNewName,DateUtils.getDate());
                        showLogText();
                    }else {
                        showToast(MSG);
                    }

                }
            }
        });


        //监听连接小车按钮
        Button connertCar = (Button) findViewById(R.id.connectCar);
        connertCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),BlueTeethActivity.class);
                startActivityForResult(intent,1);
            }
        });

        goUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(connnectName !=null &&
                        connnectName.length() >0 ){
                    sendCmd(CommonsUtils.FANG_XIANG,0+"",CommonsUtils.SEND_DATA);
                    //setLogText("前进");
                    databaseHelper.insertLog("前进",connectNewName,DateUtils.getDate());
                    showLogText();
                }else {
                    showToast(MSG);
                }
            }
        });

        goDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(connnectName !=null &&
                        connnectName.length() >0 ){
                    sendCmd(CommonsUtils.FANG_XIANG,0+"",CommonsUtils.SEND_DATA);
                    //setLogText("后退");
                    databaseHelper.insertLog("后退",connectNewName,DateUtils.getDate());
                    showLogText();
                }else {
                    showToast(MSG);
                }
            }
        });

        stopMove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(connnectName !=null &&
                        connnectName.length() >0 ){
                    sendCmd(CommonsUtils.SHA_CHE,0+"",CommonsUtils.SEND_DATA);
                    //setLogText("刹车");
                    databaseHelper.insertLog("刹车",connectNewName,DateUtils.getDate());
                    showLogText();
                }else {
                    showToast(MSG);
                }
            }
        });

        quitCar = (ImageButton) findViewById(R.id.quitCar);
        quitCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendCmd(CommonsUtils.STOP_SERVICE,"");//先关闭上个连接
                databaseHelper.deleteAll();
                System.exit(0);
            }
        });
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

        // 自定义摇一摇的灵敏度，默认为950，数值越小灵敏度越高。
        PgyFeedbackShakeManager.setShakingThreshold(1000);

        // 以对话框的形式弹出
        PgyFeedbackShakeManager.register(MainActivity.this);

        // 以Activity的形式打开，这种情况下必须在AndroidManifest.xml配置FeedbackActivity
        // 打开沉浸式,默认为false
        // FeedbackActivity.setBarImmersive(true);
        PgyFeedbackShakeManager.register(MainActivity.this, false);

    }

    //获取连接设备名称
    public Boolean hasConnected(){
        connnectName = databaseHelper.getConnectName();
        if(connnectName != null){
            return true;
        }
        return false;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode,Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode){
            case 1:
                Bundle bunde = data.getExtras();
                connectNewName = bunde.getString("connectName");

                if(connectNewName != null
                        && connectNewName.length() > 0 ){

                    connnectName =  connectNewName;

                    databaseHelper.insertConnectInfo(connectNewName);
                    databaseHelper.insertLog("连接小车："+connectNewName,connectNewName,DateUtils.getDate());
                    showLogText();
                }else{
                    connnectName  = null;
                }
                break;
            case 2:
                connnectName = databaseHelper.getConnectName();
                showLogText();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {

        PgyCrashManager.unregister();//解除注册蒲公英
        PgyFeedbackShakeManager.unregister();

        sendCmd(null,null,CommonsUtils.STOP_SERVICE);

        databaseHelper.deleteAll();

        super.onDestroy();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    public void sendCmd(String type,String value,int cmd){
        Intent intent = new Intent();//创建Intent对象
        intent.setAction("android.intent.action.cmd");
        Bundle bundle = new Bundle();
        bundle.putString("value", value !=null ? MsgUtils.sendMsg(type, value):"");
        intent.putExtras(bundle);
        intent.putExtra("cmd", cmd);
        sendBroadcast(intent);//发送广播
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

    /**
     * 动态设置TEXTVIEW值
     */
    public void showLogText(){
        textView.setText(null);
        textView.setText(databaseHelper.getAll(null,"time"));
        offset=textView.getLineCount()*textView.getLineHeight();
        if(offset>textView.getHeight()){
            textView.scrollTo(0,offset-textView.getHeight());
        }
    }

    public void showToast(String msg){
        Toast.makeText(getApplication(),msg,Toast.LENGTH_SHORT).show();
    }

    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            if(intent.getAction().equals("android.intent.action.main")){
                Bundle bundle = intent.getExtras();
                int cmd = bundle.getInt("cmd");


                if(cmd == CommonsUtils.CONNET_ERROR){
                    connnectName = null;
                }
            }
        }
    }
}
