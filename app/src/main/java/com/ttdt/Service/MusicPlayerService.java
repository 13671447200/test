package com.ttdt.Service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.ttdt.Activity.ConcretePlayMusicActivity;
import com.ttdt.Manager.SongManager;
import com.ttdt.MusicPlayerA;
import com.ttdt.R;
import com.ttdt.Util.Cons;
import com.ttdt.Util.Custom.MainActivityObserver;
import com.ttdt.Util.Util;
import com.ttdt.modle.Song;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Queue;
import java.util.Random;

public class MusicPlayerService extends Service {

    private MediaPlayer mediaPlayer;
    private List<Song> songArray = null;
    private int id = -100;
    private int position = 0;
    private int wyID = 0;

    private SongManager songManager;

    public static int RANDOM = 1;
    public static int ORDER = 2;//列表循环
    public static int SINGLE = 3;

    public int MODE = ORDER;
    NotificationManager manager;

    public Song currentSong = new Song();

    private Queue<Song> queue = new ArrayDeque<>();

    //观察者
    private MyObservable observable;
//    private Handler handler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            int what = msg.what;
//            if(what == OPEN_AUDIO){//这个是已经最后一层的openAudi
//                Bundle data = msg.getData();
//                Song song = (Song) data.get("song");
//                openAudio((String) msg.obj,song);
//            }
//        }
//    };

    //添加观察者
    public void addObservable(Observer observer) {
        observable.addObserver(observer);
    }

    @Override
    public void onCreate() {
        songManager = SongManager.getInstance();
        observable = new MyObservable();
    }

    public void setSongArray(List<Song> songArray, int id) {
        this.id = id;
        this.songArray = songArray;
        queue.clear();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    private MusicPlayerA.Stub iBinder = new MusicPlayerA.Stub() {
        MusicPlayerService service = MusicPlayerService.this;

        @Override
        public void setSongArray(List<Song> songArray, int id) throws RemoteException {
            service.setSongArray(songArray, id);
        }

        @Override
        public void openAudio(int position) throws RemoteException {
            service.openAudio(position);
        }

        @Override
        public int getSongID() throws RemoteException {
            return wyID;
        }

        @Override
        public String getAlbum() throws RemoteException {
            return currentSong.getAlbumName();
        }

        @Override
        public void addObservable(MainActivityObserver myObserver) throws RemoteException {
            service.addObservable(myObserver);
        }

        @Override
        public boolean isOK() throws RemoteException {
            if (songArray == null || songArray.size() < 1) {
                return false;
            }
            return true;
        }

        @Override
        public void start() throws RemoteException {
            service.start();
        }

        @Override
        public void pause() throws RemoteException {
            service.pause();
        }

        @Override
        public void stop() throws RemoteException {
            service.stop();
        }

        @Override
        public int getCurrentPosition() throws RemoteException {
            if (mediaPlayer != null) {
                return mediaPlayer.getCurrentPosition();
            } else {
                return 0;
            }
        }


        @Override
        public int getID() throws RemoteException {
            return id;
        }

        @Override
        public String getDuration() throws RemoteException {
            if (currentSong != null) {
                return currentSong.getTime();
            }
            return "0";
        }

        @Override
        public String getArtist() throws RemoteException {
            if (songArray != null && songArray.size() > position) {
                return currentSong.getArtist();
            }
            return null;
        }

        @Override
        public String getImageUrl() throws RemoteException {
            if (currentSong != null) {
                return currentSong.getArtistImage();
            }
            return null;
        }

        @Override
        public String getName() throws RemoteException {
            if (currentSong != null) {
                return currentSong.getName();
            }
            return null;
        }

        @Override
        public String getAudioPath() throws RemoteException {
            if (currentSong != null) {
                return currentSong.getUrl();
            }
            return null;
        }

        @Override
        public void next() throws RemoteException {
            service.next();
        }

        @Override
        public void pre() throws RemoteException {
            service.pre();
        }

        @Override
        public void setPlayMode(int playmode) throws RemoteException {
            if (playmode != RANDOM && playmode != ORDER && playmode != SINGLE) {
                if (MODE == RANDOM || MODE == ORDER) {
                    MODE++;
                } else if (MODE == SINGLE) {
                    MODE = RANDOM;
                }
            } else {
                MODE = playmode;
            }
        }

        @Override
        public int getPlayMode() throws RemoteException {
            return MODE;
        }

        @Override
        public boolean isPlaying() throws RemoteException {
            if (mediaPlayer != null) {
                return mediaPlayer.isPlaying();
            }
            return false;
        }

        @Override
        public void seekTo(int position) throws RemoteException {
            if (mediaPlayer != null) {
                mediaPlayer.seekTo(position);
            }
        }

        @Override
        public int getAudioSessionId() throws RemoteException {
            if (mediaPlayer != null) {
                return mediaPlayer.getAudioSessionId();
            }
            return -1;
        }

        @Override
        public Song getCurrentSong() throws RemoteException {
            return currentSong;
        }

        @Override
        public void setNextPlay(Song Song) throws RemoteException {
            queue.add(Song);
        }
    };

    /**
     * 上一首
     */
    private void pre() {
        if (mediaPlayer != null && songArray != null) {
            if (MODE == ORDER) {
                if (position == 0) {
                    position = songArray.size() - 1;
                } else {
                    position--;
                }
            } else if (MODE == RANDOM) {
                position = new Random().nextInt(songArray.size() - 1);
            } else if (MODE == SINGLE) {
                return;
            }
            openAudio(position);
        }
    }

    /**
     * 下一首
     */
    private void next() {
        if (mediaPlayer != null && songArray != null) {
            if (queue.size() > 0) {
                Song song = queue.poll();
                currentSong = song;
                getSongUrlAndOpen(song);
                return;
            } else if (MODE == ORDER) {
                if (position >= songArray.size() - 1) {
                    position = 0;
                } else {
                    position++;
                }
            } else if (MODE == RANDOM) {
                position = new Random().nextInt(songArray.size() - 1);
            } else if (MODE == SINGLE) {
                return;
            }
            openAudio(position);
        }
    }

    private void getSongUrlAndOpen(final Song song) {
        songManager.getSongUrl(song.getWyID(), new SongManager.GetSongUrlHD() {
            @Override
            public void success(String url) {
                openAudio(url, song);
            }

            @Override
            public void fail() {
                Util.prompting(MusicPlayerService.this, "获取播放地址失败");
            }
        });
    }

    /**
     * 停止
     */
    private void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    /**
     * 暂停
     */
    private void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void start() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
            Song song = songArray.get(position);
            //当播放歌曲的时候，在状态显示正在播放，点击的时候，可以进入音乐播放页面
            manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            //最主要
            Intent intent = new Intent(this, ConcretePlayMusicActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            Notification notification = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.ic_launcher_new)
                    .setContentTitle(song.getName() + "(天天动听正在播放)")
                    .setContentText(song.getArtist() + "--" + song.getAlbumName())
                    .setContentIntent(pendingIntent)
                    .build();
            notification.flags = Notification.FLAG_ONGOING_EVENT;
            manager.notify(1, notification);
        }
    }

    private void openAudio(int position) {
        if (songArray != null && songArray.size() > position) {
            Song song = songArray.get(position);
            currentSong = song;
            this.position = position;
            openAudio(song);
        } else {
            Toast.makeText(this, "数据异常", Toast.LENGTH_SHORT).show();
        }
    }

    private void openAudio(final Song song) {
        String url = song.getUrl();
        if (url == null || "null".equals(url)) {
            getSongUrlAndOpen(song);
        } else {
            openAudio(url, song);
        }
    }

    private void openAudio(String url, Song song) {
        try {
            song.setUrl(url);
            String localUrl = songManager.localIsHasSongReturnUrl(song.getName());
            if (localUrl != null) {
                url = localUrl;
                song.setLocal(true);
            }
            if (!song.isLocal()) {
                SongManager.getInstance().downSong(song, Cons.downMusicDirCache);
            }

            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
            } else {
                mediaPlayer.reset();
            }
            if (url != null && !"null".equals(url)) {
                mediaPlayer.setOnPreparedListener(new MyOnPreparedListener());
                mediaPlayer.setOnErrorListener(new MyOnErrorListener());
                mediaPlayer.setOnCompletionListener(new MyOnCompletionListener());
                mediaPlayer.setDataSource(url);
                mediaPlayer.prepareAsync();//异步准备
                wyID = song.getWyID();
                observable.notifyChanged(song);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "音乐播放错误", Toast.LENGTH_SHORT).show();
        }
    }

    private void ReleasePlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    class MyOnCompletionListener implements MediaPlayer.OnCompletionListener {

        @Override
        public void onCompletion(MediaPlayer mp) {
            next();
        }
    }

    class MyOnErrorListener implements MediaPlayer.OnErrorListener {

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            Toast.makeText(MusicPlayerService.this, "音乐播放错误" + "what" + what + " extra" + extra, Toast.LENGTH_SHORT).show();
            //next();
            return true;
        }
    }

    class MyOnPreparedListener implements MediaPlayer.OnPreparedListener {

        @Override
        public void onPrepared(MediaPlayer mp) {
            start();
        }
    }

    private int OPEN_AUDIO = 0;


    @Override
    public void onDestroy() {
        if (manager != null) {
            manager.cancel(1);
        }
        ReleasePlayer();
        super.onDestroy();
    }

    private class MyObservable extends Observable {
        public void notifyChanged(Song song) {
            this.setChanged();
            this.notifyObservers(song);
        }
    }
}
