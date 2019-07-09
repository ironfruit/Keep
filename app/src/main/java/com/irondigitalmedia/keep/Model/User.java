package com.irondigitalmedia.keep.Model;

import java.util.HashMap;
import java.util.Map;

public class User {


    public String name;
    public String username;
    public String about;
    public String email;
    public String url;
    public boolean privacy;
    public Map<String,Boolean> followers = new HashMap<>();
    // Make followers set profile followers count to current followers. Build functionality to "see" other user's profiles.

    public User() {}

    public User(String name, String username, String about, String email, String url, boolean privacy) {
        this.name = name;
        this.username = username;
        this.about = about;
        this.email = email;
        this.url = url;
        this.privacy = privacy;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isPrivacy() {
        return privacy;
    }

    public void setPrivacy(boolean privacy) {
        this.privacy = privacy;
    }
}
