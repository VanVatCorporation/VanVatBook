package com.vanvatcorporation.vanvatsach.helper;

import android.content.Context;

import com.vanvatcorporation.vanvatsach.R;

import java.util.Calendar;
import java.util.Date;

public class DateHelper {

    public static String getGreeting(Context context, String name) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        String greeting;

        if (hour >= 5 && hour < 11) {
            greeting = context.getString(R.string.greeting_morning) + ", ";
        } else if (hour >= 11 && hour < 13) {
            greeting = context.getString(R.string.greeting_noon) + ", ";
        } else if (hour >= 13 && hour < 18) {
            greeting = context.getString(R.string.greeting_afternoon) + ", ";
        } else if (hour >= 18 && hour < 22) {
            greeting = context.getString(R.string.greeting_evening) + ", ";
        } else {
            greeting = context.getString(R.string.greeting_night) + " ";
        }

        return greeting + name;
    }

    public static int getYearOfTimestamp(long timestamp)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(timestamp));

        return calendar.get(Calendar.YEAR);
    }
    public static int getDeltaOfYearTimeWithCurrentYearTime(int year)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        return Math.abs(calendar.get(Calendar.YEAR) - year);
    }

}
