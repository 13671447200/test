package com.ttdt.Activity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.bigkoo.svprogresshud.SVProgressHUD;
import com.ttdt.Manager.LoginManager;
import com.ttdt.R;
import com.ttdt.Util.Util;
import com.ttdt.modle.User;

/**
 * Created by Administrator on 2017/10/7.
 */

public class LoginActivity extends Activity {

    private EditText et_login_name;
    private EditText et_login_password;
    private Button btn_login_submit;
    private Context context;
    private TextView tv_bind_title;
    private View root,content;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        this.context = this;

        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        et_login_name = (EditText) findViewById(R.id.et_login_name);
        et_login_password = (EditText) findViewById(R.id.et_login_password);
        btn_login_submit = (Button) findViewById(R.id.btn_login_submit);
        tv_bind_title = (TextView)findViewById(R.id.tv_bind_title);
        content = findViewById(R.id.content);
        tv_bind_title.setText("登录");
        root = findViewById(R.id.root);
        controlKeyboardLayout(root,content);
        addLayoutListener(root,btn_login_submit);
        btn_login_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.hideSoftInputFromWindow(context,v);
                SVProgressHUD.showWithStatus(context, context.getString(R.string.loading));
                String name = et_login_name.getText().toString().replace(" ", "");
                String password = et_login_password.getText().toString().replace(" ", "");
                if (!name.equals("") && !password.equals("")) {
                    LoginManager.getInstance().login(name, password, new LoginManager.LoginHD() {
                        @Override
                        public void success(User user) {
                            finish();
                            if (SVProgressHUD.isShowing(context)) {
                                SVProgressHUD.dismiss(context);
                            }
                            Util.prompting(context, "登录成功");
                        }

                        @Override
                        public void fail() {
                            if (SVProgressHUD.isShowing(context)) {
                                SVProgressHUD.dismiss(context);
                            }
                            Util.prompting(context, "登录失败");
                        }
                    });
                }
            }
        });
    }


    /**
     * @param root             最外层布局
     * @param needToScrollView 要滚动的布局,就是说在键盘弹出的时候,你需要试图滚动上去的View,在键盘隐藏的时候,他又会滚动到原来的位置的布局
     */
    private void controlKeyboardLayout(final View root, final View needToScrollView) {
        root.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            private Rect r = new Rect();
            @Override
            public void onGlobalLayout() {
                //获取当前界面可视部分
                LoginActivity.this.getWindow().getDecorView().getWindowVisibleDisplayFrame(r);
                //获取屏幕的高度
                int screenHeight = LoginActivity.this.getWindow().getDecorView().getRootView().getHeight();
                //此处就是用来获取键盘的高度的， 在键盘没有弹出的时候 此高度为0 键盘弹出的时候为一个正数
                int heightDifference = screenHeight - r.bottom;

                Rect rect = new Rect();
                needToScrollView.getGlobalVisibleRect(rect);
                int viewKeepOut = heightDifference - (screenHeight - rect.bottom);
                if(viewKeepOut > 0){
                    needToScrollView.scrollTo(0, viewKeepOut);
                }else{
                    needToScrollView.scrollTo(0, 0);
                }
            }
        });
    }

    /**
     * 1、获取main在窗体的可视区域
     * 2、获取main在窗体的不可视区域高度
     * 3、判断不可视区域高度
     * 1、大于100：键盘显示  获取Scroll的窗体坐标
     * 算出main需要滚动的高度，使scroll显示。
     * 2、小于100：键盘隐藏
     *
     * @param main   根布局
     * @param scroll 需要显示的最下方View
     */
    public void addLayoutListener(final View main, final View scroll) {

//        main.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                Rect rect = new Rect();
//                main.getWindowVisibleDisplayFrame(rect);
//                int mainInvisibleHeight = main.getRootView().getHeight() - rect.bottom;
//                if (mainInvisibleHeight > 150) {
//                    int[] location = new int[2];
//                    scroll.getLocationInWindow(location);
//                    int srollHeight = (location[1] + scroll.getHeight()) - rect.bottom;
////                    main.scrollTo(0, srollHeight/2);
//                    main.scrollBy(0, mainInvisibleHeight);
//                } else {
//                    main.scrollTo(0, 0);
//                }
//            }
//        });
    }

}
