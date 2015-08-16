package com.potato.taskmanagerapp.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.potato.taskmanagerapp.R;
import com.potato.taskmanagerapp.objects.Event;
import com.potato.taskmanagerapp.utils.DBConnector;
import com.potato.taskmanagerapp.utils.ServiceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Razvan on 11.08.2015.
 */
public class SplashActivity extends Activity {
    SharedPreferences preferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);

        if (haveInternetConnection(SplashActivity.this)) {
            new AddUser().execute(null, null, null);
        } else {
            Toast.makeText(SplashActivity.this, "No Internet connection", Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    finish();
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                }
            }, 1000);
        }
    }

    private class AddUser extends AsyncTask<Void, Void, Void> {

        String result;

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            finish();
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
        }

        boolean check(Event event) throws JSONException {
            StringBuilder sb = new StringBuilder();
            InputStreamReader isr = null;
            BufferedReader reader = null;
            HttpURLConnection connection;
            OutputStreamWriter request = null;

            URL url = null;
            String response = null;
            String parameters = "method=json&action=get&id=" + event.getIdExtern();
            try {
                String link = "http://gecont.ro/paul/events.php"+"?"+parameters;
                try {
                    url = new URL(link);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setInstanceFollowRedirects(false);
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                    String line = "";
                    isr = new InputStreamReader(connection.getInputStream());
                    reader = new BufferedReader(isr);
                    line = reader.readLine();

                    while (line != null) {
                        sb.append(line);
                        try {
                            line = reader.readLine();
                        } catch (Exception e) {
                            line = null;
                        }
                        connection.disconnect();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (sb != null && !sb.equals(""))
                    response = sb.toString();
                else
                    response = "";

                result = response;

                JSONObject jsn = new JSONObject(result.toString());
                String fail = jsn.getString("status");
                if (fail.equals("fail"))
                    return false;
            }catch (Exception e){

            }
            return true;

        }
        @Override
        protected Void doInBackground(Void... arg0) {

            HttpURLConnection connection;
            OutputStreamWriter request = null;

            URL url = null;
            String response = null;
            preferences = getSharedPreferences("com.potato.TaskManagerApp_preferences", Context.MODE_PRIVATE);

            //load
            long timestamp = preferences.getLong("timestamp", 0);
            StringBuilder sb = new StringBuilder();
            InputStreamReader isr = null;
            BufferedReader reader = null;
            String parameters = "method=json&action=getAllFrom&timestamp=" + timestamp;
            try {
                String link = "http://gecont.ro/paul/events.php";
                try {
                    url = new URL(link);

                    connection = (HttpURLConnection) url.openConnection();
                    connection.setInstanceFollowRedirects(false);
                    connection.setDoOutput(true);
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                    connection.setRequestMethod("POST");

                    request = new OutputStreamWriter(connection.getOutputStream());
                    request.write(parameters);
                    request.flush();
                    request.close();

                    String line = "";
                    isr = new InputStreamReader(connection.getInputStream());
                    reader = new BufferedReader(isr);
                    line = reader.readLine();

                    while (line != null) {
                        sb.append(line);
                        try {
                            line = reader.readLine();
                        } catch (Exception e) {
                            line = null;
                        }
                    }
                    connection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (sb != null && !sb.equals(""))
                    response = sb.toString();
                else
                    response = "";
                result = response;

                JSONObject jsn = new JSONObject(result.toString());
                JSONArray arrData = jsn.getJSONArray("answer");
                ArrayList<Event> events = new ArrayList<>();
                for (int i = 0; i < arrData.length(); i++) {
                    JSONObject j = arrData.getJSONObject(i);
                    Event event = new Event();
                    event.setModified("NO");
                    event.setIdExtern(j.getString("event_id"));
                    event.setDescription(j.getString("event_description"));
                    event.setPriority(j.getString("event_priority"));
                    event.setStartDate(j.getString("event_start_date"));
                    event.setStartHour(j.getString("event_start_hour"));
                    event.setEndHour(j.getString("event_end_hour"));
                    event.setEndDate(j.getString("event_end_date"));
                    event.setType(j.getString("event_type"));
                    event.setColor(j.getString("event_color"));
                    event.setDurationText(getDuration(event));
                    if (DBConnector.getHelper(SplashActivity.this).checkEvent(event) == null) {
                        DBConnector.getHelper(SplashActivity.this).insertEvent(event);
                    } else {
                        event.setId(DBConnector.getHelper(SplashActivity.this).checkEvent(event));
                        DBConnector.getHelper(SplashActivity.this).updateEvent(event);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            // insert or update
            ArrayList<Event> events = DBConnector.getHelper(SplashActivity.this).loadModified();
            if (events.size() != 0){
                for (Event event : events) {
                    boolean check = false;
                    try {
                        check = check(event);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    sb = new StringBuilder();
                    isr = null;
                    reader = null;
                    parameters = "";
                    if (event.getIdExtern().equals("not set")) {
                        parameters = "method=json&action=add&description=" + event.getDescription() + "&priority="
                                + event.getPriority() + "&startDate=" + event.getStartDate() + "&startHour=" + event.getStartHour() +
                                "&endDate=" + event.getEndDate() + "&endHour=" + event.getEndHour() + "&type=" + event.getType() +
                                "&color=" + event.getColor();
                    } else if (!check) {
                        parameters = "method=json&action=add&description=" + event.getDescription() + "&priority="
                                + event.getPriority() + "&startDate=" + event.getStartDate() + "&startHour=" + event.getStartHour() +
                                "&endDate=" + event.getEndDate() + "&endHour=" + event.getEndHour() + "&type=" + event.getType() +
                                "&color=" + event.getColor();
                    } else {
                        parameters = "method=json&action=edit&id=" + event.getIdExtern() + "&description=" + event.getDescription() + "&priority="
                                + event.getPriority() + "&startDate=" + event.getStartDate() + "&startHour=" + event.getStartHour() +
                                "&endDate=" + event.getEndDate() + "&endHour=" + event.getEndHour() + "&type=" + event.getType() +
                                "&color=" + event.getColor();
                    }
                    try {
                        String link = "http://gecont.ro/paul/events.php";
                        try {
                            url = new URL(link);
                            connection = (HttpURLConnection) url.openConnection();
                            connection.setInstanceFollowRedirects(false);
                            connection.setDoOutput(true);
                            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                            connection.setRequestMethod("POST");

                            request = new OutputStreamWriter(connection.getOutputStream());
                            request.write(parameters);
                            request.flush();
                            request.close();

                            String line = "";
                            isr = new InputStreamReader(connection.getInputStream());
                            reader = new BufferedReader(isr);
                            line = reader.readLine();

                            while (line != null) {
                                sb.append(line);
                                try {
                                    line = reader.readLine();
                                } catch (Exception e) {
                                    line = null;
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (sb != null && !sb.equals(""))
                            response = sb.toString();
                        else
                            response = "";
                        result = response;
                        if (event.getIdExtern().equals("not set") || !check) {

                            JSONObject jsn = new JSONObject(result.toString());
                            JSONObject answerjsn = (JSONObject) jsn.get("answer");
                            event.setIdExtern(answerjsn.getString("event_id"));
                        }
                        event.setModified("NO");
                        DBConnector.getHelper(SplashActivity.this).updateEvent(event);
                    } catch (Exception e) {
                        result = "";
                    }
                }
            }
            //delete
            String[] ids = new String[0];
            if(preferences.getStringSet("delete",null) != null){
                ids = preferences.getStringSet("delete",null).toArray(new String[preferences.getStringSet("delete", null).size()]);
            }
            for(String id:ids) {
                parameters = "method=json&action=delete&id=" + id;
                try {
                    String link = "http://gecont.ro/paul/events.php";
                    try {
                        url = new URL(link);

                        connection = (HttpURLConnection) url.openConnection();
                        connection.setInstanceFollowRedirects(false);
                        connection.setDoOutput(true);
                        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                        connection.setRequestMethod("POST");

                        request = new OutputStreamWriter(connection.getOutputStream());
                        request.write(parameters);
                        request.flush();
                        request.close();

                        connection.getInputStream();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
            }

            preferences.edit().remove("delete").commit();
            preferences.edit().putLong("timestamp", Calendar.getInstance().getTimeInMillis()/1000).commit();
            return  null;
        }
    }

    private boolean haveInternetConnection(Context context) {

        ServiceManager serviceManager = new ServiceManager(context);

        if (serviceManager.isNetworkAvailable()) {
            return true;
        } else {
            return false;
        }
    }

    public String getDuration(Event event) throws ParseException {

        String duration="";
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        Date startDate = null, endDate = null;
        startDate = sdf.parse(event.getStartDate()+" "+event.getStartHour());
        endDate = sdf.parse(event.getEndDate() + " " + event.getEndHour());
        int hours = (int) (endDate.getTime() - startDate.getTime())/ (60 * 60 * 1000);
        return getDurationText(hours);
    }

    private String getDurationText(int hours) {

        int daysNo = hours/24;
        int hoursNo = hours%24;
        String text="";

        if(daysNo>0) {
            text = daysNo +" days";
            if(hoursNo>0)
                text = text + " and ";
        }
        if(hoursNo == 1)
            text = text + "1 hour";
        else
            text = text + hoursNo + " hours";
        return text;
    }
}
