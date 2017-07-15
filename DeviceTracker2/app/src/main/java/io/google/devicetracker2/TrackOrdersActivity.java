//https://developers.google.com/maps/documentation/android-api/map

package io.google.devicetracker2;

import android.provider.ContactsContract;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static io.google.devicetracker2.R.id.map;

public class TrackOrdersActivity extends FragmentActivity implements OnMapReadyCallback {

/*You can also add a MapFragment to an Activity in code. To do this, create a new
MapFragment instance, and then call FragmentTransaction.add() to add the Fragment
to the current Activity*/
/*    mMapFragment = MapFragment.newInstance();
    FragmentTransaction fragmentTransaction =
            getFragmentManager().beginTransaction();
 fragmentTransaction.add(R.id.my_container, mMapFragment);
 fragmentTransaction.commit();*/

    //ArrayList<String> mUserOnWayOrders;
    //Map<String, String> mOrdersDescriptions;
    //Map<String, String> mOrdersMotorguys;
    Map<String, MotorGuy> mMotorGuysMap;
    ArrayList<Order> mUserOrdersList;
    FirebaseDatabaseManagement mFbDatabaseManagementObj /*= new FirebaseDatabaseManagement()*/;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_orders);

//        // Get the fragment
//        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(map);
//        // Add callback to fragment
//        mapFragment.getMapAsync(this);
        triggerProcesses();

    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mFbDatabaseManagementObj = new FirebaseDatabaseManagement();
        DatabaseReference motorGuysRef = mFbDatabaseManagementObj.getMotorGuysReference();

        //Use the onMapReady callback method to get a handle to the GoogleMap object

        // you can use the GoogleMap object to set the view options for the map or add a marker
        // example
        LatLng guatemalaCity = new LatLng(14.613333, -90.535278);
        googleMap.addMarker(new MarkerOptions()
                .position(guatemalaCity)
                .title("Guatemala")
                .snippet("The most vibrant city in the land of the eternal spring"));

        for (Order order : mUserOrdersList) {

            Double lat = Double.parseDouble(mMotorGuysMap.get(order.orderId).latitude);
            Double lng = Double.parseDouble(mMotorGuysMap.get(order.orderId).longitude);
            LatLng orderPosition = new LatLng(lat, lng);

            googleMap.addMarker(new MarkerOptions()
                    .position(orderPosition)
                    .title(order.orderId)
                    .snippet(order.orderDescription));
        }


        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(guatemalaCity, 12));


    }

    public void triggerProcesses() {
        retrieveUserOrderIds();
    }

    public void retrieveUserOrderIds() {
        mFbDatabaseManagementObj = new FirebaseDatabaseManagement();
        final ArrayList<String> userOrderIds = new ArrayList<>();
        //final DatabaseReference rootReference = mFbDatabaseManagementObj.getRootReference();
        DatabaseReference userOrdersDbReference = mFbDatabaseManagementObj.getUserOrdersReference();

        userOrdersDbReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot order : dataSnapshot.getChildren()) {
                    String orderId = order.getKey();
                    userOrderIds.add(orderId);
                }
                retrieveUserOrderObjs(userOrderIds);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void retrieveUserOrderObjs(final ArrayList<String> userOrdersIds) {
        mFbDatabaseManagementObj = new FirebaseDatabaseManagement();
        mUserOrdersList = new ArrayList<>();
        final DatabaseReference orderObjsRef = mFbDatabaseManagementObj.getOrdersObjsReference();

        orderObjsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (String orderID : userOrdersIds) {
                    Order userOrder = dataSnapshot.child(orderID).getValue(Order.class);
                    if (userOrder.motorGuyAssigned != "NO_ASSIGNED") {
                        mUserOrdersList.add(userOrder);
                    }
                }
                retrieveMotorguys();
                //initializeMapFragment();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void retrieveMotorguys() {
        mFbDatabaseManagementObj = new FirebaseDatabaseManagement();
        mMotorGuysMap = new HashMap<>();
        DatabaseReference motorguysRef = mFbDatabaseManagementObj.getMotorGuysReference();

        motorguysRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (Order order : mUserOrdersList) {
                    String lat = dataSnapshot.child(order.motorGuyAssigned)
                            .child("latitude").getValue().toString();
                    String lng = dataSnapshot.child(order.motorGuyAssigned)
                            .child("longitude").getValue().toString();
                    MotorGuy aMotorguy = new MotorGuy(order.motorGuyAssigned, lat, lng);
                    mMotorGuysMap.put(order.orderId, aMotorguy);
                }
                initializeMapFragment();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }

    public void initializeMapFragment() {
        // Get the fragment
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(map);
        // Add callback to fragment
        mapFragment.getMapAsync(this);
    }


    public void addOrderToMap(GoogleMap googleMap, Order order, DatabaseReference assignedMotorGuyRef) {
        /*            Double lat = Double.parseDouble(motorGuysRef.child(order.motorGuyAssigned)
                    .child("latitude").toString());
            Double lng = Double.parseDouble(motorGuysRef.child(order.motorGuyAssigned)
                    .child("longitude").toString());
            LatLng orderPosition = new LatLng(lat, lng);

            googleMap.addMarker(new MarkerOptions()
                    .position(orderPosition)
                    .title(order.orderId)
                    .snippet(order.orderDescription));*/
        MarkerOptions orderMarker = new MarkerOptions()
                .position(new LatLng(14.613333, -90.535278))
                .title(order.orderId)
                .snippet(order.orderDescription);


        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(14.613333, -90.535278))
                .title(order.orderId)
                .snippet(order.orderDescription));


        assignedMotorGuyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });




    }











    /*    public void retrieveUserOrderDescriptions() {
        mFbDatabaseManagementObj = new FirebaseDatabaseManagement();
        DatabaseReference userOnWayOrdersDbReference = mFbDatabaseManagementObj.getUserOrdersReference();
        mOrdersDescriptions = new HashMap<>();

        userOnWayOrdersDbReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot order : dataSnapshot.getChildren()) {
                    mOrdersDescriptions.put(order.getKey(), order.getValue().toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }*/

/*    public void retrieveUserOrdersAssignedMotorGuys() {
        mFbDatabaseManagementObj = new FirebaseDatabaseManagement();
        DatabaseReference orderAssignmentRef = mFbDatabaseManagementObj.getOrderAssignmentReference();

        orderAssignmentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }*/
/*
    public void retrieveUserOnWayOrdersLatLng() {
        mFbDatabaseManagementObj = new FirebaseDatabaseManagement();
        DatabaseReference userOnWayOrdersDatabaseReference = mFbDatabaseManagementObj.getUserOrdersReference();


    }*/
}
