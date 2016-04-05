package com.example.joeyhanlon.hydra;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Static class used to maintain Bluetooth connection/communication, and pass
 * BluetoothSocket/BluetoothDevice info between BluetoothSetupActivity & MainActivity
 */
public class HydraSocket {

    // BT connection variables
    private static BluetoothSocket socket;          // Socket used for BT communication
    private static String deviceName;               // Name of connected BT device
    private static String deviceAddress;            // Mac address of connected BT device

    // BT thread variables
    private static UUID myUUID;                                     // To create comm socket
    private final static String UUID_STRING_WELL_KNOWN_SPP
            = "00001101-0000-1000-8000-00805F9B34FB";

    private static ThreadBTCommunication myThreadBTCommunication;   // Thread for BT comm

    // Returns socket for bluetooth communication (will be null if no device is connected)
    public static BluetoothSocket getSocket(){ return socket; }

    // Returns address of bluetooth device (will be null if no device is connected)
    public static String getDeviceAddress() {
        return deviceAddress;
    }

    // Returns name of bluetooth device (will be null if no device is connected)
    public static String getDeviceName() {
        return deviceName;
    }

    // Returns true if a BT device is connected
    public static boolean isConnected(){
        return (socket != null);
    }

    // When connection is lost, set all BT variables to null
    private static void connectionFailed(){
        socket = null;
        deviceName = null;
        deviceAddress = null;
    }

    // Start thread to establish bluetooth connection, returns true if connection is successful
    public static boolean BTDeviceConnect(BluetoothDevice device){
        // Create and start thread to connect to given device
        ThreadBTConnection myThreadBTConnection = new ThreadBTConnection(device);

        // Wait while thread is connecting
        while (myThreadBTConnection.connecting){}

        // Return whether connection was successful or failed
        return isConnected();
    }

    // Start thread to begin bluetooth communication, return true if thread successfully running
    public static boolean BTSocketConnect(BluetoothSocket socket){
        myThreadBTCommunication = new ThreadBTCommunication(socket);

        // Return state of connection
        return isConnected();
    }

    // Write to connected socket
    public static void write(String message){
        // Only send messages if device is connected
        if(HydraSocket.isConnected()) {
            byte[] bytesToSend = message.getBytes();
            myThreadBTCommunication.write(bytesToSend);
        }
    }

    // Read from connected socket
    public static String read(){
        // Only read messages if device is connected
        if(HydraSocket.isConnected()) {
            return myThreadBTCommunication.read();
        }
        else{
            return null;
        }
    }

    // Thread used to create bluetooth connection
    private static class ThreadBTConnection extends Thread {

        private final BluetoothDevice bluetoothDevice;      // Device to connect to
        private BluetoothSocket bluetoothSocket = null;     // Socket to connect to device

        public boolean connecting = true;                   // True if thread is trying to connect


        private ThreadBTConnection(BluetoothDevice device) {
            bluetoothDevice = device;
            myUUID = UUID.fromString(UUID_STRING_WELL_KNOWN_SPP);
            connecting = true;

            // When connecting to new device, initiate connection variables
            socket = null;
            deviceName = null;
            deviceAddress = null;

            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(myUUID);
                this.start();
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

                try {
                    bluetoothSocket.close();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }

            if(success){
                //connection successful
                socket = bluetoothSocket;
                deviceName = bluetoothDevice.getName();
                deviceAddress = bluetoothDevice.getAddress();
                BTSocketConnect(socket);
                myThreadBTCommunication.start();
            }else{
                // connection failed
                connectionFailed();
            }

            // No longer connecting
            connecting = false;
        }

        // Called when connection thread must be suspended
        public void cancel() {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    // Thread run to handle bluetooth communication
    private static class ThreadBTCommunication extends Thread {

        private final BluetoothSocket connectedBluetoothSocket; // Socket for comm
        private final InputStream connectedInputStream;         // Stream for reading
        private final OutputStream connectedOutputStream;       // Stream for writing

        public ThreadBTCommunication(BluetoothSocket socket) {

            connectedBluetoothSocket = socket;
            InputStream in = null;
            OutputStream out = null;

            try {
                in = socket.getInputStream();
                out = socket.getOutputStream();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                connectionFailed();
            }

            connectedInputStream = in;
            connectedOutputStream = out;

        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            // Continuously check for BT messages
            while (true) {
                try {
                    bytes = connectedInputStream.read(buffer);
                    String strReceived = new String(buffer, 0, bytes);
                    final String msgReceived = String.valueOf(bytes) +
                            " bytes received:\n"
                            + strReceived;

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    // Connection lost, set BT variables to null
                    connectionFailed();
                }
            }
        }

        // Write to socket
        public void write(byte[] buffer) {
            try {
                connectedOutputStream.write(buffer);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        // Read from socket
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
                connectionFailed();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
