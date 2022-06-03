//개발: 이승현

package com.lastproject.used_item_market;

import com.skt.Tmap.TMapPoint;

public class University {

    public String university;    //대학 이름
    public String latitude;      //위도
    public String longtitude;    //경도
    public double distance;
    public TMapPoint uvpoint;

    public University(){}
    public University(String university, String latitude, String longtitude){

        this.university = university;
        this.latitude = latitude;
        this.longtitude = longtitude;


    }

}
