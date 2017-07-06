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

import static android.os.Build.VERSION_CODES.M;

public class CredentialsActivity extends AppCompatActivity {


    // AUTHENTICATION STUFF
    private FirebaseAuth mAuth;
    public static final String VALID_EMAIL = "valid_email";
    public static final String VALID_PASSWORD = "valid_password";
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
        Intent credentialsIntent = new Intent(this, MainActivity.class);

        credentialsIntent.putExtra(VALID_EMAIL, mEmail);
        credentialsIntent.putExtra(VALID_PASSWORD, mPassword);
        startActivity(credentialsIntent);
    }
}
