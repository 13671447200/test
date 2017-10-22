package com.ttdt.Manager;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ttdt.Activity.LoginActivity;
import com.ttdt.Activity.MainActivity;
import com.ttdt.Adaper.PopWindowPlayListAdapter;
import com.ttdt.MusicPlayerA;
import com.ttdt.MyApplication;
import com.ttdt.R;
import com.ttdt.Util.Cons;
import com.ttdt.Util.Custom.MyJsonRequest;
import com.ttdt.Util.Util;
import com.ttdt.modle.PlayList;
import com.ttdt.modle.Song;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import static android.view.Gravity.NO_GRAVITY;

/**
 * Created by Administrator on 2017/10/22.
 */

public class PopWindowManager {

    public static int TOP = 0;
    public static int BELOW = 1;

    private Context context;

    private PopupWindowView popupWindow;
    private SongManager songManager;
    private MusicPlayerA service = null;
    public static PopWindowManager getInstance() {
        return PopWindowManager.PopWindowManagerHolder.getInstance();
    }

    static class PopWindowManagerHolder {
        static PopWindowManager instance = null;

        public static PopWindowManager getInstance() {
            if (instance == null) {
                instance = new PopWindowManager();
            }
            return instance;
        }
    }

    private PopWindowManager() {
        service = MainActivity.getService();
        context = MyApplication.getInstance();
        songManager = SongManager.getInstance();
    }

    private void finishPop() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
            popupWindow = null;
        }
    }

    /**
     * 弹出选项框
     *
     * @param v
     * @param song
     */
    public void showPupWindowMore(View v, Song song, Context context,int direction) {
        finishPop();
        View view = LayoutInflater.from(context).inflate(R.layout.popwindow_more, null);
        TextView tv_pop_more_next = (TextView) view.findViewById(R.id.tv_pop_more_next);
        TextView tv_pop_more_add = (TextView) view.findViewById(R.id.tv_pop_more_add);
        TextView tv_pop_more_collect = (TextView) view.findViewById(R.id.tv_pop_more_collect);
        TextView tv_pop_more_down = (TextView) view.findViewById(R.id.tv_pop_more_down);
        PopWindowManager.MoreOnClickListener moreOnClickListener = new PopWindowManager.MoreOnClickListener(song);
        tv_pop_more_add.setOnClickListener(moreOnClickListener);
        tv_pop_more_collect.setOnClickListener(moreOnClickListener);
        tv_pop_more_next.setOnClickListener(moreOnClickListener);
        tv_pop_more_down.setOnClickListener(moreOnClickListener);

        popupWindow = new PopupWindowView(view);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.showUp2(v);
//        int x = 0,y = 0;
//        x = (int) (-1* v.getWidth());
//        if(direction == TOP){
//            int i = -view.getMeasuredHeight();
//            int i1 = -v.getHeight();
//            y = -i - i1;
//        }
//        popupWindow.showAsDropDown(v, x, y);
    }


    class MoreOnClickListener implements View.OnClickListener {

        private Song song = null;
        private Context context = MyApplication.getInstance();
        public MoreOnClickListener(Song song) {
            this.song = song;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.tv_pop_more_add:
                    moreAdd(song);
                    break;
                case R.id.tv_pop_more_next:
                    settNextPlay(song,context);
                    break;
                case R.id.tv_pop_more_collect:
                    addSongByUserIdToCollect(LoginManager.getUserId(),song);
//                    addToCollect(position);
                    break;
                case R.id.tv_pop_more_down:
                    downSong(song);
                    break;
            }
        }

        private void moreAdd(Song song) {
            if (LoginManager.getInstance().isLogin()) {
                showPupWindowPlayList(song);
            } else {
                Intent intent = new Intent(context, LoginActivity.class);
                context.startActivity(intent);
            }
        }

        private void settNextPlay(Song song,Context context) {
            try {
                service.setNextPlay(song);
                finishPop();
                Util.prompting(context, "设置成功");
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        private void addSongByUserIdToCollect(long userId, Song song) {
            try {
                finishPop();
                String url = getUrl(song, "addSongToCollect");
                url = url + "&userId=" + userId;
                getSongByUrl(url);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        private void downSong(final Song song) {
            finishPop();
            String name = song.getName();
            if(songManager.localIsHasSong(name)){
                Util.prompting(context,"您已下载过这首歌");
                return;
            }
            String url = song.getUrl();
            if(url == null || url.equals("null")){
                songManager.getSongUrl(song.getWyID(), new SongManager.GetSongUrlHD() {
                    @Override
                    public void success(String url) {
                        song.setUrl(url);
                        songManager.downSong(song);
                    }

                    @Override
                    public void fail() {

                    }
                });
            }else{
                songManager.downSong(url,song.getName());
            }
            if(name != null){
                Util.prompting(context,"开始下载" + song.getName());
            }else{
                Util.prompting(context,"开始下载");
            }

        }

    }

    /**
     * 弹出添加歌单框
     *
     * @param song
     */
    private void showPupWindowPlayList(final Song song) {
        finishPop();
        View view = LayoutInflater.from(context).inflate(R.layout.pop_play_list, null);
        ListView list_view = (ListView) view.findViewById(R.id.list_view);
        LinearLayout ill_pop_play_list_new = (LinearLayout) view.findViewById(R.id.ill_pop_play_list_new);
        final LinearLayout ll_pop_play_list_input = (LinearLayout) view.findViewById(R.id.ll_pop_play_list_input);
        final LinearLayout ill_pop_play_list_ok = (LinearLayout) view.findViewById(R.id.ill_pop_play_list_ok);
        final EditText et_pop_play_list_new_list = (EditText) view.findViewById(R.id.et_pop_play_list_new_list);
        View root = view.findViewById(R.id.root);

        final List<PlayList> playLists = SongManager.getInstance().getPlayList();

        list_view.setAdapter(new PopWindowPlayListAdapter(context, playLists));
        ill_pop_play_list_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPlayList(et_pop_play_list_new_list, song, ill_pop_play_list_ok);
            }
        });
        ill_pop_play_list_new.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_pop_play_list_new_list.setFocusable(true);
                et_pop_play_list_new_list.setFocusableInTouchMode(true);
                et_pop_play_list_new_list.requestFocus();
                ll_pop_play_list_input.setVisibility(View.VISIBLE);
            }
        });
        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
        list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PlayList playList = playLists.get(position);
                addSongByPlayList(playList.getId(), song);
                finishPop();
            }
        });

        popupWindow = new PopupWindowView(view);
        popupWindow.setSoftInputMode(PopupWindow.INPUT_METHOD_NEEDED);
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.showAtLocation(root, NO_GRAVITY, 0, 0);
    }

    private void addSongByPlayList(long playListId, Song song) {
        try {
            String url = getUrl(song, "addSongToPlayListID");
            url = url + "&playListID=" + playListId;
            getSongByUrl(url);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void addPlayList(EditText et_pop_play_list_new_list, final Song song, final LinearLayout ill_pop_play_list_ok) {
        try {
            String url = new StringBuilder().append(Cons.baseUrl_my).append("addPlayList")
                    .append("?userId=").append(LoginManager.getUserId())
                    .append("&playListName=")
                    .append(URLEncoder.encode(et_pop_play_list_new_list.getText().toString(), "UTF-8")).toString();

            MyJsonRequest mjr = new MyJsonRequest(url, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    try {
                        if (jsonObject.has("code")) {
                            if (jsonObject.getInt("code") == 200) {
                                JSONArray playList = jsonObject.getJSONObject("content").getJSONArray("playList");
                                LoginManager.getInstance().parsePlayListJson(playList);
                                showPupWindowPlayList(song);
                                ill_pop_play_list_ok.setVisibility(View.GONE);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {

                }
            });
            Util.requestQueue.add(mjr);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void getSongByUrl(String url) {
        JsonObjectRequest mjr = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                try {
                    if (jsonObject.has("msg")) {
                        String msg = jsonObject.getString("msg");
                        Util.prompting(context, msg);
                    }
                } catch (JSONException e) {
                    Util.prompting(context, "错误");
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Util.prompting(context, "错误");
                volleyError.printStackTrace();
            }
        });
        Util.requestQueue.add(mjr);
    }

    private String getUrl(Song song, String urlName) throws UnsupportedEncodingException {
//        String url = new StringBuilder().append(Cons.baseUrl_my)
//                .append(urlName)
//                .append("?wyId=").append(song.getWyID())
//                .append("&albumName=").append(song.getAlbumName())
//                .append("&name=").append(song.getName())
//                .append("&artist=").append(song.getArtist())
//                .append("&time=").append(song.getTime())
//                .append("&url=").append(song.getUrl())
//                .append("&artistImage=").append(song.getArtistImage())
//                .toString();
//        url = url.replace(" ", "%2B");
        String url = new StringBuilder().append(Cons.baseUrl_my)
                .append(urlName)
                .append("?wyId=").append(song.getWyID())
                .append("&albumName=").append(URLEncoder.encode(song.getAlbumName(), "UTF-8"))
                .append("&name=").append(URLEncoder.encode(song.getName(), "UTF-8"))
                .append("&artist=").append(URLEncoder.encode(song.getArtist(), "UTF-8"))
                .append("&time=").append(song.getTime())
                .append("&url=").append(song.getUrl())
                .append("&artistImage=").append(song.getArtistImage())
                .toString();
        url = url.replace(" ", "%2B");
        return url;
    }


}
