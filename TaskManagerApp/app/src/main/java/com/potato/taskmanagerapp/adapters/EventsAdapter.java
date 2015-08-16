package com.potato.taskmanagerapp.adapters;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.potato.taskmanagerapp.R;
import com.potato.taskmanagerapp.objects.Event;

import java.util.ArrayList;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventViewHolder>  {

    private ArrayList<Event> events;
    private Context context;

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView description, type, startDate, priority;
        RelativeLayout relBackground;

        EventViewHolder(View itemView) {

            super(itemView);
            itemView.setClickable(true);

            description = (TextView)itemView.findViewById(R.id.description);
            type = (TextView)itemView.findViewById(R.id.type);
            startDate = (TextView) itemView.findViewById(R.id.startDate);
            priority = (TextView) itemView.findViewById(R.id.priority);
            relBackground = (RelativeLayout) itemView.findViewById(R.id.relBackground);
        }
    }

    public EventsAdapter(ArrayList<Event> events, Context context){
        this.events = events;
        this.context = context;
    }

    @Override
    public EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_card, parent, false);
        EventViewHolder evh = new EventViewHolder(v);
        return evh;
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    @Override
    public void onBindViewHolder(EventViewHolder holder, int position) {

        Event tempEvent = events.get(position);

        holder.itemView.setSelected(true);
        holder.description.setText(tempEvent.getDescription());
        holder.type.setText(tempEvent.getType());
        holder.startDate.setText(tempEvent.getStartDate()+" "+tempEvent.getStartHour());

        int prio = Integer.parseInt(tempEvent.getPriority().toString());
        String priority="";
        switch (prio){
            case 1: priority = "low";
                break;
            case 2: priority = "medium";
                break;
            case 3: priority = "high";
        }

        holder.priority.setText(priority);

        int bkColor = context.getResources().getIdentifier(tempEvent.getColor(),"color",context.getPackageName());
        GradientDrawable drawable = (GradientDrawable) holder.relBackground.getBackground();
        drawable.setColor(context.getResources().getColor(bkColor));

    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}
