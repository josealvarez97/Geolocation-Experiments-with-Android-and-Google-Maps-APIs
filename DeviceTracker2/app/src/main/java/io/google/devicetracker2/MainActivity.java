package io.google.devicetracker2;

import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initializing LOCATION
        createLocationRequest();
        getCurrentLocation();


        // Initializing AUTHENTICATION
        mAuth = FirebaseAuth.getInstance();
        //createAccount("alvarez.jo.2017@gmail.com", "superPikachu");
        signIn("alvarez.jo.2017@gmail.com", "superPikachu");
        //FirebaseUser theFbUser = mAuth.getCurrentUser();


        // Initializing DATABASE
        initializeDatabase();
        writeNewUserToDatabase(getCurrentUserId(), getCurrentUser(mAuth.getCurrentUser()).username, getCurrentUser(mAuth.getCurrentUser()).email);
        //writeNewUserToDatabase("JaviId", "displayName", "afuckingemail");
        //updateUserEmail("JaviId", "j.alvarez@minerva.kgi.edu");
        //mDatabase.getReference("JoseId").addValueEventListener(userListener);
        //mDatabase.getReference("JoseId").addListenerForSingleValueEvent(userListener);





        // Working out LOCATION updates
        mLocationCallback = new LocationCallback () {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for(Location location : locationResult.getLocations()) {
                    //UpdateUI with location data
                    // ...

                    // Update Current Location
                    mCurrentLocation = location;

                    // UpdateUserLocationOnDatabase
                    updateUserLocationOnDatabase(getCurrentUserId(), mCurrentLocation);

                    // Update UserInterface
                    updateUILocation(/*Maybe I'll add an overload here with the location*/);
                }
            }
        };

        //updateValuesFromBundle(savedInstanceState); // Really need to know what's going on here...



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
        mDatabaseReference.child("users").child(userID).child("latitude").setValue(latitude);
        mDatabaseReference.child("users").child(userID).child("longitude").setValue(longitude);
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


    public User getCurrentUser(FirebaseUser fbUser) {

        //FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();


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
    public String getCurrentUserId() {
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
        User user = getCurrentUser(fbUser);
    }

}
