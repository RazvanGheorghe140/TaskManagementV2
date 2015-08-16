package com.potato.taskmanagerapp.fragments;

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
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.potato.taskmanagerapp.R;
import com.potato.taskmanagerapp.objects.Event;
import com.potato.taskmanagerapp.utils.DBConnector;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
    private CircularProgressButton mailExport, fileExport, pdfExport;

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
        pdfExport = (CircularProgressButton) view.findViewById(R.id.pdf_export);

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

        pdfExport.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ArrayList<Event> events = new ArrayList<Event>();
                if(checkIfValid(txtStartDate.getText().toString(),txtEndDate.getText().toString())){
                    events = DBConnector.getHelper(getActivity()).findEventsBetween(txtStartDate.getText().toString(),txtEndDate.getText().toString(),"date","ascending");
                    if(events.size()==0) {
                        Toast.makeText(getActivity(), "There aren't any events to export.", Toast.LENGTH_LONG).show();
                    }
                    else {
                        Document document = new Document();
                        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
                        File file = new File(Environment.getExternalStorageDirectory(), "Export_" + sdf.format(Calendar.getInstance().getTime()) + ".pdf");
                        try {
                            PdfWriter.getInstance(document, new FileOutputStream(file));
                        } catch (DocumentException e) {
                            simulateErrorProgress(pdfExport);
                            e.printStackTrace();
                        } catch (FileNotFoundException e) {
                            simulateErrorProgress(pdfExport);
                            e.printStackTrace();
                        }
                        document.open();
                        try {
                            document.add(new Paragraph("Name, Start Date, Start Hour, Type, Priority, End Date, End Hour"));
                        } catch (DocumentException e) {
                            simulateErrorProgress(pdfExport);
                            e.printStackTrace();
                        }
                        for (Event event : events) {
                            try {
                                document.add(new Paragraph(event.getDescription() + ", " + event.getStartDate() + ", " + event.getStartHour() + ", " + event.getType() + ", " + event.getPriority() + ", " + event.getEndDate() + ", " + event.getEndHour()));
                            } catch (DocumentException e) {
                                simulateErrorProgress(pdfExport);
                                e.printStackTrace();
                            }
                        }
                        document.close();
                        simulateSuccessProgress(pdfExport);
                    }
                }
                else {
                    Toast.makeText(getActivity(),"Pick valid dates!",Toast.LENGTH_LONG).show();
                }
            }
        });

        fileExport.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ArrayList<Event> events = new ArrayList<Event>();
                if(checkIfValid(txtStartDate.getText().toString(),txtEndDate.getText().toString())){
                    events = DBConnector.getHelper(getActivity()).findEventsBetween(txtStartDate.getText().toString(),txtEndDate.getText().toString(),"date","ascending");
                    if (events.size() != 0){
                        try {
                            DBConnector.getHelper(getActivity()).exportTheDB(events);
                        } catch (IOException e) {
                            e.printStackTrace();
                            simulateErrorProgress(fileExport);
                        }
                        simulateSuccessProgress(fileExport);
                    }
                    else {
                    Toast.makeText(getActivity(),"There aren't any events to export.",Toast.LENGTH_LONG).show();
                    }
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
                    events = DBConnector.getHelper(getActivity()).findEventsBetween(txtStartDate.getText().toString(),txtEndDate.getText().toString(),"date","ascending");
                    if(events.size()==0) {
                        Toast.makeText(getActivity(), "There aren't any events to export.", Toast.LENGTH_LONG).show();
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
                            e.printStackTrace();
                            simulateErrorProgress(mailExport);
                        } catch (IOException e) {
                            simulateErrorProgress(mailExport);
                            e.printStackTrace();
                        }
                    }
                }
                else {
                    Toast.makeText(getActivity(),"Pick valid dates!",Toast.LENGTH_LONG).show();
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

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {

        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser) {
            if(mailExport!= null && fileExport != null && pdfExport!=null) {
                txtStartDate.setText("Pick Start Date   ");
                txtEndDate.setText("Pick End Date   ");
                mailExport.setProgress(0);
                fileExport.setProgress(0);
                pdfExport.setProgress(0);
            }
        }
    }
}
