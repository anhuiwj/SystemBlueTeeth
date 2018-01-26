package com.example.wangjie.systemblueteeth.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by wangjie on 2018/1/24.
 */

public class DatabaseHelper extends SQLiteOpenHelper {
    
    //数据库名称
    private static final String name = "blueTeeth_db";

    //数据库版本
    private static final int version = 1;
    
    public DatabaseHelper(Context context) {
        //第三个参数CursorFactory指定在执行查询时获得一个游标实例的工厂类,设置为null,代表使用系统默认的工厂类
        super(context, name, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS log (id integer primary key autoincrement, text varchar(220), connect_name varchar(220),time varchar(20))");
        db.execSQL("CREATE TABLE IF NOT EXISTS connect_info (id integer primary key autoincrement,  connect_name varchar(220))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public StringBuffer getAll(String where, String orderBy){
        StringBuffer res = new StringBuffer();


        StringBuilder buf=new StringBuilder("SELECT id, text, connect_name, time FROM log ");

        if (where!=null) {
            buf.append(" WHERE ");
            buf.append(where);
        }

        if (orderBy!=null) {
            buf.append(" ORDER BY ");
            buf.append(orderBy);
        }

        Cursor cursor = (getReadableDatabase().rawQuery(buf.toString(), null));
        if(cursor.moveToFirst()){
            do {
                res.append(cursor.getString(3)
                        + '\t'
                        + cursor.getString(1)
                        + '\n');
            } while (cursor.moveToNext());
        }

        return res;
    }

    /**
     * 获取连接设备名称
     * @return
     */
    public String getConnectName(){
        String res = null;
        StringBuilder buf=new StringBuilder("SELECT connect_name FROM connect_info ORDER BY id ");
        Cursor cursor = (getReadableDatabase().rawQuery(buf.toString(), null));
        if(cursor.moveToFirst()){
            res = cursor.getString(0);
        }
        return res;
    }

    public void insertLog(String text, String connectName, String time) {
        ContentValues cv=new ContentValues();

        cv.put("text", text);
        cv.put("connect_name", connectName);
        cv.put("time", time);
        getWritableDatabase().insert("log", "name", cv);
    }

    public void insertConnectInfo( String connectName) {
        ContentValues cv=new ContentValues();

        cv.put("connect_name", connectName);
        getWritableDatabase().insert("connect_info", "name", cv);
    }

    //删除连接表
    public void deleteConnectInfo(){
        getWritableDatabase().execSQL("delete from connect_info");
    }

    //删除日志
    public void deleteLog(){
        getWritableDatabase().execSQL("delete from log");
    }

    public void deleteAll(){
        deleteConnectInfo();
        deleteLog();
    }

}
