package com.ttdt.modle;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by Administrator on 2017/10/9.
 */
@Entity
public class PlayList {
    @Id
    private long id;
    private String playListName;
    private String image;
    private int playCount;
    private int userId;
    private int number;


    @Generated(hash = 1678611998)
    public PlayList(long id, String playListName, String image, int playCount,
            int userId, int number) {
        this.id = id;
        this.playListName = playListName;
        this.image = image;
        this.playCount = playCount;
        this.userId = userId;
        this.number = number;
    }

    @Generated(hash = 438209239)
    public PlayList() {
    }


    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPlayListName() {
        return playListName;
    }

    public void setPlayListName(String playListName) {
        this.playListName = playListName;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getPlayCount() {
        return playCount;
    }

    public void setPlayCount(int playCount) {
        this.playCount = playCount;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "PlayList{" +
                "id=" + id +
                ", playListName='" + playListName + '\'' +
                ", image='" + image + '\'' +
                ", playCount=" + playCount +
                ", userId=" + userId +
                '}';
    }
}
