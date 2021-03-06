/*
 * Copyright (c) 2017, Progrema Studio. All rights reserved.
 */

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

    public String phoneNumber;

    public String profileUrl;

    public Post(){
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public Post(String uid,
                String displayName,
                String email,
                String timestamp,
                String locationCoordinate,
                String message,
                String pictureUrl,
                String emergencyType,
                String phoneNumber,
                String profileUrl) {

        this.uid = uid;
        this.displayName = displayName;
        this.email = email;
        this.timestamp = timestamp;
        this.locationCoordinate = locationCoordinate;
        this.message = message;
        this.pictureUrl = pictureUrl;
        this.emergencyType = emergencyType;
        this.phoneNumber = phoneNumber;
        this.profileUrl = profileUrl;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("displayName", displayName);
        result.put("email", email);
        result.put("timestamp", timestamp);
        result.put("locationCoordinate", locationCoordinate);
        result.put("message", message);
        result.put("pictureUrl", pictureUrl);
        result.put("emergencyType", emergencyType);
        result.put("phoneNumber", phoneNumber);
        result.put("profileUrl", profileUrl);
        return result;
    }

}
