package com.potato.organiser.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItem;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;
import com.potato.organiser.R;
import com.potato.organiser.fragments.CalendarFragment;
import com.potato.organiser.fragments.EventsFragment;
import com.potato.organiser.fragments.ExportFragment;
import com.potato.organiser.objects.Event;
import com.potato.organiser.objects.Priority;
import com.potato.organiser.utils.DBConnector;


/**
 * Created by Razvan on 10.07.2015.
 */
public class MainActivity extends ActionBarActivity {
    static FloatingActionsMenu fabmenu;
    ViewPager viewPager;
    static FloatingActionButton addfab;
    static Event selectedEvent;
    FloatingActionButton deletefab, editfab;
    DBConnector dbConnector;

    public static FloatingActionsMenu getFabmenu() {
        return fabmenu;
    }


    public static FloatingActionButton getAddfab() {
        return addfab;
    }

    public static void setSelectedObject(Event event){
        selectedEvent = event;
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        dbConnector = new DBConnector(this);
        if(dbConnector.findAllPriorities().size()==0) {
            dbConnector.insertPriority(new Priority("2", "Sedinta"));
            dbConnector.insertPriority(new Priority("3", "Examen"));
            dbConnector.insertPriority(new Priority("1", "Cumparaturi"));
            dbConnector.insertPriority(new Priority("1", "Petrecere"));
        }

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayUseLogoEnabled(false);
            actionBar.setHomeButtonEnabled(true);
            actionBar.hide();
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("TaskApp");
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        toolbar.setSubtitleTextColor(getResources().getColor(R.color.white));

        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(), FragmentPagerItems.with(this)
                .add(FragmentPagerItem.of("Evenimente", EventsFragment.class))
                .add(FragmentPagerItem.of("Calendar", CalendarFragment.class))
                .add(FragmentPagerItem.of("Organizator", EventsFragment.class))
                .add(FragmentPagerItem.of("Export", ExportFragment.class))
                .create());
        viewPager.setAdapter(adapter);
        final SmartTabLayout viewPagerTab = (SmartTabLayout) findViewById(R.id.viewpagertab);
        int position =  getIntent().getIntExtra("intent",0);

        viewPager.setCurrentItem(position);
        viewPagerTab.setBackgroundColor(getResources().getColor(R.color.orange));
        toolbar.setBackgroundColor(getResources().getColor(R.color.orange));


        addfab = (FloatingActionButton) findViewById(R.id.addfab);

        addfab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int x = viewPager.getCurrentItem();
                Intent intent = new Intent(MainActivity.this, AddActivity.class);
//                intent.putExtra("page", x);
                startActivity(intent);
            }
        });

        fabmenu = (FloatingActionsMenu) findViewById(R.id.fabmenu);
        viewPagerTab.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if(fabmenu.isExpanded()){
                    fabmenu.collapse();
                    fabmenu.setVisibility(View.GONE);
                    addfab.setVisibility(View.VISIBLE);
                }
            }
        });
        viewPagerTab.setViewPager(viewPager);


        deletefab = (FloatingActionButton) findViewById(R.id.deletefab);
        editfab = (FloatingActionButton) findViewById(R.id.editfab);
        deletefab.setColorNormalResId(R.color.delete_primary);
        deletefab.setColorPressedResId(R.color.delete_primary_pressed);
        editfab.setColorNormalResId(R.color.edit_primary);
        editfab.setColorPressedResId(R.color.edit_primary_pressed);


        fabmenu.getChildAt(2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabmenu.collapse();
                addfab.setVisibility(View.VISIBLE);
                fabmenu.setVisibility(View.GONE);
            }
        });
        deletefab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);

                // Setting Dialog Title
                alertDialog.setTitle("Confirmare stergere...");

                // Setting Dialog Message
                alertDialog.setMessage("Sunteti sigur ca vreti sa stergeti evenimentul "+selectedEvent.getDescription()+"?");


                // Setting Positive "Yes" Button
                alertDialog.setPositiveButton("Stergere", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        DBConnector.getHelper(getApplicationContext()).deleteEvent(selectedEvent.getId());

                    }
                });

                // Setting Negative "NO" Button
                alertDialog.setNegativeButton("Anulare", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Write your code here to invoke NO event
                        dialog.cancel();
                    }
                });

                // Showing Alert Message
                alertDialog.show();

                fabmenu.collapse();
                fabmenu.setVisibility(View.GONE);
                addfab.setVisibility(View.VISIBLE);
            }
        });
        editfab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), AddActivity.class);
                intent.putExtra("edit", selectedEvent);
                startActivity(intent);
                fabmenu.collapse();
                fabmenu.setVisibility(View.GONE);
                addfab.setVisibility(View.VISIBLE);
            }
        });

    }
}
