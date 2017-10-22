package com.ttdt.Adaper;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ttdt.R;
import com.ttdt.modle.Song;

import java.util.List;

/**
 * Created by Administrator on 2017/10/14.
 */

public class SelectSongAdapter extends BaseAdapter {

    private List<Song> songArray;
    private Context context;

    private SelectSongIV selectSongIV;

    public void setSelectSongIV(SelectSongIV selectSongIV) {
        this.selectSongIV = selectSongIV;
    }

    public SelectSongAdapter(List<Song> songArray, Context context){
        this.songArray = songArray;
        this.context = context;
    }

    public SelectSongAdapter(List<Song> songArray, Context context,SelectSongIV selectSongIV){
        this.songArray = songArray;
        this.context = context;
        this.selectSongIV = selectSongIV;
    }

    @Override
    public int getCount() {
        return songArray.size();
    }

    @Override
    public Object getItem(int position) {
        return songArray.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        SelectSongHolder holder = null;
        if(convertView == null){
            holder = new SelectSongHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.select_song_item, null);
            holder.iv_select_song = (ImageView) convertView.findViewById(R.id.iv_select_song);
            holder.ll_select_song = (LinearLayout) convertView.findViewById(R.id.ll_select_song);
            holder.tv_select_song_name = (TextView) convertView.findViewById(R.id.tv_select_song_name);
            holder.iv_select_upload = (ImageView) convertView.findViewById(R.id.iv_select_upload);
            holder.ll_select_upload = (LinearLayout) convertView.findViewById(R.id.ll_select_upload);
            convertView.setTag(holder);
        }else{
            holder = (SelectSongHolder) convertView.getTag();
        }
        final Song song = songArray.get(position);
        if(song.isUploading()){
            holder.ll_select_upload.setVisibility(View.VISIBLE);
            holder.ll_select_song.setVisibility(View.GONE);
            ObjectAnimator anim = ObjectAnimator.ofFloat(holder.iv_select_upload, "rotation", 0f, 359f);
            anim.setDuration(500);
            anim.setRepeatCount(-1);
            anim.start();
            holder.iv_select_upload.setTag(anim);
        }else{
            holder.ll_select_upload.setVisibility(View.GONE);
            holder.ll_select_song.setVisibility(View.VISIBLE);
            Object tag = holder.iv_select_upload.getTag();
            if(tag != null){
                ObjectAnimator anim = (ObjectAnimator) tag;
                anim.cancel();
                holder.iv_select_upload.setTag(null);
            }
        }


        holder.tv_select_song_name.setText(song.getName());
        if(song.isSelect()){
            holder.iv_select_song.setImageResource(R.drawable.pictures_selected);
        }else{
            holder.iv_select_song.setImageResource(R.drawable.picture_unselected);
        }
//        holder.ll_select_song.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                song.setSelect(!song.isSelect());
//                notifyDataSetChanged();
////                if(selectSongIV != null){
////                    selectSongIV.onClick(position);
////                }
//            }
//        });

        return convertView;
    }

    class SelectSongHolder{
        LinearLayout ll_select_song;
        ImageView iv_select_song;
        TextView tv_select_song_name;
        ImageView iv_select_upload;
        LinearLayout ll_select_upload;
    }

    interface SelectSongIV{

        void onClick(int position);

    }

}
