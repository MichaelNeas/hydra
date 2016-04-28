package com.example.joeyhanlon.hydrawear;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.WearableListView;
import android.widget.Toast;

import com.google.android.gms.wearable.DataMap;
import com.mariux.teleport.lib.TeleportClient;

import java.util.ArrayList;

public class WearActivity extends WearableActivity implements WearableListView.ClickListener {

    private ArrayList<String> modeNames;
    private WearableListView wearableListView;
    private TeleportClient mTeleportClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.round_activity_wear);

        // startService(new Intent(this, WearService.class));

        mTeleportClient = new TeleportClient(this);
        mTeleportClient.setOnSyncDataItemTask(new ShowToastHelloWorldTask());

        modeNames = new ArrayList<String>();
        modeNames.add("Point");
        modeNames.add("Shoot");
        modeNames.add("Ayeee");

        wearableListView = (WearableListView) findViewById(R.id.wearable_List);
        wearableListView.setAdapter(new WearableAdapter(this, modeNames));
        wearableListView.setClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mTeleportClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mTeleportClient.disconnect();

    }

    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {

        Toast.makeText(WearActivity.this,
                String.format("Selected: %s", modeNames.get(viewHolder.getLayoutPosition())),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTopEmptyRegionClick() {}

    public class ShowToastHelloWorldTask extends TeleportClient.OnSyncDataItemTask {

        @Override
        protected void onPostExecute(DataMap dataMap) {

            String hello = dataMap.getString("hello");

            Toast.makeText(getApplicationContext(),hello,Toast.LENGTH_SHORT).show();
        }

    }
}