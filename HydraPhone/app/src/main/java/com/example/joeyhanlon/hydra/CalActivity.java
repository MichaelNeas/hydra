package com.example.joeyhanlon.hydra;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
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
    int calStage;           // Keeps track of step in calibration process
    int WAIT_TIME = 2000;   // Time to wait for Arduino to calibrate

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cal);

        backButton = (ImageButton) findViewById(R.id.backButton);
        backButton.setOnClickListener(this);

        calButton = (Button) findViewById(R.id.calButton);
        calButton.setOnClickListener(this);

        // Stage 0 of calibration
        calStage = 0;
        calButton.setText("Calibrate.");
        calButton.setBackgroundResource(R.color.colorPrimary);

    }

    public void onClick(View view) {
        switch (view.getId()) {
            case (R.id.backButton):
                // To main activity
                onBackPressed();
                break;

            case (R.id.calButton):
                calibrate();
                break;
        }
    }

    private void calibrate() {

        switch (calStage){
            case 0:
                // Start calibration
                HydraSocket.write("C;");

                // Wait for Arduino to acknowledge calibration started
                waitForArd();

                // Enter stage 1
                calStage ++;
                break;

            case 1: // Stage 1 - Low threshold calibration
                calButton.setText(Html.fromHtml("Press when <b>relaxed</b>."));
                // On press, send ACK
                HydraSocket.writeACK();

                // Wait for low calibration to finish
                waitForArd();

                // Enter stage 2
                calStage ++;
                break;

            case 2:
                // Stage 2 - High threshold calibration
                calButton.setText(Html.fromHtml("Press when <b>flexed</b>."));
                // On press, send ACK
                HydraSocket.writeACK();

                // Wait for high calibration to finish
                waitForArd();

                HydraSocket.writeACK();

                // Allow user to recalibrate if desired
                calStage = 0;
                calButton.setText("");
                break;
        }

    }

    // Exit CalActivity with back button
    @Override
    public void onBackPressed(){
        finish();
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