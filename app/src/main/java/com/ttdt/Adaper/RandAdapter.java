package com.ttdt.Adaper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.ttdt.R;
import com.ttdt.modle.Rank;

import java.util.List;

/**
 * Created by Administrator on 2017/9/19.
 */

public class RandAdapter extends BaseAdapter {

    private Context context;
    private List<Rank> rankArray = null;
    public RandAdapter(List<Rank> rankArray, Context context){
        this.rankArray = rankArray;
        this.context = context;
    }

    @Override
    public int getCount() {
        return rankArray.size();
    }

    @Override
    public Object getItem(int position) {
        return rankArray.get(position);
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
            convertView = LayoutInflater.from(context).inflate(R.layout.rank_item,null);
            viewHolder.item_img = (ImageView) convertView.findViewById(R.id.item_img);
            viewHolder.item_text = (TextView) convertView.findViewById(R.id.item_text);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder)convertView.getTag();
        }
        Rank rank = rankArray.get(position);
        viewHolder.item_text.setText(rank.getName());
        if(rank.getImageId() != null){
            Picasso.with(context).load(rank.getImageId()).into(viewHolder.item_img);
        }else{
            viewHolder.item_img.setVisibility(View.GONE);
        }
        return convertView;
    }
    class ViewHolder {
        ImageView item_img;
        TextView item_text;
    }

}
