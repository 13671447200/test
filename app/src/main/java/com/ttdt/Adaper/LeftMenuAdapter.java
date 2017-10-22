package com.ttdt.Adaper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ttdt.R;
import com.ttdt.modle.LeftMenu;

import java.util.List;

/**
 * Created by Administrator on 2017/10/8.
 */

public class LeftMenuAdapter extends BaseAdapter {

    private List<LeftMenu> leftMenus;
    private Context context;

    public LeftMenuAdapter(List<LeftMenu> leftMenus,Context context){
        this.leftMenus = leftMenus;
        this.context = context;
    }


    @Override
    public int getCount() {
        return leftMenus.size();
    }

    @Override
    public Object getItem(int position) {
        return leftMenus.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.left_menu,parent,false);
            viewHolder = new ViewHolder();
            viewHolder.iv_left_menu = (ImageView) convertView.findViewById(R.id.iv_left_menu);
            viewHolder.tv_left_menu = (TextView) convertView.findViewById(R.id.tv_left_menu);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.tv_left_menu.setText(leftMenus.get(position).getText());
        viewHolder.iv_left_menu.setVisibility(View.GONE);
        return convertView;
    }

    class ViewHolder {
        ImageView iv_left_menu;
        TextView tv_left_menu;
    }

}
