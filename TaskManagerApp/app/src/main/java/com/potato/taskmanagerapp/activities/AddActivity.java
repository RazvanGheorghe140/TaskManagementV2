package com.potato.taskmanagerapp.activities;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.dd.CircularProgressButton;
import com.potato.taskmanagerapp.R;
import com.potato.taskmanagerapp.objects.Event;
import com.potato.taskmanagerapp.objects.Priority;
import com.potato.taskmanagerapp.utils.DBConnector;
import com.potato.taskmanagerapp.utils.ServiceManager;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rey.material.widget.Spinner;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Razvan on 14.07.2015.
 */
public class AddActivity extends Activity implements TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener, NumberPicker.OnValueChangeListener{

    private Event event;
    private DBConnector dbConnector;
    private com.rey.material.widget.Spinner sp;
    private TextView txtDayPick, txtTimePick, txtSetDuration, header_text, msg_err;;
    private MaterialEditText matEdt;
    private ImageButton crc_orange, crc_red, crc_blue, crc_green, crc_teal;
    private String currentColor = "orange", text="1 hour";
    private int hoursDuration = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_activity);

        dbConnector = new DBConnector(this);

        sp = (Spinner) findViewById(R.id.spinner);

        txtDayPick = (TextView) findViewById(R.id.txtDayPick);
        txtTimePick = (TextView) findViewById(R.id.txtTimePick);
        txtSetDuration = (TextView) findViewById(R.id.duration);
        matEdt = (MaterialEditText) findViewById(R.id.matEdt);
        header_text = (TextView) findViewById(R.id.header_text);
        msg_err = (TextView) findViewById(R.id.msg_err);

        crc_orange = (ImageButton) findViewById(R.id.crc_orange);
        crc_teal = (ImageButton) findViewById(R.id.crc_teal);
        crc_green = (ImageButton) findViewById(R.id.crc_green);
        crc_blue = (ImageButton) findViewById(R.id.crc_blue);
        crc_red = (ImageButton) findViewById(R.id.crc_red);

        txtSetDuration.setText("1 hour");

        crc_blue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentColor = "blue";
                header_text.setBackgroundColor(getResources().getColor(R.color.blue));
            }
        });
        crc_orange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentColor = "orange";
                header_text.setBackgroundColor(getResources().getColor(R.color.orange));
            }
        });
        crc_green.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentColor = "green";
                header_text.setBackgroundColor(getResources().getColor(R.color.green));
            }
        });
        crc_teal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentColor = "teal";
                header_text.setBackgroundColor(getResources().getColor(R.color.teal));
            }
        });
        crc_red.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentColor = "red";
                header_text.setBackgroundColor(getResources().getColor(R.color.red));
            }
        });

        txtTimePick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Calendar now = Calendar.getInstance();
                TimePickerDialog tpd = TimePickerDialog.newInstance(
                        AddActivity.this,
                        now.get(Calendar.HOUR_OF_DAY),
                        now.get(Calendar.MINUTE),
                        true
                );
                tpd.setThemeDark(true);
                tpd.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        Log.d("TimePicker", "Dialog was cancelled");
                    }
                });
                tpd.show(AddActivity.this.getFragmentManager(), "Pick time");
            }
        });


        txtDayPick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Calendar now = Calendar.getInstance();
                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        AddActivity.this,
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                );
                dpd.setThemeDark(true);
                dpd.show(AddActivity.this.getFragmentManager(), "Pick a date");
            }
        });

        txtSetDuration.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showNoPickDialog();
            }
        });


        final ArrayList<Priority> aList = dbConnector.findAllPriorities();
        String[] priorities = new String[aList.size()];
        for(int i = 0; i < aList.size(); i++){
            priorities[i] = aList.get(i).getEventType();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,priorities);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(adapter);
        sp.setSelection(0);

        event = (Event) getIntent().getSerializableExtra("edit");

        if(event != null){
            header_text.setText("Edit event");
            matEdt.setText(event.getDescription());
            header_text.setBackgroundResource(getResources().getIdentifier(event.getColor(),"color",getPackageName()));
            txtDayPick.setText(event.getStartDate());
            txtTimePick.setText(event.getStartHour());
            txtSetDuration.setText(event.getDurationText());
            sp.setSelection(adapter.getPosition(event.getType()));
            currentColor = event.getColor();
        }

        final CircularProgressButton saveButton = (CircularProgressButton) findViewById(R.id.circularButton1);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean valid = true;
                boolean edit = true;
                String description = matEdt.getText().toString();
                String tip = sp.getSelectedItem().toString();
                String data = txtDayPick.getText().toString();
                String time = txtTimePick.getText().toString();

                if (description.equals("") || data.equals("Pick Date   ") || time.equals("Pick hour   ")) {
                    valid = false;
                }

                if (valid) {

                    msg_err.setVisibility(View.GONE);

                    if (event == null) {
                        event = new Event();
                        edit = false;
                        event.setIdExtern("not set");
                    }

                    event.setModified("YES");
                    event.setColor(currentColor);
                    event.setDescription(description);
                    event.setStartDate(data);
                    event.setStartHour(time);
                    event.setType(tip);
                    event.setPriority(aList.get(sp.getSelectedItemPosition()).getEventPriority());

                    Date startDate = null;
                    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                    try {
                        startDate = sdf.parse(data+" "+time);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    Date endDate = new Date(startDate.getTime());
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(endDate);
                    calendar.add(Calendar.HOUR, hoursDuration);

                    endDate = calendar.getTime();

                    event.setEndDate(sdf.format(endDate).split(" ")[0]);
                    event.setEndHour(sdf.format(endDate).split(" ")[1]);
                    event.setDurationText(txtSetDuration.getText().toString());

                    if(edit == true){
                        if(haveInternetConnection(AddActivity.this)) {
                            String parameters = "method=json&action=edit&id="+event.getIdExtern()+"&description=" + event.getDescription() + "&priority="
                                    + event.getPriority() + "&startDate=" + event.getStartDate() + "&startHour=" + event.getStartHour() +
                                    "&endDate=" + event.getEndDate() + "&endHour=" + event.getEndHour() + "&type=" + event.getType() +
                                    "&color=" + event.getColor();
                            event.setModified("NO");
                            new AddUser().execute(event, parameters, null);
                        }
                        DBConnector.getHelper(getApplicationContext()).updateEvent(event);

                    }
                    else {
                        if(haveInternetConnection(AddActivity.this)){
                            String  parameters = "method=json&action=add&description=" + event.getDescription() + "&priority="
                                    + event.getPriority() + "&startDate=" + event.getStartDate() + "&startHour=" + event.getStartHour() +
                                    "&endDate=" + event.getEndDate() + "&endHour=" + event.getEndHour() + "&type=" + event.getType() +
                                    "&color=" + event.getColor();
                            event.setModified("NO");
                            new AddUser().execute(event, parameters, null);
                        }
                        DBConnector.getHelper(getApplicationContext()).insertEvent(event);
                    }

                    simulateSuccessProgress(saveButton);

                    onBackPressed();
                    finish();

                } else {
                    msg_err.setVisibility(View.VISIBLE);
                    simulateErrorProgress(saveButton);
                }

            }
        });

    }

    private void upload(Event event, String parameters) {
        HttpClient httpclient = new DefaultHttpClient();


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
                String line = "";
                StringBuilder sb = new StringBuilder();
                InputStreamReader isr = null;
                BufferedReader reader = null;
                isr = new InputStreamReader(connection.getInputStream());
                reader = new BufferedReader(isr);
                line = reader.readLine();

                while (line != null) {
                    sb.append(line);
                    try {
                        line = reader.readLine();
                    } catch (Exception e) {
                        line = null;
                    }
                }
                if (sb != null && !sb.equals(""))
                    response = sb.toString();
                else
                    response = "";
                String result = response;
            } catch (Exception e) {
                e.printStackTrace();

            }
        }catch (Exception e){

        }
    }

    private void simulateSuccessProgress(final CircularProgressButton button) {

        ValueAnimator widthAnimation = ValueAnimator.ofInt(1, 100);
        widthAnimation.setDuration(1500);
        widthAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        widthAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Integer value = (Integer) animation.getAnimatedValue();
                button.setProgress(value);
            }
        });
        widthAnimation.start();
    }

    private void simulateErrorProgress(final CircularProgressButton button) {

        ValueAnimator widthAnimation = ValueAnimator.ofInt(1, 99);
        widthAnimation.setDuration(1500);
        widthAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        widthAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Integer value = (Integer) animation.getAnimatedValue();
                button.setProgress(value);
                if (value == 99) {
                    button.setProgress(-1);
                }
            }
        });
        widthAnimation.start();
    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {

        String hourString = hourOfDay < 10 ? "0" + hourOfDay : "" + hourOfDay;
        String minuteString = minute < 10 ? "0" + minute : "" + minute;
        String time = hourString + ":" + minuteString;
        txtTimePick.setText(time);
    }

    @Override
    public void onDateSet(com.wdullaer.materialdatetimepicker.date.DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {

        String dayString = dayOfMonth < 10 ? "0" + dayOfMonth : "" + dayOfMonth;
        monthOfYear += 1;
        String monthString = monthOfYear < 10 ? "0" + monthOfYear : "" + monthOfYear;
        String date = dayString + "." + monthString + "." + year;
        txtDayPick.setText(date);
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {

        hoursDuration = newVal;
        int daysNo = newVal/24;
        int hoursNo = newVal%24;
        text="";

        if(daysNo>0) {
            text = daysNo +" days";
            if(hoursNo>0)
                text = text + " and ";
        }
            if(hoursNo == 1)
                text = text + "1 hour";
            else
                text = text + hoursNo + " hours";
    }

    public void showNoPickDialog() {

        final Dialog dialog = new Dialog(AddActivity.this);
        dialog.setTitle("Pick duration");
        dialog.setContentView(R.layout.np_dialog);

        Button b1 = (Button) dialog.findViewById(R.id.button1);
        Button b2 = (Button) dialog.findViewById(R.id.button2);

        final NumberPicker np = (NumberPicker) dialog.findViewById(R.id.numberPicker1);
        np.setMaxValue(72);
        np.setMinValue(1);
        np.setWrapSelectorWheel(false);
        np.setValue(hoursDuration);
        np.setOnValueChangedListener(this);

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtSetDuration.setText(text);
                dialog.dismiss();
            }
        });
        b2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private boolean haveInternetConnection(Context context) {

        ServiceManager serviceManager = new ServiceManager(context);
        if (serviceManager.isNetworkAvailable()) {
            return true;
        } else {
            return false;
        }
    }
    private class AddUser extends AsyncTask<Object, Void, Void> {
        String result;
        boolean check(Event event) throws JSONException {
            StringBuilder sb = new StringBuilder();
            InputStreamReader isr = null;
            BufferedReader reader = null;
            HttpURLConnection connection;
            OutputStreamWriter request = null;

            URL url = null;
            String response = null;
            String parameters = "method=json&action=get&id=" + event.getIdExtern();
            try {
                String link = "http://gecont.ro/paul/events.php"+"?"+parameters;
                try {
                    url = new URL(link);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setInstanceFollowRedirects(false);
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                    String line = "";
                    isr = new InputStreamReader(connection.getInputStream());
                    reader = new BufferedReader(isr);
                    line = reader.readLine();

                    while (line != null) {
                        sb.append(line);
                        try {
                            line = reader.readLine();
                        } catch (Exception e) {
                            line = null;
                        }
                        connection.disconnect();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (sb != null && !sb.equals(""))
                    response = sb.toString();
                else
                    response = "";

                result = response;

                JSONObject jsn = new JSONObject(result.toString());
                String fail = jsn.getString("status");
                if (fail.equals("fail"))
                    return false;
            }catch (Exception e){

            }
            return true;

        }
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground( Object... arg0) {
            Event event = (Event) arg0[0];
            String parameters = (String) arg0[1];
            try {
                if(!check(event)){
                    parameters ="method=json&action=add&description=" + event.getDescription() + "&priority="
                            + event.getPriority() + "&startDate=" + event.getStartDate() + "&startHour=" + event.getStartHour() +
                            "&endDate=" + event.getEndDate() + "&endHour=" + event.getEndHour() + "&type=" + event.getType() +
                            "&color=" + event.getColor();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            upload(event,parameters);
            return null;
        }
    }
}
