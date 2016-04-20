package com.example.joeyhanlon.hydra;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.Objects;

/**
 * Activity started when user presses calibrate button in MainActivity
 */
public class CalActivity extends AppCompatActivity {
    Button calButton;
    boolean calibrating;
    int calStage;           // Keeps track of step in calibration process
    int WAIT_TIME = 2000;   // Time to wait for Arduino to calibrate

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cal);

        calButton = (Button) findViewById(R.id.calButton);

        CalButtonListener myCalListener = new CalButtonListener();
        calButton.setOnClickListener(myCalListener);

        // Stage 0 of calibration
        calStage = 0;
        calButton.setText("Calibrate.");
        calButton.setBackgroundResource(R.color.colorPrimary);

    }

    private class CalButtonListener implements Button.OnClickListener{
        @Override
        public void onClick(View v) {
            switch (calStage){
                case 0:
                    // Start calibration
                    HydraSocket.write("C;");

                    // Wait for Arduino to acknowledge calibration started
                    waitForArd();

                    // Enter stage 1
                    calStage ++;
                    calButton.setBackgroundResource(R.color.colorPrimary);
                    calButton.setText("Press when RELAXed.");
                    break;
                case 1: // Stage 1 - Low threshold calibration
                    // On press, send ACK
                    HydraSocket.writeACK();

                    // Wait for low calibration to finish
                    waitForArd();

                    // Enter stage 2
                    calStage ++;
                    calButton.setBackgroundResource(R.color.colorPrimary);
                    calButton.setText("Press when FLEXed.");
                    break;
                case 2: // Stage 2 - High threshold calibration
                    // On press, send ACK
                    HydraSocket.writeACK();

                    // Wait for high calibration to finish
                    waitForArd();

                    HydraSocket.writeACK();

                    // Allow user to recalibrate if desired
                    calStage = 0;
                    calButton.setBackgroundResource(R.color.colorPrimary);
                    calButton.setText("Re-calibrate.");
                    break;
            }
        }
    }

    // Exit CalActivity with back button
    @Override
    public void onBackPressed(){
        // Only exit if calibration is not currently occurring
        if (calStage == 0){
            finish();
        }
        // If calibrating, disregard
        else {
            return;
        }
    }

    // Delay for WAIT_TIME so that Arduino can process
    private void waitForArd(){
        calButton.setBackgroundResource(R.color.colorAccent);
        switch (calStage){
            case 0:
                calButton.setText("Wait...");
                break;
            case 1:case 2:
                calButton.setText("Calibrating...");
                break;
        }

        try {
            Thread.sleep(WAIT_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



}