package io.google.devicetracker2;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

import static android.R.attr.order;

/**
 * Created by Fed2 on 13/07/2017.
 */

@IgnoreExtraProperties
public class MotorGuy {

    String motorguyID;
    String latitude;
    String longitude;


    public MotorGuy() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)

    }

    public MotorGuy(String motorguyID, String latitude, String longitude) {
        this.motorguyID = motorguyID;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("motorguyID", motorguyID);
        result.put("latitude", latitude);
        result.put("longitude", longitude);

        return result;
    }

}


