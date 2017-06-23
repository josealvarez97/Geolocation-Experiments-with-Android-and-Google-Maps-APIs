package com.example.fed2.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    private static final String TOTAL_COUNT = "total_count";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * Show a toast
     * @param view -- the view that is clicked
     */
    public void toastMe(View view){
        //Toast myToast = Toast.makeText(this, message, duration);
        Toast myToast = Toast.makeText(this, "Hello Toast xD!!!", Toast.LENGTH_SHORT );
        myToast.show();
    }

    public void addUpCount(View view){
        // Get View by id or better said the text view
        TextView counterTextView = (TextView) findViewById(R.id.textView);

        // get current count
        Integer  currentCount = Integer.parseInt(counterTextView.getText().toString());

        // add up count
        currentCount++;

        // Set new value
        counterTextView.setText(currentCount.toString());
    }

    public void randomMe(View view){

        //Get the textview with the count and get its current count
        TextView counterTextView = (TextView) findViewById(R.id.textView);
        int  currentCount = Integer.parseInt(counterTextView.getText().toString());


        //Create an intent to start the second activity
        Intent randomIntent = new Intent(this, SecondActivity.class);
        randomIntent.putExtra(TOTAL_COUNT, currentCount);

        // Start the new activity
        startActivity(randomIntent);

    }
}
