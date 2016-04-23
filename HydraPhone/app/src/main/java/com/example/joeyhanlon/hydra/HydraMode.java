package com.example.joeyhanlon.hydra;

/**
 * Structure for a mode in Hydra, stores parameter settings
 */

public class HydraMode {

    private String myName;           // Name of mode

    static int NUM_SERVOS = 3;

    // Parameter variables
    private boolean _dynamic;       // Dynamic or static movement
    private float _actThreshold;    // Percent of range needed to initiate action
    private float _writeDelay;      // Delay between motor motion
    private int[] _gripDepth;       // Per servo, extent of Servo movement
    private float[] _servoSpeed;    // Per servo, incrementing of values

    // Default constructor, create new mode with given name and parameters
    public HydraMode(String name, boolean dynamic, float actThreshold, float writeDelay,
                     int gripDepthA, int gripDepthB, int gripDepthC,
                     float servoSpeedA, float servoSpeedB, float servoSpeedC){

        myName = name;
        _dynamic = dynamic;
        _actThreshold = actThreshold;
        _writeDelay = writeDelay;

        _gripDepth = new int[NUM_SERVOS];
        _gripDepth[0] = gripDepthA;
        _gripDepth[1] = gripDepthB;
        _gripDepth[2] = gripDepthC;

        _servoSpeed = new float[NUM_SERVOS];
        _servoSpeed[0] = servoSpeedA;
        _servoSpeed[1] = servoSpeedB;
        _servoSpeed[2] = servoSpeedC;
    }

    public HydraMode(String name, boolean dynamic, float actThreshold, float writeDelay,
                     int[] gripDepth, float[] servoSpeed){

        myName = name;
        _dynamic = dynamic;
        _actThreshold = actThreshold;
        _writeDelay = writeDelay;
        _gripDepth = gripDepth;
        _servoSpeed = servoSpeed;
    }

    public HydraMode(HydraMode hm) {
        myName = hm.getName();
        _dynamic = (boolean) hm.getParam(1);
        _actThreshold = (float) hm.getParam(2);
        _writeDelay = (float) hm.getParam(3);
        _gripDepth = (int[]) hm.getParam(4);
        _servoSpeed = (float[]) hm.getParam(5);
    }

    // Empty mode constructor
    public HydraMode(){

        myName = "";
        _dynamic = true;
        _actThreshold = 0.5f;
        _writeDelay = 5.0f;

        _gripDepth = new int[NUM_SERVOS];
        _gripDepth[0] = 100;
        _gripDepth[1] = 100;
        _gripDepth[2] = 100;

        _servoSpeed = new float[NUM_SERVOS];
        _servoSpeed[0] = 5.0f;
        _servoSpeed[1] = 5.0f;
        _servoSpeed[2] = 5.0f;

        //new HydraMode("", true, 0.5f, 5.0f, 100, 100, 100, 5.0f, 5.0f, 5.0f);
    }

    // Return nth parameter string
    public Object getParam(int n) {
        switch(n){
            case 1:
                return _dynamic;
            case 2:
                return _actThreshold;
            case 3:
                return _writeDelay;
            case 4:
                return _gripDepth;
            case 5:
                return _servoSpeed;
            default:
                System.out.println("Invalid parameter passed.");
                return null;
        }
    }

    // Set nth parameter to param object (only for non per Servo parameters)
    public void setParam(int n, Object param) {
        switch(n){
            case 1:
                _dynamic = (boolean) param;
                break;
            case 2:
                _actThreshold = (float) param;
                break;
            case 3:
                _writeDelay = (float) param;
                break;
            default:
                System.out.println("Invalid parameter passed.");
                break;
        }
    }

    // Set nth parameter to param object (only for per Servo parameters)
    public void setParam(int n, int servo, Object param) {
        switch(n){
            case 4:
                _gripDepth[servo] = (int) param;
                break;
            case 5:
                _servoSpeed[servo] = (float) param;
                break;
            default:
                System.out.println("Invalid parameter passed.");
                break;
        }
    }

    // Builds string message for all mode parameters
    public String getModeString(){
        String message = "1=";

        if (_dynamic){
            message = message + "D;";
        }
        else{
            message = message + "S;";
        }

        message = message + "2=" + Float.toString(_actThreshold) + ";";
        message = message + "3=" + Float.toString(_writeDelay) + ";";
        message = message + "4=" + Integer.toString(_gripDepth[0]) + "," +
                Integer.toString(_gripDepth[1]) + "," + Integer.toString(_gripDepth[2]) + ";";
        message = message + "5=" + Float.toString(_servoSpeed[0]) + "," +
                Float.toString(_servoSpeed[1]) + "," + Float.toString(_servoSpeed[2]) + ";";

        return message;
    }

    public String getName() {
        return myName;
    }

    public void setName(String name){ myName = name; }
}
