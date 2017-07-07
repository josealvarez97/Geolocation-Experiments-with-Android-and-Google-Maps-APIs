package io.google.devicetracker2;

import android.util.Log;

import com.google.android.gms.tagmanager.Container;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.R.attr.value;
import static io.google.devicetracker2.MainActivity.NO_ASSIGNED;
import static io.google.devicetracker2.MainActivity.ORDERS_QUEUE;
import static io.google.devicetracker2.MainActivity.ORDER_ASSIGNMENT;
import static io.google.devicetracker2.MainActivity.ORDER_DESCRIPTIONS;
import static io.google.devicetracker2.MainActivity.USERS;

/**
 * Created by Fed2 on 06/07/2017.
 */

public class FirebaseDatabaseManagement {

    public FirebaseDatabase mDatabase;
    public DatabaseReference mDatabaseReference;
    DatabaseReference mMyRef;
    Map<String, String> mMap;
    List<String> mList;
    ArrayList<String> mArrayList;



    // OTHER STUFF
    private static final String TAG = FirebaseDatabaseManagement.class.getSimpleName();
    public static final String ORDERS_QUEUE = "ordersQueue";
    public static final String ON_WAY_ORDERS = "onWayOrders";
    public static final String USERS = "users";
    public static final String MOTOR_GUYS = "motorguys";
    public static final String ORDER_ASSIGNMENT= "orderAssignment";
    public static final String NO_ASSIGNED = "NO_ASSIGNED";
    public static final String ORDER_DESCRIPTIONS = "orderDescriptions";
    private String mCurrentTypeOfUser = "hola";







    public FirebaseDatabaseManagement() {
        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference();

    }

    public void writeToDatabase(){
        mDatabase = FirebaseDatabase.getInstance();
        mMyRef = mDatabase.getReference("message");

        mMyRef.setValue("Hello, World!");
    }

    public void pushOrderToFirebaseDatabase(String orderDescription) {
        // Get a key so we can identify new order
        String orderKey = mDatabaseReference.child("ordersQueue").push().getKey();
        // We finish pushing new order to the ORDERS QUEUE
        mDatabaseReference.child("ordersQueue").child(orderKey).setValue(true);
        // We also tie new order to a list of orders on the client profile
        mDatabaseReference.child(USERS).child(getCurrentFbUserId()).child("orders").child(orderKey).setValue(false);
        // We also have to update ORDER ASSIGNMENT
        mDatabaseReference.child(ORDER_ASSIGNMENT).child(orderKey).child(NO_ASSIGNED);
        // And of course, we tie the description to the order
        mDatabaseReference.child(ORDER_DESCRIPTIONS).child(orderKey).setValue(orderDescription);
    }

    public String getCurrentFbUserId() {
        FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();

        if (fbUser != null) {
            return fbUser.getUid();
        } else {
            return null;
        }

    }

    public Map<String, String> getMapFromAPath(DatabaseReference aReference) {
        mMap = null;
        aReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, String> objectHashMap = (HashMap<String,String>) dataSnapshot.getValue();
                mMap = objectHashMap;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return mMap;
    }

    public List<String> getListFromAPath(DatabaseReference aReference) {
        mList = null;

        aReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, String> objectHashMap = (HashMap<String,String>) dataSnapshot.getValue();
                List<String> objectList = new ArrayList<String>(objectHashMap.values());
                mList = objectList;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return mList;
    }

    public void getChildArrayListFromReference(DatabaseReference aReference) {
        mArrayList = new ArrayList<>();
        aReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childrenSnapshot: dataSnapshot.getChildren()) {
                    // Handle children
                    mArrayList.add(childrenSnapshot.getKey());
                }
                // Next step...
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                    // Getting childSnapshot failed, log a message
                Log.w(TAG, "loadChild:onCancelled", databaseError.toException());
                // ...
            }
        });

        //return mArrayList;
    }



}
