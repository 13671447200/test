package com.ttdt.Adaper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ttdt.R;
import com.ttdt.modle.PlayList;

import java.util.List;

/**
 * Created by Administrator on 2017/10/9.
 */

public class PopWindowPlayListAdapter extends BaseAdapter {

    private List<PlayList> playLists;
    private Context context;
    public PopWindowPlayListAdapter(Context context, List<PlayList> playLists){
        this.playLists = playLists;
        this.context = context;
    }

    @Override
    public int getCount() {
        return playLists.size();
    }

    @Override
    public Object getItem(int position) {
        return playLists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if(convertView == null){
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.pop_play_list_item, null);
            viewHolder.tv_play_list_item_name = (TextView) convertView.findViewById(R.id.tv_play_list_item_name);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.tv_play_list_item_name.setText(playLists.get(position).getPlayListName());
        return convertView;
    }

    class ViewHolder {
        TextView tv_play_list_item_name;
    }

}
