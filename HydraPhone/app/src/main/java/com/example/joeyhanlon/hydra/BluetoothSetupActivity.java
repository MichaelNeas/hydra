package com.example.joeyhanlon.hydra;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class BluetoothSetupActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_ENABLE_BT = 1;

    BluetoothAdapter bluetoothAdapter;
    ArrayList<BluetoothDevice> pairedDeviceArrayList;
    ArrayList<String> pairedDeviceArrayAdapter;
    Button btnBluetooth;

    // Adapter to display more info in the paired device listview
    ArrayAdapter<String> pairedDeviceAdapter;

    // BT debugging text views
    TextView textInfo, textStatus, textPrompt;

    // Listview to display all paired devices
    ListView listViewPairedDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btsetup);

        textInfo = (TextView)findViewById(R.id.info);
        textStatus = (TextView)findViewById(R.id.status);
        textPrompt = (TextView)findViewById(R.id.prompt);

        listViewPairedDevice = (ListView)findViewById(R.id.pairedlist);

        btnBluetooth = (Button)findViewById(R.id.buttonPair);
        btnBluetooth.setOnClickListener(this);

        // ----- BT verification -----

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)){
            Toast.makeText(this,
                    "FEATURE_BLUETOOTH NOT support",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }


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
    }

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
                    Toast.makeText(BluetoothSetupActivity.this,
                            "Connecting...\n" + "Name: " + device.getName() + "\n"
                                    + "Address: " + device.getAddress(),
                            Toast.LENGTH_LONG).show();

                    boolean connected = HydraSocket.BTDeviceConnect(device);
                    if (connected){
                        Toast.makeText(BluetoothSetupActivity.this,
                                "Connected to " + device.getName(),
                                Toast.LENGTH_LONG).show();
                            setResult(Activity.RESULT_OK);
                            finish();
                    }
                    else {
                        Toast.makeText(BluetoothSetupActivity.this, "Connection failed.",
                                Toast.LENGTH_LONG).show();
                        // Remain in Bluetooth activity if connection fails
                        setResult(Activity.RESULT_CANCELED);
                    }
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case (R.id.buttonPair):
                // TODO implement bluetooth pairing button
        }
    }

}
