package com.example.joeyhanlon.hydra;


import android.content.Context;
import android.view.LayoutInflater;
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


    public ModeManager(Context context){
        currentMode = null;
        modes = new ArrayList<HydraMode>();
    }

    // Add new empty mode to Hydra with specified name
    public HydraMode addNewBlankMode(){
        modes.add(new HydraMode());
        HydraMode thisMode = modes.get(modes.size() - 1);
        currentMode = thisMode;
        return thisMode;
    }

    public HydraMode addMode(HydraMode hm) {
        modes.add(hm);
        HydraMode thisMode = modes.get(modes.size() - 1);
        currentMode = thisMode;
        return thisMode;
    }

    // Add new mode to Hydra with specified name and settings
    public HydraMode addNewMode(String name, boolean dynamic, float actThreshold, float writeDelay,
                           int gripDepthA, int gripDepthB, int gripDepthC,
                           float servoSpeedA, float servoSpeedB, float servoSpeedC){

        modes.add(new HydraMode(name, dynamic, actThreshold, writeDelay,
                gripDepthA, gripDepthB, gripDepthC,
                servoSpeedA, servoSpeedB, servoSpeedC));
        HydraMode thisMode = modes.get(modes.size() - 1);
        currentMode = thisMode;
        return thisMode;
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
            // View
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.listview_mode, parent, false);
            }
            // Text view to display mode name
            TextView modeName = (TextView) convertView.findViewById(R.id.mode_name);
            modeName.setText(mode.getName());

            return convertView;
        }
    }

    // Instantiates adapter for modes manager in given context
    public void createAdapter(Context context){
        adapter = new ModesAdapter(context, modes);
    }

    // To assist UI display of modes
    public ModesAdapter getAdapter(){ return adapter; }

    // Returns current Hydra mode
    public HydraMode getCurrentMode(){ return currentMode; }

    // Sets the Hydra mode to be currently used
    public void setCurrentMode(HydraMode mode){ currentMode = mode; }

    // Gets mode pressed at given position in the list view
    public HydraMode getMode(int position) { return modes.get(position); }

    // Delete specified mode
    public void delete(HydraMode mode){ modes.remove(mode); }
}
