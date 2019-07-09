package com.irondigitalmedia.keep.Model;

public class Grocery {

    private String uid;
    private String name;
    private String url;

    public Grocery() {
    }

    public Grocery(String uid, String name, String url) {
        this.uid = uid;
        this.name = name;
        this.url = url;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
