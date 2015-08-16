package com.potato.taskmanagerapp.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItem;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;
import com.potato.taskmanagerapp.R;
import com.potato.taskmanagerapp.fragments.CalendarFragment;
import com.potato.taskmanagerapp.fragments.EventsFragment;
import com.potato.taskmanagerapp.fragments.ExportFragment;
import com.potato.taskmanagerapp.fragments.OrganizerFragment;
import com.potato.taskmanagerapp.objects.Event;
import com.potato.taskmanagerapp.objects.Priority;
import com.potato.taskmanagerapp.utils.DBConnector;
import com.potato.taskmanagerapp.utils.ServiceManager;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;


/**
 * Created by Razvan on 10.07.2015.
 */
public class MainActivity extends ActionBarActivity {

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    private static FloatingActionsMenu fabmenu;
    private ViewPager viewPager;
    private static FloatingActionButton addfab;
    private static Event selectedEvent;
    private FloatingActionButton deletefab, editfab;
    private DBConnector dbConnector;

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
            DBConnector.getHelper(this).insertPriority(new Priority("2", "Sedinta"));
            DBConnector.getHelper(this).insertPriority(new Priority("3", "Examen"));
            DBConnector.getHelper(this).insertPriority(new Priority("1", "Cumparaturi"));
            DBConnector.getHelper(this).insertPriority(new Priority("1", "Petrecere"));
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
        toolbar.setTitle("Task Manager");
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        toolbar.setSubtitleTextColor(getResources().getColor(R.color.white));

        toolbar.getMenu().add("Log Out");
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getTitle().toString().equals("Log Out")){
                    SharedPreferences preferences = getSharedPreferences("com.potato.TaskManagerApp_preferences", MODE_PRIVATE);
                    preferences.edit().remove("user").commit();
                    Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
                return false;
            }
        });

        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(), FragmentPagerItems.with(this)
                .add(FragmentPagerItem.of("Evenimente", EventsFragment.class))
                .add(FragmentPagerItem.of("Calendar", CalendarFragment.class))
                .add(FragmentPagerItem.of("Organizator", OrganizerFragment.class))
                .add(FragmentPagerItem.of("Export", ExportFragment.class))
                .create());

        viewPager.setAdapter(adapter);
        final SmartTabLayout viewPagerTab = (SmartTabLayout) findViewById(R.id.viewpagertab);

        viewPager.setCurrentItem(0);

        viewPagerTab.setBackgroundColor(getResources().getColor(R.color.orange));
        toolbar.setBackgroundColor(getResources().getColor(R.color.orange));

        addfab = (FloatingActionButton) findViewById(R.id.addfab);
        fabmenu = (FloatingActionsMenu) findViewById(R.id.fabmenu);
        deletefab = (FloatingActionButton) findViewById(R.id.deletefab);
        editfab = (FloatingActionButton) findViewById(R.id.editfab);
        deletefab.setColorNormalResId(R.color.delete_primary);
        deletefab.setColorPressedResId(R.color.delete_primary_pressed);
        editfab.setColorNormalResId(R.color.edit_primary);
        editfab.setColorPressedResId(R.color.edit_primary_pressed);


        preferences = getSharedPreferences("com.potato.TaskManagerApp_preferences",MODE_PRIVATE);
        editor = preferences.edit();
        addfab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddActivity.class);
                startActivity(intent);
            }
        });

        viewPagerTab.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {

                super.onPageSelected(position);

                if (fabmenu.isExpanded()) {

                    fabmenu.collapse();
                    fabmenu.setVisibility(View.GONE);
                    addfab.setVisibility(View.VISIBLE);
                }
            }
        });

        viewPagerTab.setViewPager(viewPager);

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
                alertDialog.setMessage("Sunteti sigur ca vreti sa stergeti evenimentul " + selectedEvent.getDescription() + "?");

                // Setting Positive "Yes" Button
                alertDialog.setPositiveButton("Stergere", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        if(haveInternetConnection(MainActivity.this)){
                            new AddUser().execute(null, null, null);
                        }
                        else {
                            Set<String> set = preferences.getStringSet("delete", null);
                            if (set == null) {
                                set = new HashSet<String>();
                            }
                            set.add(selectedEvent.getIdExtern());
                            editor.putStringSet("delete", set);
                            editor.commit();
                        }
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
    private boolean haveInternetConnection(Context context) {

        ServiceManager serviceManager = new ServiceManager(context);
        if (serviceManager.isNetworkAvailable()) {
            return true;
        } else {
            return false;
        }
    }

    private class AddUser extends AsyncTask<Void, Void, Void> {

        String result;
        ProgressDialog dlg;
        boolean loginResult = false;

        private void remove(Event event) {
            HttpClient httpclient = new DefaultHttpClient();

            String parameters = "method=json&action=delete&id=" + event.getIdExtern();
            HttpURLConnection connection;
            OutputStreamWriter request = null;

            URL url = null;
            String response = null;
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
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            remove(selectedEvent);
            return null;
        }
    }
}
