package com.example.wangjie.systemblueteeth;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wangjie.systemblueteeth.myView.Rudder;
import com.example.wangjie.systemblueteeth.util.CommonsUtils;
import com.example.wangjie.systemblueteeth.util.DateUtils;
import com.example.wangjie.systemblueteeth.util.MsgUtils;
import com.pgyersdk.crash.PgyCrashManager;
import com.pgyersdk.feedback.PgyFeedbackShakeManager;

public class MainActivity extends Activity {
    private TextView textView;

    private ImageButton goUp;

    private ImageButton goDown;

    private int offset;

    private String connnectName;

    private String connectNewName;//新连接名称

    private MyReceiver receiver;

    private Button quitCar;

    private ImageButton stopMove;
    private boolean ifQuit = true;//是否退出

    private String MSG = "请先连接蓝牙智能小车";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PgyCrashManager.register(this);

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
       // goDown = (ImageButton) findViewById(R.id.goDown);

        stopMove = (ImageButton)findViewById(R.id.stopMove);
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
                        setLogText("转向："+angle);
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
                if(ifQuit){
                    Intent intent = new Intent(getApplicationContext(),BlueTeethActivity.class);
                    startActivityForResult(intent,1);
                }else {
                    showToast("请先退出当前连接");
                }
            }
        });

        goUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(connnectName !=null &&
                        connnectName.length() >0 ){
                    sendCmd(CommonsUtils.FANG_XIANG,0+"",CommonsUtils.SEND_DATA);
                    setLogText("前进");
                }else {
                    showToast(MSG);
                }
            }
        });

//        goDown.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(connnectName !=null &&
//                        connnectName.length() >0 ){
//                    sendCmd(CommonsUtils.FANG_XIANG,0+"",CommonsUtils.SEND_DATA);
//                    setLogText("后退");
//                }else {
//                    showToast(MSG);
//                }
//            }
//        });

        stopMove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(connnectName !=null &&
                        connnectName.length() >0 ){
                    sendCmd(CommonsUtils.SHA_CHE,0+"",CommonsUtils.SEND_DATA);
                    setLogText("刹车");
                }else {
                    showToast(MSG);
                }
            }
        });

        quitCar = (Button) findViewById(R.id.quitCar);
        quitCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendCmd(CommonsUtils.STOP_SERVICE,"");//先关闭上个连接
                ifQuit = true;
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode,Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 1:
                Bundle bunde = data.getExtras();
                connectNewName = bunde.getString("connectName");

                if(connectNewName != null
                        && connectNewName.length() > 0 ){

                    connnectName =  connectNewName;

                    setLogText(connnectName);

                    ifQuit = false;
                }else{
                    connnectName  = null;
                }
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
     * @param value
     */
    public void setLogText(String value){
        textView.append(DateUtils.getDate()+" "+value+"\n");
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
