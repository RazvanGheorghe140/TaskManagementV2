package com.potato.taskmanagerapp.fragments;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.potato.taskmanagerapp.R;
import com.potato.taskmanagerapp.objects.Event;
import com.potato.taskmanagerapp.utils.DBConnector;

import org.eazegraph.lib.charts.BarChart;
import org.eazegraph.lib.models.BarModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Razvan on 10.08.2015.
 */
public class OrganizerFragment extends Fragment {

    private TextView lowtxt, medtxt, hightxt;
    private TextView name, priority, date, type;
    private TextView noEvents;
    private BarChart barChart;
    private RelativeLayout background;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.organizer_fragment, container, false);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        lowtxt = (TextView) view.findViewById(R.id.low);
        medtxt = (TextView) view.findViewById(R.id.medium);
        hightxt = (TextView) view.findViewById(R.id.high);
        name = (TextView) view.findViewById(R.id.description);
        priority = (TextView) view.findViewById(R.id.priority);
        date = (TextView) view.findViewById(R.id.startDate);
        type = (TextView) view.findViewById(R.id.type);
        noEvents = (TextView) view.findViewById(R.id.no_events);

        background = (RelativeLayout) view.findViewById(R.id.relBackground);

        barChart = (BarChart) view.findViewById(R.id.barchart);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyy HH:mm");
        String dat = sdf.format(calendar.getTime());
        String[] d = dat.split(" ");
        ArrayList<Event> events = DBConnector.getHelper(getActivity()).findEventsBetweenDateAndHours(d[0], d[0], d[1], " 23:59");

        Event event = null;
        if(events.size()>0) {
            noEvents.setVisibility(View.GONE);
            background.setVisibility(View.VISIBLE);
            event = events.get(0);
            name.setText(event.getDescription());
            switch (Integer.parseInt(event.getPriority())) {
                case 1:
                    priority.setText("low");
                    break;
                case 2:
                    priority.setText("medium");
                    break;
                case 3:
                    priority.setText("high");
                    break;
            }

            type.setText(event.getType());
            date.setText(event.getStartDate() + " " + event.getStartHour());

            int bkColor = getActivity().getResources().getIdentifier(event.getColor(), "color", getActivity().getPackageName());
            GradientDrawable drawable = (GradientDrawable) background.getBackground();
            drawable.setColor(getActivity().getResources().getColor(bkColor));
        }
        else{
            background.setVisibility(View.GONE);
            noEvents.setVisibility(View.VISIBLE);
        }

        int lowCount = 0, medCount = 0, highCount = 0;

        for(Event e : events){
            switch (Integer.parseInt(e.getPriority())){
                case 1: lowCount++;
                    break;
                case 2: medCount++;
                    break;
                case 3: highCount++;
                    break;
            }
        }

        lowtxt.setText(""+lowCount);
        medtxt.setText(""+medCount);
        hightxt.setText(""+highCount);

        lowCount = medCount = highCount = 0;

        calendar.set(Calendar.DAY_OF_MONTH,calendar.getActualMinimum(Calendar.DAY_OF_MONTH));

        String startDate = sdf.format(calendar.getTime());

        calendar.set(Calendar.DAY_OF_MONTH,calendar.getActualMaximum(Calendar.DAY_OF_MONTH));

        String endDate = sdf.format(calendar.getTime());
        events = DBConnector.getHelper(getActivity()).findEventsBetween(startDate.split(" ")[0], endDate.split(" ")[0],"date","ascending");

        for(Event e:events){
            switch (Integer.parseInt(e.getPriority())){
                case 1: lowCount++;
                    break;
                case 2: medCount++;
                    break;
                case 3: highCount++;
                    break;
            }
        }

        barChart.addBar(new BarModel("low priority",lowCount,getResources().getColor(R.color.dark_yellow)));
        barChart.addBar(new BarModel("medium priority",medCount,getResources().getColor(R.color.orange)));
        barChart.addBar(new BarModel("high priority",highCount,getResources().getColor(R.color.red)));

    }
}
