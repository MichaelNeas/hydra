package com.example.joeyhanlon.hydra;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class MainActivity extends AppCompatActivity implements ListView.OnItemClickListener, SeekBar.OnSeekBarChangeListener, View.OnClickListener, RadioGroup.OnCheckedChangeListener, CompoundButton.OnCheckedChangeListener {

    // Child activity codes
    private static final int BT_CONNECT = 3;

    // Toolbar
    Toolbar toolbar;

    // Mode manager (getting real descriptive with the comments)
    ModeManager myModeManager;

    // The functional application window. Used to mask it during BT init and such.
    LinearLayout inputPane;

    // Field shows string to be sent
    EditText inputField;
    FloatingActionButton fab;

    // To display connected device info in main window
    TextView connectedDeviceName;
    TextView connectedDeviceAddress;

    // SELECTED ACTION
    ListView modesListView;
    // Planning to replace the radio group with the listview above, which will also have radio
    //  buttons and all that jazz
    RadioGroup actionList;
    RadioButton grip;
    RadioButton pinch;
    RadioButton click;
    RadioButton point;
    RadioButton hook;
    Button saveModeButton, newModeButton;

    // ----- Hydra Parameter UI Elements -----
    // STATIC / DYNAMIC
    Switch dynSwitch;
    // SERVO SPEED
    CardView speedCard;
    SeekBar servoSpeedSeekBar0;
    SeekBar servoSpeedSeekBar1;
    SeekBar servoSpeedSeekBar2;
    TextView servoSpeedIndicator0;
    TextView servoSpeedIndicator1;
    TextView servoSpeedIndicator2;
    // GRIP DEPTH
    SeekBar gripDepthSeekBar0;
    SeekBar gripDepthSeekBar1;
    SeekBar gripDepthSeekBar2;
    TextView gripDepthIndicator0;
    TextView gripDepthIndicator1;
    TextView gripDepthIndicator2;
    // ACTION THRESHOLD
    SeekBar actThreshSeekBar;
    TextView actThreshIndicator;
    // WRITE DELAY
    SeekBar writeDelSeekBar;
    TextView writeDelIndicator;
    // ----- /Hydra Parameter UI Elements -----

    // String array to be sent to the arm, initialized to default values
    String[] SEND_STRING = {"1=D;","2=0.5;","3=5.0;","4=100,100,100;","5=5.0,5.0,5.0;"};

    // BT thread variables
    TextView textStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Start bluetooth connection activity on launch
        Intent btIntent = new Intent(this, BluetoothSetupActivity.class);
        startActivityForResult(btIntent, BT_CONNECT);

        setContentView(R.layout.activity_main);

        // Initialize toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(null);

        // ----- BT WINDOW SETUP -----
        textStatus = (TextView)findViewById(R.id.status);

        // ----- /BT WINDOW SETUP -----


        // ----- MAIN WINDOW SETUP -----
        inputPane = (LinearLayout)findViewById(R.id.inputPane);
        inputField = (EditText)findViewById(R.id.input);

        // TODO instantiate modesListView, saveModeButton, newModeButton

        actionList = (RadioGroup) findViewById(R.id.actionList);
        // Relay information regarding selected action on click
        actionList.setOnCheckedChangeListener(this);

        // TODO imageView in sendActionWindow

        connectedDeviceName = (TextView)findViewById(R.id.connectedDeviceName);
        connectedDeviceAddress = (TextView)findViewById(R.id.connectedDeviceAddress);

        speedCard = (CardView)findViewById(R.id.speedCard);
        servoSpeedSeekBar0 = (SeekBar)findViewById(R.id.servoSpeedSeekBar0);
        servoSpeedSeekBar1 = (SeekBar)findViewById(R.id.servoSpeedSeekBar1);
        servoSpeedSeekBar2 = (SeekBar)findViewById(R.id.servoSpeedSeekBar2);
        servoSpeedIndicator0 = (TextView)findViewById(R.id.servoSpeedIndicator0);
        servoSpeedIndicator1 = (TextView)findViewById(R.id.servoSpeedIndicator1);
        servoSpeedIndicator2 = (TextView)findViewById(R.id.servoSpeedIndicator2);
        servoSpeedSeekBar0.setOnSeekBarChangeListener(this);
        servoSpeedSeekBar1.setOnSeekBarChangeListener(this);
        servoSpeedSeekBar2.setOnSeekBarChangeListener(this);

        gripDepthSeekBar0 = (SeekBar)findViewById(R.id.gripDepthSeekBar0);
        gripDepthSeekBar1 = (SeekBar)findViewById(R.id.gripDepthSeekBar1);
        gripDepthSeekBar2 = (SeekBar)findViewById(R.id.gripDepthSeekBar2);
        gripDepthIndicator0 = (TextView)findViewById(R.id.gripDepthIndicator0);
        gripDepthIndicator1 = (TextView)findViewById(R.id.gripDepthIndicator1);
        gripDepthIndicator2 = (TextView)findViewById(R.id.gripDepthIndicator2);
        gripDepthSeekBar0.setOnSeekBarChangeListener(this);
        gripDepthSeekBar1.setOnSeekBarChangeListener(this);
        gripDepthSeekBar2.setOnSeekBarChangeListener(this);

        dynSwitch = (Switch)findViewById(R.id.dynSwitch);

        actThreshSeekBar = (SeekBar) findViewById(R.id.actThreshSeekBar);
        actThreshSeekBar.setOnSeekBarChangeListener(this);
        actThreshIndicator = (TextView) findViewById(R.id.actThreshIndicator);

        writeDelSeekBar = (SeekBar) findViewById(R.id.writeDelSeekBar);
        writeDelSeekBar.setOnSeekBarChangeListener(this);
        writeDelIndicator = (TextView) findViewById(R.id.writeDelIndicator);

        fab = (FloatingActionButton)findViewById(R.id.fabSave);
        fab.setOnClickListener(this);

        // Create ModeManager to store list of modes
        myModeManager = new ModeManager();

        // TODO add all default modes to list
        myModeManager.addNewMode("Default Grip", true, 0.5f, 5.0f, 100, 100, 100, 5.0f, 5.0f, 5.0f);
        setMode(myModeManager.getCurrentMode());
    }


    // ----- MAIN WINDOW METHODS -----


    // NEW mode selection
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        setMode((HydraMode) parent.getItemAtPosition((int) id));
    }


    // Gesture selection, planning to get rid of
    @Override
    public void onCheckedChanged(RadioGroup rg, int checkedId) {
        switch (rg.getCheckedRadioButtonId()) {
            case (R.id.grip):
                break;
            case (R.id.pinch):
                break;
            case (R.id.click):
                break;
            case (R.id.point):
                break;
            case (R.id.hook):
                break;
            default:
                break;
        }
    }

    // OnClick method for all buttons
    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case (R.id.fabSave):

                //sendArduinoMessage("1=D;2=0.5;3=5.0;4=100,100,100;5=5.0,5.0,5.0;");

                // Save currently selected mode with currently selected settings
                saveHydraMode();
                break;

            // TODO new mode button
            // myModeManager.addNewBlankMode();
        }
    }

    // Seek bar settings
    @Override
    public void onProgressChanged (SeekBar seekBar,int progressValue, boolean b){

        String message = "";        // Message to build for Arduino

        // Action threshold adjusted
        if (seekBar == actThreshSeekBar){

            // Convert progress bar value
            float param2 = (float) (actThreshSeekBar.getProgress()/100) * (float) (0.75 - 0.05) + (float) 0.05;
            // String of float value
            String param2Str = Float.toString(param2);

            message = "2=" + param2Str + ";";
            actThreshIndicator.setText(param2Str);
        }

        // Write delay adjusted
        else if (seekBar == writeDelSeekBar) {

            // Convert progress bar value
            float param3 = (float) (writeDelSeekBar.getProgress()/100) * (float) (10.0 - 1.0) + (float) 1.0;
            // String of float value
            String param3Str = Float.toString(param3);

            message = "3=" + param3Str + ";";
            writeDelIndicator.setText(param3Str);
        }

        // A grip depth adjusted
        else if (seekBar == gripDepthSeekBar0 || seekBar == gripDepthSeekBar1 || seekBar == gripDepthSeekBar2){

            // Convert each depth value to string
            String depthProg0 = Integer.toString(gripDepthSeekBar0.getProgress());
            String depthProg1 = Integer.toString(gripDepthSeekBar1.getProgress());
            String depthProg2 = Integer.toString(gripDepthSeekBar2.getProgress());

            // Message includes each Servo's depth value
            message = "4=" + depthProg0 + "," + depthProg1 + "," + depthProg2 + ";";

            // Only need to update changed depth's indicator value
            switch (seekBar.getId()){
                case (R.id.gripDepthSeekBar0):
                    gripDepthIndicator0.setText(depthProg0);
                    break;
                case (R.id.gripDepthSeekBar1):
                    gripDepthIndicator1.setText(depthProg1);
                    break;
                case (R.id.gripDepthSeekBar2):
                    gripDepthIndicator2.setText(depthProg2);
                    break;
            }
        }

        // A servo speed adjusted
        else if (seekBar == servoSpeedSeekBar0 || seekBar == servoSpeedSeekBar1 || seekBar == servoSpeedSeekBar2) {

            // Convert progress bar values
            float param5a = (float) (servoSpeedSeekBar0.getProgress()/100) * (float) (10.0 - 1.0) + (float) 1.0;
            float param5b = (float) (servoSpeedSeekBar1.getProgress()/100) * (float) (10.0 - 1.0) + (float) 1.0;
            float param5c = (float) (servoSpeedSeekBar2.getProgress()/100) * (float) (10.0 - 1.0) + (float) 1.0;

            // Convert each speed value to string
            String param5aStr = Float.toString(param5a);
            String param5bStr = Float.toString(param5b);
            String param5cStr = Float.toString(param5c);

            // Message includes each Servo's speed value
            message = "5=" + param5aStr + "," + param5bStr + "," + param5cStr + ";";

            // Only need to update changed speed's indicator value
            switch (seekBar.getId()){
                case (R.id.servoSpeedSeekBar0):
                    servoSpeedIndicator0.setText(param5aStr);
                    break;
                case (R.id.servoSpeedSeekBar1):
                    servoSpeedIndicator1.setText(param5bStr);
                    break;
                case (R.id.servoSpeedSeekBar2):
                    servoSpeedIndicator2.setText(param5cStr);
                    break;
            }
        }

        sendArduinoMessage(message);  // Send built message to bluetooth

    }

    @Override
    public void onStartTrackingTouch (SeekBar seekBar){}
    @Override
    public void onStopTrackingTouch (SeekBar seekBar){}

    /*
    public String formatIndicatorField(int speedVal) {
        if (speedVal == 10){return "10";}
        else {return "0" + Integer.toString(speedVal);}
    }
    */

    // Switch for dynamic option
    @Override
    public void onCheckedChanged(CompoundButton sw, boolean isChecked) {
        if (isChecked) {
            sendArduinoMessage("1=D;");
            //disableSpeed();
            //SEND_STRING[0] = "1=S;";
        }
        else {
            sendArduinoMessage("1=S;");
            //enableSpeed();
            //SEND_STRING[0] = "1=D;";
        }

        //updateSendString();
        //send();
    }

    /*
    public void enableSpeed() {
        speedCard.setVisibility(View.VISIBLE);
    }

    public void disableSpeed() {
        speedCard.setVisibility(View.GONE);
    }

    // Update debug text field with string to be sent
    public void updateSendString() {
        String toSet = "";
        for (int i = 0; i < 5; i ++) {
            toSet = toSet + SEND_STRING[i];
        }
        inputField.setText(toSet);
    }

    // send current SEND_STRING over the current BT connection
    public void send() {
        if(myThreadConnected!=null){
            byte[] bytesToSend = inputField.getText().toString().getBytes();
            myThreadConnected.write(bytesToSend);
            inputField.setText("");
        }
    }
    */



    // ----- /MAIN WINDOW METHODS -----

    // ----- MODE MANAGING METHODS -----
    // Saves the set parameters for the current mode to its HydraMode class
    private void saveHydraMode(){
        HydraMode mode = myModeManager.getCurrentMode();

        // Dynamic or Static
        mode.setParam(1, dynSwitch.getShowText());

        // Action threshold
        float param2 = (float) (actThreshSeekBar.getProgress()/100) * (float) (0.75 - 0.05) + (float) 0.05;
        mode.setParam(2, param2);

        // Write delay
        float param3 = (float) (writeDelSeekBar.getProgress()/100) * (float) (10.0 - 1.0) + (float) 1.0;
        mode.setParam(3, param3);

        // Grip depth
        mode.setParam(4, 0, gripDepthSeekBar0.getProgress());
        mode.setParam(4, 1, gripDepthSeekBar1.getProgress());
        mode.setParam(4, 2, gripDepthSeekBar2.getProgress());

        // Servo speed
        float param5a = (float) (servoSpeedSeekBar0.getProgress()/100) * (float) (10.0 - 1.0) + (float) 1.0;
        mode.setParam(5, 0, param5a);
        float param5b = (float) (servoSpeedSeekBar1.getProgress()/100) * (float) (10.0 - 1.0) + (float) 1.0;
        mode.setParam(5, 1, param5b);
        float param5c = (float) (servoSpeedSeekBar2.getProgress()/100) * (float) (10.0 - 1.0) + (float) 1.0;
        mode.setParam(5, 2, param5c);

        //TODO store myModeManager in phone memory

        sendArduinoMessage(mode.getModeString());
    }

    // Sets current mode to mode (updates UI, sends message)
    private void setMode(HydraMode mode){
        myModeManager.setCurrentMode(mode);
        // Update parameter UI stuff
        // Dynamic or Static
        dynSwitch.setShowText((boolean) mode.getParam(1));

        // TODO implement missing cards
        //Action threshold
        int progAT = (int) Math.round( 100 * ( (float) mode.getParam(2) / (0.75 - 0.05)) );
        actThreshSeekBar.setProgress(progAT);

        // Write delay
        int progWD = (int) Math.round( 100 * ( (float) mode.getParam(3) / (10.0 - 1.0)) );
        writeDelSeekBar.setProgress(progWD);

        // Grip depth
        int[] progsGD = (int[]) mode.getParam(4);
        gripDepthSeekBar0.setProgress(progsGD[0]);
        gripDepthSeekBar1.setProgress(progsGD[1]);
        gripDepthSeekBar2.setProgress(progsGD[2]);

        // Servo speed
        float[] progsSS = (float[]) mode.getParam(5);
        int progSS = (int) Math.round(100 * ((float) progsSS[0] / (10.0 - 1.0)));
        servoSpeedSeekBar0.setProgress(progSS);
        progSS = (int) Math.round(100 * ((float) progsSS[1] / (10.0 - 1.0)));
        servoSpeedSeekBar1.setProgress(progSS);
        progSS = (int) Math.round(100 * ((float) progsSS[2] / (10.0 - 1.0)));
        servoSpeedSeekBar2.setProgress(progSS);

        // Send Arduino the new mode
        sendArduinoMessage(mode.getModeString());
    }
    // ----- /MODE MANAGING METHODS -----


    // ----- BLUETOOTH COMM METHODS -----
    // Send the given string to Arduino via Bluetooth
    public void sendArduinoMessage(String message){
        // For debugging sent messages
        inputField.setText(message);
        HydraSocket.write(message);
    }

    // Return string sent by Arduino, used for calibration
    public String readArduinoMessage(){
        return HydraSocket.read();
    }
    // ----- /BLUETOOTH COMM METHODS -----


    // ----- OPTIONS MENU -----

    // TODO setup options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // ----- /OPTIONS MENU -----


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Bluetooth connection activity has completed
        if (requestCode == BT_CONNECT){

            String name = HydraSocket.getDeviceName();
            String address = HydraSocket.getDeviceAddress();

            // There is a bluetooth connection
            if (resultCode == RESULT_OK){

                // Show connected device

                Toast.makeText(MainActivity.this,
                        "Talking to\n" +
                                "Name: " + name + "\n"
                                + "Address: " + address,
                        Toast.LENGTH_LONG).show();

                connectedDeviceName.setText(name);
                connectedDeviceAddress.setText(address);

            }
            // No bluetooth device
            else {
                Toast.makeText(MainActivity.this,
                        "No device connected.",
                        Toast.LENGTH_LONG).show();
                connectedDeviceName.setText("Not connected.");
                connectedDeviceAddress.setText(null);
            }
        }

    }
}