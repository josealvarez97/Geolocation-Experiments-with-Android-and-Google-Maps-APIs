package io.google.devicetracker2;

import android.content.Intent;
import android.nfc.Tag;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static android.os.Build.VERSION_CODES.M;
import static io.google.devicetracker2.FirebaseDatabaseManagement.MOTOR_GUYS;
import static io.google.devicetracker2.FirebaseDatabaseManagement.USERS;

public class CredentialsActivity extends AppCompatActivity {


    // AUTHENTICATION STUFF
    private FirebaseAuth mAuth;
    public static final String VALID_EMAIL = "valid_email";
    public static final String VALID_PASSWORD = "valid_password";
    public static final String TYPE_OF_INDIVIDUAL = "type_of_individual";
    //private boolean mSuccesfullAuth = false;
    private String mEmail;
    private String mPassword;

    // OTHER STUFF
    private static final String TAG = MainActivity.class.getSimpleName();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credentials);

        mAuth = FirebaseAuth.getInstance();




    }

    public void logInClick(View view) {
        TextView emailTextView = (TextView) findViewById(R.id.emailInput);
        TextView passwordTextView = (TextView) findViewById(R.id.passwordInput);
        mEmail = emailTextView.getText().toString();
        mPassword = passwordTextView.getText().toString();

        signIn(mEmail, mPassword);
    }

    public void signIn(String email, String password) {

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success
                            Log.d(TAG, "signInWithEmail:success");
                            //FirebaseUser fbUser = mAuth.getCurrentUserObj();
                            // Create an intent to change activity
                            //mSuccesfullAuth = true;
                            validateAndPassInformation();

                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(CredentialsActivity.this, "Authentication failed.",
                                     Toast.LENGTH_LONG).show();

                        }
                    }
                });
    }


    public void validateAndPassInformation() {
        initializeUserDetails();
    }

    public void validateAndPassInformation(String typeOfIndividual) {
        Intent credentialsForUsersIntent = new Intent(this, MainActivity.class);
        Intent credentialsForMGuysIntent = new Intent(this, MotorGuysMainActivity.class);

        if(typeOfIndividual.equals(USERS)) {

            credentialsForUsersIntent.putExtra(VALID_EMAIL, mEmail);
            credentialsForUsersIntent.putExtra(VALID_PASSWORD, mPassword);
            credentialsForUsersIntent.putExtra(TYPE_OF_INDIVIDUAL, typeOfIndividual);
            startActivity(credentialsForUsersIntent);

        } else if (typeOfIndividual.equals(MOTOR_GUYS)){
            startActivity(credentialsForMGuysIntent);
        }




    }



    public void initializeUserDetails() {
        //DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

        DatabaseReference clientRef = FirebaseDatabase.getInstance().getReference().child(MOTOR_GUYS).child(mAuth.getCurrentUser().getUid());
        clientRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    validateAndPassInformation(MOTOR_GUYS);

                } else {
                    validateAndPassInformation(USERS);
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {


            }
        });
        // https://stackoverflow.com/questions/37397205/google-firebase-check-if-child-exists
        //https://stackoverflow.com/questions/43959582/how-to-check-if-a-value-exists-in-firebase-database-android
    }



}
