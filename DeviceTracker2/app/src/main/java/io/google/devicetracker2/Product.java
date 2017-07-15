package io.google.devicetracker2;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Fed2 on 14/07/2017.
 */

@IgnoreExtraProperties
public class Product {
    public final String RESTAURANT = "restaurant";
    // etc, other categories


    public String productID;
    public String category;
    public String productName;
    public String description;
    public String price;
    public String imageURL;

    public Product() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public Product(String productID, String category, String productName, String description, String price, String imageURL) {
        this.productID = productID;
        this.category = category;
        this.productName = productName;
        this.description = description;
        this.price = price;
        this.imageURL = imageURL;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("productID", productID);
        result.put("category", category);
        result.put("productName", productName);
        result.put("description", description);
        result.put("price", price);
        result.put("imageURL", imageURL);

        return result;
    }
}
