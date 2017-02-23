package com.progremastudio.emergencymedicalteam.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Post {

    public String uid;
    public String displayName;
    public String email;
    public String timestamp;
    public String locationCoordinate;
    public String message;
    public String pictureUrl;
    public String emergencyType;

    public Post(){
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public Post(String uid, String message) {
        this.uid = uid;
        this.message = message;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("message", message);
        return result;
    }

}
