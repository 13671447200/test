package com.ttdt.Activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.ttdt.Adaper.SelectSongAdapter;
import com.ttdt.Manager.SongManager;
import com.ttdt.MyApplication;
import com.ttdt.R;
import com.ttdt.Util.Util;
import com.ttdt.modle.Song;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/10/14.
 */

public class SelectSongActivity extends Activity {

    private LinearLayout ill_play_list_back;
    private TextView tv_select_list_title,tv_select_list_all,tv_select_list_upload;
    private ListView list_view;
    private List<Song> songArray = new ArrayList<>();
    private SelectSongAdapter adapter;
    private Context context;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_list);
        context = this;
        initView();
        setLister();
        initData();

    }

    private void initData() {
        adapter = new SelectSongAdapter(songArray, this);
        list_view.setAdapter(adapter);
        SongManager.getInstance().getLocalMusic(new SongManager.GetSongHD() {
            @Override
            public void success(List<Song> localSongArray) {
                songArray.addAll(localSongArray);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void fail() {
                Util.prompting(SelectSongActivity.this,"获取数据失败！");
            }
        });

    }

    private void setLister() {
        ill_play_list_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tv_select_list_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAllSongSelect();
            }
        });
        tv_select_list_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadSong();
            }
        });
        list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Song song = songArray.get(position);
                song.setSelect(!song.isSelect());
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void uploadSong() {

        SongManager songManager = SongManager.getInstance();
        for(int i = 0; i < songArray.size(); i++){
            final Song song = songArray.get(i);
            if(song.isSelect() && !song.isUploading()){
                song.setUploading(true);
                songManager.uploadSong(song, new SongManager.GetJsonHD() {
                    @Override
                    public void success(JSONObject jsonObject) {
                        try {
                            final String msg = jsonObject.getString("msg");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    notifyChangce(MyApplication.getInstance(), msg);
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void fail() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                notifyChangce(context, "上传音乐失败");
                            }
                        });
                    }

                    private void notifyChangce(Context context, String msg) {
                        song.setUploading(false);
                        song.setSelect(false);
                        adapter.notifyDataSetChanged();
                        Util.prompting(context, msg);
                    }
                });
            }
            adapter.notifyDataSetChanged();
        }


    }

    private void setAllSongSelect() {
        for(Song song : songArray){
            song.setSelect(true);
        }
        adapter.notifyDataSetChanged();
    }

    private void initView() {
        ill_play_list_back = (LinearLayout)findViewById(R.id.ill_play_list_back);
        tv_select_list_title = (TextView)findViewById(R.id.tv_select_list_title);
        tv_select_list_all = (TextView)findViewById(R.id.tv_select_list_all);
        list_view = (ListView)findViewById(R.id.list_view);
        tv_select_list_upload = (TextView)findViewById(R.id.tv_select_list_upload);
    }
}
