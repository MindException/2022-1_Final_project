//개발: 이승현, 김도훈

package com.lastproject.used_item_market;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.StringTokenizer;

//마지막에 저장할 때 상품에서 이미지 키값 가지고 있으려면
//애초에 이미지 가져올때부터 handler로 해야한다.

public class PostPage extends AppCompatActivity {

    String mykey;
    String myUniv;
    String email;
    String nickname;
    String Address;
    String latitude;            //도착 위도 값
    String longtitude;          //도착 경도 값
    String myimg;
    ArrayList<String> suriList;

    //스피너만 따로
    Spinner purposeSpinner;          //모집인원
    Spinner categorySpinner;         //년도

    //가져온 스피너 결과 값
    String result_purpose = "";
    String result_category = "";

    //Realtime-Database
    private FirebaseDatabase database;
    private DatabaseReference myRef;

    //새DB
    private FirebaseFirestore firestore;        //DB
    private StorageReference storageRef;        //정확한 위치에 파일 저장
    private FirebaseStorage storage;            //이미지 저장소

    //이미지 관련
    int requestCode;
    private RecyclerView rv;
    public RecyclePostAdapter adapter;
    TextView img_countView;                                //사진 개수
    //새 이미지 관련
    ArrayList<Uri> uriArrayList = new ArrayList<>();

    //위젯 모음
    ImageButton done_btn;                      //작성하기
    ImageButton back_btn;                      //뒤로가기
    TextView map_btn;                       //맵 선택 버튼
    EditText et_title;                      //제목
    EditText et_cash;                       //가격
    EditText et_text;                       //내용
    private DecimalFormat decimalFormat = new DecimalFormat("#,###");
    private String cash_result="";

    String beforeString = "";               //전 가격 내용
    boolean freeTrigger = false;            //무료인지 아닌지

    Product productInfo;
    ChatInfo chatInfo;

    //다시 되돌아오는 세팅 값
    String ret_title;
    String ret_purpose;
    String ret_category;
    String ret_cash;
    String ret_text;

    //이미지 값 저장
    ArrayList<String> realPath = new ArrayList<>();

    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_page);

        //기본세팅
        email = getIntent().getStringExtra("email");
        mykey = getIntent().getStringExtra("mykey");
        nickname = getIntent().getStringExtra("nickname");
        myUniv = getIntent().getStringExtra("myUniv");
        Address = getIntent().getStringExtra("Address");
        myimg = getIntent().getStringExtra("myimg");
        //t-map 설정 후 다시 돌아왔을 때를 위함이다.
        latitude = getIntent().getStringExtra("latitude");                  //가져올때 키값 잘 보기
        longtitude = getIntent().getStringExtra("longtitude");
        suriList = getIntent().getStringArrayListExtra("uriArrayList");
        //t-map 설정 후 다시 돌아왔을 때 상품 정보
        ret_title = getIntent().getStringExtra("title");
        ret_purpose = getIntent().getStringExtra("purpose");
        ret_category = getIntent().getStringExtra("category");
        ret_cash = getIntent().getStringExtra("cash");
        ret_text = getIntent().getStringExtra("text");

        //파이어베이스 데이터베이스 연동
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        //파이어 베이스 리얼 타임 데이터베이스 연동
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();


        //제품 생성
        productInfo = new Product(nickname, mykey, myUniv);
        productInfo.pictures = new ArrayList<String>();

        rv = (RecyclerView)findViewById(R.id.prouduct_recycleview);
        img_countView = (TextView)findViewById(R.id.count_img);

        //위젯
        back_btn = (ImageButton)findViewById(R.id.pg_xbtn);
        done_btn = (ImageButton)findViewById(R.id.pg_done);
        et_title = (EditText) findViewById(R.id.title_text);
        et_text = (EditText) findViewById(R.id.text);
        map_btn = (TextView)findViewById(R.id.tmap_btn);
        et_cash = (EditText) findViewById(R.id.et_cash);


        if(longtitude != null){     //거래 장소가 있을 경우

            map_btn.setBackgroundDrawable(getResources().getDrawable(R.drawable.bt_bg_blue));

        }



        //999,999,999 값으로 값을 표현
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!TextUtils.isEmpty(charSequence.toString()) && !charSequence.toString().equals(cash_result)){
                    cash_result = decimalFormat.format(Double.parseDouble(charSequence.toString()
                            .replaceAll(",","")));
                    et_cash.setText(cash_result);
                    et_cash.setSelection(cash_result.length());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };

        et_cash.addTextChangedListener(watcher);



        if (suriList == null){      //사진이 없는 경우
            uriArrayList = new ArrayList<>();
        }else{      //사진이 있는 경우
            //사진 ArrayList로 가져오기
            for (int i = 0; i < suriList.size(); i++){

                Uri uri = Uri.parse(suriList.get(i));
                System.out.println(uri.toString());
                uriArrayList.add(uri);

            }
            img_countView.setText("사진 추가(" + uriArrayList.size() + "/5)");
            init();
        }


        back();
        done();
        setPurposeSpinner();
        setCategorySpinner();
        saveImage();
        saveMap();

        //맵에서 다시 돌아온 경우를 위해 초기 설정
        if(ret_title != null){  //제목
            et_title.setText(ret_title);
        }
        if(ret_purpose != null){    //거래 목적
            purposeSpinner.setSelection(getIndex(purposeSpinner, ret_purpose));
        }
        if(ret_category != null){   //카테고리
            categorySpinner.setSelection(getIndex(categorySpinner, ret_category));
        }
        if(ret_text != null){   //글 내용
            et_text.setText(ret_text);
        }

    }

    public void back(){
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(PostPage.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                intent.putExtra("email", email);
                intent.putExtra("mykey", mykey);
                intent.putExtra("nickname", nickname);
                intent.putExtra("myUniv", myUniv);
                startActivity(intent);
            }
        });
    }

    void done(){ // 체크버튼 클릭 시 게시글 작성 완료
        done_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //StorageReference imgRef = storageRef.child("images").child(my).child("test");
                //UploadTask uploadTask = imgRef.putFile(uri);

                String nowTime = Time.nowTime();        //작성 시간을 저장
                String title = et_title.getText().toString();
                String cash = et_cash.getText().toString();
                cash = cashTonum(cash);
                String text = et_text.getText().toString();
                if(cash.equals("") && freeTrigger == true){     //freeTrigger 스피너 참조
                    cash = "0";     //무료이니까 0원이다.
                }

                if(uriArrayList.size() > 0) {

                    if (!title.equals("") && !cash.equals("") && !text.equals("")) {        //공백으로 받는게 없어야 한다.

                        try {       //여기서

                            Toast.makeText(PostPage.this, "상품 등록 중", Toast.LENGTH_SHORT).show();
                            Long icash = Long.parseLong(cash);     //비용 변환 String -> Long
                            //나머지 상품정보 저장
                            productInfo.title = title;
                            productInfo.cost = icash;
                            productInfo.purpose = result_purpose;
                            productInfo.category = result_category;
                            productInfo.text = text;
                            productInfo.time = nowTime;

                            if (longtitude != null && latitude != null) {     //위도 경도가 있을 경우

                                productInfo.destination_longtitude = longtitude;
                                productInfo.destination_latitude = latitude;

                            }
                            //여기서 서버 저장 product -> 이미지(폴더 방식) -> Realtime -> chatInfo 순으로 저장한다.
                            for (int i = 0; i < uriArrayList.size(); i++) {       //진짜 경로를 키값으로 이미지들을 저장한다.(성공)

                                realPath.add(lastName(getRealPathFromURI(uriArrayList.get(i))));
                                productInfo.pictures.add(realPath.get(i));

                            }
                            count = 0;
                            firestore.collection("Product").add(productInfo)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {

                                            productInfo.key = documentReference.getId();
                                            DocumentReference doc = firestore.collection("Product")
                                                    .document(productInfo.key);
                                            doc.set(productInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {

                                                    //상품 저장 성공공
                                                    chatInfo = new ChatInfo(nickname);
                                                    //Realtime-Database에 채팅내용테이블을 생성한다.
                                                    myRef.child("Chatting").child(productInfo.key)
                                                            .setValue(chatInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {    //실시간 데이터베이스에 성공적으로 만들었을 경우

                                                            ChattingRoomInfo chattingRoomInfo = new ChattingRoomInfo(mykey, nickname, chatInfo.start_time, productInfo.title);
                                                            //모든 이미지 파일은 유저키로 할 것이다. profile/유저키
                                                            chattingRoomInfo.customer_images.add(mykey);        //사진을 넣어준다.
                                                            if (productInfo.pictures.size() != 0) {
                                                                chattingRoomInfo.product_imgkey = productInfo.pictures.get(0);      //첫 번째 사진 저장
                                                            }
                                                            chattingRoomInfo.chat_key = productInfo.key;
                                                            //초반 채팅방 설정
                                                            chattingRoomInfo.last_SEE.add(0);
                                                            chattingRoomInfo.last_index = 0;
                                                            chattingRoomInfo.out_customer_index.add(0);
                                                            firestore.collection("ChattingRoom").document(productInfo.key).set(chattingRoomInfo)
                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void unused) {

                                                                            StorageReference imgRef = storageRef.child("images");
                                                                            uploadImg(imgRef);      //사진 저장

                                                                        }
                                                                    });
                                                        }
                                                    });


                                                }//key 값 저장 성공
                                            });
                                        }
                                    });

                        } catch (Exception e) {
                        }

                    } else {
                        Toast.makeText(PostPage.this, "전부 작성하여 주세요.", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(PostPage.this, "사진을 저장하여 주세요", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    void setPurposeSpinner(){
        purposeSpinner = (Spinner) findViewById(R.id.purpose);
        ArrayAdapter ppAdapter = ArrayAdapter.createFromResource(this,R.array.purpose, android.R.layout.simple_spinner_dropdown_item);
        ppAdapter.setDropDownViewResource(R.layout.postpage_spinner_sell_item); //선택목록이 나타날 때 사용할 레이아웃 지정
        purposeSpinner.setAdapter(ppAdapter);  //스피너에 어댑터 적용

        purposeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                result_purpose = purposeSpinner.getSelectedItem().toString(); // 스피너 선택값 가져오기


                //무료나눔일 경우 세팅을 다시하여야 한다.
                if (result_purpose.equals("무료나눔")){

                    freeTrigger = true;
                    et_cash.setText("");
                    et_cash.setHint("무료");
                    et_cash.setEnabled(false);          //무료로 가격 입력 없애기

                }else{

                    freeTrigger = false;
                    et_cash.setEnabled(true);           //판매로 다시 가격 입력하기
                    beforeString = "";
                    et_cash.setText("");
                    et_cash.setHint("가격(원)을 입력하세요.");

                    if(ret_cash != null){   //금액
                        if(!ret_cash.equals("")){
                            CharSequence charSequence = ret_cash;
                            cash_result = decimalFormat.format(Double.parseDouble(charSequence.toString()
                                    .replaceAll(",","")));
                            et_cash.setText(cash_result, TextView.BufferType.EDITABLE);
                            et_cash.setSelection(cash_result.length());
                            ret_cash = null;
                        }
                    }

                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    void setCategorySpinner(){
        categorySpinner = (Spinner) findViewById(R.id.category);
        ArrayAdapter cgAdapter = ArrayAdapter.createFromResource(this,R.array.category, android.R.layout.simple_spinner_dropdown_item);
        cgAdapter.setDropDownViewResource(R.layout.postpage_spinner_sell_item); //선택목록이 나타날 때 사용할 레이아웃 지정
        categorySpinner.setAdapter(cgAdapter);  //스피너에 어댑터 적용

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                result_category = categorySpinner.getSelectedItem().toString(); // 스피너 선택값 가져오기
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    void saveImage(){

        ImageView bt_saveimg = (ImageView)findViewById(R.id.add_img_btn);
        bt_saveimg.setOnClickListener(new View.OnClickListener() {      //사진 추가를 위해 이미지가 눌렸을 경우
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                requestCode = 1;
                startActivityForResult(intent, 1);      //코드 번호로 아래에서 동작한다.

            }
        });
    }

    //갤러리에서 가져온 이미지 저장(완성)
    @Override
    protected void onActivityResult(int requstCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (resultCode == RESULT_OK) {

            //새로운 저장 방법(uri로) 다이렉트로 해버린다.
            Uri uri = data.getData();
            String sUri = getRealPathFromURI(uri);

            if(uriArrayList.size() < 5){       //사진을 5개까지 받는다.
                boolean trigger = true;        //false이면 중복사진으로 저장이 안된다.
                for(int i = 0; i < uriArrayList.size(); i++){
                    if (sUri.equals(getRealPathFromURI(uriArrayList.get(i)))){      //절대 경로가 같을 경우
                        trigger = false;
                        break;
                    }
                }

                if(trigger == true) {
                    //절대 경로가 같은게 없을 경우
                    uriArrayList.add(uri);
                    img_countView.setText("사진 추가(" + uriArrayList.size() + "/5)");
                    init();

                }else{
                    Toast.makeText(PostPage.this, "중복된 사진입니다.", Toast.LENGTH_SHORT).show();
                }
            }



        }//request code 끝
    }

    //리사이클 뷰 동작
    void init(){

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);             //이렇게 하면 수평으로 생성
        rv.setLayoutManager(linearLayoutManager);
        adapter = new RecyclePostAdapter();
        adapter.setOnItemClickListener(new RecyclePostAdapter.OnItemClickListener() {       //사진을 클릭하여 삭제한다.
            @Override
            public void onItemClick(View v, int pos) {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(PostPage.this, R.style.AlertDialogTheme);
                View view = LayoutInflater.from(PostPage.this).inflate(R.layout.dialog, (LinearLayout)findViewById(R.id.layoutDialog));

                alertBuilder.setView(view);
                ((TextView)view.findViewById(R.id.textTitle)).setText("안내");
                ((TextView)view.findViewById(R.id.textMessage)).setText("선택하신 사진을 삭제하시겠습니까?");
                ((Button)view.findViewById(R.id.btnOK)).setText("아니오");
                ((Button)view.findViewById(R.id.btnNO)).setText("예");

                AlertDialog alertDialog = alertBuilder.create();

                view.findViewById(R.id.btnOK).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                    }
                });
                view.findViewById(R.id.btnNO).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        uriArrayList.remove(pos);
                        init();
                        img_countView.setText("사진 추가(" + uriArrayList.size() + "/5)");
                        alertDialog.dismiss();
                        Toast.makeText(PostPage.this, "사진 삭제 성공", Toast.LENGTH_SHORT).show();
                    }
                });

                //다이얼로그 형태 지우기
                if(alertDialog.getWindow() != null){
                    alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                }

                alertDialog.show();

                /*
                alertBuilder.setTitle("안내");
                alertBuilder.setMessage("선택하신 사진을 삭제하시겠습니까?");
                alertBuilder.setPositiveButton("아니오",new DialogInterface.OnClickListener(){         //오른쪽버튼
                    public void onClick(DialogInterface dialog,int which){
                        //삭제하지 않음으로 그냥 둔다.
                    }
                });
                alertBuilder.setNegativeButton("예", new DialogInterface.OnClickListener() {          //왼쪽버튼
                    @Override
                    public void onClick(DialogInterface dialog, int which) { //사진 삭제

                        uriArrayList.remove(pos);
                        init();
                        img_countView.setText("사진 추가(" + uriArrayList.size() + "/5)");
                        Toast.makeText(PostPage.this, "사진 삭제 성공", Toast.LENGTH_SHORT).show();

                    }
                });
                AlertDialog alertDialog = alertBuilder.create();
                alertDialog.show();

                 */
            }
        });
        for(int i = 0; i < uriArrayList.size(); i++){
            adapter.addItem(uriArrayList.get(i));
        }
        rv.addItemDecoration(new RecyclerDecoration(1));       //간격을 추가한다.
        rv.setAdapter(adapter);

    }

    //맵을 여기다가 저장한다.(인탠트 액티비티 죽이면 uri가 초기화 된다.)
    void saveMap(){     //맵 결과값을 인탠트로 가져올 예정

        map_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {        //티맵 버튼이 눌렸을 경우

                //저장되었으니 인탠트로 넘어간다.
                Intent intent = new Intent(PostPage.this, TradeMap.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                intent.putExtra("email", email);
                intent.putExtra("mykey", mykey);
                intent.putExtra("nickname", nickname);
                intent.putExtra("myUniv", myUniv);
                //uri를 String 값으로 변환하여 인탠트 해줘야한다.
                ArrayList<String> suriArrayList = new ArrayList<>();
                for(int i = 0; i < uriArrayList.size(); i++){       //uri를 String 값으로 변환한다.
                    String suri = uriArrayList.get(i).toString();
                    suriArrayList.add(suri);
                }
                intent.putStringArrayListExtra("uriArrayList", suriArrayList);
                intent.putExtra("title", et_title.getText().toString());
                intent.putExtra("purpose", result_purpose);
                intent.putExtra("category", result_category);
                intent.putExtra("cash", cashTonum(et_cash.getText().toString()));       // ',' 제거하고 잘 들어간다.
                intent.putExtra("text", et_text.getText().toString());
                startActivity(intent);

            }
        });

    }


    @SuppressLint("Range")
    private String getRealPathFromURI(Uri uri) {     //절대경로를 불러온다.(이것으로 같은 사진인지 아닌지 비교한다.)

        String ret = "";
        Cursor returnCursor =
                getContentResolver().query(uri, null, null, null, null);
        returnCursor.moveToNext();
        ret = returnCursor.getString( returnCursor.getColumnIndex("_data"));
        returnCursor.close();

        return ret;
    }

    @Override
    public void onBackPressed(){
        //뒤로가기 막기
    }

    //스피너 값을 통하여 위치 반환
    private int getIndex(Spinner spinner, String value){
        for (int i = 0; i < spinner.getCount(); i++){
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)){
                return i;
            }
        }
        return 0;
    }

    String cashTonum(String cash){

        String ret = "";
        StringTokenizer st = new StringTokenizer(cash,",");
        while(st.hasMoreTokens()){
            ret = ret + st.nextToken();
        }
        return ret;

    }

    String lastName(String cash){

        String ret = "";
        StringTokenizer st = new StringTokenizer(cash,"/");
        while(st.hasMoreTokens()){
            ret = st.nextToken();
        }
        return ret;

    }

    void uploadImg(StorageReference storageReference){

        if(count == -1 || count >= realPath.size()){     //사진 전체 저장 후 다음

            Toast.makeText(PostPage.this, "등록 성공", Toast.LENGTH_SHORT).show();
            //저장되었으니 인탠트로 넘어간다.
            Intent intent = new Intent(PostPage.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            intent.putExtra("email", email);
            intent.putExtra("mykey", mykey);
            intent.putExtra("nickname", nickname);
            intent.putExtra("myUniv", myUniv);
            intent.putExtra("myimg", myimg);
            startActivity(intent);
            finish();


        }else{

            StorageReference imgRef = storageReference.child(productInfo.key).child(productInfo.pictures.get(count));
            UploadTask uploadTask = (UploadTask)imgRef.putFile(uriArrayList.get(count))
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            count++;
                            uploadImg(storageReference);

                        }
                    });//성공
        }


    }

    @Override
    protected void onStop(){
        super.onStop();
        count = -1;
    }



}