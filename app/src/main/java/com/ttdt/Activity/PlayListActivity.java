package com.ttdt.Activity;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Message;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
import com.bigkoo.svprogresshud.SVProgressHUD;
import com.google.android.gms.common.api.GoogleApiClient;
import com.ttdt.Adaper.PopWindowPlayListAdapter;
import com.ttdt.Adaper.SongListAdapter;
import com.ttdt.Manager.LoginManager;
import com.ttdt.Manager.SongManager;
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
 * Created by Administrator on 2017/9/21.
 */

public class PlayListActivity extends PlayMusicBaseActivity {

    private TextView tv_play_list_title, tv_play_list_no_data;
    private String name;
    private SongManager songManager;
    private ListView list_view;
    private SongListAdapter songListAdapter = null;
    private PopupWindow popupWindow;
    private View root;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    int getViewId() {
        return R.layout.activity_play_list;
    }

    @Override
    void setLister() {
        super.setLister();
        list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id1) {
                try {
                    if (service == null) {
                        return;
                    }
                    int serviceID = service.getID();
                    if (serviceID != id) {
                        service.setSongArray(arraySong, id);
                        Message msg = Message.obtain();
                        msg.what = UPDATE_SONG_ARRAY;
                        msg.obj = position;
                        handler.sendMessageDelayed(msg, 2000);
                    } else {
                        openAudio(position);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    void initData() {
        super.initData();
        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        tv_play_list_title.setText(name);
        id = intent.getIntExtra("id", -1);
        long playListId = intent.getLongExtra("playListId", -1L);
        long uploadIds = intent.getIntExtra("uploadId", -1);
        long collectId = intent.getIntExtra("collectId", -1);
        songListAdapter = new SongListAdapter(arraySong, context);
        songListAdapter.setMoreOnclick(new SongListAdapter.MoreOnclick() {
            @Override
            public void onClick(View v, int position) {
                showPupWindowMore(v, position);
            }
        });
        list_view.setAdapter(songListAdapter);
        songManager = SongManager.getInstance();
        if (id != -1) {
            getSongArrayByNet();
        } else if (playListId != -1) {
            getSongArrayByPlayList(playListId);
        } else if (uploadIds != -1) {
            getSongArrayByUploadIds(uploadIds);
        } else if (collectId != -1) {
            getSongArrayByCollectId(collectId);
        } else {
            getSongArrayByLocal();
        }
    }

    private void getSongArrayByCollectId(long collectId) {
        SVProgressHUD.showWithStatus(context, context.getString(R.string.loading));
        songManager.getSongArrayByCollectId(collectId, new SongManager.GetSongHD() {
            @Override
            public void success(List<Song> songArray) {
                if (SVProgressHUD.isShowing(context)) {
                    SVProgressHUD.dismiss(context);
                }
                setDataAndInform(songArray, false);
            }

            @Override
            public void fail() {
                if (SVProgressHUD.isShowing(context)) {
                    SVProgressHUD.dismiss(context);
                }
                Util.prompting(context, "请求数据失败");
            }
        });
    }

    private void getSongArrayByUploadIds(long uploadIds) {
        SVProgressHUD.showWithStatus(context, context.getString(R.string.loading));
        songManager.getSongArrayByUploadIds(uploadIds, new SongManager.GetSongHD() {
            @Override
            public void success(List<Song> songArray) {
                if (SVProgressHUD.isShowing(context)) {
                    SVProgressHUD.dismiss(context);
                }
                setDataAndInform(songArray, false);
            }

            @Override
            public void fail() {
                if (SVProgressHUD.isShowing(context)) {
                    SVProgressHUD.dismiss(context);
                }
                Util.prompting(context, "请求数据失败");
            }
        });
    }

    private void getSongArrayByPlayList(long playListId) {
        SVProgressHUD.showWithStatus(context, context.getString(R.string.loading));
        songManager.getPlayListByID(playListId, new SongManager.GetSongHD() {
            @Override
            public void success(List<Song> songArray) {
                if (SVProgressHUD.isShowing(context)) {
                    SVProgressHUD.dismiss(context);
                }
                setDataAndInform(songArray, false);
            }

            @Override
            public void fail() {
                if (SVProgressHUD.isShowing(context)) {
                    SVProgressHUD.dismiss(context);
                }
                Util.prompting(context, "请求数据失败");
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    /**
     * 弹出添加歌单框
     *
     * @param arrayPosition
     */
    private void showPupWindowPlayList(final int arrayPosition) {
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
                addPlayList(et_pop_play_list_new_list, arrayPosition, ill_pop_play_list_ok);
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
                Song song = arraySong.get(arrayPosition);
                addSongByPlayList(playList.getId(), song);
                finishPop();
            }
        });

        popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        popupWindow.setSoftInputMode(PopupWindow.INPUT_METHOD_NEEDED);
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.showAtLocation(root, NO_GRAVITY, 0, 0);
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

    private void addSongByPlayList(long playListId, Song song) {
        try {
            String url = getUrl(song, "addSongToPlayListID");
            url = url + "&playListID=" + playListId;
            getSongByUrl(url);
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

    private void addPlayList(EditText et_pop_play_list_new_list, final int arrayPosition, final LinearLayout ill_pop_play_list_ok) {
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
                                showPupWindowPlayList(arrayPosition);
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
     * @param position
     */
    private void showPupWindowMore(View v, int position) {
        finishPop();
        View view = LayoutInflater.from(context).inflate(R.layout.popwindow_more, null);
        TextView tv_pop_more_next = (TextView) view.findViewById(R.id.tv_pop_more_next);
        TextView tv_pop_more_add = (TextView) view.findViewById(R.id.tv_pop_more_add);
        TextView tv_pop_more_collect = (TextView) view.findViewById(R.id.tv_pop_more_collect);
        TextView tv_pop_more_down = (TextView) view.findViewById(R.id.tv_pop_more_down);
        MoreOnClickListener moreOnClickListener = new MoreOnClickListener(position);
        tv_pop_more_add.setOnClickListener(moreOnClickListener);
        tv_pop_more_collect.setOnClickListener(moreOnClickListener);
        tv_pop_more_next.setOnClickListener(moreOnClickListener);
        tv_pop_more_down.setOnClickListener(moreOnClickListener);
        popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.showAsDropDown(v, (int) (-1.5 * v.getWidth()), 0);
    }

    class MoreOnClickListener implements View.OnClickListener {

        private int position = -1;

        public MoreOnClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.tv_pop_more_add:
                    moreAdd(position);
                    break;
                case R.id.tv_pop_more_next:
                    settNextPlay(arraySong.get(position));
                    break;
                case R.id.tv_pop_more_collect:
                    addToCollect(position);
                    break;
                case R.id.tv_pop_more_down:
                    downSong(position);
                    break;
            }
        }

        private void settNextPlay(Song song) {
            try {
                service.setNextPlay(song);
                finishPop();
                Util.prompting(context, "设置成功");
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void downSong(int position) {
        finishPop();
        final Song song = arraySong.get(position);
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
                    songManager.downSong(song,true);
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

    /**
     * 收藏函数暂时不做
     *
     * @param position
     */
    private void addToCollect(int position) {
        Song song = arraySong.get(position);
        addSongByUserIdToCollect(LoginManager.getUserId(), song);
    }

    private void moreAdd(int position) {
        if (LoginManager.getInstance().isLogin()) {
            showPupWindowPlayList(position);
        } else {
            Intent intent = new Intent(context, LoginActivity.class);
            startActivity(intent);
        }
    }

    private void getSongArrayByLocal() {
        songManager.getLocalMusic(new SongManager.GetSongHD() {
            @Override
            public void success(List<Song> songArray) {
                setDataAndInform(songArray, false);
            }

            @Override
            public void fail() {

            }
        });
    }

    private void getSongArrayByNet() {
        SVProgressHUD.showWithStatus(context, context.getString(R.string.loading));
        songManager.rankDataByNet(id, new SongManager.GetSongHD() {
            @Override
            public void success(List<Song> songArray) {
                if (SVProgressHUD.isShowing(context)) {
                    SVProgressHUD.dismiss(context);
                }
                setDataAndInform(songArray, false);
            }

            @Override
            public void fail() {
                if (SVProgressHUD.isShowing(context)) {
                    SVProgressHUD.dismiss(context);
                }
                Util.prompting(context, "请求数据失败");
            }
        });
    }

    @Override
    void initView() {
        super.initView();
        list_view = (ListView) findViewById(R.id.list_view);
        tv_play_list_title = (TextView) findViewById(R.id.tv_play_list_title);
        root = findViewById(R.id.root);
        tv_play_list_no_data = (TextView) findViewById(R.id.tv_play_list_no_data);
    }

    private void setDataAndInform(List<Song> songArray, boolean isClean) {
        try {
            if (isClean) {
                arraySong.clear();
            }
            arraySong.addAll(songArray);
            songListAdapter.notifyDataSetChanged();
            if (arraySong != null && arraySong.size() > 0) {
                service.setSongArray(arraySong, id);
                tv_play_list_no_data.setVisibility(View.GONE);
                list_view.setVisibility(View.VISIBLE);
            } else {
                tv_play_list_no_data.setVisibility(View.VISIBLE);
                list_view.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
