package com.lastproject.used_item_market;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapView;
import com.skt.Tmap.poi_item.TMapPOIItem;

import java.util.ArrayList;

public class SignUpMap extends AppCompatActivity {

    TMapView mapView;
    TMapGpsManager tMapGPS;

    LocationManager mLM;
    String mProvider = LocationManager.NETWORK_PROVIDER;

    EditText keywordView;
    ListView listView;
    ArrayAdapter<POI> mAdapter;
    Location cacheLocation = null;
    private Context context = this;
    //위도,경도,이름을 받아올 변수 지정
    final String[] start = new String[1];
    final double[] startX = new double[1];
    final double[] startY = new double[1];
    final TMapPoint[] tMapPoint = {new TMapPoint(startY[0], startX[0])};
    //request 값을 받아올 변수 설정
    private static int REQUEST_ACCESS_FINE_LOCATION = 1000;

    //이전으로 부터 받아온 유저정보 저장
    String email = "";
    String password = "";
    String nickname = "";

    //학교 정보저장
    String nospace_school = "";      //스페이스가 안들어간 학교
    String school = "";              //스페이스가 들어간 학교
    String latitude = "";            //위도
    String longtitude = "";          //경도

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up_map);

        //가져온 저장
        email = getIntent().getStringExtra("email");
        password = getIntent().getStringExtra("password");
        nickname = getIntent().getStringExtra("nickname");

        //어플리케이션 실행 시 위치설정권한이 먼저 확인되어야 하므로 GPSTracker함수 실행
        GPSTracker();

    }

    //어플을 시작하기전 GPS사용여부 알림 및 GPS 사용
    public void GPSTracker() {
        // OS가 Marshmallow(23) 이상일 경우 권한체크 해야함.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                // 권한 없음
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ACCESS_FINE_LOCATION);
            } else {
                // ACCESS_FINE_LOCATION 에 대한 권한이 이미 있음.
                Mapsetting();// permissionselfcheck가 되고나서 Tmap을 실행시켜야 하므로 onstart함수로 이동

            }
        } else {
            // OS가 Marshmallow 이전일 경우 권한체크를 하지 않는다.
            Mapsetting();// permissionselfcheck가 되고나서 Tmap을 실행시켜야 하므로 onstart함수로 이동
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // grantResults[0] 거부 -> -1
        // grantResults[0] 허용 -> 0 (PackageManager.PERMISSION_GRANTED)
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {// ACCESS_FINE_LOCATION 에 대한 권한 획득.
            //권한을 획득하면 GPS설정을 통해 사용자의 위치를 받아옴
            tMapGPS = new TMapGpsManager(this);
            tMapGPS.setProvider(tMapGPS.NETWORK_PROVIDER);
            tMapGPS.OpenGps();
            onStart();// permissionselfcheck가 되고나서 Tmap을 실행시켜야 하므로 onstart함수로 이동
        } else {
            // ACCESS_FINE_LOCATION 에 대한 권한 거부.
            onStart();// permissionselfcheck가 되고나서 Tmap을 실행시켜야 하므로 onstart함수로 이동
        }

    }

    void Mapsetting() {
        //티맵 뷰
        setContentView(R.layout.sign_up_map);
        mLM = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        mapView = (TMapView) findViewById(R.id.start_map_view);
        //기본 위치를 선문대로 지정하기 위해 선문대학교 본관의 위도, 경도값을 지정한다.
        double lng = 127.07475585;
        double lat = 36.80029455;
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

        //검색 리스트 뷰 setting
        keywordView = (EditText) findViewById(R.id.start_edit_keyword);
        listView = (ListView) findViewById(R.id.start_listView);
        //listView.setBackgroundColor(Color.BLACK);   찾았다 이것 때문에 안보이는 것
        listView.setVisibility(listView.INVISIBLE);
        mAdapter = new ArrayAdapter<POI>(this, android.R.layout.simple_list_item_1); // simple_list_item_1는 안드로이드에서 제공하는 폼
        listView.setAdapter(mAdapter);
        InputMethodManager manager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);  // 키패드 컨트롤

        //서치 버튼 클릭시 이벤트 처리
        Button btn = (Button) findViewById(R.id.start_btn_search);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listView.setVisibility(listView.VISIBLE);
                searchPOI();
            }
        });

        //세팅 후 다음으로 넘어가는 버튼
        nextButton();

        //리스트 뷰 클릭시 이벤트 처리
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                //재선택일 경우를 위하여 초기화
                nospace_school = "";
                school = "";
                latitude = "";
                longtitude = "";

                POI poi = (POI) listView.getItemAtPosition(position);
                moveMap(poi.item.getPOIPoint().getLatitude(), poi.item.getPOIPoint().getLongitude()); // 리스트에서 선택한 장소로 맵 이동
                listView.setVisibility(listView.INVISIBLE);
                keywordView.setText(poi.item.getPOIName());
                start[0] = poi.item.getPOIName();
                startX[0] = poi.item.getPOIPoint().getLatitude();
                startY[0] = poi.item.getPOIPoint().getLongitude();
                //System.out.println("대학 지역 : " + poi.item.getPOIName());
                latitude = Double.toString(poi.item.getPOIPoint().getLatitude());
                System.out.println("대학 위도 : " + latitude);
                longtitude = Double.toString(poi.item.getPOIPoint().getLongitude());
                System.out.println("대학 경도 : " + longtitude);
                try {
                    school = poi.item.getPOIName().substring(0, start[0].indexOf(" "));
                    System.out.println("대학 스페이스 들어간 이름 : " + school);

                }catch (Exception e){
                    nospace_school = poi.item.getPOIName();
                    System.out.println("대학 노스페이스 이름 : " + poi.item.getPOIName());
                }
                TMapMarkerItem item = new TMapMarkerItem();
                tMapPoint[0] = new TMapPoint(poi.item.getPOIPoint().getLatitude(), poi.item.getPOIPoint().getLongitude());
                item.setTMapPoint(tMapPoint[0]);//마커위치 포인트 설정
                Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),R.drawable.blue);
                item.setIcon(bitmap);//마커 아이콘
                item.setPosition(0.5f, 1);//마커 크기
                mapView.addMarkerItem("item", item); //마커 추가
                manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS); // 키패드 내리기
            }
        });


    }

    private void searchPOI() {
        TMapData data = new TMapData();
        String keyword = keywordView.getText().toString();
        if (!TextUtils.isEmpty(keyword)) {
            data.findAllPOI(keyword, new TMapData.FindAllPOIListenerCallback() {
                @Override
                public void onFindAllPOI(final ArrayList<TMapPOIItem> arrayList) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mapView.removeAllMarkerItem();
                            mAdapter.clear();

                            for (TMapPOIItem poi : arrayList) {
                                mAdapter.add(new POI(poi));
                            }

                            if (arrayList.size() > 0) {
                                TMapPOIItem poi = arrayList.get(0);//리스트에서 검색한 장소 받아옴
                                moveMap(poi.getPOIPoint().getLatitude(), poi.getPOIPoint().getLongitude());//검색한 위치로 맵 중심이동
                            }
                        }
                    });
                }
            });
        }
    }
    boolean isInitialized = false;

    private void setupMap() {

        isInitialized = true;
        mapView.setMapType(TMapView.MAPTYPE_STANDARD); //Tmap Type설정
        if (cacheLocation != null) {
            moveMap(cacheLocation.getLatitude(), cacheLocation.getLongitude());
            setMyLocation(cacheLocation.getLatitude(), cacheLocation.getLongitude());
        }
        mapView.setOnClickListenerCallBack(new TMapView.OnClickListenerCallback() {
            @Override
            public boolean onPressEvent(ArrayList<TMapMarkerItem> arrayList, ArrayList<TMapPOIItem> arrayList1, TMapPoint tMapPoint, PointF pointF) {
                listView.setVisibility(listView.INVISIBLE);
                return false;
            }

            @Override
            public boolean onPressUpEvent(ArrayList<TMapMarkerItem> arrayList, ArrayList<TMapPOIItem> arrayList1, TMapPoint tMapPoint, PointF pointF) {
                return false;
            }
        });
        mapView.setOnCalloutRightButtonClickListener(new TMapView.OnCalloutRightButtonClickCallback() {
            @Override
            public void onCalloutRightButton(TMapMarkerItem tMapMarkerItem) {

            }
        });
    }


    // 퍼미션 체크하는 함수
    @Override
    protected void onStart() {
        super.onStart();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Mapsetting(); // Tmap 실행
            return;
        }
        Location location = mLM.getLastKnownLocation(mProvider);
        if (location != null) {
            mListener.onLocationChanged(location);
        }
        mLM.requestSingleUpdate(mProvider, mListener, null);
    }
    // 어플리케이션이 종료되면 GPS 백그라운드 제거
    @Override
    protected void onStop() {
        super.onStop();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLM.removeUpdates(mListener);
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

    //다음으로 넘어가는 버튼
    void nextButton(){

        Button bt_next = (Button)findViewById(R.id.next_btn_search);
        bt_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                System.out.println(school + nospace_school);


                if(!latitude.equals("") || !longtitude.equals("")){      //검색하였을 경우만 가능하다.

                    if(nospace_school.equals("")){          //스페이바가 없는 경우로 들어왔을 때

                        if(school.indexOf("대학교") == -1){  //대학교가 아니라 다른거 검색한 경우

                            keywordView.setText("");
                            keywordView.setHint("다시 입력하여 주세요.");

                        }else{          //잘 입력했음으로 다음으로 보낸다.
                            nextInfo();
                        }

                    }else{                                  //스페이스바가 있는 경우로 들어왔을 때

                        if(nospace_school.indexOf("대학교") == -1){  //대학교가 아니라 다른거 검색한 경우

                            keywordView.setText("");
                            keywordView.setHint("다시 입력하여 주세요.");

                        }else{          //잘 입력했음으로 다음으로 보낸다.
                            nextInfo();
                        }
                    }

                }
           }
        });

    }

    void nextInfo(){        //다음으로 넘어갈 경우 줘야하는 정보

        Intent signUp_intent = new Intent(SignUpMap.this, SetInFo.class);
        signUp_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        signUp_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        signUp_intent.putExtra("email", email);
        signUp_intent.putExtra("password",password);
        signUp_intent.putExtra("nickname",nickname);
        signUp_intent.putExtra("latitude", latitude);
        signUp_intent.putExtra("longtitude",longtitude);
        if(nospace_school.equals("")){      //스페이가 있는 경우
            signUp_intent.putExtra("university",school);
        }else{
            signUp_intent.putExtra("university",nospace_school);
        }
        startActivity(signUp_intent);
        finish();

    }


}