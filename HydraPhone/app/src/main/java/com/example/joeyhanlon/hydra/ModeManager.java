package com.example.joeyhanlon.hydra;


import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Keeps list of modes, saves those created by the user
 */

public class ModeManager {
    private ArrayList<HydraMode> modes;
    private HydraMode currentMode;
    private ModesAdapter adapter;

    public ModeManager(){
        currentMode = null;
        modes = new ArrayList<HydraMode>();
    }

    // Add new mode to Hydra with specified name and settings
    public void addNewMode(String name, boolean dynamic, float actThreshold, float writeDelay,
                           int gripDepthA, int gripDepthB, int gripDepthC,
                           float servoSpeedA, float servoSpeedB, float servoSpeedC){

        modes.add(new HydraMode(name, dynamic, actThreshold, writeDelay,
                gripDepthA, gripDepthB, gripDepthC,
                servoSpeedA, servoSpeedB, servoSpeedC));
        HydraMode thisMode = modes.get(modes.size() - 1);
        currentMode = thisMode;
        //adapter.add(thisMode);
    }

    // Adapter used to display modes in listview of main activity
    public class ModesAdapter extends ArrayAdapter<HydraMode> {

        public ModesAdapter(Context context, ArrayList<HydraMode> modes) {
            super(context, 0, modes);
        }

        @Override
        // Display name of mode in list
        public View getView(int position, View convertView, ViewGroup parent) {
            // Mode in the list
            HydraMode mode = modes.get(position);
            // Text view to display mode name
            TextView modeView = new TextView(getContext());
            modeView.setText(mode.myName);
            return modeView;
        }
    }

    // Instantiates adapter for modes manager in given context
    public void createAdapter(Context context){
        adapter = new ModesAdapter(context, modes);
    }

    public ModesAdapter getAdapter(){ return adapter; }

    public ArrayList<HydraMode> getModes() { return modes; }

    public HydraMode getCurrentMode(){ return currentMode; }

    public void setCurrentMode(HydraMode mode){
        currentMode = mode;
    }

    public HydraMode getMode(int position) { return modes.get(position); }

    public void setModeParam(int n, Object param){
        currentMode.setParam(n, param);
    }

    public void setModeParam(int n, int servo, Object param) { currentMode.setParam(n, servo, param); }
}
