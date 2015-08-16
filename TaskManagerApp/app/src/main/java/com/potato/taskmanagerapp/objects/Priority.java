package com.potato.taskmanagerapp.objects;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

/**
 * Created by Razvan on 12-Jul-15.
 */
public class Priority implements Comparable<Priority> {

    String id;
    String eventType;
    String eventPriority;

    public Priority(){}

    public Priority(String eventPriority, String eventType) {
        this.id = UUID.randomUUID().toString();
        this.eventPriority = eventPriority;
        this.eventType = eventType;
    }

    public String toJson() {
        JSONObject jsn = new JSONObject();

        try {
            jsn.put("eventType", eventType);
            jsn.put("eventPriority", eventPriority);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsn.toString();
    }

    public Priority fromJson(JSONObject jsn) {

        try {
            this.eventType = jsn.getString("eventType");
            this.eventPriority = jsn.getString("eventPriority");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {

        this.id = id;
    }


    public String getEventPriority() {
        return eventPriority;
    }

    public void setEventPriority(String eventPriority) {
        this.eventPriority = eventPriority;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    @Override
    public int compareTo(Priority another) {
        int thisPriority = Integer.parseInt(this.getEventPriority());
        int anotherPriority = Integer.parseInt(another.getEventPriority());
        return thisPriority-anotherPriority;
    }
}
