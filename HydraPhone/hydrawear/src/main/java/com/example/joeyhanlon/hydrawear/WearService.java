package com.example.joeyhanlon.hydrawear;

import com.mariux.teleport.lib.TeleportService;

/**
 * Created by joeyhanlon on 4/27/16.
 */
public class WearService extends TeleportService {

    private static final String STARTACTIVITY = "startActivity";

    @Override
    public void onCreate() {
        super.onCreate();

        setOnGetMessageTask(new GetModeList());

    }

    //Task that shows the path of a received message
    public class GetModeList extends TeleportService.OnGetMessageTask {

        @Override
        protected void onPostExecute(String path) {

            if (path.equals(STARTACTIVITY)){

            }

            //let`s reset the task (otherwise it will be executed only once)
            setOnGetMessageTask(new GetModeList());
        }
    }

}