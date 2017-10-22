package com.ttdt.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.ttdt.MusicPlayerA;
import com.ttdt.R;
import com.ttdt.Util.Custom.MainActivityObserver;
import com.ttdt.Util.Util;
import com.ttdt.modle.Song;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

/**
 * Created by Administrator on 2017/9/21.
 */

public abstract class PlayMusicBaseActivity extends Activity {
    int id;
    LinearLayout ill_main_next, ill_main_play;
    LinearLayout ill_main_previous, ill_play_list_back;
    ImageView img_main_show_info, iv_main_play;
    //歌唱家，名称，当前时间，总时间
    TextView tv_main_artist, tv_main_name, tv_main_current_time, tv_main_total;
    Context context;
    MusicPlayerA service = null;
    int UPDATE_LYRIC = 0;
    int UPDATE_SONG_ARRAY = 1;
    List<Song> arraySong = new ArrayList<Song>();
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == UPDATE_LYRIC) {
                try {
                    String time = Util.getTime(service.getCurrentPosition());
                    tv_main_current_time.setText(time);
                    sendEmptyMessageDelayed(UPDATE_LYRIC, 1000);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }else if(msg.what == UPDATE_SONG_ARRAY){
                try {
                    if(service.getID() != id){
                        sendEmptyMessageDelayed(UPDATE_SONG_ARRAY,1000);
                    }else{
                        openAudio((Integer) msg.obj);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    void openAudio(int position) throws RemoteException {
        service.openAudio(position);
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            if (service != null && service.isPlaying()) {
                handler.removeMessages(UPDATE_LYRIC);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (service != null && service.isPlaying()) {
                setBottomView(service.getName(), service.getArtist(), String.valueOf(service.getDuration()), service.getImageUrl());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int id = getViewId();
        setContentView(id);
        context = this;
        initView();
        initData();
        setLister();
    }

    abstract int getViewId();

    void setLister() {
        img_main_show_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (service != null && service.isOK()) {
                        Intent intent = new Intent(context,ConcretePlayMusicActivity.class);
                        startActivity(intent);
                    }
                } catch (RemoteException e) {

                }
            }
        });

        ill_main_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (service != null && service.isOK()) {
                        service.next();
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

            }
        });
        ill_main_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (service != null && service.isOK()) {
                        if (service.isPlaying()) {
                            service.pause();
                            iv_main_play.setImageResource(R.drawable.playbar_btn_pause);
                            handler.removeMessages(UPDATE_LYRIC);
                        } else {
                            service.start();
                            handler.sendEmptyMessage(UPDATE_LYRIC);
                            iv_main_play.setImageResource(R.drawable.playbar_btn_play);
                        }
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

            }
        });
        ill_main_previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (service != null && service.isOK()) {
                        service.pre();
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
        ill_play_list_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    void initData() {
        service = MainActivity.getService();
        try {
            service.addObservable(observer);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    MainActivityObserver observer = new MainActivityObserver() {
        @Override
        public void update(Observable observable, Object data) {
            Song song = (Song) data;
            setBottomView(song);
        }
    };

    void setBottomView(Song song) {
        setBottomView(song.getName(), song.getArtist(), song.getTime(), song.getArtistImage());
    }

    void setBottomView(String name, String artist, String time, String imageUrl) {
        setTextAll();
        tv_main_name.setText(name);
        tv_main_artist.setText(artist);
        tv_main_total.setText(Util.getTime(Integer.valueOf(time)));
        if (imageUrl == null || imageUrl.equals("null")) {
            Picasso.with(context).load(R.drawable.img_album_background)
                    .into(img_main_show_info);
        } else {
            Picasso.with(context).load(imageUrl)
                    .placeholder(R.drawable.img_album_background)
                    .error(R.drawable.img_album_background)
                    .into(img_main_show_info);
        }
        handler.removeMessages(UPDATE_LYRIC);
        handler.sendEmptyMessage(UPDATE_LYRIC);
    }

    private void setTextAll(){
//        tv_main_name.setText(name);
//        tv_main_artist.setText(artist);
        Util.setTextMarquee(tv_main_artist);
        Util.setTextMarquee(tv_main_name);
    }

    void initView() {
        img_main_show_info = (ImageView) findViewById(R.id.img_main_show_info);
        tv_main_artist = (TextView) findViewById(R.id.tv_main_artist);
        tv_main_name = (TextView) findViewById(R.id.tv_main_name);
        tv_main_current_time = (TextView) findViewById(R.id.tv_main_current_time);
        tv_main_total = (TextView) findViewById(R.id.tv_main_total);
        img_main_show_info = (ImageView) findViewById(R.id.img_main_show_info);
        ill_main_next = (LinearLayout) findViewById(R.id.ill_main_next);
        ill_main_play = (LinearLayout) findViewById(R.id.ill_main_play);
        iv_main_play = (ImageView) findViewById(R.id.iv_main_play);
        ill_main_previous = (LinearLayout) findViewById(R.id.ill_main_previous);
        ill_play_list_back = (LinearLayout) findViewById(R.id.ill_play_list_back);
    }
}
