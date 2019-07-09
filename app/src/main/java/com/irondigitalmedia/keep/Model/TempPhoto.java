package com.irondigitalmedia.keep.Model;

public class TempPhoto {

    public String name;
    public String url;

    public TempPhoto() {
    }

    public TempPhoto(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
