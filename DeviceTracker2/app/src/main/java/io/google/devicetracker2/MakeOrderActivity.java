package io.google.devicetracker2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MakeOrderActivity extends AppCompatActivity {

    private FirebaseDatabaseManagement mFbDatabaseManagmentObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_order);

        mFbDatabaseManagmentObj = new FirebaseDatabaseManagement();


    }





    public void pushOrder(View view){

        mFbDatabaseManagmentObj.writeToDatabase();

        mFbDatabaseManagmentObj.pushOrderToFirebaseDatabase("Dos piezas de buen pollo!");
        /*Intent mainActivityIntent = new Intent(this, MainActivity.class);
        startActivity(mainActivityIntent);*/
    }
}
