package com.ttdt.Activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.ttdt.Adaper.LeftMenuAdapter;
import com.ttdt.Adaper.ViewPagerFragmentAdapter;
import com.ttdt.Fragmet.LocalMusicFragment;
import com.ttdt.Fragmet.MyFragment;
import com.ttdt.Fragmet.RankFragment;
import com.ttdt.Manager.LoginManager;
import com.ttdt.Manager.SongManager;
import com.ttdt.MusicPlayerA;
import com.ttdt.R;
import com.ttdt.Service.MusicPlayerService;
import com.ttdt.Util.Custom.MainActivityObserver;
import com.ttdt.Util.Util;
import com.ttdt.modle.LeftMenu;
import com.ttdt.modle.Song;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;


public class MainActivity extends FragmentActivity {

    private ViewPager vp_main;
    private RadioGroup rg_main;
    private RadioButton rb_rank;
    private RadioButton rb_local;
    private RadioButton rb_my;

    private ListView lv_main_left;
    private List<LeftMenu> leftMenus;

    private Context context;
    private ViewPagerFragmentAdapter viewPagerFragmentAdapter = null;
    private List<Fragment> fragments = new ArrayList<>();
    //播放器的代理类
    private static MusicPlayerA service;
    private ImageView img_main_show_info, iv_main_play;
    private LinearLayout ill_main_next,ill_main_search;
    private LinearLayout ill_main_previous;
    private LinearLayout ill_main_play;
    private DrawerLayout id_drawerLayout;
    //歌唱家，名称，当前时间，总时间
    private TextView tv_main_artist, tv_main_name, tv_main_current_time, tv_main_total,tv_main_current_user;

    public static MusicPlayerA getService() {
        return service;
    }

    private int UPDATE_SING = 0;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == UPDATE_SING) {
                try {
                    String time = Util.getTime(service.getCurrentPosition());
                    tv_main_current_time.setText(time);
                    sendEmptyMessageDelayed(UPDATE_SING, 1000);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public void openAudio(int position) throws RemoteException {
        iv_main_play.setImageResource(R.drawable.playbar_btn_play);
        MainActivity.getService().openAudio(position);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                int grantResult = grantResults[i];
                if (permission.equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        final LocalMusicFragment fragment = (LocalMusicFragment) fragments.get(1);
                        SongManager.getInstance().getLocalMusic(new SongManager.GetSongHD() {
                            @Override
                            public void success(List<Song> onSongArray) {
                                fragment.changData(onSongArray);
                            }
                            @Override
                            public void fail() {

                            }
                        });
                    } else {
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    }
                }
            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        initView();
        bindService();
        initData();
        setLister();
    }

    private void bindService() {
        Intent intent = new Intent(this, MusicPlayerService.class);
        startService(intent);
        bindService(intent, con, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection con = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            try {
                service = MusicPlayerA.Stub.asInterface(iBinder);
                service.addObservable(observer);
            } catch (RemoteException e) {

            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            try {
                if (service != null) {
                    service.stop();
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onDestroy() {
        unbindService(con);
        super.onDestroy();
    }

    private void setLister() {
        id_drawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {
                tv_main_current_user.setText(LoginManager.getUserName());
            }

            @Override
            public void onDrawerClosed(View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
        lv_main_left.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String text = leftMenus.get(position).getText();
                id_drawerLayout.closeDrawer(Gravity.LEFT);
                if(text.equals(getString(R.string.upload_music))){
                    if(LoginManager.getInstance().isLogin()){
                        Intent intent = new Intent(context,SelectSongActivity.class);
                        startActivity(intent);
                    }else{
                        Util.prompting(context,"请先登录");
                    }
                }else if(text.equals(getString(R.string.log_out))){
                    LoginManager.getInstance().logOut();
                }else if(text.equals(getString(R.string.login))){
                    Intent intent = new Intent(context,LoginActivity.class);
                    startActivity(intent);
                }else if(text.equals(getString(R.string.clear_cache))){
                    SongManager.getInstance().clearCache();
                }
            }
        });

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

        ill_main_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context,SearchActivity.class));
            }
        });
        ill_main_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (service != null) {
                    try {
                        service.next();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        ill_main_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (service != null) {
                    try {
                        if (service.isPlaying()) {
                            service.pause();
                            iv_main_play.setImageResource(R.drawable.playbar_btn_pause);
                            handler.removeMessages(UPDATE_SING);
                        } else {
                            service.start();
                            iv_main_play.setImageResource(R.drawable.playbar_btn_play);
                            handler.sendEmptyMessage(UPDATE_SING);
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        ill_main_previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (service != null) {
                    try {
                        service.pre();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        rb_rank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vp_main.setCurrentItem(0, false);
            }
        });

        rb_local.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vp_main.setCurrentItem(1, false);
            }
        });

        rb_my.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vp_main.setCurrentItem(2, false);
            }
        });

        vp_main.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position == 0) {
                    rg_main.check(R.id.rb_rank);
                } else if (position == 1) {
                    rg_main.check(R.id.rb_local);
                } else if (position == 2) {
                    rg_main.check(R.id.rb_my);
                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }
    @Override
    protected void onPause() {
        super.onPause();
        try {
            if (service != null && service.isPlaying()) {
                handler.removeMessages(UPDATE_SING);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (service!= null && service.isPlaying()) {
                setBottomView(service.getName(), service.getArtist(), String.valueOf(service.getDuration()), service.getImageUrl());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    private void setBottomView(String name, String artist, String time, String imageUrl) {
        setTextAll();
        tv_main_name.setText(name);
        tv_main_artist.setText(artist);
        tv_main_total.setText(Util.getTime(Integer.valueOf(time)));
        if(imageUrl == null){
            Picasso.with(context).load(R.drawable.img_album_background).into(img_main_show_info);
        }else{
            Picasso.with(context).load(imageUrl).into(img_main_show_info);
        }
        handler.removeMessages(UPDATE_SING);
        handler.sendEmptyMessage(UPDATE_SING);
    }

    private void setTextAll(){
//        tv_main_name.setText(name);
//        tv_main_artist.setText(artist);
        Util.setTextMarquee(tv_main_artist);
        Util.setTextMarquee(tv_main_name);
    }

    private void initData() {
//        if(Util.isGrantExternalRW(this)){
//            SongManager.getInstance().getLocalMusic(null);
//        }

        Fragment randFragment = new RankFragment();
        fragments.add(randFragment);
        fragments.add(new LocalMusicFragment());
        fragments.add(new MyFragment());
        viewPagerFragmentAdapter = new ViewPagerFragmentAdapter(fragments, getSupportFragmentManager());
        vp_main.setAdapter(viewPagerFragmentAdapter);

        leftMenus = new ArrayList<>();
        leftMenus.add(new LeftMenu(getString(R.string.upload_music),null));
        leftMenus.add(new LeftMenu(getString(R.string.clear_cache),null));
        leftMenus.add(new LeftMenu(getString(R.string.login),null));
        leftMenus.add(new LeftMenu(getString(R.string.log_out),null));
        lv_main_left.setAdapter(new LeftMenuAdapter(leftMenus,context));
    }

    private void initView() {
        vp_main = (ViewPager) findViewById(R.id.vp_main);
        rg_main = (RadioGroup) findViewById(R.id.rg_main);
        rb_rank = (RadioButton) findViewById(R.id.rb_rank);
        rb_my = (RadioButton) findViewById(R.id.rb_my);
        rb_local = (RadioButton) findViewById(R.id.rb_local);
        img_main_show_info = (ImageView) findViewById(R.id.img_main_show_info);
        iv_main_play = (ImageView) findViewById(R.id.iv_main_play);
        ill_main_next = (LinearLayout) findViewById(R.id.ill_main_next);
        ill_main_play = (LinearLayout) findViewById(R.id.ill_main_play);
        ill_main_previous = (LinearLayout) findViewById(R.id.ill_main_previous);
        tv_main_artist = (TextView) findViewById(R.id.tv_main_artist);
        tv_main_name = (TextView) findViewById(R.id.tv_main_name);
        tv_main_current_time = (TextView) findViewById(R.id.tv_main_current_time);
        tv_main_total = (TextView) findViewById(R.id.tv_main_total);
        ill_main_search = (LinearLayout)findViewById(R.id.ill_main_search);
        lv_main_left = (ListView)findViewById(R.id.lv_main_left);
        tv_main_current_user = (TextView)findViewById(R.id.tv_main_current_user);
        id_drawerLayout = (DrawerLayout)findViewById(R.id.id_drawerLayout);
    }

    private void setBottomView(Song song) {
       setBottomView(song.getName(),song.getArtist(),song.getTime(),song.getArtistImage());
    }

    private MainActivityObserver observer = new MainActivityObserver() {
        @Override
        public void update(Observable observable, Object data) {
            Song song = (Song) data;
            setBottomView(song);
            handler.sendEmptyMessage(UPDATE_SING);
        }
    };

}
