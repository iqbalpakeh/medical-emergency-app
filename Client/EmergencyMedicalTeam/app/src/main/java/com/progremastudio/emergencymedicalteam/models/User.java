package com.progremastudio.emergencymedicalteam.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class User {

    public String email;
    public String userName;
    public String phoneNumber;
    public String displayName;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String userName, String email,
                String displayName, String phoneNumber) {

        this.email = email;
        this.userName = userName;
        this.displayName = displayName;
        this.phoneNumber = phoneNumber;

    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("display_name", displayName);
        result.put("email", email);
        result.put("user_name", userName);
        result.put("phone_number", phoneNumber);
        return result;
    }


}
