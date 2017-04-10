package com.progremastudio.emergencymedicalteam.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Chat {

    public String uid;

    public String displayName;

    public String timestamp;

    public String message;

    public String profileUrl;

    public Chat() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public Chat(String uid,
                String displayName,
                String timestamp,
                String message,
                String profileUrl) {

        this.uid = uid;
        this.displayName = displayName;
        this.timestamp = timestamp;
        this.message = message;
        this.profileUrl = profileUrl;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("displayName", displayName);
        result.put("timestamp", timestamp);
        result.put("message", message);
        result.put("profileUrl", profileUrl);
        return result;
    }

}
