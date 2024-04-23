package com.dev.childmonitor;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {

    public String username;
    public String email;
    public String password;
    public Boolean isParent;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String email, String password, Boolean isParent) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.isParent = isParent;

    }

}
