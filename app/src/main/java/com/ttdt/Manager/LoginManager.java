package com.ttdt.Manager;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.ttdt.MyApplication;
import com.ttdt.Util.Cons;
import com.ttdt.Util.Custom.MyJsonRequest;
import com.ttdt.Util.Util;
import com.ttdt.modle.PlayList;
import com.ttdt.modle.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2017/10/7.
 */
public class LoginManager {

    private static User user;

    public void logOut(){
        user = null;
    }

    private SharedPreferences sp;

    private Context context;
    private LoginManager(Context context){
        this.context = context;
        sp = context.getSharedPreferences("Login",Context.MODE_PRIVATE);
    }
    public static LoginManager getInstance() {
        return LoginManager.LoginManagerHolder.getInstance();
    }

    public static String getUserName(){
        if(user != null){
            return user.getLoginName();
        }
        return  "未登录";
    }

    public static int getUserId(){
        if(user != null){
            return user.getId();
        }
        return  -1;
    }
    public boolean isLogin(){
//        return  true;
        return user != null ? true:false;
    }

    static class LoginManagerHolder {
        static LoginManager instance = null;

        public static LoginManager getInstance() {
            if (instance == null) {
                instance = new LoginManager(MyApplication.getInstance());
            }
            return instance;
        }
    }

    public void login(String name, String password, final LoginHD loginHD){
        String url = new StringBuilder().append(Cons.baseUrl_my).append("UserLogin?")
                .append("loginName=")
                .append(name)
                .append("&passWord=")
                .append(password).toString();
        MyJsonRequest mjr = new MyJsonRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                if(jsonObject.has("code")){
                    try {
                        int code = jsonObject.getInt("code");
                        if(code == 200){
                            if(user == null){
                                user = new User();
                            }
                            JSONObject content = jsonObject.getJSONObject("content");
                            JSONObject userJson = content.getJSONObject("user");
                            user.setId(userJson.getInt("id"));
                            user.setLoginName(userJson.getString("loginName"));
                            user.setNickName(userJson.getString("nickName"));
                            user.setPassword(userJson.getString("password"));
                            user.setHeadImage(userJson.getString("headImage"));
                            user.setTime(System.currentTimeMillis());
                            saveLogin(user.getLoginName(),user.getPassword());
//                            MyApplication.getDaoSession().getUserDao().insert(user);
                            JSONArray playList = content.getJSONArray("playList");
                            parsePlayListJson(playList);
                            if(loginHD != null)
                            loginHD.success(user);
                        }else{
                            if(loginHD != null)
                            loginHD.fail();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        if(loginHD != null)
                        loginHD.fail();
                    }
                }else{
                    if(loginHD != null)
                    loginHD.fail();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                volleyError.printStackTrace();
                if(loginHD != null)
                loginHD.fail();
            }
        });
        Util.requestQueue.add(mjr);
    }

    public void parsePlayListJson(JSONArray playList) throws JSONException {
        for(int i = 0; i < playList.length();i++){
            JSONObject list = playList.getJSONObject(i);
            PlayList playList1 = new PlayList();
            playList1.setId(list.getInt("id"));
            playList1.setImage(list.getString("image"));
            playList1.setPlayCount(list.getInt("playCount"));
            playList1.setUserId(list.getInt("userId"));
            playList1.setPlayListName(list.getString("playListName"));
            MyApplication.getDaoSession().getPlayListDao().insertOrReplace(playList1);
        }
    }

    public void saveLogin(String loginName,String passWord){
        sp.edit().putString("loginName",loginName).putString("passWord",passWord).commit();
    }

    public String[] getLogin(){
        String[] data = null;
        String loginName = sp.getString("loginName", null);
        String passWord = sp.getString("passWord", null);
        if(loginName != null && passWord != null){
            data = new String[2];
            data[0] = loginName;
            data[1] = passWord;
        }
        return data;
    }

    public interface LoginHD{
        void success(User user);
        void fail();
    }


}
