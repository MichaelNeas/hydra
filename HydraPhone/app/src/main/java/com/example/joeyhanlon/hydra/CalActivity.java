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
        calButton.setBackgroundColor(Color.BLUE);

    }

    private class CalButtonListener implements Button.OnClickListener{
        @Override
        public void onClick(View v) {

            // Start calibration
            if(calStage == 0){
                // Start calibration
                HydraSocket.write("C;");
                calButton.setBackgroundColor(Color.RED);
                calButton.setText("Wait...");

                // Wait for Arduino to acknowledge calibration started
                waitForArd();

                // Enter stage 1
                calStage ++;
                calButton.setBackgroundColor(Color.BLUE);
                calButton.setText("Press when RELAXed.");
            }

            // Stage 1 - Low threshold calibration
            if (calStage == 1){
                // On press, send ACK
                HydraSocket.write("1");
                calButton.setBackgroundColor(Color.RED);
                calButton.setText("Calibrating...");

                // Wait for low calibration to finish
                waitForArd();

                // Enter stage 2
                calStage ++;
                calButton.setBackgroundColor(Color.BLUE);
                calButton.setText("Press when FLEXed.");
            }

            // Stage 2 - High threshold calibration
            if (calStage == 2){
                // On press, send ACK
                HydraSocket.write("1");
                calButton.setBackgroundColor(Color.RED);
                calButton.setText("Calibrating...");

                // Wait for high calibration to finish
                waitForArd();

                // Allow user to recalibrate if desired
                calStage = 0;
                calButton.setBackgroundColor(Color.BLUE);
                calButton.setText("Re-calibrate.");
            }
            // Do nothing if there is no message from Arduino or calibration cannot be started
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
        try {
            Thread.sleep(WAIT_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



}