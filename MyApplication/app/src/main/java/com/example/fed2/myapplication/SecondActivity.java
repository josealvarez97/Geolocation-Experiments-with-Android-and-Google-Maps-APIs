package com.example.fed2.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.Random;

public class SecondActivity extends AppCompatActivity {

    public static final String TOTAL_COUNT = "total_count";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        showRandomNumber();
    }

    public void showRandomNumber(){
        // Retrieve neccessary information
        Intent intentSent = getIntent();
        int currentCount = intentSent.getIntExtra(TOTAL_COUNT, 0);


        //Get necessary views or any other thing
        TextView headingView = (TextView) findViewById(R.id.textview_label);
        TextView randomView = (TextView) findViewById(R.id.textview_random);

        // Let's compute things
        Random random = new Random();
        int randomInt = 0;
        if(currentCount>0){
            randomInt = random.nextInt(currentCount);
        }


        // Display the output
        randomView.setText(Integer.toString(randomInt));



        // Substitute the max value into the string resource
        // for the heading, and update the heading
        headingView.setText(getString(R.string.random_heading, currentCount));

        // We modify and get... and set


    }
}
