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
import java.util.Comparator;

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
                findAroundUniv(location.getLatitude(), location.getLongitude());
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

    //주변대학 검색
    public void findAroundUniv(double lat, double lng){
        TMapData tmapdata = new TMapData();
        TMapPoint point = new TMapPoint(lat, lng);
        tmapdata.findAroundNamePOI(point,"대학교",3,99,new TMapData.FindAroundNamePOIListenerCallback(){  // (위치, 카테고리, 반경거리, 검색개수)
            @Override
            public void onFindAroundNamePOI(ArrayList<TMapPOIItem> arrayList) {
                ArrayList<String> arrList = new ArrayList<String>(); //대학Array 리스트 생성
                ArrayList<Double> doubles = new ArrayList<Double>(); //거리Array 리스트 생성
                for (int i = 0; i < arrayList.size(); i++) {
                    doubles.add(arrayList.get(i).getDistance(point)); // 받아온 주소 리스트 사이즈 만큼의 대학까지의 거리를 배열에 추가
                    // 주차장 단어가 포함되어 있지 않은 대학교 검색
                    if(arrayList.get(i).getPOIName().contains("주차장")){

                    }else{
                        arrList.add(arrayList.get(i).getPOIName());  //받아온 주소 리스트 사이즈 만큼의 대학 이름을 배열에 추가
                    }
                }


                //공기계에서 접근권한 허용했는데 위치가 안변한다.(0401)

                //여기서 만든 대학 리스트를 정렬하여 출력한다.
                ArrayList<University> univlist = new ArrayList<University>();
                for(int k = 0; k < arrList.size(); k++){            //대학정보를 만들어서 넣어준다.

                    University uv = new University();
                    uv.university = arrList.get(k);
                    uv.distance = doubles.get(k);
                    univlist.add(uv);

                }

                univlist.sort(new CompareUnivDistance<University>());     //이걸로 하면 최소거리부터 정렬된다.
                ArrayList<University> stackUniv = new ArrayList<University>();      //순서대로 되었으니 대학만 받을 것이다.
                int trigger = 0;            //1이 되면 저장한적이 있음으로 저장을 안한다.
                for(int i = 0; i < univlist.size(); i++){

                    int endIndex = 0;       //대학교라는 글자의 시작 인덱스를 가져온다.
                    endIndex = univlist.get(i).university.indexOf("대학교");       //대학교에서 대의 시작 인덱스를 가져온다.
                    if(endIndex != -1){         //대학교로 검색하여 나온 경우

                        univlist.get(i).university = univlist.get(i).university.substring(0,endIndex+3);  //이렇게 하면 대학교까지의 이름만 가져온다.
                        if(stackUniv.size() == 0){       //아무것도 없을 경우인 처음에는 그냥 넣는다.
                            stackUniv.add(univlist.get(i));
                        }else{
                            for(int j = 0; j < stackUniv.size(); j++){

                                if(univlist.get(i).university.equals(stackUniv.get(j).university)){     //서로 대학이 같지 않을 경우만 추가
                                    trigger = 1;        //저장한 적이 있다.
                                }
                            }
                            if(trigger == 0){
                                stackUniv.add(univlist.get(i));
                            }
                        }

                    }else{                 //대학으로 검색하여 나온 경우

                        endIndex = univlist.get(i).university.indexOf("대학");       //대학에서 대의 시작 인덱스를 가져온다.
                        univlist.get(i).university = univlist.get(i).university.substring(0,endIndex+2);  //이렇게 하면 대학교까지의 이름만 가져온다.
                        if(stackUniv.size() == 0){       //아무것도 없을 경우인 처음에는 그냥 넣는다.
                            stackUniv.add(univlist.get(i));
                        }else{
                            for(int j = 0; j < stackUniv.size(); j++){

                                if(univlist.get(i).university.equals(stackUniv.get(j).university)){     //서로 대학이 같지 않을 경우만 추가
                                    trigger = 1;        //저장한 적이 있다.
                                }
                            }
                            if(trigger == 0){
                                stackUniv.add(univlist.get(i));
                            }
                        }

                    }

                    trigger = 0;

                }//대학교 중복 없애기(성공)

                //결론적으로 여기에 대학중복없이 최소거리부터 정렬되어 들어가있는 배열은 stackUniv이다.

                /*사용 예시
                for(int i = 0; i < stackUniv.size(); i++){

                    System.out.println("대학 리스트 출려: " + stackUniv.get(i).university);

                }
                */


            }//
        });
    }

}