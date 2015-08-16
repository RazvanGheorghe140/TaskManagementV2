package com.potato.taskmanagerapp.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.potato.taskmanagerapp.Observer.ObiectListener;
import com.potato.taskmanagerapp.R;
import com.potato.taskmanagerapp.adapters.EventsAdapter;
import com.potato.taskmanagerapp.objects.Event;
import com.potato.taskmanagerapp.utils.DBConnector;
import com.potato.taskmanagerapp.utils.EventDecorator;
import com.potato.taskmanagerapp.utils.OneDayDecorator;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateChangedListener;
import com.rey.material.app.BottomSheetDialog;
import com.rey.material.util.ViewUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Razvan on 07.08.2015.
 */
public class CalendarFragment extends Fragment implements OnDateChangedListener{

    private OneDayDecorator oneDayDecorator = new OneDayDecorator();
    private MaterialCalendarView widget;
    private ArrayList<Event> events;
    private BottomSheetDialog bottomSheetDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.calendar_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        widget = (MaterialCalendarView) view.findViewById(R.id.calendarView);

        widget.setOnDateChangedListener(this);
        widget.setShowOtherDates(false);
        widget.setFirstDayOfWeek(Calendar.MONDAY);

        Calendar calendar = Calendar.getInstance();

        widget.setSelectedDate(calendar.getTime());

        calendar.set(calendar.get(Calendar.YEAR), Calendar.JANUARY, 1);

        widget.setMinimumDate(calendar.getTime());

        calendar.set(calendar.get(Calendar.YEAR)+10, Calendar.DECEMBER, 31);

        widget.setMaximumDate(calendar.getTime());
        widget.addDecorators(oneDayDecorator);

        events = DBConnector.getHelper(getActivity()).findAllEvents("date","ascending");
        ArrayList<CalendarDay> dates = new ArrayList<>();

        for (Event e: events) {
            Calendar cal = Calendar.getInstance();

            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
            try {
                cal.setTime(sdf.parse(e.getStartDate()));
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            CalendarDay day = CalendarDay.from(cal);
            dates.add(day);
        }

        widget.addDecorator(new EventDecorator(Color.RED, dates));

        DBConnector.getHelper(getActivity()).addObiectListener(new ObiectListener() {
            @Override
            public void listaModificata() {

                events = DBConnector.getHelper(getActivity()).findAllEvents("date","ascending");
                ArrayList<CalendarDay> dates = new ArrayList<>();
                for (Event e: events) {
                    Calendar cal = Calendar.getInstance();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
                    try {
                        cal.setTime(sdf.parse(e.getStartDate()));
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    CalendarDay day = CalendarDay.from(cal);
                    dates.add(day);
                }
                widget.addDecorator(new EventDecorator(Color.RED, dates));

            }
        } );
    }

    @Override
    public void onDateChanged(MaterialCalendarView materialCalendarView, @Nullable CalendarDay calendarDay) {
        oneDayDecorator.setDate(calendarDay.getDate());
        widget.invalidateDecorators();
        ArrayList<Event> evs = new ArrayList<Event>();
        int mon = calendarDay.getMonth()+1;
        String month = ""+mon;
        if(mon<10)
            month = "0" +mon;
        String day = ""+calendarDay.getDay();
        if(calendarDay.getDay()<10)
            day = "0"+calendarDay.getDay();
        evs = DBConnector.getHelper(getActivity()).findEventsByDate(day+"."+month+"."+calendarDay.getYear());
        if(evs.size()!=0) {
            bottomSheetDialog = new BottomSheetDialog(getActivity(), R.style.Material_App_BottomSheetDialog);
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.bottom_sheet, null);
            ViewUtil.setBackground(v, getResources().getDrawable(R.drawable.bg_window_light));

            RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setHasFixedSize(false);


            RecyclerView.Adapter rAdapter = new EventsAdapter(evs, getActivity());
            recyclerView.setAdapter(rAdapter);
            bottomSheetDialog.heightParam(ViewGroup.LayoutParams.WRAP_CONTENT);
            bottomSheetDialog.contentView(v).show();
        }
        else {
            Toast.makeText(getActivity(), "There are no events for the selected date.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {

        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {

            Calendar calendar = Calendar.getInstance();

            oneDayDecorator.setDate(calendar.getTime());

            widget.invalidateDecorators();
            widget.setSelectedDate(calendar.getTime());

            events = DBConnector.getHelper(getActivity()).findAllEvents("date","ascending");
            ArrayList<CalendarDay> dates = new ArrayList<>();

            for (Event e: events) {

                Calendar cal = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
                try {
                    cal.setTime(sdf.parse(e.getStartDate()));
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                CalendarDay day = CalendarDay.from(cal);
                dates.add(day);
            }

            widget.removeDecorators();
            widget.addDecorator(new EventDecorator(Color.RED, dates));

        }
    }
}
