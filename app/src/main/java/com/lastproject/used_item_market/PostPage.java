package com.lastproject.used_item_market;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
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
import java.util.ArrayList;

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

    String product_key = "";
    Product productInfo;
    String chat_key;
    ChatInfo chatInfo;



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
                if(cash.equals("") && freeTrigger == true){     //freeTrigger 스피너 참조
                    cash = "0";     //무료이니까 0원이다.
                }

                if(!title.equals("") && !cash.equals("") && !text.equals("")){        //공백으로 받는게 없어야 한다.

                    try {       //여기서

                        Long icash = Long.parseLong(cash);     //비용 변환 String -> Long
                        //나머지 상품정보 저장
                        productInfo.title = title;
                        productInfo.cost = icash;
                        productInfo.purpose = result_purpose;
                        productInfo.category = result_category;
                        productInfo.text = text;
                        productInfo.time = nowTime;

                        if(longtitude != null && latitude != null){     //위도 경도가 있을 경우

                            productInfo.destination_longtitude = longtitude;
                            productInfo.destination_latitude = latitude;

                        }

                        //서버에 저장
                        //product를 한번 저장을 하고 키값을 저장한 후에 이미지를 가져와야 한다.
                        firestore.collection("Product").add(productInfo)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {

                                    //채팅방을 생성하여서 넣어준다.
                                    String product_key = documentReference.getId();
                                    chatInfo = new ChatInfo(nickname);
                                    //Realtime-Database에 채팅내용테이블을 생성한다.
                                    myRef.child("Chatting").child(product_key)
                                            .setValue(chatInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {    //실시간 데이터베이스에 성공적으로 만들었을 경우

                                            ChattingRoomInfo chattingRoomInfo = new ChattingRoomInfo(mykey, nickname, chatInfo.start_time, productInfo.title);
                                            //모든 이미지 파일은 유저키로 할 것이다. profile/유저키
                                            chattingRoomInfo.customer_images.add(mykey);        //사진을 넣어준다.
                                            if(productInfo.pictures.size() != 0){
                                                chattingRoomInfo.product_imgkey = productInfo.pictures.get(0);      //첫 번째 사진 저장
                                            }
                                            chattingRoomInfo.chat_key = product_key;
                                            //초반 채팅방 설정
                                            chattingRoomInfo.last_SEE.add(0);
                                            chattingRoomInfo.last_index = 0;
                                            chattingRoomInfo.out_customer_index.add(0);
                                            firestore.collection("ChattingRoom").document(product_key).set(chattingRoomInfo)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {

                                                            //저장되었으니 인탠트로 넘어간다.
                                                            Intent intent = new Intent(PostPage.this, MainActivity.class);
                                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                            intent.putExtra("email", email);
                                                            intent.putExtra("mykey", mykey);
                                                            intent.putExtra("nickname", nickname);
                                                            intent.putExtra("myUniv", myUniv);
                                                            intent.putExtra("myimg", myimg);
                                                            startActivity(intent);
                                                            finish();

                                                        }
                                                    });
                                        }
                                    });

                                }
                            })//성공 리스너
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    System.out.println("상품 초기 저장 실패함");
                                }
                            });//리스너 끝

                    }catch (Exception e){

                        et_cash.setText("");
                        et_cash.setHint("가격을 입력하세요.");

                    }
                }else{

                    Toast.makeText(PostPage.this, "상품 정보를 전부 입력하여주세요.", Toast.LENGTH_SHORT).show();

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
            String sUri = getRealPathFromURI(uri);

            if(uriArrayList.size() < 5){       //사진을 5개까지 받는다.
                boolean trigger = true;        //false이면 중복사진으로 저장이 안된다.
                for(int i = 0; i < uriArrayList.size(); i++){
                    if (sUri.equals(getRealPathFromURI(uriArrayList.get(i)))){      //절대 경로가 같을 경우
                        trigger = false;
                        break;
                    }
                }

                if(trigger){

                    //먼저 서버에 저장한다.
                    String nowTime = mykey + Time.nowNewTime();         //이미지들을 전부 시간으로 저장한다.
                    StorageReference imgRef = storageRef.child("images").
                            child(nowTime);        //  경로: 이미지/사용자키/파일이름(현재시간-년일시분초)
                    UploadTask uploadTask = (UploadTask)imgRef.putFile(uri)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {       //서버에 저장 성공
                                    uriArrayList.add(uri);                  //저장한 uri
                                    productInfo.pictures.add(nowTime);      //이미지 키값들
                                    img_countView.setText("사진 추가(" + uriArrayList.size() + "/5)");
                                    init();
                                }
                            });
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
        adapter.setOnItemClickListener(new RecyclePostAdapter.OnItemClickListener() {       //사진을 클릭하여 삭제한다.
            @Override
            public void onItemClick(View v, int pos) {
                System.out.println(pos + "번 클릭");       //시작이 0번이다.
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(PostPage.this);
                alertBuilder.setTitle("안내");
                alertBuilder.setMessage("선택하신 사진을 삭제하시겠습니까?");
                alertBuilder.setPositiveButton("No",new DialogInterface.OnClickListener(){         //오른쪽버튼
                    public void onClick(DialogInterface dialog,int which){
                        //삭제하지 않음으로 그냥 둔다.
                    }
                });
                alertBuilder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {          //왼쪽버튼
                    @Override
                    public void onClick(DialogInterface dialog, int which) { //사진 삭제

                        StorageReference deserRef = storageRef.child("images").
                                child(productInfo.pictures.get(pos));      //리사이클뷰에서 위치 가져온다.

                        deserRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {      //삭제에 성공한 경우
                            @Override
                            public void onSuccess(Void unused) {        //서버에서 삭제하였으니 클라이언트에서도 삭제

                                int position = pos;
                                productInfo.pictures.remove(position);
                                uriArrayList.remove(pos);
                                init();
                                img_countView.setText("사진 추가(" + uriArrayList.size() + "/5)");
                                Toast.makeText(PostPage.this, "사진 삭제 성공", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(PostPage.this, "사진 삭제 실패", Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                });
                AlertDialog alertDialog = alertBuilder.create();
                alertDialog.setOnShowListener( new DialogInterface.OnShowListener() {
                    @Override public void onShow(DialogInterface arg0) {
                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.GREEN);
                        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.GREEN);
                    }
                });

                alertDialog.show();
            }
        });
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


                //저장되었으니 인탠트로 넘어간다.
                Intent intent = new Intent(PostPage.this, TradeMap.class);
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


}