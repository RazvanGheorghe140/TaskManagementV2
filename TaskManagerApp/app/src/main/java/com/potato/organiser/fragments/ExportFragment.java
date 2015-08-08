package com.potato.organiser.fragments;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.dd.CircularProgressButton;
import com.potato.organiser.R;
import com.potato.organiser.objects.Event;
import com.potato.organiser.utils.DBConnector;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Razvan on 07.08.2015.
 */
public class ExportFragment extends Fragment implements DatePickerDialog.OnDateSetListener {

    private TextView txtStartDate, txtEndDate;
    CircularProgressButton mailExport, fileExport;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.export_fragment, container, false);

    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        txtStartDate = (TextView) view.findViewById(R.id.txtStartDatePick);
        txtEndDate = (TextView) view.findViewById(R.id.txtEndDatePick);

        mailExport = (CircularProgressButton) view.findViewById(R.id.mail_export);
        fileExport = (CircularProgressButton) view.findViewById(R.id.file_export);

        txtStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar now = Calendar.getInstance();
                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        ExportFragment.this,
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                );
                dpd.setThemeDark(true);
                dpd.show(getActivity().getFragmentManager(),"Pick a starting date");
            }
        });
        txtEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar now = Calendar.getInstance();
                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        ExportFragment.this,
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                );
                dpd.setThemeDark(true);
                dpd.show(getActivity().getFragmentManager(),"Pick an ending date");
            }
        });


        fileExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<Event> events = new ArrayList<Event>();
                if(checkIfValid(txtStartDate.getText().toString(),txtEndDate.getText().toString())){
                    events = DBConnector.getHelper(getActivity()).findEventsBetween(txtStartDate.getText().toString(),txtEndDate.getText().toString());
                }
                if (events.size() != 0){
                    try {
                        DBConnector.getHelper(getActivity()).exportTheDB(events);
                    } catch (IOException e) {
                        e.printStackTrace();
                        simulateErrorProgress(fileExport);
                    }
                    simulateSuccessProgress(fileExport);
                }
                else{
                    Toast.makeText(getActivity(),"Pick valid dates!",Toast.LENGTH_LONG).show();
                }
            }
        });
        mailExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<Event> events = new ArrayList<Event>();
                if(checkIfValid(txtStartDate.getText().toString(),txtEndDate.getText().toString())){
                    events = DBConnector.getHelper(getActivity()).findEventsBetween(txtStartDate.getText().toString(),txtEndDate.getText().toString());
                }
                if(events.size()==0) {
                    Toast.makeText(getActivity(), "Pick valid dates!", Toast.LENGTH_LONG).show();
                }
                else {
                    try {
                        if (DBConnector.getHelper(getActivity()).exportTheDB(events)) {
                            Calendar cal = Calendar.getInstance();
                            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
                            String TimeStampDB = sdf.format(cal.getTime());
                            String yourfilename = "/Export_" + TimeStampDB + ".csv";
                            String fileName = URLEncoder.encode(yourfilename, "UTF-8");
                            String PATH = Environment.getExternalStorageDirectory() + "/" + fileName.trim().toString();

                            Uri uri = Uri.parse("file://" + PATH);
                            Intent i = new Intent(Intent.ACTION_SEND);
                            i.setType("text/plain");
                            i.putExtra(Intent.EXTRA_EMAIL, "");
                            i.putExtra(Intent.EXTRA_SUBJECT, "Export_" + TimeStampDB);
                            i.putExtra(Intent.EXTRA_TEXT, "");
                            i.putExtra(Intent.EXTRA_STREAM, uri);
                            simulateSuccessProgress(mailExport);
                            getActivity().startActivity(Intent.createChooser(i, "Select application"));

                        } else {
                            simulateErrorProgress(mailExport);
                        }
                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    @Override
    public void onDateSet(com.wdullaer.materialdatetimepicker.date.DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        String dayString = dayOfMonth < 10 ? "0" + dayOfMonth : "" + dayOfMonth;
        monthOfYear+=1;
        String monthString = monthOfYear < 10 ? "0" + monthOfYear : "" + monthOfYear;
        String date = dayString + "." + monthString + "." + year;
        if(view.getTag().equals("Pick a starting date")){
            txtStartDate.setText(date);
        }
        else {
            txtEndDate.setText(date);
        }
    }
    public boolean checkIfValid(String startDate, String endDate){

        SimpleDateFormat sdf  = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        Date dateString1 = null;
        Date dateString2 = null;
        try {
            dateString1 = sdf.parse(startDate+" 00:00");
            dateString2 = sdf.parse(endDate+" 23:59");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        long timeInMillisSinceEpoch1 = dateString1.getTime();
        long timeInMillisSinceEpoch2 = dateString2.getTime();

        if(timeInMillisSinceEpoch1 > timeInMillisSinceEpoch2)
            return false;
        return true;
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
//    @Override
//    public void setUserVisibleHint(boolean isVisibleToUser) {
//        super.setUserVisibleHint(isVisibleToUser);
//        if (isVisibleToUser) {
//            mailExport.setProgress(0);
//            fileExport.setProgress(0);
//        }
//    }
}
