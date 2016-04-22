package com.example.joeyhanlon.hydra;

import com.google.gson.Gson;

/**
 * Created by joeyhanlon on 4/22/16.
 */

// Used to parse Java objects into json objects (strings)
public class GsonParser {

    private Gson gson;

    public GsonParser() {
        gson = new Gson();
    }

    public String toJson(Object obj) {
        return gson.toJson(obj);
    }

    public Object toObject(String json, Class classOfObj) {
         return gson.fromJson(json, classOfObj);
    }

}
