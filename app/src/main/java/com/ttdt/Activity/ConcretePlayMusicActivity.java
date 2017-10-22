package com.ttdt.Activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.ttdt.Manager.PopWindowManager;
import com.ttdt.MusicPlayerA;
import com.ttdt.R;
import com.ttdt.Service.MusicPlayerService;
import com.ttdt.Util.Cons;
import com.ttdt.Util.Custom.MainActivityObserver;
import com.ttdt.Util.Custom.MyJsonRequest;
import com.ttdt.Util.Util;
import com.ttdt.Util.lrc.DefaultLrcParser;
import com.ttdt.Util.lrc.LrcRow;
import com.ttdt.Util.lrc.LrcView;
import com.ttdt.modle.Song;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Observable;

/**
 * Created by Administrator on 2017/9/20.
 */

public class ConcretePlayMusicActivity extends Activity {

    private String TAG = "ConcretePlayMusicActivity";
    private Song song = null;
    private SeekBar seek_br_concrete;
    private ImageView iv_disk, iv_concrete_ico, iv_concrete_mode;
    private LrcView mLrcView;
    private LinearLayout ill_concrete_back, ill_concrete_mode, ill_concrete_previous, ill_concrete_play, ill_concrete_next, ill_concrete_list;
    private TextView tv_concrete_name, tv_concrete_artists, tv_concrete_album,tv_concrete_current_time,tv_concrete_total;
    //更新歌词的频率，每秒更新一次
    private int mPalyTimerDuration = 300;
    private MusicPlayerA service;
    private int id;
    List<LrcRow> rows = null;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actiivty_concrete_play_music);
        context = this;
        initView();
        initData();
        setLister();
    }

    LrcView.OnSeekToListener onSeekToListener = new LrcView.OnSeekToListener() {
        @Override
        public void onSeekTo(int progress) {
            try {
                service.seekTo(progress);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };

    private void setLister() {
        IllOnClickListener illOnClickListener = new IllOnClickListener();
        ill_concrete_list.setOnClickListener(illOnClickListener);
        ill_concrete_mode.setOnClickListener(illOnClickListener);
        ill_concrete_previous.setOnClickListener(illOnClickListener);
        ill_concrete_play.setOnClickListener(illOnClickListener);
        ill_concrete_next.setOnClickListener(illOnClickListener);
        seek_br_concrete.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                try {
                    if(fromUser){
                        service.seekTo(progress * Integer.valueOf(service.getDuration())/100);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        mLrcView.setOnSeekToListener(onSeekToListener);

//        //设置自定义的LrcView上下拖动歌词时监听
//        mLrcView.setListener(new ILrcViewListener() {
//            //当歌词被用户上下拖动的时候回调该方法,从高亮的那一句歌词开始播放
//            public void onLrcSeeked(int newPosition, LrcRow row) {
//                try {
//                    if (rows == null || rows.size() - 1 < newPosition) {
//                        return;
//                    }
//                    int time = Util.getIntTime(rows.get(newPosition).strTime);
//                    service.seekTo(time);
//                } catch (RemoteException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
        ill_concrete_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    MainActivityObserver observer = new MainActivityObserver() {
        @Override
        public void update(Observable observable, Object data) {
            song = (Song) data;
            setDataView(song);
            beginPlay(song.getWyID());
        }
    };


    private void initData() {
//        Animation animation = AnimationUtils.loadAnimation(this, R.anim.disc_rotate);
//        iv_disk.setAnimation(animation);
        service = MainActivity.getService();
        if(service != null){
            try {
                service.addObservable(observer);
                if(service.isPlaying()){
                    iv_concrete_ico.setImageResource(R.drawable.play);
                    handler.sendEmptyMessage(UPDATE_SING);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        setData();
    }

    private void setData() {
        if(service == null){
            service = MainActivity.getService();
        }
        if (service != null) {
            try {
                song = service.getCurrentSong();
                id = song.getWyID();
//                id = service.getSongID();
//                String name = service.getName();
//                String artists = service.getArtist();
//                String album = service.getAlbum();
//                String time = service.getDuration();
                setDataView(song);
                changMode(service.getPlayMode(),false);
                beginPlay(this.id);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void setDataView(Song song){
        setDataView(song.getName(),song.getArtist(),song.getAlbumName(),song.getTime());
    }

    private void setDataView(String name, String artists, String album,String time) {
        if (name != null) {
            tv_concrete_name.setText(name);
        }
        if (artists != null) {
            tv_concrete_artists.setText(artists);
        }
        if (album != null) {
            tv_concrete_album.setText(album);
        }
        if(time != null){
            tv_concrete_total.setText(Util.getTime(Integer.valueOf(time)));
//            seek_br_concrete.setProgress(Integer.valueOf(time)/100);
        }
    }



    private void beginPlay(int id) {

        String url = new StringBuilder().append(Cons.baseUrl).append("api/song/lyric?id=")
                .append(id).append("&lv=-1").toString();

        MyJsonRequest mjr = new MyJsonRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                try {
                    if (jsonObject.has("lrc")) {
                        JSONObject lrc1 = jsonObject.getJSONObject("lrc");
                        if (lrc1.has("lyric")) {
                            String lrc = lrc1.getString("lyric");
                            Log.e(TAG,"lyric " + lrc);
                            //解析歌词构造器
                       //     ILrcBuilder builder = new DefaultLrcBuilder();
                            //解析歌词返回LrcRow集合
                            rows = DefaultLrcParser.getIstance().getLrcRows(lrc);
                            //将得到的歌词集合传给mLrcView用来展示
                            mLrcView.setLrcRows(rows);
                            //开始于服务交互，根据播放进度来更新歌词
                            handler.sendEmptyMessage(1);
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


//http://music.163.com/api/song/lyric?id=202373&lv=-1


    }
    int UPDATE_SING = 0;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            try {
                if (msg.what == 1) {
                    if (service == null) {
                        service = MainActivity.getService();
                    }
                    int time = service.getCurrentPosition();
                    //滚动歌词
                    mLrcView.seekTo(time);
                    sendEmptyMessageDelayed(1, mPalyTimerDuration);
                }
                else if (msg.what == UPDATE_SING) {
                    try {
                        int currentPosition = service.getCurrentPosition();
                        String time = Util.getTime(currentPosition);
                        tv_concrete_current_time.setText(time);
                        seek_br_concrete.setProgress((currentPosition*100)/Integer.valueOf(service.getDuration()));
                        sendEmptyMessageDelayed(UPDATE_SING, 1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };

    private void initView() {
        seek_br_concrete = (SeekBar)findViewById(R.id.seek_br_concrete);

        tv_concrete_current_time = (TextView)findViewById(R.id.tv_concrete_current_time);
        tv_concrete_total = (TextView)findViewById(R.id.tv_concrete_total);

        ill_concrete_list = (LinearLayout) findViewById(R.id.ill_concrete_list);
        ill_concrete_next = (LinearLayout) findViewById(R.id.ill_concrete_next);
        ill_concrete_play = (LinearLayout) findViewById(R.id.ill_concrete_play);
        ill_concrete_previous = (LinearLayout) findViewById(R.id.ill_concrete_previous);
        ill_concrete_mode = (LinearLayout) findViewById(R.id.ill_concrete_mode);

        iv_concrete_ico = (ImageView) findViewById(R.id.iv_concrete_ico);
        iv_concrete_mode = (ImageView) findViewById(R.id.iv_concrete_mode);

        ill_concrete_back = (LinearLayout) findViewById(R.id.ill_concrete_back);
        mLrcView = (LrcView) findViewById(R.id.mLrcView);
//        iv_disk = (ImageView)findViewById(R.id.iv_disk);
        tv_concrete_name = (TextView) findViewById(R.id.tv_concrete_name);
        tv_concrete_artists = (TextView) findViewById(R.id.tv_concrete_artists);
        tv_concrete_album = (TextView) findViewById(R.id.tv_concrete_album);
    }

    private class IllOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            try {
                if (service == null) {
                    return;
                }
                switch (v.getId()) {
                    case R.id.ill_concrete_mode:
                        changMode();
                        break;
                    case R.id.ill_concrete_previous:
                        service.pre();
                        setData();
                        break;
                    case R.id.ill_concrete_play:
                        play();
                        break;
                    case R.id.ill_concrete_next:
                        service.next();
                        setData();
                        break;
                    case R.id.ill_concrete_list:
                        if(song != null){
                            PopWindowManager.getInstance().showPupWindowMore(v,song,context,PopWindowManager.TOP);
                        }
                        break;
                }
            } catch (RemoteException e) {

            }
        }
    }

    private void changMode(int mode,boolean isToast) {
        if (mode == MusicPlayerService.ORDER) {
            iv_concrete_mode.setImageResource(R.drawable.list_cycle);
        } else if (mode == MusicPlayerService.SINGLE) {
            iv_concrete_mode.setImageResource(R.drawable.single_play);
        } else if (mode == MusicPlayerService.RANDOM) {
            iv_concrete_mode.setImageResource(R.drawable.random_play);
        }

        if(isToast) {
            if (mode == MusicPlayerService.ORDER) {
                Util.prompting(context, "列表循环模式");
            } else if (mode == MusicPlayerService.SINGLE) {
                Util.prompting(context, "单曲循环模式");
            } else if (mode == MusicPlayerService.RANDOM) {
                Util.prompting(context, "随机播放模式");
            }
        }
    }

    private void changMode() throws RemoteException {
        service.setPlayMode(-1);
        int mode = service.getPlayMode();
        changMode(mode,true);
    }

    private void play() throws RemoteException {
        if (service.isPlaying()) {
            service.pause();
            handler.removeMessages(UPDATE_SING);
            iv_concrete_ico.setImageResource(R.drawable.pause);
        } else {
            service.start();
            handler.sendEmptyMessage(UPDATE_SING);
            iv_concrete_ico.setImageResource(R.drawable.play);
        }
    }
}
