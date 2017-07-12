package io.google.devicetracker2;

import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static io.google.devicetracker2.CredentialsActivity.VALID_EMAIL;
import static io.google.devicetracker2.CredentialsActivity.VALID_PASSWORD;

public class MainActivity extends AppCompatActivity {

    // LOCATION STUFF
    private FusedLocationProviderClient mFusedLocationClient;
    private Location mCurrentLocation;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private boolean mRequestingLocationUpdates;

    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";

    // FIREBASE DATABASE STUFF
    FirebaseDatabase mDatabase;
    DatabaseReference mDatabaseReference;
    DatabaseReference mMyRef;

    // FIREBASE AUTHENTICATION STUFF
    private FirebaseAuth mAuth;


    // OTHER STUFF
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String ORDERS_QUEUE = "ordersQueue";
    public static final String ON_WAY_ORDERS = "onWayOrders";
    public static final String USERS = "users";
    public static final String MOTOR_GUYS = "motorguys";
    public static final String ORDER_ASSIGNMENT= "orderAssignment";
    public static final String NO_ASSIGNED = "NO_ASSIGNED";
    public static final String ORDER_DESCRIPTIONS = "orderDescriptions";
    private String mCurrentTypeOfUser = "hola";
    private TextView currentUserTxtV;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Getting Information from CredentialsActivity
        Intent intentSent = getIntent();
        String validatedEmail = intentSent.getStringExtra(VALID_EMAIL);
        String validatedPassword = intentSent.getStringExtra(VALID_PASSWORD);



        // Initializing LOCATION
        createLocationRequest();
        getCurrentLocation();


        // Initializing AUTHENTICATION
        mAuth = FirebaseAuth.getInstance();
        //createAccount("alvarez.jo.2017@gmail.com", "superPikachu");
        signIn(validatedEmail, validatedPassword);
        //FirebaseUser theFbUser = mAuth.getCurrentUserObj();




        // Initializing DATABASE
        initializeDatabase();
        //writeNewUserToDatabase(getCurrentFbUserId(), getCurrentUserObj(mAuth.getCurrentUser()).username, getCurrentUserObj(mAuth.getCurrentUser()).email);
        //writeNewUserToDatabase("JaviId", "displayName", "afuckingemail");
        //updateUserEmail("JaviId", "j.alvarez@minerva.kgi.edu");
        //mDatabase.getReference("JoseId").addValueEventListener(userListener);
        //mDatabase.getReference("JoseId").addListenerForSingleValueEvent(userListener);






        // Working out LOCATION updates

        //updateValuesFromBundle(savedInstanceState); // Really need to know what's going on here...

        mLocationCallback = new LocationCallback () {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for(Location location : locationResult.getLocations()) {
                    //UpdateUI with location data
                    // ...

                    // Update Current Location
                    mCurrentLocation = location;

                    // UpdateUserLocationOnDatabase
                    updateUserLocationOnDatabase(getCurrentFbUserId(), mCurrentLocation);

                    // Update UserInterface
                    //View rootView = getWindow().getDecorView().getRootView();
                    //View aview = findViewById(R.id.activity_main);
                    //String name = rootView.toString();
                    if(mCurrentTypeOfUser == USERS) {
                        updateUILocation(/*Maybe I'll add an overload here with the location*/);
                    }


                }
            }
        };
        // OTHER TEMPORARY STUFF
        // Adjusting a couple of things
        initializeUserDetails();




        FirebaseUser fbUser = mAuth.getCurrentUser();
        String email = fbUser.getEmail();
        //TextView currentUserTxtV = (TextView) findViewById(R.id.currentUserTextView);
        currentUserTxtV = (TextView) findViewById(R.id.currentUserTextView);
        currentUserTxtV.setText(email);
        if (isUserAClient()) {
            Toast.makeText(MainActivity.this, "USER IS CLIENT...",
                    Toast.LENGTH_LONG).show();
            //pushOrderToFirebaseDatabase("2 PIEZAS DEL POLLO MAS RICO POR FAVOR");


        } else if (isUserAMotorGuy()) {
            Toast.makeText(MainActivity.this, "USER IS MOTOR GUY...",
                    Toast.LENGTH_LONG).show();
            //acceptOrder("-KoL_oLayAbnAoyrgc2A");
        } else {
            Toast.makeText(MainActivity.this, "Something is wrong with authentication or profiles...",
                    Toast.LENGTH_LONG).show();
        }


    }

    // GET THE LAST KNOWN LOCATION
    public void getCurrentLocation() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION},
                    0);
        } else {
            // permission has been granted, continue as usual
/*            Location myLocation =
                    LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);*/

            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {

                                mCurrentLocation = location;

                                //updateUILocation();



                            }
                        }
                    });
        }


    }

    // CHANGING LOCATION SETTINGS
    public void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                // ...
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            public void onFailure(@NonNull Exception e) {
                int statusCode = ((ApiException) e).getStatusCode();
                switch (statusCode) {
                    case CommonStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            resolvable.startResolutionForResult(MainActivity.this,
                                    /*REQUEST_CHECK_SETTINGS = 0x1*/ 0x1);
                        } catch (IntentSender.SendIntentException sendEx) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        break;
                }
            }
        });



    }


    // STOP LOCATION UPDATES

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }


    // REQUEST LOCATION UPDATES
    @Override
    protected void onResume() {
        super.onResume();
        if  (!mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }
    public void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION},
                    0);
        } else {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback,
                    null /* Looper*/);
        }
    }



    // SAVE THE STATE OF THE ACTIVITY
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);

        // ....
        super.onSaveInstanceState(outState);
    }



    public void updateValuesFromBundle(Bundle savedInstanceState) {
        // Update the value of mRequestingLocationUpdates from the Bundle.
        if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
            mRequestingLocationUpdates = savedInstanceState.getBoolean(REQUESTING_LOCATION_UPDATES_KEY);
        }
        // ...

        // Update UI to match restored state
        updateUILocation();
    }



    public void initializeDatabase() {
        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference();
    }
    public void writeToDatabase(){
        mDatabase = FirebaseDatabase.getInstance();
        mMyRef = mDatabase.getReference("message");

        mMyRef.setValue("Hello, World!");
    }
    public void writeNewUserToDatabase(String userId, String name, String email) {
        User user = new User(name, email, 0.0, 0.0);
        mDatabaseReference.child("users").child(userId).setValue(user);
    }
    public void updateUserEmail(String userID, String email) {
        mDatabaseReference.child("users").child(userID).child("email").setValue(email);
    }
    public void updateUserLocationOnDatabase(String userID, Location currentLocation) {
        double latitude = currentLocation.getLatitude();
        double longitude = currentLocation.getLongitude();
        mDatabaseReference.child(mCurrentTypeOfUser).child(userID).child("latitude").setValue(latitude);
        mDatabaseReference.child(mCurrentTypeOfUser).child(userID).child("longitude").setValue(longitude);
    }


    public void readFromDatabase() {

        // This only read or listens from mMyRef, which is a reference to "message", which only has hello world until now
        // Read from the database
        mMyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                // I'm only adding "message" = hello world to the text View in the UI
                String value = dataSnapshot.getValue(String.class);
                TextView locationView = (TextView) findViewById(R.id.textview1);
                String text = locationView.getText().toString();
                text = text + value;
                locationView.setText(text);


                Log.d(TAG, "Value is: " + value);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", databaseError.toException());

            }
        });
    }


    // This is a Listener for listening to a User object stored in the database.
    ValueEventListener userListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            // Get User object and use the values to update the UI or other stuff
            User user = dataSnapshot.getValue(User.class);
            //...
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            // Getting User failed, log a message
            Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
        }
    };



    // CHECK CURRENT AUTH STATE
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-full) and update UI accordingly
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    // SIGN UP NEW USERS
    public void createAccount(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success");
                    FirebaseUser fbUser = mAuth.getCurrentUser();
                    updateUI(fbUser);
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                    Toast.makeText(MainActivity.this, "Authentication failed. CreateAccount",
                            Toast.LENGTH_SHORT).show();
                    updateUI(null);
                }

                // ...
            }
        });
        //Add a form to register new users with their email and password and call this new method when it is submitted. You can see an example in our quickstart sample.
    }


    // SIGN IN EXISTING USERS
    public void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser fbUser = mAuth.getCurrentUser();
                            updateUI(fbUser);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed. Sign In",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }

                    //...
                });
    }
    // Add a form to sign in users with their email and password and call this new method when it is submitted. You can see an example in our quickstart sample.


    public User getCurrentUserObj(FirebaseUser fbUser) {

        //FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUserObj();


        if (fbUser != null) {
            // Name, email address, and profile photo Url
            String name = fbUser.getDisplayName();
            String email = fbUser.getEmail();
            //Uri photoUrl = fbUser.getPhotoUrl();

            // Check if user's email is verified
            //boolean emailVerified = fbUser.isEmailVerified();

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getToken() instead.
            //String uid = user.getUid();

            User user = new User(name, email, 0.0, 0.0);

            return user;

        } else {
            return null;
        }
    }
    public String getCurrentFbUserId() {
        FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();

        if (fbUser != null) {
            return fbUser.getUid();
        } else {
            return null;
        }

    }


    public void updateUILocation(){
        double latitude = mCurrentLocation.getLatitude();
        double longitude = mCurrentLocation.getLongitude();

        // REALLY MAKING BULSHIT WITH THE INTERFACE HERE... FIX REQUIRED...
        String locationStr = Double.toString(latitude) + ", " + Double.toString(longitude);
        TextView locationView = (TextView) findViewById(R.id.textview1);
        locationView.setText(locationStr);

        //writeToDatabase(); Just writes hello world to the database
        //readFromDatabase(); Only listen and adds hello world to text view


    }
    public void updateUI(FirebaseUser fbUser) {
        User user = getCurrentUserObj(fbUser);
    }

    public void pushOrderToFirebaseDatabase(String orderDescription) {
        // Get a key so we can identify new order
        String orderKey = mDatabaseReference.child(ORDERS_QUEUE).push().getKey();
        // We finish pushing new order to the ORDERS QUEUE
        mDatabaseReference.child(ORDERS_QUEUE).child(orderKey).setValue(true);
        // We also tie new order to a list of orders on the client profile
        mDatabaseReference.child(USERS).child(getCurrentFbUserId()).child("orders").child(orderKey).setValue(false);
        // We also have to update ORDER ASSIGNMENT
        mDatabaseReference.child(ORDER_ASSIGNMENT).child(orderKey).child(NO_ASSIGNED);
        // And of course, we tie the description to the order
        mDatabaseReference.child(ORDER_DESCRIPTIONS).child(orderKey).setValue(orderDescription);

    }
    public void acceptOrder(String orderKey) {
        // WE ADD ORDER TO CURRENT MOTORGUY CURRENT ORDER FIELD
        mDatabaseReference.child(MOTOR_GUYS).child(getCurrentFbUserId()).child("currentOrder").setValue(orderKey);
        // WE DELETE ORDER FROM ORDERS QUEUE
        mDatabaseReference.child(ORDERS_QUEUE).child(orderKey).removeValue();
        // WE ADD ORDER TO ON WAY ORDERS
        mDatabaseReference.child(ON_WAY_ORDERS).child(orderKey).setValue(true);
        // WE UPDATE ORDER ASSIGNMENT WITH MOTOR GUY ID
        mDatabaseReference.child(ORDER_ASSIGNMENT).child(orderKey).setValue(getCurrentFbUserId());


        //NOTE: THIS METHOD WILL BE USED ASSUMING THAT THE CURRENT AUTHENTICATED INDIVIDUAL IS A MOTORGUY
    }
    public boolean isUserAClient() {

        if(mCurrentTypeOfUser == "users") {
            return true;
        } else {
            return false;
        }
    }
    public boolean isUserAMotorGuy() {
        if(mCurrentTypeOfUser == "motorguys") {
            return true;
        } else {
            return false;
        }

    }

    public void initializeUserDetails() {
        //DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference clientRef = mDatabaseReference.child(USERS).child(getCurrentFbUserId());
        clientRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // run some code
                    mCurrentTypeOfUser = "users";
                    currentUserTxtV.append(mCurrentTypeOfUser);
                    //setContentView(R.layout.activity_credentials);

                } else {
                    mCurrentTypeOfUser = "motorguys";
                    currentUserTxtV.append(mCurrentTypeOfUser);
                    //setContentView(R.layout.activity_credentials);

                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                //
                //mCurrentTypeOfUser = "motorGuys";
                currentUserTxtV.append("unknown");


            }
        });
        // https://stackoverflow.com/questions/37397205/google-firebase-check-if-child-exists
        //https://stackoverflow.com/questions/43959582/how-to-check-if-a-value-exists-in-firebase-database-android
    }





    // CLICK EVENTS
    public void makerOrdersClick(View view) {
        Intent makerOrdersClickIntent = new Intent(this, MakeOrderActivity.class);
        startActivity(makerOrdersClickIntent);
        //pushOrderToFirebaseDatabase("Ya me tengo que ir!!!");
    }
    public void onWayOrdersClick(View view) {
        Intent onWayOrdersIntent = new Intent(this, OnWayOrdersActivity.class);
        startActivity(onWayOrdersIntent);
    }
    public void trackOrdersClick(View view) {
        Intent trackOrdersIntent = new Intent(this, TrackOrdersActivity.class);
        startActivity(trackOrdersIntent);
    }

}
