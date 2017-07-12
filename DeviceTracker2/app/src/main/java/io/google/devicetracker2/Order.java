package io.google.devicetracker2;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Fed2 on 11/07/2017.
 */

@IgnoreExtraProperties
public class Order {

    public String orderId;
    public String userId;
    public String motorGuyAssigned;
    public String orderDescription;

    public Order() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)

    }

    public Order(String orderId, String userId, String motorGuyAssigned, String orderDescription) {
        this.orderId = orderId;
        this.userId = userId;
        this.motorGuyAssigned = motorGuyAssigned;
        this.orderDescription = orderDescription;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("orderId", orderId);
        result.put("userId", userId);
        result.put("motorGuyAssigned", motorGuyAssigned);
        result.put("orderDescription", orderDescription);

        return  result;
    }
}
