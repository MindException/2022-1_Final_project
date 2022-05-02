package com.lastproject.used_item_market;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import java.util.ArrayList;

public class PostPage extends AppCompatActivity {

    String mykey;
    String myUniv;
    String email;
    String nickname;
    String Address;
    String latitude;            //도착 위도 값
    String longtitude;          //도착 경도 값

    //스피너만 따로
    Spinner purposeSpinner;          //모집인원
    Spinner categorySpinner;         //년도

    //가져온 스피너 결과 값
    String result_purpose = "";
    String result_category = "";

    //새DB
    private FirebaseFirestore firestore;        //DB
    private FirebaseStorage storage;            //이미지 저장소
    private StorageReference storageRef;        //정확한 위치에 파일 저장

    //이미지 관련
    ArrayList<String> imgarray = new ArrayList<String>();       //이미지 이진화 모음
    int requestCode;
    private RecyclerView rv;
    public RecyclePostAdapter adapter;
    TextView img_countView;                                //사진 개수
    //새 이미지 관련
    ArrayList<Uri> uriArrayList = new ArrayList<Uri>();

    //위젯 모음
    TextView done_btn;                      //작성하기
    TextView back_btn;                      //뒤로가기
    TextView map_btn;                       //맵 선택 버튼
    EditText et_title;                      //제목
    EditText et_cash;                       //가격
    EditText et_text;                       //내용
    ImageView checkiv;                      //지도정보가 있나 없나 체크

    String beforeString = "";               //전 가격 내용
    boolean freeTrigger = false;            //무료인지 아닌지



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
        //t-map 설정 후 다시 돌아왔을 때를 위함이다.
        latitude = getIntent().getStringExtra("latitude");                  //가져올때 키값 잘 보기
        longtitude = getIntent().getStringExtra("longtitude");

        //파이어베이스 데이터베이스 연동
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        rv = (RecyclerView)findViewById(R.id.prouduct_recycleview);
        img_countView = (TextView)findViewById(R.id.count_img);

        //위젯
        back_btn = (TextView)findViewById(R.id.pg_xbtn);
        done_btn = (TextView)findViewById(R.id.pg_done);
        et_title = (EditText) findViewById(R.id.title_text);
        et_cash = (EditText) findViewById(R.id.et_cash);
        et_text = (EditText) findViewById(R.id.text);
        map_btn = (TextView)findViewById(R.id.tmap_btn);
        checkiv = (ImageView)findViewById(R.id.check_mark);

        if(longtitude != null && latitude != null){     //위도 경도가 있을 경우

            checkiv.setImageResource(R.drawable.check);

        }else{      //위도 경도가 없을 경우

            checkiv.setImageResource(R.drawable.uncheck);

        }


        back();
        done();
        setPurposeSpinner();
        setCategorySpinner();
        saveImage();
        saveMap();
    }

    public void back(){
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(PostPage.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
                String text = et_text.getText().toString();
                if(cash.equals("") && freeTrigger == true){
                    cash = "0";     //무료이니까 0원이다.
                }

                if(!title.equals("") && !cash.equals("") && !text.equals("")){        //공백으로 받는게 없어야 한다.

                    try {       //여기서

                        Long icash = Long.parseLong(cash);     //한번
                        Product savepd = new Product(mykey,nickname,myUniv,title,icash,result_purpose,
                                result_category, text, nowTime);

                        /*
                                여기다가 위도 경도 존재할 경우를 넣어줘야 한다.
                         */


                        //서버에 저장
                        //product를 한번 저장을 하고 키값을 저장한 후에 이미지를 가져와야 한다.
                        firestore.collection("Product").add(savepd)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    String product_key = documentReference.getId();     //key값 가져오는거 성공

                                    //요기다가 이미지 저장 후 다시 저장
                                    for(int i = 0; i < uriArrayList.size(); i++){       //사진을 하나씩 저장
                                        //사진 저장 경로
                                        StorageReference imgRef = storageRef.child("images").
                                                child(product_key).child(Time.nowTime() + Integer.toString(i));

                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    System.out.println("상품 초기 저장 실패함");
                                }
                            });

                        //저장되었으니 인탠트로 넘어간다.
                        Intent intent = new Intent(PostPage.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("email", email);
                        intent.putExtra("mykey", mykey);
                        intent.putExtra("nickname", nickname);
                        intent.putExtra("myUniv", myUniv);
                        startActivity(intent);

                    }catch (Exception e){

                        et_cash.setText("");
                        et_cash.setHint("가격을 입력하세요.");

                    }
                }//if문 끝
            }
        });
    }

    void setPurposeSpinner(){
        purposeSpinner = (Spinner) findViewById(R.id.purpose);
        ArrayAdapter ppAdapter = ArrayAdapter.createFromResource(this,R.array.purpose, android.R.layout.simple_spinner_dropdown_item);
        ppAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); //선택목록이 나타날 때 사용할 레이아웃 지정
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
        cgAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); //선택목록이 나타날 때 사용할 레이아웃 지정
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

        ImageButton bt_saveimg = (ImageButton)findViewById(R.id.add_img_btn);
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
            //uri.compareTo로 같은 사진이면 양수가 나오고, 다른 사진이면 음수가 나온다.

            //새로운 저장 방법(uri로) 다이렉트로 해버린다.
            Uri uri = data.getData();
            if(uriArrayList.size() < 5){       //사진을 5개까지 받는다.
                boolean trigger = true;        //false이면 중복사진으로 저장이 안된다.
                for(int i = 0; i < uriArrayList.size(); i++){
                    if(uri.compareTo(uriArrayList.get(i)) < 0){     //음수일 경우
                        trigger = false;
                        break;
                    }
                }
                if(trigger){
                    uriArrayList.add(uri);
                    img_countView.setText("사진 추가(" + uriArrayList.size() + "/5)");
                    init();
                }else{      //중복 사진 발생
                    Toast.makeText(PostPage.this, "중복 사진입니다.", Toast.LENGTH_SHORT).show();
                }
            }



            /*
            StorageReference imgRef = storageRef.child(mykey).child("test");
            UploadTask uploadTask = imgRef.putFile(uri);

            //DB에서 이미지 꺼내기기
            try {
                Thread.sleep(2000);
            }catch (Exception e){}

            StorageReference getRef = storageRef.child(mykey + "/" + "test");
            try {
                File localFile = File.createTempFile("images", "jpeg");
                getRef.getFile(localFile).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println("사진 가져오기 실패");
                    }
                }).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        System.out.println("사진 가져오기 성공");
                        FileInputStream inputStream = null;
                        try {
                            inputStream = new FileInputStream(localFile);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);        //비트맵으로 가져온다.
                        ByteArrayOutputStream stream1 = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.WEBP, 100, stream1);
                        byte[] img1Byte = stream1.toByteArray();
                        String img = ParseIMG.byteArrayToBinaryString(img1Byte); //(성공)
                        if(imgarray.size() < 5){       //사진을 5개까지 받는다.
                            //배열에 저장
                            imgarray.add(img);
                            //사진 개수 카운트
                            img_countView.setText("사진 추가(" + imgarray.size() + "/5)");
                            //여기서 리사이클 뷰 실행
                            init();
                        }
                    }
                });


            } catch (IOException e) {
                System.out.println("try catch");
                e.printStackTrace();
            }
            */
        }//request code 끝
    }

    //리사이클 뷰 동작
    void init(){

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);             //이렇게 하면 수평으로 생성
        rv.setLayoutManager(linearLayoutManager);
        adapter = new RecyclePostAdapter();
        for(int i = 0; i < uriArrayList.size(); i++){
            adapter.addItem(uriArrayList.get(i));
        }
        rv.addItemDecoration(new RecyclerDecoration(5));       //간격을 추가한다.
        rv.setAdapter(adapter);

    }

    //맵을 여기다가 저장한다.
    void saveMap(){     //맵 결과값을 인탠트로 가져올 예정

        map_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {        //티맵 버튼이 눌렸을 경우

                /*       ****** class만 바꾸어서 넘어갔다 오기
                //저장되었으니 인탠트로 넘어간다.
                Intent intent = new Intent(PostPage.this, *******.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("email", email);
                intent.putExtra("mykey", mykey);
                intent.putExtra("nickname", nickname);
                intent.putExtra("myUniv", myUniv);
                startActivity(intent);
                */

            }
        });

    }
}