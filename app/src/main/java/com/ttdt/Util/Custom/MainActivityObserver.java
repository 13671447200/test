package com.ttdt.Util.Custom;

import android.annotation.SuppressLint;
import android.os.Parcel;

import java.util.Observable;

/**
 * Created by Administrator on 2017/9/20.
 */

@SuppressLint("ParcelCreator")
public class MainActivityObserver implements MyObserver {

    public static final Creator<MainActivityObserver> CREATOR = new Creator<MainActivityObserver>() {
        @Override
        public MainActivityObserver createFromParcel(Parcel in) {
            return new MainActivityObserver(in);
        }

        @Override
        public MainActivityObserver[] newArray(int size) {
            return new MainActivityObserver[size];
        }
    };

    public MainActivityObserver(Parcel in) {

    }

    public MainActivityObserver(){

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }

    @Override
    public void update(Observable observable, Object data) {

    }
}
