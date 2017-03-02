package com.progremastudio.emergencymedicalteam;

import android.content.Context;

import java.util.Calendar;


public class TextFormation {

    private static final String[] DAY_OF_WEEK_SHORT = {
            "Sun",
            "Mon",
            "Tue",
            "Wed",
            "Thur",
            "Fri",
            "Sat"
    };

    private static final String[] MONTH_OF_YEAR_SHORT = {
            "Jan",
            "Feb",
            "Mar",
            "Apr",
            "May",
            "Jun",
            "Jul",
            "Aug",
            "Sep",
            "Oct",
            "Nov",
            "Dec"
    };

    public static String date(Context context, String timeStamp) {

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(Long.parseLong(timeStamp));

        String day = DAY_OF_WEEK_SHORT[cal.get(Calendar.DAY_OF_WEEK) - 1];
        String date = String.valueOf(cal.get(Calendar.DATE));
        String month = MONTH_OF_YEAR_SHORT[cal.get(Calendar.MONTH)];
        String year = String.valueOf(cal.get(Calendar.YEAR));

        String formatted = day + ", " + date + " " + month + " " + year;

        return String.valueOf(formatted);
    }

    private static class SimpleTimeFormat {

        private long second;
        private long minute;
        private long hour;

        public SimpleTimeFormat(long millisecond) {
            second = (millisecond / 1000) % 60;
            minute = (millisecond / (1000 * 60)) % 60;
            hour = (millisecond / (1000 * 60 * 60));
        }

        public String getSecond() {
            return ((second<10) ? ("0" + String.valueOf(second)) : (String.valueOf(second)));
        }

        public String getMinute() {
            return ((minute<10) ? ("0" + String.valueOf(minute)) : (String.valueOf(minute)));
        }

        public String getHour() {
            return ((hour<10) ? ("0" + String.valueOf(hour)) : (String.valueOf(hour)));
        }
    }

}
