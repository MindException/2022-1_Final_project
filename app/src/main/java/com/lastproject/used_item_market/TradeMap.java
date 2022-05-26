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

    University university;

    //파이어베이스
    private FirebaseFirestore firestore;
    private CollectionReference universityRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        email = getIntent().getStringExtra("email");
        mykey = getIntent().getStringExtra("mykey");
        nickname = getIntent().getStringExtra("nickname");
        myUniv = getIntent().getStringExtra("myUniv");
        myimg = getIntent().getStringExtra("myimg");

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
                                    AlertDialog.Builder dlg = new AlertDialog.Builder(TradeMap.this);
                                    Handler mHandler = new Handler(Looper.getMainLooper());  //Thread 안에 Thread가 사용되기때문에 handler 사용

                                    mHandler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            dlg.setTitle("해당 위치로 지정하시겠습니까?");
                                            dlg.setPositiveButton("확인",new DialogInterface.OnClickListener(){
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {  // 대학 선택 후 확인버튼 누르면 해당 대학서버로 이동해야함
                                                    //System.out.println("선택 : " + univ.get(selecteduniv[0]));
                                                    nextInfo();

                                                }
                                            }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {

                                                }
                                            });
                                            dlg.show();
                                        }
                                    }, 0);
                                }
                            });



                        }
                    });





                }
            }
        });

        /*
        Query query = universityRef.whereEqualTo("university", myUniv);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                QuerySnapshot documentSnapshot = task.getResult();
                university = (University) documentSnapshot.toObjects(University.class);
            }
        });
*/


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
        PostPage_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PostPage_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PostPage_intent.putExtra("latitude", latitude);
        PostPage_intent.putExtra("longtitude", longtitude);
        PostPage_intent.putExtra("email", email);
        PostPage_intent.putExtra("mykey", mykey);
        PostPage_intent.putExtra("nickname", nickname);
        PostPage_intent.putExtra("myUniv", myUniv);
        PostPage_intent.putExtra("myimg", myimg);
        startActivity(PostPage_intent);
        finish();

    }

    @Override
    public void onBackPressed(){
        //뒤로가기 막기
    }



}