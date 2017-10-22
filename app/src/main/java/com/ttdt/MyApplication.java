package com.ttdt;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.widget.Toast;

import com.ttdt.Manager.SongManager;
import com.ttdt.Util.Util;
import com.ttdt.greendao.DaoMaster;
import com.ttdt.greendao.DaoSession;

/**
 * Created by Administrator on 2017/9/19.
 */

public class MyApplication extends Application {

    public static MyApplication instance = null;
    public static MyApplication getInstance(){
         return instance;
    }

    private static DaoMaster.DevOpenHelper mHelper;
    private static SQLiteDatabase db;
    private static DaoMaster mDaoMaster;
    private static DaoSession mDaoSession;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            String text = (String) msg.obj;
            Toast.makeText(instance,text,Toast.LENGTH_SHORT).show();
        }
    };

    public void showToastIsDown(String text){
        SongManager.getInstance().setUpdateLocal(true);
        showToast(text);
    }

    public void showToast(String text){
        Message message = Message.obtain();
        message.obj = text;
        handler.sendMessage(message);
    }

    @Override
    public void onCreate() {
        instance = this;
        Util.init(this);
        //解决不同分辨率字体大小的不同
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        displayMetrics.scaledDensity = displayMetrics.density;
        setDatabase();
    }

    /**
     * 设置greenDao
     */
    private void setDatabase() {
        // 通过 DaoMaster 的内部类 DevOpenHelper，你可以得到一个便利的 SQLiteOpenHelper 对象。
        // 可能你已经注意到了，你并不需要去编写「CREATE TABLE」这样的 SQL 语句，因为 greenDAO 已经帮你做了。
        // 注意：默认的 DaoMaster.DevOpenHelper 会在数据库升级时，删除所有的表，意味着这将导致数据的丢失。
        // 所以，在正式的项目中，你还应该做一层封装，来实现数据库的安全升级。
        mHelper = new DaoMaster.DevOpenHelper(this, "notes-db", null);
        db = mHelper.getWritableDatabase();
        // 注意：该数据库连接属于 DaoMaster，所以多个 Session 指的是相同的数据库连接。
        mDaoMaster = new DaoMaster(db);
        mDaoSession = mDaoMaster.newSession();
    }

    public static DaoSession getDaoSession() {
        return mDaoSession;
    }
    public static SQLiteDatabase getDb() {
        return db;
    }
}
