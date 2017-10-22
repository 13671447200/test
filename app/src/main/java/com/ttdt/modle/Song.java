package com.ttdt.modle;

import android.os.Parcel;
import android.os.Parcelable;

public class Song implements Parcelable{

    private int id;
    private int wyID;
    private String albumName = "";
    private String name;
    private String artist;
    private String time;
    private String url;
    private String artistImage;
    private boolean isSelect;//是否已经被选择
    private boolean isUploading;//是否正在下载中
    private boolean isLocal;

    public void setLocal(boolean local) {
        isLocal = local;
    }

    public boolean isLocal() {
        return isLocal;
    }

    public boolean isUploading() {
        return isUploading;
    }

    public void setUploading(boolean uploading) {
        isUploading = uploading;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public Song() {
    }
    public static final Parcelable.Creator<Song> CREATOR = new Parcelable.Creator<Song>()
    {
        public Song createFromParcel(Parcel in)
        {
            return new Song(in);
        }

        public Song[] newArray(int size)
        {
            return new Song[size];
        }
    };
    private Song(Parcel in){
        id = in.readInt();
        wyID = in.readInt();
        albumName = in.readString();
        name = in.readString();
        artist = in.readString();
        time = in.readString();
        url = in.readString();
        artistImage = in.readString();
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public Song(int id, int wyID, String albumName, String name, String artist, String time, String url, String artistImage) {
        this.id = id;
        this.wyID = wyID;
        this.albumName = albumName;
        this.name = name;
        this.artist = artist;
        this.time = time;
        this.url = url;
        this.artistImage = artistImage;
    }

    public String getArtistImage() {
        return artistImage;
    }

    public void setArtistImage(String artistImage) {
        this.artistImage = artistImage;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getWyID() {
        return wyID;
    }

    public void setWyID(int wyID) {
        this.wyID = wyID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    @Override
    public int describeContents() {
        return 0;
    }

//    readFromParcel

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(id);
        out.writeInt(wyID);
        out.writeString(albumName);
        out.writeString(name);
        out.writeString(artist);
        out.writeString(time);
        out.writeString(url);
        out.writeString(artistImage);
    }

    public void readFromParcel(Parcel in) {
        id = in.readInt();
        wyID = in.readInt();
        albumName = in.readString();
        name = in.readString();
        artist = in.readString();
        time = in.readString();
        url = in.readString();
        artistImage = in.readString();
    }

}
