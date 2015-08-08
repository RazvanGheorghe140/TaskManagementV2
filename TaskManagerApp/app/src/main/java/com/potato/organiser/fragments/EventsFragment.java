package com.potato.organiser.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.potato.organiser.Observer.ObiectListener;
import com.potato.organiser.R;
import com.potato.organiser.activities.MainActivity;
import com.potato.organiser.adapters.EventsAdapter;
import com.potato.organiser.objects.Event;
import com.potato.organiser.utils.DBConnector;
import com.potato.organiser.utils.RecyclerItemClickListener;

import java.util.ArrayList;


/**
 * Created by Razvan on 16.07.2015.
 */
public class EventsFragment extends Fragment {
    RecyclerView mRecyclerView;
    ArrayList<Event> events;
    Spinner spinner;
    String criteria = "date";
    TextView order;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recyclerview, container, false);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        MainActivity.getAddfab().setVisibility(View.VISIBLE);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        spinner = (Spinner) view.findViewById(R.id.spinner);
        order = (TextView) view.findViewById(R.id.order);


        ArrayAdapter<String> sAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item,new String[]{
                "date","priority"});

        sAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(sAdapter);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(false);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                criteria = (String) spinner.getSelectedItem();
                events = DBConnector.getHelper(getActivity()).findAllEvents(criteria, order.getText().toString().toLowerCase());
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
                if (order.getText().toString().equals("Ascending")) {
                    order.setText(getText(R.string.descending));
                    order.setTextColor(getActivity().getResources().getColor(R.color.dark_red));
                }
                else {
                    order.setText(getText(R.string.ascending));
                    order.setTextColor(getActivity().getResources().getColor(R.color.dark_blue_gray));
                }
                String ord = order.getText().toString().toLowerCase();
                criteria = (String) spinner.getSelectedItem();
                events = DBConnector.getHelper(getActivity()).findAllEvents(criteria,ord);
                RecyclerView.Adapter rAdapter = new EventsAdapter(events,getActivity());
                mRecyclerView.setAdapter(rAdapter);
                }
            });
            spinner.setSelection(0);

            events=DBConnector.getHelper(getActivity()).findAllEvents(criteria, order.getText().toString().toLowerCase());
            RecyclerView.Adapter rAdapter = new EventsAdapter(events, getActivity());
            mRecyclerView.setAdapter(rAdapter);
            DBConnector.getHelper(getActivity()).addObiectListener(new ObiectListener() {
                                  @Override
                                  public void listaModificata() {
                                      events = DBConnector.getHelper(getActivity()).findAllEvents(criteria, order.getText().toString().toLowerCase());
                                      RecyclerView.Adapter rAdapter = new EventsAdapter(events, getActivity());
                                      mRecyclerView.setAdapter(rAdapter);
                                  }
                              }
            );
        mRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        if (MainActivity.getFabmenu().isExpanded()){
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

