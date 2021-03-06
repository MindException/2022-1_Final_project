//개발: 김도훈

package com.lastproject.used_item_market;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapView;
import com.skt.Tmap.address_info.TMapAddressInfo;

import java.util.ArrayList;

public class TradeMap extends AppCompatActivity {

    TMapView mapView;
    LocationManager mLM;
    Location cacheLocation = null;
    private Context context = this;

    String latitude = "";            //위도
    String longtitude = "";          //경도
    String email;
    String mykey;
    String nickname;
    String myUniv;
    String myimg;

    //세팅값
    String ret_title;
    String ret_purpose;
    String ret_category;
    String ret_cash;
    String ret_text;

    University university;

    //파이어베이스
    private FirebaseFirestore firestore;
    private CollectionReference universityRef;

    ArrayList<String> suriArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        email = getIntent().getStringExtra("email");
        mykey = getIntent().getStringExtra("mykey");
        nickname = getIntent().getStringExtra("nickname");
        myUniv = getIntent().getStringExtra("myUniv");
        myimg = getIntent().getStringExtra("myimg");
        suriArrayList = getIntent().getStringArrayListExtra("uriArrayList");
        ret_title = getIntent().getStringExtra("title");
        ret_purpose = getIntent().getStringExtra("purpose");
        ret_category = getIntent().getStringExtra("category");
        ret_cash = getIntent().getStringExtra("cash");
        ret_text = getIntent().getStringExtra("text");

        Mapsetting();

    }



    void Mapsetting() {
        firestore = FirebaseFirestore.getInstance();
        universityRef = firestore.collection("University");

        DocumentReference documentReference = universityRef.document(myUniv);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if(documentSnapshot.exists()){
                    university = documentSnapshot.toObject(University.class);

                    //티맵 뷰
                    setContentView(R.layout.trade_map);
                    mLM = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
                    mapView = (TMapView) findViewById(R.id.trade_map_view);
                    TMapMarkerItem item = new TMapMarkerItem();
                    //기본 위치를 사용자가 지정한 학교로 한다.

                    double lng = Double.parseDouble(university.longtitude);
                    double lat = Double.parseDouble(university.latitude);
                    System.out.println("위도 : " + lng);
                    System.out.println("경도 : " + lat);
                    mapView.setCenterPoint(lng, lat);
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

                    //맵 SDK Key
                    mapView.setSKTMapApiKey("l7xx303267b599d441eb85003eeddd7b4d4c");
                    mapView.setLanguage(TMapView.LANGUAGE_KOREAN);


                    // 지도 움직이고 나면 중앙에 마커 생성
                    mapView.setOnDisableScrollWithZoomLevelListener(new TMapView.OnDisableScrollWithZoomLevelCallback() {
                        @Override
                        public void onDisableScrollWithZoomLevelEvent(float zoom, TMapPoint centerPoint) {
                            latitude = "";            //위도
                            longtitude = "";          //경도

                            item.setTMapPoint(centerPoint);//마커위치 포인트 설정
                            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.blue); //마커 비트맵
                            Bitmap checkbitmap = BitmapFactory.decodeResource(context.getResources(),R.drawable.mapcheck); //체크이미지 비트맵
                            item.setIcon(bitmap);//마커 아이콘
                            item.setPosition(0.5f, 1);//마커 중앙위치
                            mapView.addMarkerItem("item", item); //마커 추가

                            TMapData tmapdata = new TMapData();
                            tmapdata.reverseGeocoding(centerPoint.getLatitude(), centerPoint.getLongitude(), "A04", new TMapData.reverseGeocodingListenerCallback() {
                                @Override
                                public void onReverseGeocoding(TMapAddressInfo addressInfo) {
                                    if(addressInfo.strBuildingName.toString().equals("")){
                                        try {
                                            latitude = Double.toString(centerPoint.getLatitude());
                                            longtitude = Double.toString(centerPoint.getLongitude());
                                            item.setCalloutTitle(addressInfo.strRoadName);
                                        }catch (Exception e){
                                            item.setCalloutTitle("없는 주소");
                                        }
                                    }else{
                                        try {
                                            latitude = Double.toString(centerPoint.getLatitude());
                                            longtitude = Double.toString(centerPoint.getLongitude());
                                            item.setCalloutTitle(addressInfo.strBuildingName);
                                        }catch (Exception e){
                                            item.setCalloutTitle("없는 주소");
                                        }
                                    }
                                }
                            });

                            item.setCanShowCallout(true);//풍선뷰 사용여부
                            item.setCalloutRightButtonImage(checkbitmap);//풍선뷰 오른쪽 이미지 사용
                            mapView.setOnCalloutRightButtonClickListener(new TMapView.OnCalloutRightButtonClickCallback(){//풍선뷰 오른쪽 이미지 사용 이벤트 리스너
                                @Override
                                public void onCalloutRightButton(TMapMarkerItem tMapMarkerItem) {
                                    AlertDialog.Builder dlg = new AlertDialog.Builder(TradeMap.this, R.style.AlertDialogTheme);
                                    View view = LayoutInflater.from(TradeMap.this).inflate(R.layout.dialog, (LinearLayout)findViewById(R.id.layoutDialog));
                                    Handler mHandler = new Handler(Looper.getMainLooper());  //Thread 안에 Thread가 사용되기때문에 handler 사용


                                    mHandler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            dlg.setView(view);
                                            ((TextView)view.findViewById(R.id.textTitle)).setText("안내");
                                            ((TextView)view.findViewById(R.id.textMessage)).setText("해당 위치로 지정하시겠습니까?");
                                            ((Button)view.findViewById(R.id.btnOK)).setText("취소");
                                            ((Button)view.findViewById(R.id.btnNO)).setText("확인");

                                            AlertDialog alertDialog = dlg.create();

                                            view.findViewById(R.id.btnOK).setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    alertDialog.dismiss();
                                                }
                                            });
                                            view.findViewById(R.id.btnNO).setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    nextInfo();
                                                }
                                            });

                                            //다이얼로그 형태 지우기
                                            if(alertDialog.getWindow() != null){
                                                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                                            }

                                            alertDialog.show();

                                        }
                                    }, 0);


                                }
                            });



                        }
                    });





                }
            }
        });


    }



    boolean isInitialized = false;

    private void setupMap() {

        isInitialized = true;
        mapView.setMapType(TMapView.MAPTYPE_STANDARD); //Tmap Type설정

    }




    //맵을 이동시킨다.
    private void moveMap(double lat, double lng) {
        mapView.setCenterPoint(lng, lat);
    }
    //현재 자신의 위치로 맵이 이동하고 중앙에 마커를 표시한다.
    private void setMyLocation(double lat, double lng) {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),R.drawable.blue);
        mapView.setIcon(bitmap);
        mapView.setLocationPoint(lng, lat);
        mapView.setIconVisibility(true);
    }

    LocationListener mListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (isInitialized) {
                moveMap(location.getLatitude(), location.getLongitude());
                setMyLocation(location.getLatitude(), location.getLongitude());
            } else {
                cacheLocation = location;
            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    void nextInfo(){        //다음으로 넘어갈 경우 줘야하는 정보

        Intent PostPage_intent = new Intent(TradeMap.this, PostPage.class);
        //이거 인탠트 할 때 아래 행위 하지말아야한다 액티비티가 죽어버리면 같이 uri도 죽어버린다.
        PostPage_intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        PostPage_intent.putExtra("latitude", latitude);
        PostPage_intent.putExtra("longtitude", longtitude);
        PostPage_intent.putExtra("email", email);
        PostPage_intent.putExtra("mykey", mykey);
        PostPage_intent.putExtra("nickname", nickname);
        PostPage_intent.putExtra("myUniv", myUniv);
        PostPage_intent.putExtra("myimg", myimg);
        PostPage_intent.putStringArrayListExtra("uriArrayList", suriArrayList);
        PostPage_intent.putExtra("title", ret_title);
        PostPage_intent.putExtra("purpose", ret_purpose);
        PostPage_intent.putExtra("category", ret_category);
        PostPage_intent.putExtra("cash", ret_cash);
        PostPage_intent.putExtra("text", ret_text);
        startActivity(PostPage_intent);
        finish();
    }

    @Override
    public void onBackPressed(){
        //뒤로가기 막기
    }



}