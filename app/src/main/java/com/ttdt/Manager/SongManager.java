package com.ttdt.Manager;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.ttdt.MyApplication;
import com.ttdt.Util.AES;
import com.ttdt.Util.Cons;
import com.ttdt.Util.Custom.MyJsonRequest;
import com.ttdt.Util.DownloadUtil;
import com.ttdt.Util.FileUtil;
import com.ttdt.Util.MapUtils;
import com.ttdt.Util.Util;
import com.ttdt.greendao.PlayListDao;
import com.ttdt.modle.PlayList;
import com.ttdt.modle.Song;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

import static com.ttdt.Util.Util.requestQueue;
import static java.lang.String.valueOf;

/**
 * Created by Administrator on 2017/9/21.
 */

public class SongManager {

    private Context context;

    private boolean isUpdateLocal = true;
    private ContentResolver resolver;
    public void setUpdateLocal(boolean updateLocal) {
        isUpdateLocal = updateLocal;
    }

    public boolean isUpdateLocal() {
        return isUpdateLocal;
    }

    private List<Song> localMusic = null;
    private OkHttpClient mOkHttpClient;
    private SongManager() {
        mOkHttpClient = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build();
    }

    private SongManager(Context context) {
        mOkHttpClient = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build();
        this.context = context;
        resolver = context.getContentResolver();
    }


    public static SongManager getInstance() {
        return SongManagerHolder.getInstance();
    }

    static class SongManagerHolder {
        static SongManager instance = null;

        public static SongManager getInstance() {
            if (instance == null) {
                instance = new SongManager(MyApplication.getInstance());
            }
            return instance;
        }
    }



    public void uploadSong(Song song, final GetJsonHD getJsonHD) {
        File file = new File(song.getUrl());
        if (file.exists() && file.isFile()) {
            Map<Object, Object> map = MapUtils.java2Map(song);
            uploadSong(file, map, getJsonHD);
        }
    }

    public void uploadSong(File file, final Map<Object, Object> map, final GetJsonHD getJsonHD) {
//        mOkHttpClient = new OkHttpClient
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        if (file != null) {
            RequestBody body = RequestBody.create(MediaType.parse("mp3/*"), file);
            builder.addFormDataPart("songFile", file.getName(), body);
            if (map != null) {
                // map 里面是请求中所需要的 key 和 value
                for (Map.Entry entry : map.entrySet()) {
                    builder.addFormDataPart(valueOf(entry.getKey()), valueOf(entry.getValue()));
                }
            }
            builder.addFormDataPart("userId", valueOf(LoginManager.getUserId()));
            String url = Cons.baseUrl_my + "addUploadSong";
            final okhttp3.Request request = new okhttp3.Request.Builder().url(url).post(builder.build()).build();
            mOkHttpClient.newBuilder().build().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if (getJsonHD != null) {
                        getJsonHD.fail();
                    }
                }

                @Override
                public void onResponse(Call call, okhttp3.Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String str = response.body().string();
                        if (str != null && !str.equals("")) {
                            try {
                                JSONObject jsonObject = new JSONObject(str);
                                getJsonHD.success(jsonObject);
                            } catch (JSONException e) {
                                getJsonHD.fail();
                                e.printStackTrace();
                            }
                        } else {
                            getJsonHD.fail();
                        }
                    } else {
                        getJsonHD.fail();
                    }
                }
            });
        }

    }

    public List<PlayList> getPlayList() {
        PlayListDao playListDao = MyApplication.getDaoSession().getPlayListDao();
        final List<PlayList> playLists = playListDao.queryBuilder()
                .where(PlayListDao.Properties.UserId.eq(LoginManager.getUserId()))
                .list();
        return playLists;
    }

    public void getSongArrayByUploadIds(long uploadIds, final GetSongHD getSongHD) {

        String url = new StringBuilder().append(Cons.baseUrl_my)
                .append("getUploadSongByUserId?userId=")
                .append(uploadIds).toString();
        getSongByUrl(getSongHD, url);

    }

    public void clearCache(){
        Util.prompting(context,"开始清除");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    FileUtil.delFolder(Cons.downMusicDirCache,false);
                    MyApplication.getInstance().showToast("清除成功");
                }catch (Exception e){
                    MyApplication.getInstance().showToast("清除失败");
                }
            }
        }).start();
    }

    public void getSongArrayByCollectId(long collectId, final GetSongHD getSongHD) {
        String url = new StringBuilder().append(Cons.baseUrl_my)
                .append("getCollectSongByUserId?userId=")
                .append(collectId).toString();
        getSongByUrl(getSongHD, url);
    }

    public void getPlayListByID(long playListID, final GetSongHD getSongHD) {

        String url = new StringBuilder().append(Cons.baseUrl_my)
                .append("getPlayListByID?playListID=")
                .append(playListID).toString();
        getSongByUrl(getSongHD, url);
    }


    public void downSong(String url,String name){
        downSong(url,name,null);
       // DownloadUtil.get().download(context,url,Cons.downMusicDir,name,null);
    }

    public void downSong(final Song song,String save){
        downSong(song,save,true);
    }

    public void downSong(final Song song,boolean isAddContent){
        downSong(song,Cons.downMusicDir,isAddContent);
    }

    public void downSong(final Song song){
        downSong(song,Cons.downMusicDir,true);
    }


    public void downSong(final Song song, final String save , final boolean isAddContent){
        downSong(song.getUrl(), song.getName()+".mp3",save, new DownloadUtil.OnDownloadListener() {
            @Override
            public void onDownloadSuccess() {
                if(isAddContent){
                    song.setUrl(save + song.getName()+".mp3");
                    addContentResolver(song);
                }
                Util.prompting(context,"下载" + song.getName() + "成功");
            }

            @Override
            public void onDownloading(int progress) {

            }

            @Override
            public void onDownloadFailed() {
                Util.prompting(context,"下载" + song.getName() + "失败");
            }
        });
        // DownloadUtil.get().download(context,url,Cons.downMusicDir,name,null);
    }

    public void downSong(String url,String name,String save,DownloadUtil.OnDownloadListener listener){
        DownloadUtil.get().download(url,save,name,listener);
    }

    public void downSong(String url,String name,DownloadUtil.OnDownloadListener listener){
        DownloadUtil.get().download(url,Cons.downMusicDir,name,listener);
    }

    public boolean localIsHasSong(String name){
        File file = new File(Cons.downMusicDir+name+".mp3");
        return file.exists();
    }

    public String localIsHasSongReturnUrl(String name){
        File file = new File(Cons.downMusicDirCache+name+".mp3");
        if(file.exists()){
            return file.getAbsolutePath();
        }else {
            return  null;
        }
    }

    public void getSongUrl(final int wxId,final GetSongUrlHD getSongUrlHD) {
        try {
            String params = AES.getParams(wxId);
//            JSONObject paramsJson = new JSONObject();
//            paramsJson.put("params", params[0]);
//            paramsJson.put("encSecKey", params[1]);
            final MyJsonRequest jor = new MyJsonRequest(Request.Method.POST,Cons.baseUrl + "weapi/song/enhance/player/url?csrf_token="
                    , params,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject jsonObject) {
                            try {
                                if(jsonObject.has("data")){
                                    JSONArray data = jsonObject.getJSONArray("data");
                                    if(data.length() > 0){
                                        JSONObject json = data.getJSONObject(0);
                                        if (json.has("url")) {
                                            String url = json.getString("url");
                                            if(url != null && !url.trim().equals("")){
                                                  getSongUrlHD.success(url);
//                                                Message message = Message.obtain();
//                                                message.what = OPEN_AUDIO;
//                                                message.obj = url;
//                                                Bundle bundle = new Bundle();
//                                                bundle.putParcelable("song",song);
//                                                message.setData(bundle);
//                                                handler.sendMessage(message);
                                            }
                                        }
                                    }
                                }else{
                                    getSongUrlHD.fail();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                getSongUrlHD.fail();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    volleyError.printStackTrace();
                    getSongUrlHD.fail();
                }
            });
            requestQueue.add(jor);
        } catch (Exception e) {

        }
    }

    private void getSongByUrl(final GetSongHD getSongHD, String url) {
        MyJsonRequest mjr = new MyJsonRequest(Request.Method.GET, url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                parseSongJSON(jsonObject, getSongHD);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                volleyError.printStackTrace();
                getSongHD.fail();
            }
        });
        Util.requestQueue.add(mjr);
    }

    public void parseSongJSON(JSONObject jsonObject, GetSongHD getJsonHD) {
        try {
            if (jsonObject.has("code")) {
                int code = jsonObject.getInt("code");
                if (code == 200) {
                    JSONArray content = jsonObject.getJSONArray("content");
                    List<Song> songArray = new ArrayList<>();
                    for (int i = 0; i < content.length(); i++) {
                        Song song = new Song();
                        JSONObject songJson = content.getJSONObject(i);
                        if (songJson.has("id"))
                            song.setId(songJson.getInt("id"));
                        if (songJson.has("wxId"))
                            song.setWyID(songJson.getInt("wxId"));
                        if (songJson.has("url"))
                            song.setUrl(songJson.getString("url"));
                        if (songJson.has("albumName"))
                            song.setAlbumName(songJson.getString("albumName"));
                        if (songJson.has("artistImage"))
                            song.setArtistImage(songJson.getString("artistImage"));
                        if (songJson.has("name"))
                            song.setName(songJson.getString("name"));
                        if (songJson.has("artist"))
                            song.setArtist(songJson.getString("artist"));
                        if (songJson.has("time"))
                            song.setTime(songJson.getString("time"));
                        songArray.add(song);
                    }
                    getJsonHD.success(songArray);
                    return;
                }
            }
            getJsonHD.fail();
        } catch (Exception e) {
            getJsonHD.fail();
            e.printStackTrace();
        }
    }

    private void addContentResolver(Song song) {
        try {
            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            ContentValues values = new ContentValues();
            values.put(MediaStore.Audio.Media.DURATION, song.getTime());
            values.put(MediaStore.Audio.Media.DISPLAY_NAME, song.getName());
            values.put(MediaStore.Audio.Media.DATA, song.getUrl());
            values.put(MediaStore.Audio.Media.ARTIST, song.getArtist());
            values.put(MediaStore.Audio.Media.ALBUM, song.getAlbumName());
            resolver.insert(uri, values);
            //下次重新获取本地音乐列表
            setUpdateLocal(true);
        }catch (Exception e){
            MyApplication.getInstance().showToast(e.getMessage() + "增加音乐时");
        }
    }

    public void getLocalMusic(final GetSongHD getSongHD) {

        if (localMusic != null && getSongHD != null && !isUpdateLocal) {
            getSongHD.success(localMusic);
            return;
        }
        isUpdateLocal = false;
        new Thread() {
            @Override
            public void run() {
                super.run();

                localMusic = new ArrayList<>();
                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                String[] objs = {
                        MediaStore.Audio.Media.DISPLAY_NAME,//视频文件在sdcard的名称
                        MediaStore.Audio.Media.DURATION,//视频总时长
                        MediaStore.Audio.Media.DATA,//视频的绝对地址
                        MediaStore.Audio.Media.ARTIST,//歌曲的演唱者
                        MediaStore.Audio.Media.ALBUM,
                        MediaStore.Audio.Media.ALBUM_KEY
                };
                Cursor cursor = resolver.query(uri, objs, null, null, null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {

                        Song song = new Song();

                        localMusic.add(song);//写在上面

                        String name = cursor.getString(0);//视频的名称
                        song.setName(name);

                        long duration = cursor.getLong(1);//视频的时长
                        song.setTime(valueOf(duration));

                        String data = cursor.getString(2);//视频的播放地址
                        song.setUrl(data);

                        String artist = cursor.getString(3);//艺术家
                        song.setArtist(artist);

                        String album = cursor.getString(4);//专辑
                        song.setAlbumName(album);
//
//                        String image = cursor.getColumnName(5);
//                        song.setArtistImage(image);

                        song.setLocal(true);
                    }
                    cursor.close();
                }
                if (getSongHD != null) {
                    getSongHD.success(localMusic);
                }
            }
        }.start();
    }

    public void rankDataByNet(int id, final GetSongHD getSongHD) {
        try {
            //Cons.baseUrl + "api/playlist/detail?" + id + ""
            String url = new StringBuilder()
                    .append(Cons.baseUrl)
                    .append("api/playlist/detail?")
                    .append("id=").append(id).toString();
            MyJsonRequest jor = new MyJsonRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject jsonObject) {
                            List<Song> songArray = getSongArrayByWy(jsonObject);
//                            getSongUrl(songArray);
                            getSongHD.success(songArray);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    getSongHD.fail();
                    //  Util.prompting(context, "请求数据失败" + volleyError.getMessage());
                }
            });
            Util.requestQueue.add(jor);
        } catch (Exception e) {
            getSongHD.fail();
            //Util.prompting(context, "请求数据失败，请检查网络是否正常！");
        }
    }

    private List<Song> getSongArrayByWy(JSONObject jsonObject) {
        List<Song> songArray = null;
        try {
            if (jsonObject.has("result")) {
                JSONObject result = jsonObject.getJSONObject("result");
                if (result.has("tracks")) {
                    JSONArray tracks = result.getJSONArray("tracks");
                    songArray = parseJsonSong(tracks);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return songArray;

    }

    public List<Song> parseJsonSong(JSONArray tracks) throws JSONException {
        List<Song> songArray = new ArrayList<>();
        for (int i = 0; i < tracks.length(); i++) {
            JSONObject obj = tracks.getJSONObject(i);
            if (obj.has("id")) {
                Song song = new Song();
                //音乐id
                int id = obj.getInt("id");
                song.setWyID(id);
                //时间
                if (obj.has("duration")) {
                    String duration = obj.getString("duration");
                    song.setTime(duration);
                }
                //歌曲名字
                if (obj.has("name")) {
                    String name = obj.getString("name");
                    song.setName(name);
                }
                //歌唱者
                if (obj.has("artists")) {
                    JSONArray artists = obj.getJSONArray("artists");
                    if (artists.length() > 0) {
                        JSONObject jsonAr = artists.getJSONObject(0);
                        if (jsonAr.has("name")) {
                            song.setArtist(jsonAr.getString("name"));
                        }
//                                    if (jsonAr.has("img1v1Url")) {
//                                        song.setArtistImage(jsonAr.getString("img1v1Url"));
//                                    }
                    }
                }
                //图片 专辑名字
                if (obj.has("album")) {
                    JSONObject album = obj.getJSONObject("album");
                    if (album.has("blurPicUrl")) {
                        song.setArtistImage(album.getString("blurPicUrl"));
                    }
                    if (album.has("name")) {
                        song.setAlbumName(album.getString("name"));
                    }
                }
                songArray.add(song);
            }
        }
        return songArray;
    }

    public interface GetJsonHD {
        void success(JSONObject jsonObject);

        void fail();
    }

    public interface GetSongHD {
        void success(List<Song> songArray);

        void fail();
    }

    public interface GetSongUrlHD {
        void success(String url);

        void fail();
    }

}
