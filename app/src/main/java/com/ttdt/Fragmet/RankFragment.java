package com.ttdt.Fragmet;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.ttdt.Activity.MainActivity;
import com.ttdt.Activity.PlayListActivity;
import com.ttdt.Adaper.RandAdapter;
import com.ttdt.R;
import com.ttdt.modle.Rank;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/9/19.
 */

public class RankFragment extends Fragment {
    private MainActivity context;
    private GridView grid_view;
    List<Rank> rankArray = null;
    private RandAdapter randAdapter = null;
    private View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(view != null){
            ViewGroup parent = (ViewGroup) view.getParent();
            if(null != parent){
                parent.removeView(view);
            }
        }else{
            context = (MainActivity) getActivity();
            view = LayoutInflater.from(context).inflate(R.layout.rand_fragment, null);
            grid_view = (GridView) view.findViewById(R.id.grid_view);
            initData();
            setLister();
        }
        return view;
    }

    private void setLister() {
        grid_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(MainActivity.getService() != null){
                    Intent intent = new Intent(context, PlayListActivity.class);
                    Rank rank = rankArray.get(position);
                    intent.putExtra("name",rank.getName());
                    intent.putExtra("id",rank.getWxId());
                    context.startActivity(intent);
                }
            }
        });
    }

    private void initData() {
        rankArray = new ArrayList<>();
        rankArray.add(new Rank("新歌榜", null, 3779629));
        rankArray.add(new Rank("热歌榜", null, 3778678));
        rankArray.add(new Rank("原创歌曲榜", null, 2884035));
        rankArray.add(new Rank("飙升榜", null, 19723756));
        rankArray.add(new Rank("电音榜", null, 10520166));
        rankArray.add(new Rank("UK排行榜周榜", null, 180106));
        rankArray.add(new Rank("美国Billboard周榜", null, 60198));
        rankArray.add(new Rank("KTV嗨榜", null, 21845217));
        rankArray.add(new Rank("iTunes榜", null, 11641012));
        rankArray.add(new Rank("Hit FM Top榜", null, 120001));
        rankArray.add(new Rank("日本Oricon周榜",null,60131));
        rankArray.add(new Rank("韩国Melon排行榜周榜",null,3733003));
        rankArray.add(new Rank("中国TOP排行榜(港台榜)",null,112504));
        rankArray.add(new Rank("中国TOP排行榜(内地榜)",null,64016));
        rankArray.add(new Rank("香港电台中文歌曲龙虎榜",null,10169002));
        rankArray.add(new Rank("华语金曲榜",null,4395559));
        rankArray.add(new Rank("中国嘻哈榜",null,1899724));
        rankArray.add(new Rank("法国 NRJ EuroHot 30周榜",null,27135204));
        rankArray.add(new Rank("台湾Hito排行榜",null,112463));
        rankArray.add(new Rank("Beatport全球电子舞曲榜",null,3812895));
        randAdapter = new RandAdapter(rankArray, context);
        grid_view.setAdapter(randAdapter);
    }


}
