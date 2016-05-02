package com.example.joeyhanlon.hydra;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

/**
 * Activity started when user presses calibrate button in MainActivity
 */
public class CalActivity extends AppCompatActivity implements View.OnClickListener {

    // Navigation
    ImageButton backButton;

    Button calButton;
    Button launchButton;
    int calStage;           // Keeps track of step in calibration process
    int WAIT_TIME = 1200;   // Time to wait for Arduino to calibrate

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cal);

        //backButton = (ImageButton) findViewById(R.id.backButton);
        //backButton.setOnClickListener(this);

        calButton = (Button) findViewById(R.id.calButton);
        calButton.setOnClickListener(this);

        launchButton = (Button) findViewById(R.id.launchButton);
        launchButton.setOnClickListener(this);

        // Stage 0 of calibration
        calStage = 0;
        calButton.setText("Calibrate");
        calButton.setBackground( getResources().getDrawable(R.drawable.cal_button_shape) );
    }

    public void onClick(View view) {
        switch (view.getId()) {

            case (R.id.calButton):
                calibrate();
                break;

            case (R.id.launchButton):
                finish();
                break;
        }
    }

    private void calibrate() {

        launchButton.setVisibility(View.GONE);

        switch (calStage){
            case 0:
                // Start calibration
                HydraSocket.write("C;");

                // Wait for Arduino to acknowledge calibration started
                waitForArd();

                break;

            case 1: // Stage 1 - Low threshold calibration
                // On press, send ACK
                HydraSocket.writeACK();

                // Wait for low calibration to finish
                waitForArd();

                break;

            case 2:
                // Stage 2 - High threshold calibration
                // On press, send ACK
                HydraSocket.writeACK();

                // Wait for high calibration to finish
                waitForArd();

                // Final ack
                HydraSocket.writeACK();

                break;
        }

    }

    // Exit CalActivity with back button
    @Override
    public void onBackPressed(){
        if (calStage == 0){
            finish();
        }
    }

    // Delay for WAIT_TIME so that Arduino can process
    private void waitForArd(){
        calButton.setBackground( getResources().getDrawable(R.drawable.cal_button_wait) );
        switch (calStage){
            case 0:
                calButton.setText("Wait...");
                // Delay for delay time to wait for Arduino
                Handler handler0 = new Handler();
                handler0.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Enter stage 1
                        calButton.setText("Press when relaxed.");
                        calButton.setBackground( getResources().getDrawable(R.drawable.cal_button_shape) );
                        calStage ++;
                    }
                }, WAIT_TIME);

                break;
            case 1:
                calButton.setText("Calibrating...");
                // Delay for delay time to wait for Arduino
                Handler handler1 = new Handler();
                handler1.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Enter stage 2
                        calButton.setText("Press when flexed.");
                        calButton.setBackground( getResources().getDrawable(R.drawable.cal_button_shape) );
                        calStage ++;
                    }
                }, WAIT_TIME);

                break;
            case 2:
                calButton.setText("Calibrating...");
                // Delay for delay time to wait for Arduino
                Handler handler2 = new Handler();
                handler2.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Allow user to recalibrate if desired
                        calStage = 0;
                        calButton.setText("Press again to re-calibrate");
                        launchButton.setVisibility(View.VISIBLE);
                        calButton.setBackground( getResources().getDrawable(R.drawable.cal_button_wait) );
                    }
                }, WAIT_TIME);
                break;
        }


    }

    // Method for delay
    public void doNothing(){}




}