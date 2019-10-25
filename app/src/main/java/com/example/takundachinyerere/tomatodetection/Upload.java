package com.example.takundachinyerere.tomatodetection;

import android.widget.Toast;

import com.google.firebase.database.Exclude;

public class Upload {
    private String mName;
    private String mImageUrl;
    private String mKey;
    long xValue;
    float yValue;
    int y2Value;
    private String mBitmap;
    private String nBitmap;
    //int y3Value;

    public Upload() {
        //empty constructor needed
    }

    public Upload(String name, String imageUrl,long xValue, float yValue, int y2Value/*, int y3Value*/, String bitmap, String bitmap1) {
        if (name.trim().equals("")) {
            name = "No Name";
            y2Value = 0;
        }else if (name.trim().equals("healthy")){
            y2Value = 1;
        }else if (name.trim().equals("lateblight")){
            y2Value = -1;
        }

        mName = name;
        mImageUrl = imageUrl;
        this.xValue = xValue;
        this.yValue = yValue;
        this.y2Value = y2Value;
        //this.y3Value = y3Value;
        mBitmap = bitmap;
        nBitmap = bitmap1;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        mImageUrl = imageUrl;
    }

    public long getxValue() {
        return xValue;
    }

    public float getyValue() {
        return yValue;
    }

    public int gety2Value(){return y2Value;}

    //public int gety3Value(){return y3Value;}

    public String getBitmap1() {
        return nBitmap;
    }

    public void setBitmap1(String bitmap1) {
        nBitmap = bitmap1;
    }

    public String getBitmap() {
        return mBitmap;
    }

    public void setBitmap(String bitmap) {
        mBitmap = bitmap;
    }


    @Exclude
    public String getKey() {
        return mKey;
    }

    @Exclude
    public void setKey(String key) {
        mKey = key;
    }
}