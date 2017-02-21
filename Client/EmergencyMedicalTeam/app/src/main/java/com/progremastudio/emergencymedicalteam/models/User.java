package com.progremastudio.emergencymedicalteam.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {

    public String email;
    public String userName;
    public String phoneNumber;
    public String displayName;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String email, String userName,
                String displayName, String phoneNumber) {

        this.email = email;
        this.userName = userName;
        this.displayName = displayName;
        this.phoneNumber = phoneNumber;

    }

}
