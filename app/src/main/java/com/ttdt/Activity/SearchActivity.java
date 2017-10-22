package com.ttdt.Activity;

import android.os.RemoteException;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.baoyz.widget.PullRefreshLayout;
import com.bigkoo.svprogresshud.SVProgressHUD;
import com.ttdt.Adaper.SongListAdapter;
import com.ttdt.Manager.SongManager;
import com.ttdt.R;
import com.ttdt.Util.Cons;
import com.ttdt.Util.Custom.MyJsonRequest;
import com.ttdt.Util.Util;
import com.ttdt.modle.Song;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Administrator on 2017/9/21.
 */
public class SearchActivity extends PlayMusicBaseActivity {
    private EditText et_bottom_border;
    private LinearLayout ill_main_search;
    private PullRefreshLayout pull_refresh;
    private ListView list_view;
    private SongListAdapter songListAdapter;
    private int limit = 25;

    @Override
    int getViewId() {
        return R.layout.activity_search;
    }

    @Override
    void setLister() {
        super.setLister();
        pull_refresh.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                pull_refresh.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pull_refresh.setRefreshing(false);
                    }
                }, 3000);
            }
        });

        ill_main_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = et_bottom_border.getText().toString();
                if (check(text)) {
                    SVProgressHUD.showWithStatus(context,context.getString(R.string.loading));
                    getSearchDataByNet(text,limit);
                }
            }
        });
        list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id1) {
                try {
                    if(service == null){
                        service = MainActivity.getService();
                        return;
                    }
                    if(service.getID() != -1){
                        service.setSongArray(arraySong,-1);
                    }
                    service.openAudio(position);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    void initData() {
        super.initData();
        songListAdapter = new SongListAdapter(arraySong, context);
        list_view.setAdapter(songListAdapter);
    }

    @Override
    void initView() {
        super.initView();
        list_view = (ListView) findViewById(R.id.list_view);
        pull_refresh = (PullRefreshLayout) findViewById(R.id.pull_refresh);
        et_bottom_border = (EditText) findViewById(R.id.et_bottom_border);
        ill_main_search = (LinearLayout) findViewById(R.id.ill_main_search);
    }

    private void getSearchDataByNet(String text,int limit) {

        String url = Cons.baseUrl + "/api/search/pc";

        String params = new StringBuilder()
                .append("s=").append(text)
                .append("&offset=0&limit=").append(limit).append("&type=1").toString();
        MyJsonRequest mjr = new MyJsonRequest(Request.Method.POST, url, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                try {
                    if(SVProgressHUD.isShowing(context)){
                        SVProgressHUD.dismiss(context);
                    }
                    if (jsonObject.has("result")) {
                        JSONObject result = jsonObject.getJSONObject("result");
                        if(result.has("songs")){
                            JSONArray songs = result.getJSONArray("songs");
                            List<Song> songArrayGET = SongManager.getInstance().parseJsonSong(songs);
                            arraySong.clear();
                            arraySong.addAll(songArrayGET);
                            songListAdapter.notifyDataSetChanged();

                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if(SVProgressHUD.isShowing(context)){
                    SVProgressHUD.dismiss(context);
                }
            }
        });
        Util.requestQueue.add(mjr);
    }

    private boolean check(String text) {
        if (text.trim().equals("")) {
            Util.prompting(context, "请输入搜索内容");
            return false;
        } else {
            return true;
        }
    }
}
