package com.potato.taskmanagerapp.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import com.potato.taskmanagerapp.Observer.ObiectListener;
import com.potato.taskmanagerapp.Observer.ObiectSubject;
import com.potato.taskmanagerapp.objects.Event;
import com.potato.taskmanagerapp.objects.Priority;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;


public class DBConnector extends SQLiteOpenHelper implements ObiectSubject {

    ArrayList<ObiectListener> listeners = new ArrayList<ObiectListener>();
    static final String TAG = "DBConnector";
    static final String DATABASE_NAME = "taskDB.sqlite";
    static final int DATABASE_VERSION = 1;
    public static String DATABASE_PATH = "/data/data/com.potato.taskmanagerapp/databases/";

    private final Context myContext;
    public Cursor cursor;
    private SQLiteDatabase tasksDB;
    private static DBConnector instance;

    public DBConnector(Context context) {
        super(context, DATABASE_NAME, null, 1);
        this.myContext = context;
    }

    public static synchronized DBConnector getHelper(Context context) {
        if (instance == null)
            instance = new DBConnector(context);

        return instance;
    }


    private void copyDataBase() throws IOException {
        InputStream myInput = myContext.getAssets().open(DATABASE_NAME);
        String outFileName = DATABASE_PATH + DATABASE_NAME;
        OutputStream myOutput = new FileOutputStream(outFileName);
        byte[] buffer = new byte[2048];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }
        myOutput.flush();
        myOutput.close();
        myInput.close();
    }

    public synchronized void open() throws Exception {
        boolean dbExist = checkDataBase();

        if (dbExist) {
            tasksDB = getWritableDatabase();
        } else {
            String myPath = DATABASE_PATH + DATABASE_NAME;
            tasksDB = getWritableDatabase();
            copyDataBase();
            tasksDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
        }
    }

    private boolean checkDataBase() {

        SQLiteDatabase checkDB = null;
        try {
            String myPath = DATABASE_PATH + DATABASE_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
        } catch (SQLiteException e) {
        }
        if (checkDB != null) {
            checkDB.close();
        }
        return checkDB != null ? true : false;
    }

    @Override
    public synchronized void close() {
        if (tasksDB != null)
            tasksDB.close();
        if (cursor != null)
            cursor.close();
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + "to" + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS events");
        onCreate(db);
    }

    public synchronized void insertEvent(Event event) {

        ContentValues newCon = new ContentValues();
        newCon.put("Id", event.getId());
        newCon.put("JSON", event.toJson());
        newCon.put("Modified", event.getModified());
        newCon.put("ObjType", event.getType());
        newCon.put("Priority", event.getPriority());
        newCon.put("IdExtern", event.getIdExtern());

        try {
            open();
        } catch (Exception e) {
            e.printStackTrace();
        }
        tasksDB.insert("Event", null, newCon);
        notifyListeners();
        close();
    }

    public synchronized void insertPriority(Priority priority) {

        ContentValues newCon = new ContentValues();
        newCon.put("Id", priority.getId());
        newCon.put("JSON", priority.toJson());

        try {
            open();
        } catch (Exception e) {
            e.printStackTrace();
        }
        tasksDB.insert("Priority", null, newCon);
        close();
    }

    public void updateEvent(Event event) {

        ContentValues editCon = new ContentValues();
        editCon.put("Id", event.getId());
        editCon.put("JSON", event.toJson());
        editCon.put("Modified", event.getModified());
        editCon.put("ObjType", event.getType());
        editCon.put("Priority", event.getPriority());
        editCon.put("IdExtern", event.getIdExtern());

        try {
            open();
        } catch (Exception e) {
            e.printStackTrace();
        }

        tasksDB.update("Event", editCon, "Id=\"" + event.getId() + "\"", null);
        notifyListeners();
        close();
    }

    public void updatePriority(Priority priority) {

        ContentValues editCon = new ContentValues();
        editCon.put("Id", priority.getId());
        editCon.put("JSON", priority.toJson());

        try {
            open();
        } catch (Exception e) {
            e.printStackTrace();
        }
        tasksDB.update("Priority", editCon, "Id=\"" + priority.getId()
                + "\"", null);
        close();
    }


    public void deleteEvent(String id) {
        try {
            open();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            tasksDB.delete("Event", "Id=\"" + id + "\"", null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        notifyListeners();
        close();
    }

    public void deletePriority(String id) {

        try {
            open();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            tasksDB.delete("Priority", "Id=\"" + id + "\"", null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        notifyListeners();
        close();
    }

    public ArrayList<Event> findAllEvents(String criteria, String order) {

        ArrayList<Event> events = new ArrayList<Event>();
        try {
            open();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            cursor = tasksDB.rawQuery("SELECT * FROM Event ", null);
            cursor.moveToFirst();
            do {
                String id = cursor.getString(0);
                String JSON = cursor.getString(1);
                String type = cursor.getString(2);
                String modified = cursor.getString(3);
                String priority = cursor.getString(4);
                String idExtern = cursor.getString(5);

                JSONObject jsn = new JSONObject(JSON);
                Event event = new Event();
                event.setId(id);
                event.setIdExtern(idExtern);
                event.setModified(modified);
                event.setType(type);
                event.setPriority(priority);
                event = event.fromJson(jsn);
                events.add(event);

            } while (cursor.moveToNext());
            cursor.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        close();
        if(criteria.equals("date") && order.equals("ascending") ) {
            Collections.sort(events);
        }

        if(criteria.equals("date") && order.equals("descending")){
            Collections.sort(events,Collections.reverseOrder());
        }

        if(criteria.equals("priority") && order.equals("ascending")){
            Collections.sort(events, new Comparator<Event>() {
                @Override
                public int compare(Event lhs, Event rhs) {
                    int l = Integer.parseInt(lhs.getPriority());
                    int r = Integer.parseInt(rhs.getPriority());
                    return l-r;
                }
            });
        }

        if(criteria.equals("priority") && order.equals("descending")){
            Collections.sort(events,Collections.reverseOrder(new Comparator<Event>() {
                @Override
                public int compare(Event lhs, Event rhs) {
                    int l = Integer.parseInt(lhs.getPriority());
                    int r = Integer.parseInt(rhs.getPriority());
                    return l-r;
                }
            }));
        }

        return events;
    }

    public ArrayList<Priority> findAllPriorities() {
        ArrayList<Priority> priorities = new ArrayList<Priority>();
        try {
            open();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            cursor = tasksDB.rawQuery("SELECT * FROM Priority", null);
            cursor.moveToFirst();
            do {

                String id = cursor.getString(0);
                String JSON = cursor.getString(1);
                JSONObject jsn = new JSONObject(JSON);
                Priority priority = new Priority();
                priority.setId(id);
                priority = priority.fromJson(jsn);
                priorities.add(priority);

            } while (cursor.moveToNext());
            cursor.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        close();
        Collections.sort(priorities);
        return priorities;
    }
    public ArrayList<Event> loadModified(){
        ArrayList<Event> events = new ArrayList<Event>();
        try {
            open();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            cursor = tasksDB.rawQuery("SELECT * FROM Event where Modified='YES'", null);
            cursor.moveToFirst();
            do {
                String id = cursor.getString(0);
                String JSON = cursor.getString(1);
                String type = cursor.getString(2);
            //    String modified = cursor.getString(3);
                String priority = cursor.getString(4);
                String idExtern = cursor.getString(5);

                JSONObject jsn = new JSONObject(JSON);
                Event event = new Event();
                event.setId(id);
               // event.setModified(modified);
                event.setIdExtern(idExtern);
                event.setType(type);
                event.setPriority(priority);
                event = event.fromJson(jsn);
                events.add(event);

            } while (cursor.moveToNext());
            cursor.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        close();
        return events;
    }
    public ArrayList<Event> findEventsByDate(String date) {

        ArrayList<Event> events = new ArrayList<Event>();
        try {
            open();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            cursor = tasksDB.rawQuery("SELECT * FROM Event", null);
            cursor.moveToFirst();
            do {
                String id = cursor.getString(0);
                String JSON = cursor.getString(1);
                String type = cursor.getString(2);
                String modified = cursor.getString(3);
                String priority = cursor.getString(4);
                String idExtern = cursor.getString(5);
                JSONObject jsn = new JSONObject(JSON);
                Event event = new Event();
                event.setId(id);
                event.setIdExtern(idExtern);
                event.setModified(modified);
                event.setPriority(priority);
                event.setType(type);
                event = event.fromJson(jsn);
                if (event.getStartDate().equals(date))
                    events.add(event);

            }
            while (cursor.moveToNext());
            cursor.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        close();
        return events;
    }

    public ArrayList<Event> findEventsBetween(String startDate, String endDate, String criteria, String order){
        ArrayList<Event> events = new ArrayList<Event>();
        try {
            open();
        } catch (Exception e) {
            e.printStackTrace();
        }
        startDate += " 00:00";
        endDate += " 23:59";

        try {
            cursor = tasksDB.rawQuery("SELECT * FROM Event", null);
            cursor.moveToFirst();
            do {
                String id = cursor.getString(0);
                String JSON = cursor.getString(1);
                String type = cursor.getString(2);
                String modified = cursor.getString(3);
                String priority = cursor.getString(4);
                String idExtern = cursor.getString(5);

                JSONObject jsn = new JSONObject(JSON);
                Event event = new Event();
                event.setId(id);
                event.setIdExtern(idExtern);
                event.setModified(modified);
                event.setPriority(priority);
                event.setType(type);
                event = event.fromJson(jsn);
                if(checkIfValid(startDate, event.getStartDate() + " " + event.getStartHour(), endDate))
                    events.add(event);

            }
            while (cursor.moveToNext());
            cursor.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        close();

        if(criteria.equals("date") && order.equals("ascending") ) {
            Collections.sort(events);
        }

        if(criteria.equals("date") && order.equals("descending")){
            Collections.sort(events,Collections.reverseOrder());
        }

        if(criteria.equals("priority") && order.equals("ascending")){
            Collections.sort(events, new Comparator<Event>() {
                @Override
                public int compare(Event lhs, Event rhs) {
                    int l = Integer.parseInt(lhs.getPriority());
                    int r = Integer.parseInt(rhs.getPriority());
                    return l-r;
                }
            });
        }

        if(criteria.equals("priority") && order.equals("descending")){
            Collections.sort(events,Collections.reverseOrder(new Comparator<Event>() {
                @Override
                public int compare(Event lhs, Event rhs) {
                    int l = Integer.parseInt(lhs.getPriority());
                    int r = Integer.parseInt(rhs.getPriority());
                    return l-r;
                }
            }));
        }
        return events;
    }
    public ArrayList<Event> findEventsBetweenDateAndHours(String startDate, String endDate, String startHour, String endHour){

        ArrayList<Event> events = new ArrayList<Event>();
        try {
            open();
        } catch (Exception e) {
            e.printStackTrace();
        }
        startDate += " "+startHour;
        endDate += " "+endHour;

        try {
            cursor = tasksDB.rawQuery("SELECT * FROM Event", null);
            cursor.moveToFirst();
            do {
                String id = cursor.getString(0);
                String JSON = cursor.getString(1);
                String type = cursor.getString(2);
                String modified = cursor.getString(3);
                String priority = cursor.getString(4);
                String idExtern = cursor.getString(5);

                JSONObject jsn = new JSONObject(JSON);
                Event event = new Event();
                event.setId(id);
                event.setIdExtern(idExtern);
                event.setModified(modified);
                event.setPriority(priority);
                event.setType(type);
                event = event.fromJson(jsn);
                if(checkIfValid(startDate,event.getStartDate()+" "+event.getStartHour(),endDate))
                    events.add(event);

            }
            while (cursor.moveToNext());
            cursor.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        close();
        Collections.sort(events);
        return events;
    }

    public boolean checkIfValid(String startDate, String date, String endDate){
        SimpleDateFormat sdf  = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        Date dateStringStart = null;
        Date dateString = null;
        Date dateStringEnd = null;
        try {
            dateStringStart = sdf.parse(startDate);
            dateString = sdf.parse(date);
            dateStringEnd = sdf.parse(endDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        long timeInMillisSinceEpochStart = dateStringStart.getTime();
        long timeInMillisSinceEpoch = dateString.getTime();
        long timeInMillisSinceEpochEnd = dateStringEnd.getTime();

        if(timeInMillisSinceEpochStart<=timeInMillisSinceEpoch && timeInMillisSinceEpoch<=timeInMillisSinceEpochEnd)
            return true;
        return false;
    }


    public boolean exportTheDB(ArrayList<Event> events) throws IOException {
        boolean success = false;
        File myFile;
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        String TimeStampDB = sdf.format(cal.getTime());
        myFile = new File(Environment.getExternalStorageDirectory() + "/Export_" + TimeStampDB + ".csv");
        myFile.createNewFile();
        FileOutputStream fOut = new FileOutputStream(myFile);
        OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
        myOutWriter.append("Name, Start Date, Start Hour, Type, Priority, End Date, End Hour");
        myOutWriter.append("\n");

        success = true;
        for(Event event:events){
            myOutWriter.append(event.getDescription() + ", " + event.getStartDate() + ", " + event.getStartHour() + ", " + event.getType() + ", " + event.getPriority() + ", " + event.getEndDate()+", "+event.getEndHour());
            myOutWriter.append("\n");
        }
        myOutWriter.close();
        fOut.close();
        return success;
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    @Override
    public void addObiectListener(ObiectListener ol) {
        listeners.add(ol);
    }

    @Override
    public void notifyListeners() {
        for(ObiectListener ol:listeners){
            ol.listaModificata();
        }
    }

    public String checkEvent(Event event) {
        try {
            open();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            cursor = tasksDB.rawQuery("SELECT * FROM Event where IdExtern='"+event.getIdExtern()+"'", null);
            cursor.moveToFirst();
            if(cursor.getCount()>=1){
                String id = cursor.getString(0);
                cursor.close();
                close();
                return id;
            }
            cursor.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        close();
        return null;
    }
}

