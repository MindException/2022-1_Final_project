package com.lastproject.used_item_market;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.StringTokenizer;

public class Time {                 //현재 시간을 구하여 주는 클래스이다.

    int year;
    int month;
    int day;
    int hour;
    int minute;

    Time(int year, int month, int day, int hour, int minute){

        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.minute = minute;

    }



    static String nowTime(){        //202111261230  2021년 11월 26일 12시 30분을 이렇게 출력(성공)

        String ret = "";

        //년월일 저장
        LocalDate now = LocalDate.now();        //현재 시간
        ret = Integer.toString(now.getYear());      //년 저장
        int month = now.getMonthValue();            //월 저장
        if (month < 10){
            ret = ret + '0' + Integer.toString(month);
        }else{
            ret = ret + Integer.toString(month);
        }
        int day = now.getDayOfMonth();
        if (day < 10){
            ret = ret + '0' + Integer.toString(day);
        }else{
            ret = ret + Integer.toString(day);
        }

        //시분 저장장
        LocalTime nowTime = LocalTime.now();
        int hour = nowTime.getHour();
        if (hour < 10){
            ret = ret + '0' + Integer.toString(hour);
        }else{
            ret = ret + Integer.toString(hour);
        }
        int minute = nowTime.getMinute();
        if (minute < 10){
            ret = ret + '0' + Integer.toString(minute);
        }else{
            ret = ret + Integer.toString(minute);
        }


        return ret;

    }

    String translateTime(){

        String ret = "";

        //년월일 저장
        ret = Integer.toString(this.year);      //년 저장
        int month = this.month;            //월 저장
        if (month < 10){
            ret = ret + '0' + Integer.toString(month);
        }else{
            ret = ret + Integer.toString(month);
        }
        int day = this.day;
        if (day < 10){
            ret = ret + '0' + Integer.toString(day);
        }else{
            ret = ret + Integer.toString(day);
        }

        //시분 저장장
        int hour = this.hour;
        if (hour < 10){
            ret = ret + '0' + Integer.toString(hour);
        }else{
            ret = ret + Integer.toString(hour);
        }
        int minute = this.minute;
        if (minute < 10){
            ret = ret + '0' + Integer.toString(minute);
        }else{
            ret = ret + Integer.toString(minute);
        }

        return ret;

    }

    String date(){

        String ret = "";

        //년월일 저장
        ret = Integer.toString(this.year);      //년 저장
        ret = ret + "년";
        int month = this.month;            //월 저장
        if (month < 10){
            ret = ret + '0' + Integer.toString(month);
        }else{
            ret = ret + Integer.toString(month);
        }
        ret = ret + "월";

        int day = this.day;
        if (day < 10){
            ret = ret + '0' + Integer.toString(day);
        }else{
            ret = ret + Integer.toString(day);
        }
        ret = ret + "일";

        return ret;
    }

    String time(){

        String ret = "";

        //시분 저장장
        int hour = this.hour;
        if (hour < 10){
            ret = ret + '0' + Integer.toString(hour);
        }else{
            ret = ret + Integer.toString(hour);
        }

        ret = ret + "시";

        int minute = this.minute;
        if (minute < 10){
            ret = ret + '0' + Integer.toString(minute);
        }else{
            ret = ret + Integer.toString(minute);
        }

        ret = ret + "분";

        return ret;

    }



}
