package com.lastproject.used_item_market;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapView;
import com.skt.Tmap.address_info.TMapAddressInfo;

public class SellPlaceMap extends AppCompatActivity {

    TMapView mapView;
    private Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Mapsetting();

    }



    void Mapsetting() {
        //티맵 뷰
        setContentView(R.layout.sell_place_map);
        mapView = (TMapView) findViewById(R.id.sell_place_view);
        mapView.setUserScrollZoomEnable(true);     //지도 고정
        TMapMarkerItem item = new TMapMarkerItem();

        //double lng = Double.parseDouble(*****.longtitude);    파이어베이스에서 받아온 경도값
        //double lat = Double.parseDouble(*****.latitude);      파이어베이스에서 받아온 위도값
        //mapView.setCenterPoint(lng, lat); sellpage에서 설정된 위도 경도 값을 받아온 상태이면 주석을 풀면된다.
        //TMapPoint centerPoint = new TMapPoint(lng, lat);
        mapView.setOnApiKeyListener(new TMapView.OnApiKeyListenerCallback() {
            @Override
            public void SKTMapApikeySucceed() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setupMap();
                    }
                });
            }

            @Override
            public void SKTMapApikeyFailed(String s) {

            }

        });

        /*
        //item.setTMapPoint(centerPoint);//마커위치 포인트 설정
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.blue); //마커 비트맵
        Bitmap checkbitmap = BitmapFactory.decodeResource(context.getResources(),R.drawable.mapcheck); //체크이미지 비트맵
        item.setIcon(bitmap);//마커 아이콘
        item.setPosition(0.5f, 1);//마커 중앙위치
        mapView.addMarkerItem("item", item); //마커 추가
*/
        //맵 SDK Key
        mapView.setSKTMapApiKey("l7xx303267b599d441eb85003eeddd7b4d4c");
        mapView.setLanguage(TMapView.LANGUAGE_KOREAN);

    }

    private void setupMap() {
        mapView.setMapType(TMapView.MAPTYPE_STANDARD); //Tmap Type설정
    }

    //나가기 버튼 추가 필요
    @Override
    public void onBackPressed(){
        //뒤로가기 막기
    }



}