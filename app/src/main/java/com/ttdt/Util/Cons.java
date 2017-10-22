package com.ttdt.Util;

import android.os.Environment;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/9/19.
 */

public class Cons implements Serializable{

    public static String downMusicDir = Environment.getExternalStorageDirectory() + "/music/";
    public static String downMusicDirCache = Environment.getExternalStorageDirectory() + "/cache/";
    public static String baseUrl = "http://music.163.com/";
//    public static String baseUrl_my = "http://192.168.1.102:8080/TTDT/";
    public static String baseUrl_my = "http://119.29.201.160:8080/NetTTDT/";

}
