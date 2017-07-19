package io.google.devicetracker2;

import android.location.Location;
import android.provider.ContactsContract;
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

/*import static android.R.attr.order;
import static android.R.attr.start;
import static android.R.attr.value;
import static io.google.devicetracker2.MainActivity.NO_ASSIGNED;
import static io.google.devicetracker2.MainActivity.ORDERS_QUEUE;
import static io.google.devicetracker2.MainActivity.ORDER_ASSIGNMENT;
import static io.google.devicetracker2.MainActivity.ORDER_DESCRIPTIONS;
import static io.google.devicetracker2.MainActivity.USERS;*/

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
    public static final String ORDER_OBJS = "ordersOBJs";
    public static final String USER_ORDEROBJS = "user-ordersOBJs";
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

    public void addProductToCart(String productID) {
        mDatabaseReference.child(USERS).child(getCurrentFbUserId()).child("userCart").push().setValue(productID);
    }

    public void pushOrderToFirebaseDatabase(String orderDescription) {
        // Get a key so we can identify new order
        String orderKey = mDatabaseReference.child("ordersQueue").push().getKey();
        // We finish pushing new order to the ORDERS QUEUE
        mDatabaseReference.child("ordersQueue").child(orderKey).setValue(getCurrentFbUserId());
        // We also tie new order to a list of orders on the client profile
        mDatabaseReference.child(USERS).child(getCurrentFbUserId()).child("orders").child(orderKey).setValue(orderDescription);
        // We also have to update ORDER ASSIGNMENT
        mDatabaseReference.child(ORDER_ASSIGNMENT).child(orderKey).child(NO_ASSIGNED);
        // And of course, we tie the description to the order
        mDatabaseReference.child(ORDER_DESCRIPTIONS).child(orderKey).setValue(orderDescription);


        // WRITE OBJECT
        Order newOrder = new Order(orderKey, getCurrentFbUserId(), NO_ASSIGNED, orderDescription);
        Map<String, Object> newOrderValues = newOrder.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/ordersOBJs/"+orderKey, newOrderValues);
        childUpdates.put("/user-ordersOBJs/"+getCurrentFbUserId()+"/"+orderKey, newOrderValues);

        mDatabaseReference.updateChildren(childUpdates);

        startAcceptOrderProcess(orderKey); // I think I must delete this later

        // I also must delete cart after pushing an order
    }

    public void startAcceptOrderProcess(final String orderKey) {
        DatabaseReference orderQueueRef = mDatabaseReference.child("ordersQueue");
        final String ordKey = orderKey;
        orderQueueRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String acceptedOrderUserId = dataSnapshot.child(ordKey).getValue(String.class);
                acceptOrder(ordKey, "wOuifegVftaz3xbpwqErAY7qSt03", acceptedOrderUserId);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    public void acceptOrder(String orderKey) {
        // We save userIdValue
        String acceptedOrderUserId = mDatabaseReference.child(ORDERS_QUEUE).child(orderKey).toString();

        // WE ADD ORDER TO CURRENT MOTORGUY CURRENT ORDER FIELD
        mDatabaseReference.child(MOTOR_GUYS).child(getCurrentFbUserId()).child("currentOrder").setValue(orderKey);
        // WE DELETE ORDER FROM ORDERS QUEUE
        mDatabaseReference.child(ORDERS_QUEUE).child(orderKey).removeValue();
        // WE ADD ORDER TO ON WAY ORDERS
        mDatabaseReference.child(ON_WAY_ORDERS).child(orderKey).setValue(false);
        // WE UPDATE ORDER ASSIGNMENT WITH MOTOR GUY ID
        mDatabaseReference.child(ORDER_ASSIGNMENT).child(orderKey).setValue(getCurrentFbUserId());


        //NOTE: THIS METHOD WILL BE USED ASSUMING THAT THE CURRENT AUTHENTICATED INDIVIDUAL IS A MOTORGUY
        // Update motorGuyAssigned fields
        mDatabaseReference.child("ordersOBJs").child(orderKey).child("motorGuyAssigned").setValue(getCurrentFbUserId());
        mDatabaseReference.child("user-ordersOBJs").child(acceptedOrderUserId).child(orderKey).child("motorGuyAssigned").setValue(getCurrentFbUserId());
    }

    public void acceptOrder(String orderKey, String motorGuyId, String acceptedOrderUserId) {
        // We save userIdValue
        //String acceptedOrderUserId = mDatabaseReference.child(ORDERS_QUEUE).child(orderKey).toString();

        // WE ADD ORDER TO CURRENT MOTORGUY CURRENT ORDER FIELD
        mDatabaseReference.child(MOTOR_GUYS).child(motorGuyId).child("currentOrder").setValue(orderKey);
        // WE DELETE ORDER FROM ORDERS QUEUE
        mDatabaseReference.child(ORDERS_QUEUE).child(orderKey).removeValue();
        // WE ADD ORDER TO ON WAY ORDERS
        mDatabaseReference.child(ON_WAY_ORDERS).child(orderKey).setValue(false);
        // WE UPDATE ORDER ASSIGNMENT WITH MOTOR GUY ID
        mDatabaseReference.child(ORDER_ASSIGNMENT).child(orderKey).setValue(motorGuyId);


        //NOTE: THIS METHOD WILL BE USED ASSUMING THAT THE CURRENT AUTHENTICATED INDIVIDUAL IS A MOTORGUY
        // Update motorGuyAssigned fields
        mDatabaseReference.child("ordersOBJs").child(orderKey).child("motorGuyAssigned").setValue(motorGuyId);
        mDatabaseReference.child("user-ordersOBJs").child(acceptedOrderUserId).child(orderKey).child("motorGuyAssigned").setValue(motorGuyId);
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
    public DatabaseReference getUserOrdersReference() {
        DatabaseReference requestedReference = mDatabaseReference
                .child("users")
                .child(getCurrentFbUserId())
                .child("orders");
        return requestedReference;
    }

    public DatabaseReference getOrderDescriptionsReference() {
        DatabaseReference requestedReference = mDatabaseReference
                .child("orderDescriptions");
        return requestedReference;
    }

    public DatabaseReference getOrderAssignmentReference() {
        DatabaseReference requestedReference = mDatabaseReference
                .child(ORDER_ASSIGNMENT);

        return requestedReference;
    }

    public DatabaseReference getRootReference() {
        return mDatabaseReference;
    }

    public DatabaseReference getOrdersObjsReference() {
        DatabaseReference requestedReference = mDatabaseReference
                .child(ORDER_OBJS);

        return requestedReference;
    }

    public DatabaseReference getMotorGuysReference() {
        DatabaseReference requestedReference = mDatabaseReference
                .child(MOTOR_GUYS);

        return requestedReference;
    }

    public DatabaseReference getProductOBJsReference() {
        DatabaseReference requestedReference = mDatabaseReference
                .child("productsOBJs");
        return requestedReference;
    }



    public void updateUserLocationOnDatabase(String userID, Location currentLocation) {
        double latitude = currentLocation.getLatitude();
        double longitude = currentLocation.getLongitude();
        mDatabaseReference.child(FirebaseDatabaseManagement.MOTOR_GUYS).child(userID).child("latitude").setValue(latitude);
        mDatabaseReference.child(FirebaseDatabaseManagement.MOTOR_GUYS).child(userID).child("longitude").setValue(longitude);
    }




}
