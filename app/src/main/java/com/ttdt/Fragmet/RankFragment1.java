//package com.ttdt.Fragmet;
//
//import android.os.Bundle;
//import android.os.RemoteException;
//import android.support.annotation.Nullable;
//import android.support.v4.app.Fragment;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.AdapterView;
//import android.widget.ListView;
//
//import com.android.volley.Request;
//import com.android.volley.RequestQueue;
//import com.android.volley.Response;
//import com.android.volley.VolleyError;
//import com.ttdt.Activity.MainActivity;
//import com.ttdt.Adaper.RandAdapter;
//import com.ttdt.Adaper.SongListAdapter;
//import com.ttdt.MusicPlayerA;
//import com.ttdt.R;
//import com.ttdt.Util.Cons;
//import com.ttdt.Util.Custom.MyJsonRequest;
//import com.ttdt.Util.Util;
//import com.ttdt.modle.Song;
//
//import org.json.JSONArray;
//import org.json.JSONObject;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * Created by Administrator on 2017/9/19.
// */
//
//public class RankFragment1 extends Fragment {
//
//    private MainActivity context;
//    private ListView list_view;
//    List<Song> arraySong = new ArrayList<Song>();
//    private RequestQueue requestQueue;
//    private RandAdapter randAdapter = null;
//    MusicPlayerA service = null;
//    @Nullable
//    @Override
//    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        context = (MainActivity) getActivity();
//        View view = LayoutInflater.from(context).inflate(R.layout.rand_fragment, null);
//        list_view = (ListView) view.findViewById(R.id.list_view);
//        initData();
//        rankDataByNet(3779629);
//        setLister();
//        return view;
//    }
//
//    private void setLister() {
//        list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                try {
//                    if(service == null){
//                        service = context.getService();
//                    }
//                    service.setSongArray(arraySong);
//                    service.openAudio(position);
//                } catch (RemoteException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//    }
//
//    private void initData() {
//
//        requestQueue = Util.requestQueue;
//        randAdapter = new SongListAdapter(arraySong, context);
//        list_view.setAdapter(randAdapter);
//        service = context.getService();
//    }
//
//    private void rankDataByNet(int id) {
//        try {
//            //Cons.baseUrl + "api/playlist/detail?" + id + ""
//            String url = new StringBuilder()
//                    .append(Cons.baseUrl)
//                    .append("api/playlist/detail?")
//                    .append("id=").append(id).toString();
//            MyJsonRequest jor = new MyJsonRequest(Request.Method.GET, url, null,
//                    new Response.Listener<JSONObject>() {
//                        @Override
//                        public void onResponse(JSONObject jsonObject) {
//                            List<Song> songArray = getSongArrayByWy(jsonObject);
////                            getSongUrl(songArray);
//                            setDataAndInform(songArray);
//                        }
//                    }, new Response.ErrorListener() {
//                @Override
//                public void onErrorResponse(VolleyError volleyError) {
//                    Util.prompting(context, "请求数据失败" + volleyError.getMessage());
//                }
//            });
//
//            requestQueue.add(jor);
//        } catch (Exception e) {
//            Util.prompting(context, "请求数据失败，请检查网络是否正常！");
//        }
//    }
//
//
//
//    private List<Song> getSongArrayByWy(JSONObject jsonObject) {
//        List<Song> songArray = new ArrayList<>();
//        try {
//            if (jsonObject.has("result")) {
//                JSONObject result = jsonObject.getJSONObject("result");
//                if (result.has("tracks")) {
//                    JSONArray tracks = result.getJSONArray("tracks");
//                    for (int i = 0; i < tracks.length(); i++) {
//                        JSONObject obj = tracks.getJSONObject(i);
//                        if (obj.has("id")) {
//                            Song song = new Song();
//                            //音乐id
//                            int id = obj.getInt("id");
//                            song.setWyID(id);
//                            //时间
//                            if (obj.has("duration")) {
//                                String duration = obj.getString("duration");
//                                song.setTime(duration);
//                            }
//                            //歌曲名字
//                            if (obj.has("name")) {
//                                String name = obj.getString("name");
//                                song.setName(name);
//                            }
//                            //歌唱者
//                            if (obj.has("artists")) {
//                                JSONArray artists = obj.getJSONArray("artists");
//                                if (artists.length() > 0) {
//                                    JSONObject jsonAr = artists.getJSONObject(0);
//                                    if (jsonAr.has("name")) {
//                                        song.setArtist(jsonAr.getString("name"));
//                                    }
////                                    if (jsonAr.has("img1v1Url")) {
////                                        song.setArtistImage(jsonAr.getString("img1v1Url"));
////                                    }
//                                }
//                            }
//                            //图片
//                            if(obj.has("album")){
//                                JSONObject album = obj.getJSONObject("album");
//                                if(album.has("blurPicUrl")){
//                                    song.setArtistImage(album.getString("blurPicUrl"));
//                                }
//                            }
//                            songArray.add(song);
//                        }
//                    }
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return songArray;
//
//    }
//
//    private synchronized void setDataAndInform(List<Song> songArray) {
//        arraySong.addAll(songArray);
//        randAdapter.notifyDataSetChanged();
//    }
//
//}
