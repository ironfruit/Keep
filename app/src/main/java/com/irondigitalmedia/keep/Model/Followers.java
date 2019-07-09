package com.irondigitalmedia.keep.Model;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class Followers {

    public String userId;
    public String followerId;
    public int followercount = 0;
    public Map<String,Boolean> followers = new HashMap<>();

    public Followers() {
    }

    public Followers(String userId, String followerId) {
        this.userId = userId;
        this.followerId = followerId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFollowerId() {
        return followerId;
    }

    public void setFollowerId(String followerId) {
        this.followerId = followerId;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("userId",userId);
        result.put("followerId",followerId);
        result.put("followercount",followercount);
        result.put("followers",followers);
        return result;
    }


}
