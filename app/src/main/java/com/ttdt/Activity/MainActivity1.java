//package com.ttdt.Activity;
//
//import android.content.ComponentName;
//import android.content.Context;
//import android.content.Intent;
//import android.content.ServiceConnection;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.IBinder;
//import android.os.Message;
//import android.os.RemoteException;
//import android.support.v4.app.Fragment;
//import android.support.v4.app.FragmentActivity;
//import android.support.v4.view.ViewPager;
//import android.view.View;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.RadioButton;
//import android.widget.RadioGroup;
//import android.widget.TextView;
//
//import com.squareup.picasso.Picasso;
//import com.ttdt.Adaper.ViewPagerFragmentAdapter;
//import com.ttdt.Fragmet.RankFragment;
//import com.ttdt.MusicPlayerA;
//import com.ttdt.R;
//import com.ttdt.Service.MusicPlayerService;
//import com.ttdt.Util.Custom.MainActivityObserver;
//import com.ttdt.Util.Util;
//import com.ttdt.modle.Song;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Observable;
//
//public class MainActivity1 extends FragmentActivity{
//
//    private ViewPager vp_main;
//    private RadioGroup rg_main;
//    private RadioButton rb_rank;
//    private RadioButton rb_local;
//    private RadioButton rb_search;
//
//    private ImageView img_main_show_info,iv_main_play;
//    private ImageView img_main_list;//列表
//    private LinearLayout ill_main_next;
//    private LinearLayout ill_main_previous;
//    private LinearLayout ill_main_play;
//    //歌唱家，名称，当前时间，总时间
//    private TextView tv_main_artist, tv_main_name, tv_main_current_time, tv_main_total;
//
//    private Context context;
//    private ViewPagerFragmentAdapter viewPagerFragmentAdapter = null;
//    private List<Fragment> fragments = new ArrayList<>();
//    //播放器的代理类
//    private MusicPlayerA service;
//
//    public MusicPlayerA getService() {
//        return service;
//    }
//    private int UPDATE_LYRIC = 0;
//    private Handler handler = new Handler(){
//        @Override
//        public void handleMessage(Message msg) {
//            if(msg.what == UPDATE_LYRIC){
//                try {
//                    String time = Util.getTime(service.getCurrentPosition());
//                    tv_main_current_time.setText(time);
//                    sendEmptyMessageDelayed(UPDATE_LYRIC,1000);
//                } catch (RemoteException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    };
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        context = this;
//        initView();
//        bindService();
//        initData();
//        setLister();
//    }
//
//    private void bindService() {
//        Intent intent = new Intent(this, MusicPlayerService.class);
//        bindService(intent, con, Context.BIND_AUTO_CREATE);
//    }
//
//    private ServiceConnection con = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder iBinder) {
//            try {
//                service = MusicPlayerA.Stub.asInterface(iBinder);
//                service.addObservable(observer);
//            } catch (RemoteException e) {
//                e.printStackTrace();
//            }
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//            try {
//                if (service != null) {
//                    service.stop();
//                }
//            } catch (RemoteException e) {
//                e.printStackTrace();
//            }
//        }
//    };
//
//    private void setLister() {
//        ill_main_next.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(service != null){
//                    try {
//                        service.next();
//                    } catch (RemoteException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        });
//        ill_main_play.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(service != null){
//                    try {
//                        if(service.isPlaying()){
//                            service.pause();
//                            iv_main_play.setImageResource(android.R.drawable.ic_media_pause);
//                        }else{
//                            service.start();
//                            iv_main_play.setImageResource(android.R.drawable.ic_media_play);
//                        }
//                    } catch (RemoteException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        });
//        ill_main_previous.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(service != null){
//                    try {
//                        service.pre();
//                    } catch (RemoteException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        });
//    }
//
//    private void initData() {
//        Fragment randFragment = new RankFragment();
//        fragments.add(randFragment);
//        viewPagerFragmentAdapter = new ViewPagerFragmentAdapter(fragments, getSupportFragmentManager());
//        vp_main.setAdapter(viewPagerFragmentAdapter);
//    }
//
//    private void initView() {
//        vp_main = (ViewPager) findViewById(R.id.vp_main);
//        rg_main = (RadioGroup) findViewById(R.id.rg_main);
//        rb_rank = (RadioButton) findViewById(R.id.rb_rank);
//        rb_search = (RadioButton) findViewById(R.id.rb_search);
//        img_main_show_info = (ImageView) findViewById(R.id.img_main_show_info);
//        img_main_list = (ImageView) findViewById(R.id.img_main_list);
//        iv_main_play = (ImageView)findViewById(R.id.iv_main_play);
//        ill_main_next = (LinearLayout) findViewById(R.id.ill_main_next);
//        ill_main_play = (LinearLayout) findViewById(R.id.ill_main_play);
//        ill_main_previous = (LinearLayout)findViewById(R.id.ill_main_previous);
//        tv_main_artist = (TextView) findViewById(R.id.tv_main_artist);
//        tv_main_name = (TextView) findViewById(R.id.tv_main_name);
//        tv_main_current_time = (TextView) findViewById(R.id.tv_main_current_time);
//        tv_main_total = (TextView) findViewById(R.id.tv_main_total);
//    }
//
//    private void setBottomView(Song song) {
//        tv_main_name.setText(song.getName());
//        tv_main_artist.setText(song.getArtist());
//        tv_main_total.setText(Util.getTime(Integer.valueOf(song.getTime())));
//        Picasso.with(context).load(song.getArtistImage()).into(img_main_show_info);
//    }
//
//    private MainActivityObserver observer = new MainActivityObserver(){
//        @Override
//        public void update(Observable observable, Object data) {
//            Song song = (Song)data;
//            setBottomView(song);
//            handler.sendEmptyMessage(UPDATE_LYRIC);
//        }
//    };
//
//}
