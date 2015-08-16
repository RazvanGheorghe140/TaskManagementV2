package com.potato.taskmanagerapp.broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Razvan on 14-Apr-15.
 */
public class AutoStart extends BroadcastReceiver {

    Alarm alarm = new Alarm();

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")  )
        { Log.e("AUTOSTART", "AUTOSTART");
            alarm.SetAlarm(context);
        }
    }
}