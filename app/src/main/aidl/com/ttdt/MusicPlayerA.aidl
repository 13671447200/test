// MusicPlayerA.aidl
package com.ttdt;
import com.ttdt.modle.Song;
import com.ttdt.Util.Custom.MainActivityObserver;
interface MusicPlayerA {

    /**
    *   获取数据源
    */
   void setSongArray(in List<Song> songArray,int id);
     /**
    * 根据位置打开对应的音频文件
    * @param position
    */
    void openAudio(int position);

    int getSongID();

    String getAlbum();

    //增加观察者
    void addObservable(in MainActivityObserver myObserver);

    boolean isOK();

   /**
    * 播放音乐
    */
    void start();

   /**
    * 播暂停音乐
    */
    void pause();

   /**
    * 停止
    */
    void stop();

   /**
    * 得到当前的播放进度
    * @return
    */
    int getCurrentPosition();

    int getID();

   /**
    * 得到当前音频的总时长
    * @return
    */
    String getDuration();

   /**
    * 得到艺术家
    * @return
    */
    String getArtist();

    String getImageUrl();

   /**
    * 得到歌曲名字
    * @return
    */
    String getName();


   /**
    * 得到歌曲播放的路径
    * @return
    */
    String getAudioPath();

   /**
    * 播放下一个视频
    */
    void next();


   /**
    * 播放上一个视频
    */
    void pre();

   /**
    * 设置播放模式
    * @param playmode
    */
    void setPlayMode(int playmode);

   /**
    * 得到播放模式
    * @return
    */
    int getPlayMode();

    /**
    * 是否正在播放
    */
     boolean isPlaying();

     /**
      拖动音频
     */
     void seekTo(int position);

     int getAudioSessionId();

     Song getCurrentSong();

     void setNextPlay(in Song song);

}
