package com.example.joeyhanlon.hydrawear;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.WearableListView;
import android.widget.Toast;

import java.util.ArrayList;

public class WearActivity extends WearableActivity implements WearableListView.ClickListener{

    // Default when not connected, only temporary
    private ArrayList<String> defaultList;
    // New adapter updates on data sync
    private ArrayList<String> appSyncList;
    private WearableAdapter listAdapter;
    private WearableListView wearableListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.round_activity_wear);

        defaultList = new ArrayList<String>();
        defaultList.add("Point");
        defaultList.add("Shoot");
        defaultList.add("Ayeee");

        setAmbientEnabled();

        listAdapter = new WearableAdapter(this, defaultList);

        wearableListView = (WearableListView) findViewById(R.id.wearable_List);
        wearableListView.setAdapter(listAdapter);
        wearableListView.setClickListener(this);

        // To be populated with phone list data
        appSyncList = new ArrayList<String>();

    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {

        //Send clicked index
        //mTeleportClient.sendMessage(String.valueOf(viewHolder.getLayoutPosition()), null);

        Toast.makeText(WearActivity.this,
                String.format("Position: %s", String.valueOf(viewHolder.getLayoutPosition())),
                Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onTopEmptyRegionClick() {}

}