package com.ttdt.Util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.io.File;
import java.io.IOException;
import java.util.Formatter;

/**
 * Created by Administrator on 2017/9/19.
 */

public class Util {

    public static RequestQueue requestQueue;

    public static void init(Context context) {
        requestQueue = Volley.newRequestQueue(context);
        creatFile();
    }

    public static void setTextMarquee(TextView textView) {
        if (textView != null) {
            textView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            textView.setSingleLine(true);
            textView.setSelected(true);
            textView.setFocusable(true);
            textView.setFocusableInTouchMode(true);
        }
    }

    public static void hideSoftInputFromWindow(Context context,View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        //隐藏键盘
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private static void creatFile() {
        File file = new File(Cons.downMusicDir);
        if (!file.mkdirs()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        file = null;
        file = new File(Cons.downMusicDirCache);
        if (!file.mkdirs()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean isGrantExternalRW(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity.checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            activity.requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 1);

            return false;
        }

        return true;
    }

    public static void prompting(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    private int getIntTime1(String time) {
        int index = time.indexOf(":");
        int iTime = 0;
        if (index != -1) {
            iTime += Integer.valueOf(time.substring(0, index)) * 60;
            iTime += Integer.valueOf(time.substring(index, time.length()));
        }
        return iTime;
    }

    public static String getTime(int timeMs) {
        Formatter mFormatter = new Formatter();
        int totalSeconds = timeMs / 1000;
        int seconds = totalSeconds % 60;

        int minutes = (totalSeconds / 60) % 60;

        int hours = totalSeconds / 3600;
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds)
                    .toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    public static int getIntTime(String strTime) {

        try {
            int time = 0;
            strTime = strTime.replace(" ", "");
            int indexBegin = 0;
            int indexEnd = strTime.indexOf(":");
            if (indexBegin != -1 && indexEnd != -1) {
                time += Integer.valueOf(strTime.substring(indexBegin + 1, indexEnd)) * 60;
            }

            indexBegin = strTime.indexOf(".");
            if (indexBegin != -1) {
                time += Integer.valueOf(strTime.substring(indexEnd + 1, indexBegin));
            }
            return time * 1000;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
