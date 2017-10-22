package com.ttdt.Fragmet;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.ttdt.Activity.MainActivity;
import com.ttdt.Adaper.SongListAdapter;
import com.ttdt.Manager.SongManager;
import com.ttdt.MusicPlayerA;
import com.ttdt.R;
import com.ttdt.Util.Util;
import com.ttdt.modle.Song;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/9/21.
 */

public class LocalMusicFragment extends Fragment {

    MainActivity context = null;
    private LinearLayout ll_my_music_local;
    private ListView list_view;
    private List<Song> songArray = new ArrayList<>();
    private SongListAdapter songListAdapter;
    private View view;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        context = (MainActivity) getActivity();
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(context == null){
            return;
        }
        
        if(isVisibleToUser){
            if(SongManager.getInstance().isUpdateLocal()) {
                getLocatSong();
            }
        }

    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            try {
                if (msg.what == 1) {
                    songListAdapter.notifyDataSetChanged();
                    if (MainActivity.getService() == null) {
                        sendEmptyMessageDelayed(2, 1000);
                    } else {
                        MainActivity.getService().setSongArray(songArray, 0);
                    }
                } else if (msg.what == 2) {
                    if (MainActivity.getService() != null) {
                        MainActivity.getService().setSongArray(songArray, 0);
                    } else {
                        sendEmptyMessageDelayed(2, 1000);
                    }
                }
            } catch (RemoteException e) {

            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (null != view) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.removeView(view);
            }
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.my_music_fragment, null);
            list_view = (ListView) view.findViewById(R.id.list_view);
            songListAdapter = new SongListAdapter(songArray, context);
            list_view.setAdapter(songListAdapter);
            //有可能在分线程执行
            if (Util.isGrantExternalRW(getActivity())) {
                getLocatSong();
            }
            list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                    final MusicPlayerA service = MainActivity.getService();
                    try {
                        if (service == null) {
                            return;
                        }
                        if (service.getID() != 0) {
                            service.setSongArray(songArray, 0);
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        context.openAudio(position);
                                    } catch (RemoteException e) {
                                        e.printStackTrace();
                                    }
                                }
                            },1000);
                        } else if (service.isOK()) {
                            context.openAudio(position);
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        return view;
    }

    private void getLocatSong() {
        SongManager.getInstance().getLocalMusic(new SongManager.GetSongHD() {
            @Override
            public void success(List<Song> onSongArray) {
                changData(onSongArray);
            }

            @Override
            public void fail() {

            }
        });
    }

    public void changData(List<Song> onSongArray) {
        songArray.clear();
        songArray.addAll(onSongArray);
        handler.sendEmptyMessage(1);
    }
}
