package com.potato.taskmanagerapp.objects;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class Event implements Comparable<Event>, Serializable {

    String id;

    public String getIdExtern() {
        return idExtern;
    }

    public void setIdExtern(String idExtern) {
        this.idExtern = idExtern;
    }

    String idExtern;
    String startDate;
    String startHour;
    String endDate;
    String endHour;
    String type;
    String description;
    String priority;
    String modified;
    String color;
    String durationText;

    public String getDurationText() {
        return durationText;
    }

    public void setDurationText(String durationText) {
        this.durationText = durationText;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
    public Event(){
        this.id = UUID.randomUUID().toString();
    }

    public Event(String startDate, String startHour, String endDate, String endHour, String type,
                 String description, String priority, String modified, String color) {
        this.id = UUID.randomUUID().toString();
        this.startDate = startDate;
        this.startHour = startHour;
        this.endDate = endDate;
        this.endHour = endHour;
        this.type = type;
        this.description = description;
        this.priority = priority;
        this.color = color;

        this.modified = modified;
    }


    public String getModified() {
        return modified;
    }

    public void setModified(String modified) {
        this.modified = modified;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndHour() {
        return endHour;
    }

    public void setEndHour(String endHour) {
        this.endHour = endHour;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getStartHour() {
        return startHour;
    }

    public void setStartHour(String startHour) {
        this.startHour = startHour;
    }

    public String getId() {
        return id;
    }

    public void setId(String idTask) {
        this.id = idTask;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String toJson() {
        JSONObject jsn = new JSONObject();

        try {
            jsn.put("startDate", startDate);
            jsn.put("endDate", endDate);
            jsn.put("startHour", startHour);
            jsn.put("endHour", endHour);
            jsn.put("type", type);
            jsn.put("description", description);
            jsn.put("color", color);
            jsn.put("duration",durationText);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsn.toString();
    }

    public Event fromJson(JSONObject jsn) {

        try {
            this.startDate = jsn.getString("startDate");
            this.startHour = jsn.getString("startHour");
            this.endDate = jsn.getString("endDate");
            this.endHour = jsn.getString("endHour");
            this.type = jsn.getString("type");
            this.description = jsn.getString("description");
            this.color = jsn.getString("color");
            this.durationText = jsn.getString("duration");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this;
    }

    @Override
    public int compareTo(Event anotherEvent) {

        SimpleDateFormat sdf  = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        Date dateString1 = null;
        Date dateString2 = null;
        try {
            dateString1 = sdf.parse(startDate+" "+startHour);
            dateString2 = sdf.parse(anotherEvent.startDate+" "+anotherEvent.startHour);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        long timeInMillisSinceEpoch1 = dateString1.getTime();
        long timeInMillisSinceEpoch2 = dateString2.getTime();

        if(timeInMillisSinceEpoch1 > timeInMillisSinceEpoch2)
            return 1;
        if(timeInMillisSinceEpoch1 == timeInMillisSinceEpoch2)
            return 0;
        if(timeInMillisSinceEpoch1 < timeInMillisSinceEpoch2)
            return -1;
        return 0;
    }
}