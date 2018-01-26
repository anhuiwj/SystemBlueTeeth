package com.example.wangjie.systemblueteeth;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.example.wangjie.systemblueteeth.db.DatabaseHelper;
import com.pgyersdk.crash.PgyCrashManager;
import com.taobao.sophix.PatchStatus;
import com.taobao.sophix.SophixManager;
import com.taobao.sophix.listener.PatchLoadStatusListener;

import static com.ta.utdid2.b.a.j.TAG;

/**
 * Created by wangjie on 2017/6/23.
 */

public class AppApplication extends Application {

    public DatabaseHelper databaseHelper = null;

    @Override
    public void onCreate() {

        SophixManager.getInstance().setContext(this)
                .setAppVersion(String.valueOf(getAPPVersion()))
                .setAesKey(null)
                .setEnableDebug(true)
                .setPatchLoadStatusStub(new PatchLoadStatusListener() {
                    @Override
                    public void onLoad(final int mode, final int code, final String info, final int handlePatchVersion) {
                        // 补丁加载回调通知
                        if (code == PatchStatus.CODE_LOAD_SUCCESS) {
                            // 表明补丁加载成功
                            Log.d(TAG, "补丁加载成功");
                        } else if (code == PatchStatus.CODE_LOAD_RELAUNCH) {
                            // 表明新补丁生效需要重启. 开发者可提示用户或者强制重启;
                            // 建议: 用户可以监听进入后台事件, 然后应用自杀
                            Log.d(TAG, "表明新补丁生效需要重启");
                        } else if (code == PatchStatus.CODE_LOAD_FAIL) {
                            // 内部引擎异常, 推荐此时清空本地补丁, 防止失败补丁重复加载
                            // SophixManager.getInstance().cleanPatches();
                            Log.d(TAG, "内部引擎异常");
                        } else {
                            // 其它错误信息, 查看PatchStatus类说明
                        }
                    }
                }).initialize();
        SophixManager.getInstance().queryAndLoadNewPatch();

        PgyCrashManager.register(this);//蒲公英sdk注册

        //加载数据库
        databaseHelper = new DatabaseHelper(this);

        super.onCreate();
    }

    /**
     * 获取软件版本号
     */
    private int getAPPVersion() {
        PackageManager pm = this.getPackageManager();//得到PackageManager对象

        try {
            PackageInfo pi = pm.getPackageInfo(this.getPackageName(), 0);//得到PackageInfo对象，封装了一些软件包的信息在里面
            int appVersion = pi.versionCode;//获取清单文件中versionCode节点的值
            Log.d(TAG, "appVersion="+appVersion);
            return appVersion;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, "getAppVersion:"+e.getCause());
        }
        return 0;
    }

    public DatabaseHelper getDatabaseHelper() {
        return databaseHelper;
    }
}
