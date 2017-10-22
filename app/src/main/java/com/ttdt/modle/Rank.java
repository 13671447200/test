package com.ttdt.modle;

/**
 * Created by Administrator on 2017/9/21.
 */

public class Rank {

    private String name;
    private Integer imageId;
    private Integer wxId;

    public Integer getWxId() {
        return wxId;
    }

    public void setWxId(Integer wxId) {
        this.wxId = wxId;
    }

    public Rank(String name, Integer imageId, Integer wxId) {
        this.name = name;
        this.imageId = imageId;
        this.wxId = wxId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getImageId() {
        return imageId;
    }

    public void setImageId(Integer imageId) {
        this.imageId = imageId;
    }

    public Rank(String name, Integer imageId) {
        this.name = name;
        this.imageId = imageId;
    }

    public Rank() {
    }

    @Override
    public String toString() {
        return "Rank{" +
                "name='" + name + '\'' +
                ", imageId=" + imageId +
                ", wxId=" + wxId +
                '}';
    }
}
