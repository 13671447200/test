package com.ttdt.Fragmet;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.ttdt.Activity.PlayListActivity;
import com.ttdt.Adaper.PopWindowPlayListAdapter;
import com.ttdt.Manager.LoginManager;
import com.ttdt.Manager.SongManager;
import com.ttdt.R;
import com.ttdt.Util.Util;
import com.ttdt.modle.PlayList;

import java.util.List;

/**
 * Created by Administrator on 2017/9/21.
 */

public class MyFragment extends Fragment {

    Context context = null;
    private ImageView iv_my_fragment_upload;
    private ImageView iv_my_fragment_like;
    private ImageView iv_my_fragment_play_list;
    private LinearLayout ll_my_upload, ll_my_music_play_list;
    private LinearLayout ll_my_music_like;

    private ListView lv_my_fragment_play_list;

    private boolean uploadIsOpen = false;
    private boolean likeIsOpen = false;
    private boolean playListOpen = false;
    private List<PlayList> playLists;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(context).inflate(R.layout.my_fragment, null);
        initView(view);
        initLister();
        initData();
        return view;
    }

    private void initData() {
        playLists = SongManager.getInstance().getPlayList();
        lv_my_fragment_play_list.setAdapter(new PopWindowPlayListAdapter(context, playLists));
    }

    private void initLister() {
        lv_my_fragment_play_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (playLists != null) {
                    PlayList playList = playLists.get(position);
                    Intent intent = new Intent(context, PlayListActivity.class);
                    intent.putExtra("name", playList.getPlayListName());
                    intent.putExtra("playListId", playList.getId());
                    startActivity(intent);
                }
            }
        });
        ll_my_music_play_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playListOpen) {
                    playListOpen = false;
                    closeRotate(iv_my_fragment_play_list);
                    lv_my_fragment_play_list.setVisibility(View.GONE);
                } else {
                    playListOpen = true;
                    openRotate(iv_my_fragment_play_list);
                    lv_my_fragment_play_list.setVisibility(View.VISIBLE);
                }
            }
        });
        ll_my_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (LoginManager.getInstance().isLogin()) {
                    Intent intent = new Intent(context, PlayListActivity.class);
                    intent.putExtra("name", "上传的音乐");
                    intent.putExtra("uploadId", LoginManager.getUserId());
                    startActivity(intent);
                }else{
                    Util.prompting(context,"请先登录！");
                }
            }
        });
        ll_my_music_like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (LoginManager.getInstance().isLogin()) {
                    Intent intent = new Intent(context, PlayListActivity.class);
                    intent.putExtra("name", "收藏的音乐");
                    intent.putExtra("collectId", LoginManager.getUserId());
                    startActivity(intent);
                }else{
                    Util.prompting(context,"请先登录！");
                }
            }
        });
    }

    private void initView(View view) {
        iv_my_fragment_like = (ImageView) view.findViewById(R.id.iv_my_fragment_like);
        iv_my_fragment_upload = (ImageView) view.findViewById(R.id.iv_my_fragment_upload);
        iv_my_fragment_play_list = (ImageView) view.findViewById(R.id.iv_my_fragment_play_list);
        ll_my_upload = (LinearLayout) view.findViewById(R.id.ll_my_upload);
        ll_my_music_like = (LinearLayout) view.findViewById(R.id.ll_my_music_like);
        ll_my_music_play_list = (LinearLayout) view.findViewById(R.id.ll_my_music_play_list);
        lv_my_fragment_play_list = (ListView) view.findViewById(R.id.lv_my_fragment_play_list);
    }

    private void closeRotate(View v) {
        rotate(v, 90f, 0f);
    }

    private void openRotate(View v) {
        rotate(v, 0f, 90f);
    }

    private void rotate(View v, float start, float end) {
        ObjectAnimator anim = ObjectAnimator.ofFloat(v, "rotation", start, end);
        anim.setDuration(100);
        anim.start();
    }

}
