package com.potato.taskmanagerapp.broadcasts;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import com.potato.taskmanagerapp.R;
import com.potato.taskmanagerapp.activities.MainActivity;
import com.potato.taskmanagerapp.objects.Event;
import com.potato.taskmanagerapp.utils.DBConnector;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Razvan on 14-Apr-15.
 */
public class Alarm extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wl.acquire();
        // Put here YOUR code.
        createNotification(context);

        wl.release();
    }

    public void SetAlarm(Context context) {

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, Alarm.class);
        PendingIntent pi = PendingIntent.getBroadcast(context,25455588, i, 0);
        am.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), 1800000, pi);
    }

    public void CancelAlarm(Context context)
    {
        Intent intent = new Intent(context, Alarm.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }

    public void createNotification(Context context) {

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, 24);

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");

        String endDate = sdf.format(calendar.getTime());

        calendar.add(Calendar.MINUTE,-30);

        String startDate = sdf.format(calendar.getTime());
        String[] end = endDate.split(" ");
        String[] start = startDate.split(" ");

        ArrayList<Event> events = DBConnector.getHelper(context).findEventsBetweenDateAndHours(start[0], end[0], start[1], end[1]);

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0);
        Notification noti;
        if(events.size()>0) {
            if(events.size()==1){
                noti = new Notification.Builder(context)
                        .setContentTitle("Notificare")
                        .setContentText("Aveti "+events.get(0).getDescription()+" maine la ora "+events.get(0).getStartHour())
                        .setSmallIcon(R.drawable.notification_icon)
                        .setContentIntent(pIntent)
                        .getNotification();
            }
            else {
                noti = new Notification.Builder(context)
                        .setContentTitle("Notificare")
                        .setContentText("Aveti " + events.size() + " evenimente maine in intervalul orar: " + start[1] + " - " + end[1])
                        .setSmallIcon(R.drawable.notification_icon)
                        .setContentIntent(pIntent)
                        .getNotification();
            }
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            noti.flags |= Notification.FLAG_AUTO_CANCEL;

            notificationManager.notify(0, noti);
        }
    }
}