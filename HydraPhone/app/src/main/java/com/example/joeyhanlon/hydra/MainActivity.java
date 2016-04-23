package com.example.joeyhanlon.hydra;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.Map;


public class MainActivity extends AppCompatActivity implements ListView.OnItemClickListener, SeekBar.OnSeekBarChangeListener, View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    // Bluetooth activity code
    private static final int BT_CONNECT = 1;

    // Handles storing/setting of different modes
    ModeManager myModeManager;

    // Phone memory storage manager
    MemoryManager myMemoryManager;
    Map<String, ?> modesInMemory;
    // To parse java objects as [json] strings
    Gson gson;

    // The functional application window. Used to mask it during BT init and such.
    LinearLayout inputPane;

    // Debugging fields
    EditText inputField;

    // To display connected device info in main window
    TextView connectedDeviceName;
    TextView connectedDeviceAddress;


    // ----- Hydra UI Elements -----


    // --- HYDRA CONTROL ---

    // List of Hydra Modes
    ListView modesListView;

    // Text showing current mode
    TextView currentModeText;

    // Buttons to affect mode settings
    ImageButton saveModeButton, addModeButton, resetModeButton;

    // Buttons to interact with arm
    ImageButton calButton, breakButton;

    // Button for bluetooth activity
    ImageButton btoothButton;

    // --- /HYDRA CONTROL ---


    // --- PARAMETER CONTROL ---

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

    // --- PARAMETER CONTROL ---


    // ----- /Hydra Parameter UI Elements -----


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Start bluetooth connection activity on launch
        Intent btIntent = new Intent(this, BluetoothSetupActivity.class);
        startActivityForResult(btIntent, BT_CONNECT);

        setContentView(R.layout.activity_main);

        // ----- MAIN WINDOW SETUP -----
        inputPane = (LinearLayout)findViewById(R.id.inputPane);
        inputField = (EditText)findViewById(R.id.input);

        // Bluetooth info display
        connectedDeviceName = (TextView)findViewById(R.id.connectedDeviceName);
        connectedDeviceAddress = (TextView)findViewById(R.id.connectedDeviceAddress);

        modesListView = (ListView)findViewById(R.id.modesListView);
        currentModeText = (TextView)findViewById(R.id.currentModeText);

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
        dynSwitch.setOnCheckedChangeListener(this);

        actThreshSeekBar = (SeekBar) findViewById(R.id.actThreshSeekBar);
        actThreshSeekBar.setOnSeekBarChangeListener(this);
        actThreshIndicator = (TextView) findViewById(R.id.actThreshIndicator);

        writeDelSeekBar = (SeekBar) findViewById(R.id.writeDelSeekBar);
        writeDelSeekBar.setOnSeekBarChangeListener(this);
        writeDelIndicator = (TextView) findViewById(R.id.writeDelIndicator);

        addModeButton = (ImageButton) findViewById(R.id.addModeButton);
        addModeButton.setOnClickListener(this);
        saveModeButton = (ImageButton) findViewById(R.id.saveModeButton);
        saveModeButton.setOnClickListener(this);
        resetModeButton = (ImageButton) findViewById(R.id.resetModeButton);
        resetModeButton.setOnClickListener(this);

        calButton = (ImageButton) findViewById(R.id.calButton);
        calButton.setOnClickListener(this);
        breakButton = (ImageButton) findViewById(R.id.breakButton);
        breakButton.setOnClickListener(this);

        btoothButton = (ImageButton) findViewById(R.id.btoothButton);

        // Create ModeManager to store list of modes
        myModeManager = new ModeManager(this.getApplicationContext());

        // Instantiate memory manager with activity context
        myMemoryManager = new MemoryManager(this.getApplicationContext());
        gson = new Gson();

        // Set up adapter for list of modes
        myModeManager.createAdapter(this);
        modesListView.setAdapter(myModeManager.getAdapter());
        modesListView.setOnItemClickListener(this);

        // Mode testing ----------

        myModeManager.addNewMode("Init", false, 0.5f, 5.0f, 0, 100, 100, 0f, 5.0f, 5.0f);

        // TODO MODE NOT BEING STORED PROPERLY WITH GSON NEED TO FIX IT

        modesInMemory = myMemoryManager.getAllFromMemory();

        // Only add default modes if they do not already exist in the memory map
        if (modesInMemory.isEmpty()) {
            HydraMode defaultGrip = new HydraMode();
            defaultGrip.setName("Default Grip");
            myMemoryManager.writeToMemory(defaultGrip.getName(), gson.toJson(defaultGrip));

            HydraMode test1 = new HydraMode();
            test1.setName("Click");
            myMemoryManager.writeToMemory(test1.getName(), gson.toJson(test1));
        }

        // Load all modes in memory and add them to the mode manager
        for (Map.Entry<String, ?> entry : modesInMemory.entrySet()) {
            String json = entry.getValue().toString();
            HydraMode temp = gson.fromJson(json, HydraMode.class);
            myModeManager.addMode(temp);
        }


        // ----------

    }


    // ----- MAIN WINDOW METHODS -----

    // Mode selection
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        setMode(myModeManager.getMode(position));
    }


    // Button implementation
    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            // Add new mode
            case R.id.addModeButton:
                newHydraMode();
                break;
            // Reset stored settings of current mode
            case R.id.resetModeButton:
                setMode(myModeManager.getCurrentMode());
                break;
            // Save new settings to current mode
            case R.id.saveModeButton:
                saveHydraMode();
                break;

            // Start calibration activity
            case R.id.calButton:
                Intent calIntent = new Intent(this, CalActivity.class);
                startActivity(calIntent);
                break;
            // Send acknowledgement to Ard to break grip if button is enabled
            case R.id.breakButton:

                break;

            // Start Bluetooth activity
            case R.id.btoothButton:
                Intent btIntent = new Intent(this, BluetoothSetupActivity.class);
                startActivityForResult(btIntent, BT_CONNECT);
                break;
        }
    }

    // Seek bar settings
    @Override
    public void onProgressChanged (SeekBar seekBar,int progressValue, boolean b){

        /*
        speed 1 10
        depth 0 100 %
        thresh 5 75 %
        write delay 0 to 10
        */

        String speedDisplay = Integer.toString((progressValue / 11) + 1);
        String depthDisplay = Integer.toString(progressValue) + "%";

        switch (seekBar.getId()) {

            case (R.id.servoSpeedSeekBar0):
                servoSpeedIndicator0.setText(speedDisplay);
                break;
            case (R.id.servoSpeedSeekBar1):
                servoSpeedIndicator1.setText(speedDisplay);
                break;
            case (R.id.servoSpeedSeekBar2):
                servoSpeedIndicator2.setText(speedDisplay);
                break;
            case (R.id.gripDepthSeekBar0):
                gripDepthIndicator0.setText(depthDisplay);
                break;
            case (R.id.gripDepthSeekBar1):
                gripDepthIndicator1.setText(depthDisplay);
                break;
            case (R.id.gripDepthSeekBar2):
                gripDepthIndicator2.setText(depthDisplay);
                break;
            case (R.id.actThreshSeekBar):
                String sensDisplay = Integer.toString(((progressValue*7) / 10) + 5) + "%";
                actThreshIndicator.setText(sensDisplay);
                break;
            case (R.id.writeDelSeekBar):
                String delDisplay = Integer.toString(progressValue / 10);
                writeDelIndicator.setText(delDisplay);
                break;

            default:
                break;
        }

    }

    @Override
    public void onStartTrackingTouch (SeekBar seekBar){}
    @Override
    public void onStopTrackingTouch (SeekBar seekBar) {

        String message = "";        // Message to build for Arduino
        boolean indicatorSet = false;       // tracks if indicator has been set (shortens the statement space)

        switch (seekBar.getId()) {

            case (R.id.servoSpeedSeekBar0):
            case (R.id.servoSpeedSeekBar1):
            case (R.id.servoSpeedSeekBar2):

                // Called for all servoSpeed cases as a result of not breaking between them
                // Convert progress bar values
                float param5a = (float) ((servoSpeedSeekBar0.getProgress() / 11) + 1);
                float param5b = (float) ((servoSpeedSeekBar1.getProgress() / 11) + 1);
                float param5c = (float) ((servoSpeedSeekBar2.getProgress() / 11) + 1);

                // Convert each speed value to string
                String param5aStr = Float.toString(param5a);
                String param5bStr = Float.toString(param5b);
                String param5cStr = Float.toString(param5c);

                // Message includes each Servo's speed value
                message = "5=" + param5aStr + "," + param5bStr + "," + param5cStr + ";";
                break;

            case (R.id.gripDepthSeekBar0):
            case (R.id.gripDepthSeekBar1):
            case (R.id.gripDepthSeekBar2):
                // Called for all gripDepth cases as a result of not breaking between them
                // Convert each depth value to string
                String depthProg0 = Integer.toString(gripDepthSeekBar0.getProgress());
                String depthProg1 = Integer.toString(gripDepthSeekBar1.getProgress());
                String depthProg2 = Integer.toString(gripDepthSeekBar2.getProgress());

                // Message includes each Servo's depth value
                message = "4=" + depthProg0 + "," + depthProg1 + "," + depthProg2 + ";";

                break;

            case (R.id.actThreshSeekBar):
                // Convert progress bar value
                float param2 = (float) (actThreshSeekBar.getProgress()*7f) / 10f + 5f;
                // String of float value
                String param2Str = Float.toString(param2);
                message = "2=" + param2Str + ";";
                break;

            case (R.id.writeDelSeekBar):
                // Convert progress bar value
                float param3 = (float) (writeDelSeekBar.getProgress() / 10);
                // String of float value
                String param3Str = Float.toString(param3);
                message = "3=" + param3Str + ";";
                break;

            default:
                break;

        }

        sendArduinoMessage(message);  // Send built message to bluetooth

    }

    // Switch for dynamic option
    @Override
    public void onCheckedChanged(CompoundButton sw, boolean isChecked) {
        if (isChecked) {
            sendArduinoMessage("1=D;");
        }
        else {
            sendArduinoMessage("1=S;");
            // TODO: allow break grip button
        }
    }

    // ----- /MAIN WINDOW METHODS -----

    // ----- MODE MANAGING METHODS -----
    private void newHydraMode(){

        // Pop-up dialog to get user name
        AlertDialog.Builder popup = new AlertDialog.Builder(this);
        popup.setTitle(R.string.NEWMODENAME_LABEL);

        LayoutInflater inflater = this.getLayoutInflater();
        popup.setView(inflater.inflate(R.layout.new_mode_popup, null))
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Dialog f = (Dialog) dialog;
                        EditText nameInput = (EditText) f.findViewById(R.id.nameInput);

                        String inputString = nameInput.getText().toString();

                        HydraMode mode = myModeManager.addNewBlankMode();
                        mode.setName(inputString);

                        myMemoryManager.writeToMemory(mode.getName(), gson.toJson(mode));

                        //commented out because if the device isnt connected to BT app crashes
                        //setCurrentSettings(mode);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        popup.create().show();
    }

    // Saves the set parameters for the current mode to its HydraMode class
    private void saveHydraMode(){
        HydraMode mode = myModeManager.getCurrentMode();
        setCurrentSettings(mode);
        myMemoryManager.writeToMemory(mode.getName(), gson.toJson(mode));
    }

    // Sets parameters of given mode to current user parameter settings
    private void setCurrentSettings(HydraMode mode){
        // Dynamic or Static
        mode.setParam(1, dynSwitch.getShowText());

        // Action threshold
        float param2 = (float) (((actThreshSeekBar.getProgress()*7) / 10) + 5);
        mode.setParam(2, param2);

        // Write delay
        float param3 = (float) (writeDelSeekBar.getProgress() / 10);
        mode.setParam(3, param3);

        // Grip depth
        mode.setParam(4, 0, gripDepthSeekBar0.getProgress());
        mode.setParam(4, 1, gripDepthSeekBar1.getProgress());
        mode.setParam(4, 2, gripDepthSeekBar2.getProgress());

        // Servo speed
        float param5a = Float.parseFloat(servoSpeedIndicator0.getText().toString());
        mode.setParam(5, 0, param5a);
        float param5b = Float.parseFloat(servoSpeedIndicator1.getText().toString());
        mode.setParam(5, 1, param5b);
        float param5c = Float.parseFloat(servoSpeedIndicator2.getText().toString());
        mode.setParam(5, 2, param5c);
    }

    // Sets current mode to mode (updates UI, sends message)
    private void setMode(HydraMode mode){
        myModeManager.setCurrentMode(mode);
        currentModeText.setText(mode.getName());
        
        // Update parameter UI stuff
        // Dynamic or Static
        dynSwitch.setShowText((boolean) mode.getParam(1));

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
    // ----- /BLUETOOTH COMM METHODS -----


    @Override
    protected void onStart() {
        super.onStart();
        //setMode(myModeManager.getCurrentMode());
    }

    @Override
    protected void onDestroy() {
        HydraSocket.writeCLOSE();
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

                // Acknowledge the bluetooth connection to Arduino
                HydraSocket.writeSTART();

                // Run calibration upon connection
                // TODO CalActivity fix - works with Arduino calibrate method
                Intent calIntent = new Intent(this, CalActivity.class);
                startActivity(calIntent);

            }
            // No bluetooth device
            else {
                HydraSocket.writeSTART();
                Toast.makeText(MainActivity.this,
                        "No device connected.",
                        Toast.LENGTH_LONG).show();
                connectedDeviceName.setText("Not connected.");
                connectedDeviceAddress.setText(null);
            }
        }

    }
}