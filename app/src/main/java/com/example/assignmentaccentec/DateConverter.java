package com.example.assignmentaccentec;

import android.app.Application;
import android.text.format.DateFormat;

import java.util.Calendar;
import java.util.Locale;

public class DateConverter extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
    }
    public static String formatTimestamp(long timeStamp){
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(timeStamp);
        String date = DateFormat.format("dd/MM/yyyy",calendar).toString();
        return date;
    }

}
