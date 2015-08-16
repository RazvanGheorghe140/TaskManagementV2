package com.potato.taskmanagerapp.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.potato.taskmanagerapp.Observer.ObiectListener;
import com.potato.taskmanagerapp.R;
import com.potato.taskmanagerapp.activities.MainActivity;
import com.potato.taskmanagerapp.adapters.EventsAdapter;
import com.potato.taskmanagerapp.objects.Event;
import com.potato.taskmanagerapp.utils.DBConnector;
import com.potato.taskmanagerapp.utils.RecyclerItemClickListener;
import com.rey.material.widget.CheckBox;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


/**
 * Created by Razvan on 16.07.2015.
 */
public class EventsFragment extends Fragment {
    RecyclerView mRecyclerView;
    ArrayList<Event> events;
    Spinner spinner;
    String criteria = "date";
    TextView order;
    CheckBox checkBox, checkbox2;

    @Override
    @Nullable
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recyclerview, container, false);

    }

    @Override
    @Nullable
    public void onViewCreated(View view, Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        spinner = (Spinner) view.findViewById(R.id.spinner);
        order = (TextView) view.findViewById(R.id.order);
        checkBox = (CheckBox) view.findViewById(R.id.checkbox);
        checkbox2 = (CheckBox) view.findViewById(R.id.checkbox2);


        ArrayAdapter<String> sAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item,new String[]{"date","priority"});

        sAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(sAdapter);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(false);
        spinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                MainActivity.getFabmenu().collapse();
                return false;
            }
        });
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                criteria = (String) spinner.getSelectedItem();
                if (checkBox.isChecked()) {
                    events = DBConnector.getHelper(getActivity()).findAllEvents(criteria, order.getText().toString().toLowerCase());
                } else {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
                    String date = sdf.format(Calendar.getInstance().getTime());
                    events = DBConnector.getHelper(getActivity()).findEventsBetween(date, date, criteria, order.getText().toString().toLowerCase());
                }
                RecyclerView.Adapter rAdapter = new EventsAdapter(events, getActivity());
                mRecyclerView.setAdapter(rAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                criteria = "date";
            }
        });


        order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.getFabmenu().collapse();
                if (order.getText().toString().equals("Ascending")) {
                    order.setText(getText(R.string.descending));
                    order.setTextColor(getActivity().getResources().getColor(R.color.dark_red));
                } else {
                    order.setText(getText(R.string.ascending));
                    order.setTextColor(getActivity().getResources().getColor(R.color.dark_blue_gray));
                }
                String ord = order.getText().toString().toLowerCase();
                criteria = (String) spinner.getSelectedItem();
                if (checkBox.isChecked()) {
                    events = DBConnector.getHelper(getActivity()).findAllEvents(criteria, ord);
                } else {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
                    String date = sdf.format(Calendar.getInstance().getTime());
                    events = DBConnector.getHelper(getActivity()).findEventsBetween(date, date, criteria, ord);
                }
                RecyclerView.Adapter rAdapter = new EventsAdapter(events, getActivity());
                mRecyclerView.setAdapter(rAdapter);
            }
        });
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.getFabmenu().collapse();
                if(checkBox.isChecked()) {
                    checkbox2.setChecked(false);
                    events = DBConnector.getHelper(getActivity()).findAllEvents(criteria, order.getText().toString().toLowerCase());
                }
                else{
                    checkbox2.setChecked(true);
                    SimpleDateFormat sdf =new  SimpleDateFormat("dd.MM.yyyy");
                    String date = sdf.format(Calendar.getInstance().getTime());
                    events = DBConnector.getHelper(getActivity()).findEventsBetween(date,date,criteria,order.getText().toString().toLowerCase());
                }
                RecyclerView.Adapter rAdapter = new EventsAdapter(events, getActivity());
                mRecyclerView.setAdapter(rAdapter);
            }
        });

        checkbox2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.getFabmenu().collapse();
                if(checkbox2.isChecked()) {
                    checkBox.setChecked(false);
                    SimpleDateFormat sdf =new  SimpleDateFormat("dd.MM.yyyy");
                    String date = sdf.format(Calendar.getInstance().getTime());
                    events = DBConnector.getHelper(getActivity()).findEventsBetween(date, date, criteria, order.getText().toString().toLowerCase());

                }
                else{
                    checkBox.setChecked(true);
                    events = DBConnector.getHelper(getActivity()).findAllEvents(criteria, order.getText().toString().toLowerCase());
                }
                RecyclerView.Adapter rAdapter = new EventsAdapter(events, getActivity());
                mRecyclerView.setAdapter(rAdapter);
            }
        });
        spinner.setSelection(0);

        if(checkBox.isChecked()) {
            checkbox2.setChecked(false);
            events = DBConnector.getHelper(getActivity()).findAllEvents(criteria, order.getText().toString().toLowerCase());
        }
        else{
            SimpleDateFormat sdf =new  SimpleDateFormat("dd.MM.yyyy");
            String date = sdf.format(Calendar.getInstance().getTime());
            events = DBConnector.getHelper(getActivity()).findEventsBetween(date,date,criteria,order.getText().toString().toLowerCase());
        }
        if(checkbox2.isChecked()) {
            checkBox.setChecked(false);
            SimpleDateFormat sdf =new  SimpleDateFormat("dd.MM.yyyy");
            String date = sdf.format(Calendar.getInstance().getTime());
            events = DBConnector.getHelper(getActivity()).findEventsBetween(date, date, criteria, order.getText().toString().toLowerCase());

        }
        else{
            events = DBConnector.getHelper(getActivity()).findAllEvents(criteria, order.getText().toString().toLowerCase());
        }

        checkBox.setChecked(true);

        RecyclerView.Adapter rAdapter = new EventsAdapter(events, getActivity());
        mRecyclerView.setAdapter(rAdapter);
        DBConnector.getHelper(getActivity()).addObiectListener(new ObiectListener() {
            @Override
            public void listaModificata() {
                if(checkBox.isChecked()) {
                    events = DBConnector.getHelper(getActivity()).findAllEvents(criteria, order.getText().toString().toLowerCase());
                }
                else{
                    SimpleDateFormat sdf =new  SimpleDateFormat("dd.MM.yyyy");
                    String date = sdf.format(Calendar.getInstance().getTime());
                    events = DBConnector.getHelper(getActivity()).findEventsBetween(date,date,criteria,order.getText().toString().toLowerCase());
                }
                RecyclerView.Adapter rAdapter = new EventsAdapter(events, getActivity());
                mRecyclerView.setAdapter(rAdapter);
            }});
        mRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        if (MainActivity.getFabmenu().isExpanded()) {
                            MainActivity.getFabmenu().collapse();
                        }
                        MainActivity.getAddfab().setVisibility(View.GONE);
                        MainActivity.getFabmenu().setVisibility(View.VISIBLE);
                        MainActivity.getFabmenu().expand();
                        MainActivity.setSelectedObject(events.get(position));
                    }
                })
        );
    }
}


