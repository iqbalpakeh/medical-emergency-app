package com.progremastudio.emergencymedicalteam.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class User {

    public String uid;

    public String email;

    public String phoneNumber;

    public String displayName;

    public String pictureUrl;

    public String token;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String uid, String displayName, String email, String phoneNumber, String pictureUrl, String token) {
        this.uid = uid;
        this.email = email;
        this.displayName = displayName;
        this.phoneNumber = phoneNumber;
        this.pictureUrl = pictureUrl;
        this.token = token;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("displayName", displayName);
        result.put("email", email);
        result.put("phoneNumber", phoneNumber);
        result.put("pictureUrl", pictureUrl);
        result.put("token", token);
        return result;
    }
}
