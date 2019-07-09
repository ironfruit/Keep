package com.irondigitalmedia.keep.Model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Recipe {

    public String uid;
    public String title;
    public String desc;
    public String prepTime;
    public String url;
    public String creatorId;
    public int likecount = 0;
    // Eventually set the like count on the
    // floating action button as a badge in
    // one of the upper corners of the button.
    public Map<String,Boolean> likes = new HashMap<>();

    public Recipe(){
    }

    public Recipe(String creatorId, String uid, String title, String desc, String time, String url) {
        this.uid = uid;
        this.title = title;
        this.desc = desc;
        this.prepTime = time;
        this.url = url;
        this.creatorId = creatorId;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getPrepTime() {
        return prepTime;
    }

    public void setPrepTime(String prepTime) {
        this.prepTime = prepTime;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("creatorId",creatorId);
        result.put("title", title);
        result.put("desc", desc);
        result.put("prepTime", prepTime);
        result.put("url", url);
        result.put("likecount", likecount);
        result.put("likes", likes);

        return result;
    }


}
