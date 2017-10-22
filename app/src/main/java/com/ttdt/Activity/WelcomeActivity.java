package com.ttdt.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.ttdt.Manager.LoginManager;
import com.ttdt.R;
import com.ttdt.modle.User;

public class WelcomeActivity extends Activity {

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
            finish();
        }
    };
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);
		//初始化控件
		setupView();

		initData();

	}

	private void initData() {

        String[] login = LoginManager.getInstance().getLogin();
        if(login != null){
            String loginName = login[0];
            String passWord = login[1];
            LoginManager.getInstance().login(loginName, passWord, new LoginManager.LoginHD() {
                @Override
                public void success(User user) {
                    handler.sendEmptyMessageDelayed(1,3000);
                }

                @Override
                public void fail() {
                    handler.sendEmptyMessageDelayed(1,3000);
                }
            });
        }else{
            handler.sendEmptyMessageDelayed(1,3000);
        }



//        UserDao userDao = MyApplication.getDaoSession().getUserDao();
//        String sql = "SELECT * FROM " + UserDao.TABLENAME +" where " + UserDao.Properties.Time + "=" + "(select max (" + UserDao.Properties.Time + ") from " + UserDao.TABLENAME + ")";
//        Cursor cursor = MyApplication.getDb().rawQuery(sql, null);
//        while(cursor.moveToNext()){
//            String loginName = cursor.getString(1);
//            String passWord = cursor.getString(2);
//            LoginManager.getInstance().login(loginName,passWord,null);
//            break;
//        }
//        cursor.close();


	}

	private void setupView() {
	}


}

