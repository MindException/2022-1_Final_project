//개발: 김도훈

package com.lastproject.used_item_market;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapView;
import com.skt.Tmap.poi_item.TMapPOIItem;

import java.util.ArrayList;
import java.util.Map;

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

        //가져온 저장
        email = getIntent().getStringExtra("email");
        password = getIntent().getStringExtra("password");
        nickname = getIntent().getStringExtra("nickname");

        //어플리케이션 실행 시 위치설정권한이 먼저 확인되어야 하므로 GPSTracker함수 실행

    }


    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // grantResults[0] 거부 -> -1
        // grantResults[0] 허용 -> 0 (PackageManager.PERMISSION_GRANTED)
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // ACCESS_FINE_LOCATION 에 대한 권한 획득.
            tMapGPS = new TMapGpsManager(this);
            tMapGPS.setProvider(tMapGPS.NETWORK_PROVIDER);
            tMapGPS.OpenGps();
            onStart();
        } else {
            // ACCESS_FINE_LOCATION 에 대한 권한 거부.
            Mapsetting();
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

        listView.setVisibility(listView.INVISIBLE);
        mAdapter = new ArrayAdapter<POI>(this, android.R.layout.simple_list_item_1){
            @Override   //리스트뷰 글자 색 설정
            public  View getView(int position, View convertView, ViewGroup parent){
                View view = super.getView(position, convertView, parent);
                TextView tv = (TextView) view.findViewById(android.R.id.text1);
                tv.setTextColor(Color.BLACK);
                return view;
            }
        }; // simple_list_item_1는 안드로이드에서 제공하는 폼
        listView.setAdapter(mAdapter);
        InputMethodManager manager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);  // 키패드 컨트롤

        //서치 버튼 클릭시 이벤트 처리
        TextView btn = (TextView) findViewById(R.id.start_btn_search);
        btn.setOnClickListener(new View.OnClickListener() {
            public boolean equals(){
                String str = keywordView.getText().toString();
                if(str.contains("대") | str.contains("대학") | str.contains("대학교")){  //검색에서 대,대학,대학교가 포함되었는지 확인
                    return true;
                }
                return false;
            }
            @Override
            public void onClick(View view) {
                if(keywordView.getText().toString().length() == 0) {
                    keywordView.setHint("검색어를 입력해주세요.");
                }else {
                    listView.setVisibility(listView.VISIBLE);  // 리스트 뷰 출력
                    if(equals() == true) {  //검색어에 대,대학,대학교가 포함되었다면 검색실행
                        searchPOI();
                        manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS); // 키패드 내리기
                    }else{  //검색어에 대,대학,대학교가 포함되지 않는다면 다시 검색하라는 알림창 출력
                        listView.setVisibility(listView.INVISIBLE); //리스트 뷰 감추기
                        //다이얼로그
                        AlertDialog.Builder custom_alertBuilder = new AlertDialog.Builder(SignUpMap.this);
                        View v = LayoutInflater.from(SignUpMap.this).inflate(R.layout.dialog2, (LinearLayout)findViewById(R.id.layoutDialog2));

                        custom_alertBuilder.setView(v);
                        ((TextView)v.findViewById(R.id.textTitle2)).setText("안내");
                        ((TextView)v.findViewById(R.id.textMessage2)).setText("검색어에 '대', '대학', '대학교'가 포함된 검색어를 입력해주세요.");
                        ((Button)v.findViewById(R.id.btnOK2)).setText("예");

                        AlertDialog alertDialog = custom_alertBuilder.create();

                        v.findViewById(R.id.btnOK2).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                alertDialog.dismiss();
                            }
                        });

                        //다이얼로그 형태 지우기
                        if(alertDialog.getWindow() != null){
                            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                        }

                        alertDialog.show();
                        custom_alertBuilder.setCancelable(false);  //외부 창 클릭시 꺼짐 막기
                        /*
                        custom_alertBuilder.setTitle("대학교를 검색해주세요"); //제목
                        custom_alertBuilder.setMessage("검색어에 '대', '대학', '대학교'가 포함된 검색어를 입력해주세요"); // 메시지
                        custom_alertBuilder.setPositiveButton("확인",new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        custom_alertBuilder.show();
                         */

                    }
                }
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

                latitude = Double.toString(poi.item.getPOIPoint().getLatitude());

                longtitude = Double.toString(poi.item.getPOIPoint().getLongitude());

                try {
                    school = poi.item.getPOIName().substring(0, start[0].indexOf(" "));


                }catch (Exception e){
                    nospace_school = poi.item.getPOIName();
                }
                TMapMarkerItem item = new TMapMarkerItem();
                tMapPoint[0] = new TMapPoint(poi.item.getPOIPoint().getLatitude(), poi.item.getPOIPoint().getLongitude());
                item.setTMapPoint(tMapPoint[0]);//마커위치 포인트 설정
                Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),R.drawable.blue);
                item.setIcon(bitmap);//마커 아이콘
                item.setPosition(0.5f, 1);//마커 크기
                mapView.addMarkerItem("item", item); //마커 추가
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //권한 없음
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ACCESS_FINE_LOCATION);
                return;
            }else {
                // ACCESS_FINE_LOCATION 에 대한 권한이 이미 있음.
                Mapsetting();
            }
            Location location = mLM.getLastKnownLocation(mProvider);
            if (location != null) {
                mListener.onLocationChanged(location);
            }
            mLM.requestSingleUpdate(mProvider, mListener, null);
        }else{
            // OS가 Marshmallow 이전일 경우 권한체크를 하지 않는다.
        }
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

        TextView t_next = (TextView) findViewById(R.id.next_btn_search);
        t_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


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
        signUp_intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
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
        tmapdata.findAroundNamePOI(point,"대학교",5,99,new TMapData.FindAroundNamePOIListenerCallback(){  // (위치, 카테고리, 반경거리, 검색개수)
            @Override
            public void onFindAroundNamePOI(ArrayList<TMapPOIItem> arrayList) {
                ArrayList<String> arrList = new ArrayList<String>(); //대학Array 리스트 생성
                ArrayList<String> SortList = new ArrayList<String>(); //대학Array 리스트 생성
                ArrayList<TMapPoint> Sortuvpoint = new ArrayList<>(); //대학 위치 리스트 생성
                ArrayList<Double> doubles = new ArrayList<Double>(); //거리Array 리스트 생성
                ArrayList<TMapPoint> uvpoint = new ArrayList<>(); //대학 위치 리스트 생성

                if (arrayList != null) {
                    for (int i = 0; i < arrayList.size(); i++) {
                        doubles.add(arrayList.get(i).getDistance(point)); // 받아온 주소 리스트 사이즈 만큼의 대학까지의 거리를 배열에 추가
                        arrayList.get(i).getPOIPoint();
                        // 주차장 단어가 포함되어 있지 않은 대학교 검색
                        if (arrayList.get(i).getPOIName().contains("주차장")) {

                        } else {
                            arrList.add(arrayList.get(i).getPOIName());  //받아온 주소 리스트 사이즈 만큼의 대학 이름을 배열에 추가
                            uvpoint.add(arrayList.get(i).getPOIPoint());
                        }
                    }

                    //공기계에서 접근권한 허용했는데 위치가 안변한다.(0401)

                    //여기서 만든 대학 리스트를 정렬하여 출력한다.
                    ArrayList<University> univlist = new ArrayList<University>();
                    for (int k = 0; k < arrList.size(); k++) {            //대학정보를 만들어서 넣어준다.

                        University uv = new University();
                        uv.university = arrList.get(k);
                        uv.distance = doubles.get(k);
                        uv.uvpoint = uvpoint.get(k);
                        univlist.add(uv);

                    }

                    univlist.sort(new CompareUnivDistance<University>());     //이걸로 하면 최소거리부터 정렬된다.
                    ArrayList<University> stackUniv = new ArrayList<University>();      //순서대로 되었으니 대학만 받을 것이다.
                    ArrayList<TMapPoint> stackpoint = new ArrayList<>(); //대학 위치 리스트 생성
                    int trigger = 0;            //1이 되면 저장한적이 있음으로 저장을 안한다.
                    for (int i = 0; i < univlist.size(); i++) {

                        int endIndex = 0;       //대학교라는 글자의 시작 인덱스를 가져온다.
                        endIndex = univlist.get(i).university.indexOf("대학교");       //대학교에서 대의 시작 인덱스를 가져온다.
                        if (endIndex != -1) {         //대학교로 검색하여 나온 경우

                            univlist.get(i).university = univlist.get(i).university.substring(0, endIndex + 3);  //이렇게 하면 대학교까지의 이름만 가져온다.
                            if (stackUniv.size() == 0) {       //아무것도 없을 경우인 처음에는 그냥 넣는다.
                                stackUniv.add(univlist.get(i));
                                stackpoint.add(univlist.get(i).uvpoint);
                            } else {
                                for (int j = 0; j < stackUniv.size(); j++) {

                                    if (univlist.get(i).university.equals(stackUniv.get(j).university)) {     //서로 대학이 같지 않을 경우만 추가
                                        trigger = 1;        //저장한 적이 있다.
                                    }
                                }
                                if (trigger == 0) {
                                    stackUniv.add(univlist.get(i));
                                    stackpoint.add(univlist.get(i).uvpoint);
                                }
                            }

                        } else {                 //대학으로 검색하여 나온 경우

                            endIndex = univlist.get(i).university.indexOf("대학");       //대학에서 대의 시작 인덱스를 가져온다.
                            univlist.get(i).university = univlist.get(i).university.substring(0, endIndex + 2);  //이렇게 하면 대학교까지의 이름만 가져온다.
                            if (stackUniv.size() == 0) {       //아무것도 없을 경우인 처음에는 그냥 넣는다.
                                stackUniv.add(univlist.get(i));
                                stackpoint.add(univlist.get(i).uvpoint);
                            } else {
                                for (int j = 0; j < stackUniv.size(); j++) {

                                    if (univlist.get(i).university.equals(stackUniv.get(j).university)) {     //서로 대학이 같지 않을 경우만 추가
                                        trigger = 1;        //저장한 적이 있다.
                                    }
                                }
                                if (trigger == 0) {
                                    stackUniv.add(univlist.get(i));
                                    stackpoint.add(univlist.get(i).uvpoint);
                                }
                            }

                        }

                        trigger = 0;

                    }//대학교 중복 없애기(성공)

                    //결론적으로 여기에 대학중복없이 최소거리부터 정렬되어 들어가있는 배열은 stackUniv이다.

                    for (int i = 0; i < stackUniv.size(); i++) {
                        if(i < 5) {  // 대학리스트 5개까지만 넘김
                            SortList.add(stackUniv.get(i).university);
                            Sortuvpoint.add(stackpoint.get(i));
                        }
                    }
                    dialog(SortList, Sortuvpoint);


                }else{
                    dialog(null, null);
                }
            }
        });

    }
    void dialog(ArrayList<String> university, ArrayList<TMapPoint> universitypoint){
        AlertDialog.Builder dlg = new AlertDialog.Builder(SignUpMap.this, R.style.AlertDialogTheme2);
        AlertDialog.Builder cusdlg = new AlertDialog.Builder(SignUpMap.this, R.style.AlertDialogTheme);
        View view = LayoutInflater.from(SignUpMap.this).inflate(R.layout.dialog, (LinearLayout)findViewById(R.id.layoutDialog));
        final int[] selecteduniv = {0};

        Handler mHandler = new Handler(Looper.getMainLooper());  //Thread 안에 Thread가 사용되기때문에 handler 사용

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dlg.setCancelable(false);
                if(university != null){
                    dlg.setTitle("반경 2KM 대학");
                    dlg.setSingleChoiceItems(university.toArray(new String[0]), 0, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            selecteduniv[0] = which;

                        }
                    }).setPositiveButton("취소",new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which) {  // 대학 선택 후 확인버튼 누르면 해당 대학서버로 이동해야함

                        }
                    }).setNegativeButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            school = university.get(selecteduniv[0]);
                            nospace_school = university.get(selecteduniv[0]);
                            latitude = Double.toString(universitypoint.get(selecteduniv[0]).getLatitude());
                            longtitude = Double.toString(universitypoint.get(selecteduniv[0]).getLongitude());
                            nextInfo();
                        }
                    });
                    dlg.show();
                }
                if(university == null){
                    cusdlg.setView(view);
                    ((TextView)view.findViewById(R.id.textTitle)).setText("대학");
                    ((TextView)view.findViewById(R.id.textMessage)).setText("주변에 대학이 존재하지 않습니다.");
                    ((Button)view.findViewById(R.id.btnOK)).setText("확인");

                    AlertDialog alertDialog = cusdlg.create();

                    view.findViewById(R.id.btnOK).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alertDialog.dismiss();
                        }
                    });

                    //다이얼로그 형태 지우기
                    if(alertDialog.getWindow() != null){
                        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                    }

                    alertDialog.show();
                    /*
                    dlg.setTitle("대학");
                    dlg.setMessage("주변에 대학이 존재하지 않습니다."); // 메시지
                    dlg.setPositiveButton("확인",new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    dlg.show();

                     */
                }
            }
        }, 1000);



    }

    @Override
    public void onBackPressed(){
        //뒤로가기 막기
    }



}