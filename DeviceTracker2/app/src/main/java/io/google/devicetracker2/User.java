package io.google.devicetracker2;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by Fed2 on 26/06/2017.
 */

@IgnoreExtraProperties
public class User {

    public String username;
    public String email;
    public Double latitude;
    public Double longitude;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(io.google.devicetracker2.User.class)
    }

    public User(String username, String email, Double latitude, Double longitude) {
        this.username = username;
        this.email = email;
        this.latitude = latitude;
        this.longitude = longitude;
    }

}
