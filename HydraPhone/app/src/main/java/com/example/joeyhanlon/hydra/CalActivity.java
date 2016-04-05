package com.example.joeyhanlon.hydra;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/**
 * Activity started when user presses calibrate button in MainActivity
 */
public class CalActivity extends AppCompatActivity {
    Button calButton;
    boolean calibrating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cal);

        calButton = (Button) findViewById(R.id.calButton);
        calButton.setText("Relax!");

        CalButtonListener myCalListener = new CalButtonListener();
        calButton.setOnClickListener(myCalListener);

        HydraSocket.write("C;");    // Start calibration
        calibrating = true;         // Indicate that cal is underway
    }

    private class CalButtonListener implements Button.OnClickListener{
        @Override
        public void onClick(View v) {

            // Stage 1 - Wait for Arduino command to relax
            if (HydraSocket.read().equals("Relax!")){

                // On press, send ACK
                HydraSocket.write("1");

                // Wait for calibration to finish
                while (!HydraSocket.read().equals("Low set.")){}

                // Tell user to flex after low is set
                calButton.setText("Flex!");
            }

            // Stage 2 - Wait for Arduino command to flex
            if (HydraSocket.read().equals("Flex!")){

                // On press, send ACK
                HydraSocket.write("1");

                // Wait for calibration to finish
                while (!HydraSocket.read().equals("High set.")){}

                // Allow user to recal if desired
                calButton.setText("Re-calibrate.");

                // Not currently calibrating, so activity can exit
                calibrating = false;
            }

            // Stage 3 - If not calibrating currently, start new cal
            if (calibrating == false){

                // Initiate cal with Arduino
                HydraSocket.write("C;");

                // Tell user to relax, goes back to Stage 1
                calButton.setText("Relax!");
            }

            else {
                // Do nothing if there is no message from Arduino or calibration cannot be started
            }

        }
    }

    // Exit CalActivity with back button
    @Override
    public void onBackPressed(){
        // Only exit if calibration is not currently occurring
        if (!calibrating){
            finish();
        }
    }


}