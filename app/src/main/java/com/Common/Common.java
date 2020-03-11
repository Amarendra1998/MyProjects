package com.Common;

import android.location.Location;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Common {
    public static String API_KEY = "16027048bd87637df4ecce043600b0cd";
    public static Location current_location = null;

    public static String API_LINK = "http://api.openweathermap.org/data/2.5/weather";
    public static String apiRequest(String lat, String lng){
        StringBuilder sb = new StringBuilder(API_LINK);
        sb.append(String.format("?lat-%s&lon-%s&APPID-%s&units-metric",lat,lng,API_KEY));
        return sb.toString();
    }
    public static String unixTimeStampToDateTime(double unixTimeStamp){
        DateFormat dateFormat = new SimpleDateFormat("HH:mm");
        Date date = new Date();
        date.setTime((long)unixTimeStamp*1000);
        return dateFormat.format(date);
    }
    public static String getImage(String icon){
        return String.format("http://openweathermap.org/img/w/%s.png",icon);
    }
    public static String getDataFlow(){
        DateFormat dateFormat = new SimpleDateFormat("dd-MMMM-yyyy   HH:mm");
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static String convertUnixToDate(long dt) {
        Date date = new Date(dt*1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm EEE MM yyyy");
        String formatted = sdf.format(date);
        return formatted;
    }

    public static String convertUnixToHour(long dt) {
        Date date = new Date(dt*1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String formatted = sdf.format(date);
        return formatted;
    }
}
