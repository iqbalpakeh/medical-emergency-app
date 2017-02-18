package com.progremastudio.emergencymedicalteam.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Post {

    public String uid;
    public String content;

    public Post(){
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public Post(String uid, String content) {
        this.uid = uid;
        this.content = content;
    }

    // [START post_to_map]
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("content", content);
        return result;
    }

}
