package com.ttdt.modle;

/**
 * Created by Administrator on 2017/10/8.
 */

public class LeftMenu {

    private String text;
    private String imageUrl;
    private int image;

    public LeftMenu() {
    }

    public LeftMenu(String text, int image) {
        this.text = text;
        this.image = image;
    }

    public LeftMenu(String text, String imageUrl) {
        this.text = text;
        this.imageUrl = imageUrl;
    }

    public LeftMenu(String text, String imageUrl, int image) {
        this.text = text;
        this.imageUrl = imageUrl;
        this.image = image;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }



}
