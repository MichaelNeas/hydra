package com.example.joeyhanlon.hydra;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.Map;

/**
 * Created by joeyhanlon on 4/22/16.
 */
public class MemoryManager {

    private String PREF_FILE_NAME = "prefs";

    private Context context;

    // Phone memory access
    private final SharedPreferences sharedPrefs;
    private final Editor editor;

    public MemoryManager(Context c) {
        this.context = c;
        sharedPrefs = context.getSharedPreferences(PREF_FILE_NAME, Activity.MODE_PRIVATE);
        editor = sharedPrefs.edit();
    }

    public void writeToMemory(String key, String value) {
        editor.putString(key,value);
        editor.commit();
    }

    public Map<String, ?> getAllFromMemory() {
        return sharedPrefs.getAll();
    }

    /*
    Map<String, ?> allEntries = sharedPrefs.getAll();
    for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
        // need to add mode based on title and string of parameters
        myModeManager.addModeWithKeyName(entry.getKey());
    }

    */


}
