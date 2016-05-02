package com.example.joeyhanlon.hydra;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements ListView.OnItemClickListener, SeekBar.OnSeekBarChangeListener, View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    // Bluetooth activity code
    private static final int BT_CONNECT = 1;

    private Toolbar toolbar;

    // Handles storing/setting of different modes
    ModeManager myModeManager;
    HydraMode defaultMode;

    // Phone memory storage manager
    MemoryManager myMemoryManager;
    Map<String, ?> modesInMemory;
    // To parse java objects as [json] strings
    Gson gson;

    // Data sync with wearable
    ArrayList<HydraMode> modeArrayList;

    // The functional application window. Used to mask it during BT init and such.
    LinearLayout inputPane;

    // Debugging fields
    EditText inputField;

    // To display connected device info in main window
    TextView connectedDeviceName;
    ImageView connectionStatusIcon;

    TextView currentModeLabel;

    // Snackbar for button feedback
    Snackbar snackbar;


    // ----- Hydra UI Elements -----


    // --- HYDRA CONTROL ---

    // List of Hydra Modes
    ListView modesListView;

    // Text showing current mode
    //TextView currentModeText;

    // Buttons to affect mode settings
    ImageButton saveModeButton, addModeButton, resetModeButton, deleteModeButton;

    // Buttons to interact with arm
    Button breakButton;

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
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        inputPane = (LinearLayout) findViewById(R.id.inputPane);
        inputField = (EditText) findViewById(R.id.input);

        currentModeLabel = (TextView) findViewById(R.id.current_mode_label);

        // Bluetooth info display
        connectedDeviceName = (TextView) findViewById(R.id.connectedDeviceName);
        connectionStatusIcon = (ImageView) findViewById(R.id.connectionStatusIcon);

        speedCard = (CardView) findViewById(R.id.speedCard);
        servoSpeedSeekBar0 = (SeekBar) findViewById(R.id.servoSpeedSeekBar0);
        servoSpeedSeekBar1 = (SeekBar) findViewById(R.id.servoSpeedSeekBar1);
        servoSpeedSeekBar2 = (SeekBar) findViewById(R.id.servoSpeedSeekBar2);
        servoSpeedSeekBar0.setMax(90);
        servoSpeedSeekBar1.setMax(90);
        servoSpeedSeekBar2.setMax(90);
        servoSpeedIndicator0 = (TextView) findViewById(R.id.servoSpeedIndicator0);
        servoSpeedIndicator1 = (TextView) findViewById(R.id.servoSpeedIndicator1);
        servoSpeedIndicator2 = (TextView) findViewById(R.id.servoSpeedIndicator2);
        servoSpeedSeekBar0.setOnSeekBarChangeListener(this);
        servoSpeedSeekBar1.setOnSeekBarChangeListener(this);
        servoSpeedSeekBar2.setOnSeekBarChangeListener(this);

        gripDepthSeekBar0 = (SeekBar) findViewById(R.id.gripDepthSeekBar0);
        gripDepthSeekBar1 = (SeekBar) findViewById(R.id.gripDepthSeekBar1);
        gripDepthSeekBar2 = (SeekBar) findViewById(R.id.gripDepthSeekBar2);
        gripDepthIndicator0 = (TextView) findViewById(R.id.gripDepthIndicator0);
        gripDepthIndicator1 = (TextView) findViewById(R.id.gripDepthIndicator1);
        gripDepthIndicator2 = (TextView) findViewById(R.id.gripDepthIndicator2);
        gripDepthSeekBar0.setOnSeekBarChangeListener(this);
        gripDepthSeekBar1.setOnSeekBarChangeListener(this);
        gripDepthSeekBar2.setOnSeekBarChangeListener(this);

        dynSwitch = (Switch) findViewById(R.id.dynSwitch);
        dynSwitch.setOnCheckedChangeListener(this);

        breakButton = (Button) findViewById(R.id.break_button);
        breakButton.setOnClickListener(this);

        actThreshSeekBar = (SeekBar) findViewById(R.id.actThreshSeekBar);
        actThreshSeekBar.setMax(70);
        actThreshSeekBar.setOnSeekBarChangeListener(this);
        actThreshIndicator = (TextView) findViewById(R.id.actThreshIndicator);

        writeDelSeekBar = (SeekBar) findViewById(R.id.writeDelSeekBar);
        writeDelSeekBar.setMax(90);
        writeDelSeekBar.setOnSeekBarChangeListener(this);
        writeDelIndicator = (TextView) findViewById(R.id.writeDelIndicator);

        addModeButton = (ImageButton) findViewById(R.id.addModeButton);
        addModeButton.setOnClickListener(this);
        saveModeButton = (ImageButton) findViewById(R.id.saveModeButton);
        saveModeButton.setOnClickListener(this);
        resetModeButton = (ImageButton) findViewById(R.id.resetModeButton);
        resetModeButton.setOnClickListener(this);
        deleteModeButton = (ImageButton) findViewById(R.id.deleteModeButton);
        deleteModeButton.setOnClickListener(this);

        //breakButton = (ImageButton) findViewById(R.id.breakButton);
        //breakButton.setOnClickListener(this);

        // Create ModeManager to store list of modes
        myModeManager = new ModeManager(this.getApplicationContext());

        modesListView = (ListView) findViewById(R.id.modesListView);

        // Instantiate memory manager with activity context
        myMemoryManager = new MemoryManager(this.getApplicationContext());
        gson = new Gson();

        // Set up adapter for list of modes
        myModeManager.createAdapter(this);
        modesListView.setAdapter(myModeManager.getAdapter());
        modesListView.setOnItemClickListener(this);

        // Default mode, always hard coded to ensure there is always a working mode (not in memory)
        defaultMode = new HydraMode("Default Grip", false, 0.5f, 5.0f, 100, 100, 100, 7.0f, 7.0f, 7.0f);
        myModeManager.addMode(defaultMode);

        // retrieve map of all modes in memory
        modesInMemory = myMemoryManager.getAllFromMemory();

        // Load all modes in memory if any exist and add them to the mode manager
        if (!modesInMemory.isEmpty()) {
            for (Map.Entry<String, ?> entry : modesInMemory.entrySet()) {
                String json = entry.getValue().toString();
                HydraMode temp = gson.fromJson(json, HydraMode.class);
                myModeManager.addMode(temp);
            }
        }

        modeArrayList = myModeManager.getModeList();

        // Always have default grip selected initially (DOES NOT WORK VISUALLY FOR SOME REASON)
        //setMode(myModeManager.getMode(0));
        //modesListView.setSelection(0);


        // ----------

    }


    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    // ----- MAIN WINDOW METHODS -----

    // Mode selection
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        HydraMode newMode = myModeManager.getMode(position);
        setMode(newMode);
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
                snackbar = Snackbar.make(findViewById(R.id.main_content),"Mode added.",Snackbar.LENGTH_LONG);
                snackbar.show();
                break;
            // Reset stored settings of current mode
            case R.id.resetModeButton:
                setMode(myModeManager.getCurrentMode());
                snackbar = Snackbar.make(findViewById(R.id.main_content),"Mode reset.",Snackbar.LENGTH_LONG);
                snackbar.show();
                break;
            // Save new settings to current mode
            case R.id.saveModeButton:
                saveHydraMode();
                snackbar = Snackbar.make(findViewById(R.id.main_content),"Mode saved.",Snackbar.LENGTH_LONG);
                snackbar.show();
                break;
            case R.id.deleteModeButton:
                deleteHydraMode();
                break;
            // Add new mode
            case R.id.break_button:
                HydraSocket.writeACK();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case (R.id.action_calibrate):
                // Calibrate activity
                if (HydraSocket.isConnected()) {
                    Intent calIntent = new Intent(this, CalActivity.class);
                    startActivity(calIntent);
                }
                else {
                    snackbar = Snackbar.make(findViewById(R.id.main_content),"Lost connection",Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
                break;
            case (R.id.action_btsettings):
                // Close current activity, open bluetooth activity (essentially just reset the app)
                Intent btIntent = new Intent(this, BluetoothSetupActivity.class);
                startActivityForResult(btIntent, BT_CONNECT);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    // Seek bar settings
    @Override
    public void onProgressChanged (SeekBar seekBar,int progressValue, boolean b){

        switch (seekBar.getId()) {

            case (R.id.servoSpeedSeekBar0):
                servoSpeedIndicator0.setText(Float.toString(((float)progressValue + 10f) / 10f));
                break;
            case (R.id.servoSpeedSeekBar1):
                servoSpeedIndicator1.setText(Float.toString(((float)progressValue + 10f) / 10f));
                break;
            case (R.id.servoSpeedSeekBar2):
                servoSpeedIndicator2.setText(Float.toString(((float)progressValue + 10f) / 10f));
                break;
            case (R.id.gripDepthSeekBar0):
                gripDepthIndicator0.setText(progressValue + "%");
                break;
            case (R.id.gripDepthSeekBar1):
                gripDepthIndicator1.setText(progressValue + "%");
                break;
            case (R.id.gripDepthSeekBar2):
                gripDepthIndicator2.setText(progressValue + "%");
                break;
            case (R.id.actThreshSeekBar):
                actThreshIndicator.setText(progressValue + 5 + "%");
                break;
            case (R.id.writeDelSeekBar):
                writeDelIndicator.setText(Float.toString(((float)progressValue +10f) / 10f));
                break;

            default:
                break;
        }

    }

    @Override
    public void onStartTrackingTouch (SeekBar seekBar){}
    @Override
    public void onStopTrackingTouch (SeekBar seekBar) {

        String message = "";                // Message to build for Arduino

        switch (seekBar.getId()) {

            case (R.id.servoSpeedSeekBar0):
            case (R.id.servoSpeedSeekBar1):
            case (R.id.servoSpeedSeekBar2):

                // Called for all servoSpeed cases as a result of not breaking between them
                // Convert progress bar values
                float param5a = (float) servoSpeedSeekBar0.getProgress() + 10f;
                float param5b = (float) servoSpeedSeekBar1.getProgress() + 10f;
                float param5c = (float) servoSpeedSeekBar2.getProgress() + 10f;

                // Convert each speed value to string
                String param5aStr = Float.toString(param5a/10f);
                String param5bStr = Float.toString(param5b/10f);
                String param5cStr = Float.toString(param5c/10f);

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
                float param2 = (float) actThreshSeekBar.getProgress() + 5f;
                // String of float value
                String param2Str = Float.toString(param2/100f);
                message = "2=" + param2Str + ";";
                break;

            case (R.id.writeDelSeekBar):
                // Convert progress bar value
                float param3 = (float) writeDelSeekBar.getProgress() + 10f;
                // String of float value
                String param3Str = Float.toString(param3/10f);
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
            breakButton.setVisibility(View.GONE);
        }
        else {
            sendArduinoMessage("1=S;");
            breakButton.setVisibility(View.VISIBLE);
            // TODO: allow break grip button
        }
    }

    // ----- /MAIN WINDOW METHODS -----


    // ----- ANDROID WEAR SYNC -----




    // ----- /ANDROID WEAR SYNC -----


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
                        setCurrentSettings(mode);
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

    private void deleteHydraMode() {
        new AlertDialog.Builder(this)
                .setIcon(R.drawable.ic_report_yellow_24dp)
                .setTitle("Delete this mode?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        myMemoryManager.removeEntry(myModeManager.getCurrentMode().getName());
                        myModeManager.delete(myModeManager.getCurrentMode());
                        setMode(defaultMode);
                        snackbar = Snackbar.make(findViewById(R.id.main_content),"Mode deleted.",Snackbar.LENGTH_LONG);
                        snackbar.show();
                    }

                })
                .setNegativeButton("No", null)
                .show();
    }

    // Sets parameters of given mode to current user parameter settings
    private void setCurrentSettings(HydraMode mode){
        // Dynamic or Static
        mode.setParam(1, dynSwitch.isChecked());

        // Action threshold
        float param2 = (actThreshSeekBar.getProgress() / 10f);
        mode.setParam(2, param2);

        // Write delay
        float param3 = (writeDelSeekBar.getProgress() / 10f);
        mode.setParam(3, param3);

        // Grip depth
        mode.setParam(4, 0, gripDepthSeekBar0.getProgress());
        mode.setParam(4, 1, gripDepthSeekBar1.getProgress());
        mode.setParam(4, 2, gripDepthSeekBar2.getProgress());

        // Servo speed
        float param5a = (servoSpeedSeekBar0.getProgress() / 10f);
        mode.setParam(5, 0, param5a);
        float param5b = (servoSpeedSeekBar1.getProgress() / 10f);
        mode.setParam(5, 1, param5b);
        float param5c = (servoSpeedSeekBar2.getProgress() / 10f);
        mode.setParam(5, 2, param5c);
    }

    // Sets current mode to mode (updates UI, sends message)
    private void setMode(HydraMode mode){
        myModeManager.setCurrentMode(mode);
        currentModeLabel.setText(mode.getName());
        
        // Update parameter UI stuff
        // Dynamic or Static
        dynSwitch.setShowText((boolean) mode.getParam(1));
        if ((boolean)mode.getParam(1) == true) {
            dynSwitch.setChecked(true);
            breakButton.setVisibility(View.GONE);
        }
        else {
            dynSwitch.setChecked(false);
            breakButton.setVisibility(View.VISIBLE);
        }

        //Action threshold
        int progAT = (int) ((float)mode.getParam(2) * 10f);
        actThreshSeekBar.setProgress(progAT);

        // Write delay
        int progWD = (int) ((float)mode.getParam(3) * 10f);
        writeDelSeekBar.setProgress(progWD);

        // Grip depth
        int[] progsGD = (int[]) mode.getParam(4);
        gripDepthSeekBar0.setProgress(progsGD[0]);
        gripDepthSeekBar1.setProgress(progsGD[1]);
        gripDepthSeekBar2.setProgress(progsGD[2]);

        // Servo speed
        float[] progsSS = (float[]) mode.getParam(5);
        int progSS = (int) (progsSS[0] * 10f);
        servoSpeedSeekBar0.setProgress(progSS);
        progSS = (int) (progsSS[1] * 10f);
        servoSpeedSeekBar1.setProgress(progSS);
        progSS = (int) (progsSS[2] * 10f);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Bluetooth connection activity has completed
        if (requestCode == BT_CONNECT){

            String name = HydraSocket.getDeviceName();
            String address = HydraSocket.getDeviceAddress();

            // There is a bluetooth connection
            if (resultCode == RESULT_OK){

                // Show connected device

                //snackbar = Snackbar.make(findViewById(R.id.main_content),"Connected to " + name,Snackbar.LENGTH_LONG);
                //snackbar.show();

                connectionStatusIcon.setImageResource(R.drawable.ic_check_circle_green_24dp);
                connectedDeviceName.setText("Connected to " + name);

                // Acknowledge the bluetooth connection to Arduino
                HydraSocket.writeSTART();

                // Run calibration upon connection
                // TODO CalActivity fix - works with Arduino calibrate method
                Intent calIntent = new Intent(this, CalActivity.class);
                startActivity(calIntent);

            }
            // No bluetooth device
            else {

                //snackbar = Snackbar.make(findViewById(R.id.main_content),"No device connected.",Snackbar.LENGTH_LONG);
                //snackbar.show();

                connectionStatusIcon.setImageResource(R.drawable.ic_report_yellow_24dp);
                connectedDeviceName.setText("Not connected.");
            }
        }

    }
}