package com.example.joeyhanlon.hydra;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends AppCompatActivity implements ListView.OnItemClickListener, SeekBar.OnSeekBarChangeListener, View.OnClickListener, RadioGroup.OnCheckedChangeListener, CompoundButton.OnCheckedChangeListener {

    // BT stuff
    private static final int REQUEST_ENABLE_BT = 1;
    BluetoothAdapter bluetoothAdapter;
    ArrayList<BluetoothDevice> pairedDeviceArrayList;
    ArrayList<String> pairedDeviceArrayAdapter;

    // Toolbar
    Toolbar toolbar;

    // BT debugging text views
    TextView textInfo, textStatus, textPrompt;

    // Button to open system bluetooth menu
    Button btnBluetooth;

    // Listview to display all paired devices
    ListView listViewPairedDevice;

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

    // ----- SETTINGS INPUT FIELDS -----

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
    // STATIC / DYNAMIC
    Switch dynSwitch;
    // SPEED
    CardView speedCard;
    SeekBar thumbSpeed;
    SeekBar indexSpeed;
    SeekBar outerSpeed;
    TextView thumbSpeedIndicator;
    TextView indexSpeedIndicator;
    TextView outerSpeedIndicator;
    // DEPTH
    SeekBar thumbDepth;
    SeekBar indexDepth;
    SeekBar outerDepth;
    TextView thumbDepthIndicator;
    TextView indexDepthIndicator;
    TextView outerDepthIndicator;
    // Threhhold
    SeekBar actThreshSeekBar;
    TextView actThreshIndicator;
    // WriteDelay
    SeekBar writeDelSeekBar;
    TextView writeDelIndicator;
    // String array to be sent to the arm, initialized to default values
    String[] SEND_STRING = {"1=D;","2=0.5;","3=5.0;","4=100,100,100;","5=5.0,5.0,5.0;"};


    // Adapter to display more info in the paired device listview
    ArrayAdapter<String> pairedDeviceAdapter;

    // BT thread variables
    private UUID myUUID;
    private final String UUID_STRING_WELL_KNOWN_SPP = "00001101-0000-1000-8000-00805F9B34FB";
    ThreadConnectBTdevice myThreadConnectBTdevice;
    ThreadConnected myThreadConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(null);

        // ----- BT WINDOW SETUP -----
        textInfo = (TextView)findViewById(R.id.info);
        textStatus = (TextView)findViewById(R.id.status);
        textPrompt = (TextView)findViewById(R.id.prompt);

        listViewPairedDevice = (ListView)findViewById(R.id.pairedlist);

        btnBluetooth = (Button)findViewById(R.id.buttonPair);
        btnBluetooth.setOnClickListener(this);

        // ----- /BT WINDOW SETUP -----

        // ----- BT verification -----

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)){
            Toast.makeText(this,
                    "FEATURE_BLUETOOTH NOT support",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        myUUID = UUID.fromString(UUID_STRING_WELL_KNOWN_SPP);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this,
                    "Bluetooth is not supported on this hardware platform",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        String stInfo = bluetoothAdapter.getName() + "\n" +
                bluetoothAdapter.getAddress();
        textInfo.setText(stInfo);

        // ----- /BT verification -----

        // ----- MAIN WINDOW SETUP -----
        inputPane = (LinearLayout)findViewById(R.id.inputpane);
        inputField = (EditText)findViewById(R.id.input);

        // TODO instantiate modesListView, saveModeButton, newModeButton

        actionList = (RadioGroup) findViewById(R.id.actionList);
        // Relay information regarding selected action on click
        actionList.setOnCheckedChangeListener(this);

        // TODO imageView in sendActionWindow

        connectedDeviceName = (TextView)findViewById(R.id.connectedDeviceName);
        connectedDeviceAddress = (TextView)findViewById(R.id.connectedDeviceAddress);

        speedCard = (CardView)findViewById(R.id.speedCard);
        thumbSpeed = (SeekBar)findViewById(R.id.thumbSpeed);
        indexSpeed = (SeekBar)findViewById(R.id.indexSpeed);
        outerSpeed = (SeekBar)findViewById(R.id.outerSpeed);
        thumbSpeedIndicator = (TextView)findViewById(R.id.thumbSpeedIndicator);
        indexSpeedIndicator = (TextView)findViewById(R.id.indexSpeedIndicator);
        outerSpeedIndicator = (TextView)findViewById(R.id.outerSpeedIndicator);
        thumbSpeed.setOnSeekBarChangeListener(this);
        indexSpeed.setOnSeekBarChangeListener(this);
        outerSpeed.setOnSeekBarChangeListener(this);

        thumbDepth = (SeekBar)findViewById(R.id.thumbDepth);
        indexDepth = (SeekBar)findViewById(R.id.indexDepth);
        outerDepth = (SeekBar)findViewById(R.id.outerDepth);
        thumbDepthIndicator = (TextView)findViewById(R.id.thumbDepthIndicator);
        indexDepthIndicator = (TextView)findViewById(R.id.indexDepthIndicator);
        outerDepthIndicator = (TextView)findViewById(R.id.outerDepthIndicator);
        thumbDepth.setOnSeekBarChangeListener(this);
        indexDepth.setOnSeekBarChangeListener(this);
        outerDepth.setOnSeekBarChangeListener(this);

        dynSwitch = (Switch)findViewById(R.id.staticSwitch);

        actThreshSeekBar = (SeekBar) findViewById(R.id.actThreshSeekBar);
        actThreshSeekBar.setOnSeekBarChangeListener(this);
        actThreshIndicator = (TextView) findViewById(R.id.actThreshIndicator);

        writeDelSeekBar = (SeekBar) findViewById(R.id.writeDelSeekBar);
        writeDelSeekBar.setOnSeekBarChangeListener(this);
        writeDelIndicator = (TextView) findViewById(R.id.writeDelIndicator);

        fab = (FloatingActionButton)findViewById(R.id.fabSave);
        fab.setOnClickListener(this);

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
                sendArduinoMessage(inputField.getText().toString());
                // Save currently selected mode with currently selected settings
                //saveHydraMode();
                break;

            // TODO new mode button
            // myModeManager.addNewBlankMode();
        }
    }

    // Seek bar settings
    @Override
    public void onProgressChanged (SeekBar seekBar,int progressValue, boolean b){
        int progress = 0;
        progress = progressValue;
        int speedVal = progress / 10;
        String indicatorString = formatIndicatorField(speedVal);

        switch (seekBar.getId()) {
            case (R.id.thumbSpeed):
                thumbSpeedIndicator.setText(indicatorString);
                break;
            case (R.id.indexSpeed):
                indexSpeedIndicator.setText(indicatorString);
                break;
            case (R.id.outerSpeed):
                outerSpeedIndicator.setText(indicatorString);
                break;
            case (R.id.thumbDepth):
                thumbDepthIndicator.setText(indicatorString);
                break;
            case (R.id.indexDepth):
                indexDepthIndicator.setText(indicatorString);
                break;
            case (R.id.outerDepth):
                outerDepthIndicator.setText(indicatorString);
                break;
        }

        // Need to outsource this shit \/
        SEND_STRING[4]= "5=" + Integer.toString(speedVal) +".0," + Integer.toString(speedVal) + ".0," + Integer.toString(speedVal) + ".0;";
        updateSendString();
        send();

    }

    @Override
    public void onStartTrackingTouch (SeekBar seekBar){}
    @Override
    public void onStopTrackingTouch (SeekBar seekBar){}

    public String formatIndicatorField(int speedVal) {
        if (speedVal == 10){return "10";}
        else {return "0" + Integer.toString(speedVal);}
    }

    // Switch for static option
    @Override
    public void onCheckedChanged(CompoundButton sw, boolean isChecked) {
        if (isChecked) {
            disableSpeed();
            SEND_STRING[0] = "1=S;";
        }
        else {
            enableSpeed();
            SEND_STRING[0] = "1=D;";
        }

        updateSendString();
        send();
    }

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
        mode.setParam(4, 0, thumbDepth.getProgress());
        mode.setParam(4, 1, indexDepth.getProgress());
        mode.setParam(4, 2, outerDepth.getProgress());

        // Servo speed
        float param5a = (float) (thumbSpeed.getProgress()/100) * (float) (10.0 - 1.0) + (float) 1.0;
        mode.setParam(5, 0, param5a);
        float param5b = (float) (indexSpeed.getProgress()/100) * (float) (10.0 - 1.0) + (float) 1.0;
        mode.setParam(5, 1, param5b);
        float param5c = (float) (outerSpeed.getProgress()/100) * (float) (10.0 - 1.0) + (float) 1.0;
        mode.setParam(5, 2, param5c);

        //TODO store myModeManager in phone memory

        //sendArduinoMessage(mode.getModeString());
    }

    // Sets current mode to mode
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
        thumbDepth.setProgress(progsGD[0]);
        indexDepth.setProgress(progsGD[1]);
        outerDepth.setProgress(progsGD[2]);

        // Servo speed
        float[] progsSS = (float[]) mode.getParam(5);
        int progSS = (int) Math.round(100 * ((float) progsSS[0] / (10.0 - 1.0)));
        thumbSpeed.setProgress(progSS);
        progSS = (int) Math.round(100 * ((float) progsSS[1] / (10.0 - 1.0)));
        indexSpeed.setProgress(progSS);
        progSS = (int) Math.round(100 * ((float) progsSS[2] / (10.0 - 1.0)));
        outerSpeed.setProgress(progSS);

        // Send Arduino the new mode
        sendArduinoMessage(mode.getModeString());
    }
    // ----- /MODE MANAGING METHODS -----


    // ----- BLUETOOTH ACCESS METHODS -----
    // Send the given string to Arduino via Bluetooth
    public void sendArduinoMessage(String message){
        if(myThreadConnected!=null) {
            byte[] bytesToSend = message.getBytes();
            myThreadConnected.write(bytesToSend);
        }
    }

    // Return string sent by Arduino, used for calibration
    public String readArduinoMessage(){
        //while (myThreadConnected == null){} // Wait for bluetooth to be available
        return myThreadConnected.read();
    }
    // ----- /BLUETOOTH ACCESS METHODS -----


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


    // ----- BLUETOOTH CONNECTION METHODS -----
    @Override
    protected void onStart() {
        super.onStart();

        //Turn ON BlueTooth if it is OFF
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

        setup();
    }

    private void setup() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            pairedDeviceArrayList = new ArrayList<BluetoothDevice>();
            pairedDeviceArrayAdapter = new ArrayList<String>();

            for (BluetoothDevice device : pairedDevices) {
                pairedDeviceArrayList.add(device);
                pairedDeviceArrayAdapter.add(device.getName() + "\n  " + device.getAddress());
            }

            pairedDeviceAdapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, pairedDeviceArrayAdapter);
            listViewPairedDevice.setAdapter(pairedDeviceAdapter);

            listViewPairedDevice.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    BluetoothDevice device =
                            (BluetoothDevice) pairedDeviceArrayList.get(position);
                    /*Toast.makeText(MainActivity.this,
                            "Connecting...\n" +
                            "Name: " + device.getName() + "\n"
                                    + "Address: " + device.getAddress(),
                                    //+ "BondState: " + device.getBondState() + "\n"
                                    //+ "BluetoothClass: " + device.getBluetoothClass() + "\n"
                                    //+ "Class: " + device.getClass(),
                            Toast.LENGTH_LONG).show();
                    */
                    Toast.makeText(MainActivity.this,
                            "Attempting to make connection...",
                            Toast.LENGTH_LONG).show();
                    //textStatus.setText("start ThreadConnectBTdevice");
                    myThreadConnectBTdevice = new ThreadConnectBTdevice(device);
                    myThreadConnectBTdevice.start();
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(myThreadConnectBTdevice!=null){
            myThreadConnectBTdevice.cancel();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==REQUEST_ENABLE_BT){
            if(resultCode == Activity.RESULT_OK){
                setup();
            }else{
                Toast.makeText(this,
                        "BlueTooth NOT enabled",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }


    private void startThreadConnected(BluetoothSocket socket){

        myThreadConnected = new ThreadConnected(socket);
        myThreadConnected.start();
    }


    // The bluetooth thread used to maintain a connection
    private class ThreadConnectBTdevice extends Thread {

        private BluetoothSocket bluetoothSocket = null;
        private final BluetoothDevice bluetoothDevice;


        private ThreadConnectBTdevice(BluetoothDevice device) {
            bluetoothDevice = device;

            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(myUUID);
                textStatus.setText("Creating socket...");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            boolean success = false;
            try {
                bluetoothSocket.connect();
                success = true;
            } catch (IOException e) {
                e.printStackTrace();

                final String eMessage = e.getMessage();
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        textStatus.setText("Error during connection attempt: \n" + eMessage);
                    }
                });

                try {
                    bluetoothSocket.close();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }

            if(success){
                //connection successful
                final String msgconnected = "Connected to:\n" + bluetoothDevice;

                runOnUiThread(new Runnable(){

                    @Override
                    public void run() {
                        textStatus.setText(msgconnected);

                        textPrompt.setVisibility(View.GONE);
                        listViewPairedDevice.setVisibility(View.GONE);
                        findViewById(R.id.top_line).setVisibility(View.GONE);
                        fab.setVisibility(View.VISIBLE);
                        toolbar.setVisibility(View.VISIBLE);
                        inputPane.setVisibility(View.VISIBLE);

                        connectedDeviceName.setText(bluetoothDevice.getName());
                        connectedDeviceAddress.setText(bluetoothDevice.getAddress());
                    }});

                startThreadConnected(bluetoothSocket);
            }else{
                //fail
            }
        }

        public void cancel() {

            Toast.makeText(getApplicationContext(),
                    "Socket closed",
                    Toast.LENGTH_LONG).show();

            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

    }

    private class ThreadConnected extends Thread {
        private final BluetoothSocket connectedBluetoothSocket;
        private final InputStream connectedInputStream;
        private final OutputStream connectedOutputStream;

        public ThreadConnected(BluetoothSocket socket) {
            connectedBluetoothSocket = socket;
            InputStream in = null;
            OutputStream out = null;

            try {
                in = socket.getInputStream();
                out = socket.getOutputStream();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            connectedInputStream = in;
            connectedOutputStream = out;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    bytes = connectedInputStream.read(buffer);
                    String strReceived = new String(buffer, 0, bytes);
                    final String msgReceived = String.valueOf(bytes) +
                            " bytes received:\n"
                            + strReceived;

                    runOnUiThread(new Runnable(){

                        @Override
                        public void run() {
                            textStatus.setText(msgReceived);
                        }});

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();

                    final String msgConnectionLost = "Connection lost:\n"
                            + e.getMessage();
                    runOnUiThread(new Runnable(){

                        @Override
                        public void run() {
                            textStatus.setText(msgConnectionLost);
                        }});
                }
            }
        }

        public void write(byte[] buffer) {
            try {
                connectedOutputStream.write(buffer);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        public String read() {
            String message= "";
            byte[] buffer = new byte[10];
            try {
                connectedInputStream.read(buffer);
                message = buffer.toString();
            } catch(IOException e) {
                e.printStackTrace();
            }
            return message;
        }

        public void cancel() {
            try {
                connectedBluetoothSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    // ----- /BLUETOOTH CONNECTION METHODS -----

}