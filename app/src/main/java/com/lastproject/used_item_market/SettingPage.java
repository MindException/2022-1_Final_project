package com.lastproject.used_item_market;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SettingPage extends AppCompatActivity {

    //기본 나의 정보
    String email = "";
    String mykey = "";
    String nickname = "";
    String myUniv = "";
    String myimg = "";

    //위젯 모음
    TextView selling;
    TextView free_providing;
    TextView success_deal;
    TextView emailText;
    TextView nicknameText;

    //새DB
    private FirebaseFirestore firestore;        //DB
    CollectionReference product_Ref;
    DocumentReference user_Ref;

    //RealtimeDatbase
    private FirebaseDatabase database;
    private DatabaseReference myRef;

    //이미지 DB
    private FirebaseStorage storage;            //이미지 저장소
    private StorageReference storageRef;        //정확한 위치에 파일 저장

    //이미지 관련 위젯 및 어뎁터
    RecyclerView recyclerView;
    MyPageAdapter1 myPageAdapter1;
    MyPageAdapter2 myPageAdapter2;

    // 상품 관련
    List<User> userList = new ArrayList<User>();
    List<Product> productList = new ArrayList<Product>();  //여기에 모든 상품들이 들어간다.

    //유저
    private User user;

    //프로필 사진진
    ImageView imageView;
    private int requestCode;

    ImageButton adapterImageButton;
    ImageButton profileImageButton;

    //삭제 혹은 성공을 위한 정보
    ChattingRoomInfo chattingRoomInfo;
    ChatInfo chatInfo;

    String where = "";
    String selected = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_page);

        //기본세팅
        email = getIntent().getStringExtra("email");
        mykey = getIntent().getStringExtra("mykey");
        nickname = getIntent().getStringExtra("nickname");
        myUniv = getIntent().getStringExtra("myUniv");
        myimg = getIntent().getStringExtra("myimg");

        //세팅
        imageView = (ImageView)findViewById(R.id.mypage_profile);
        emailText = (TextView)findViewById(R.id.mypage_email);
        nicknameText = (TextView)findViewById(R.id.mypage_name);

        //프로필 버튼
        profileImageButton = (ImageButton) findViewById(R.id.mypage_button);

        //어댑터 버튼
        adapterImageButton = (ImageButton) findViewById(R.id.mypage_list_set);

        //리사이클러 뷰 관련 위젯
        recyclerView = (RecyclerView)findViewById(R.id.mp_rv);

        //파이어베이스 데이터베이스 연동
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        user_Ref = firestore.collection("User").document(mykey);
        product_Ref = firestore.collection("Product");

        //파이어 베이스 리얼 타임 데이터베이스 연동
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();

        setting();
        mypage_1();
        imageView();
        Back();
        imagetrigger();
        profileImageButtonListener();
    }

    void Back(){
        ImageButton back = (ImageButton)findViewById(R.id.mypage_back);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingPage.this, MainActivity.class);
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
                System.exit(0);
            }
        });
    }

    void mypage_1(){
        // 위젯 연결
        selling = (TextView)findViewById(R.id.selling);
        free_providing = (TextView)findViewById(R.id.trans_complete);
        success_deal = (TextView)findViewById(R.id.www);

        selling.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("판매 중 눌림");
                where = "판매";
                productList = new ArrayList<>();
                selling.setBackgroundDrawable(getResources().getDrawable(R.drawable.bt_bg_yellow));
                free_providing.setBackgroundDrawable(getResources().getDrawable(R.drawable.bt_bg_white));
                success_deal.setBackgroundDrawable(getResources().getDrawable(R.drawable.bt_bg_white));

                product_Ref = firestore.collection("Product");
                Query query = product_Ref.whereEqualTo("seller_key", mykey)
                        .whereEqualTo("purpose", "판매")
                        .whereEqualTo("success_time", "000000000000")
                        .orderBy("time", Query.Direction.DESCENDING);

                query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            //DocumentSnapshot document = task.getResult();
                            //System.out.println("ddd:" + document);
                            for(DocumentSnapshot document : task.getResult()){
                                Product product = document.toObject(Product.class);
                                productList.add(product);

                                System.out.println("task :" + document.getId());
                                System.out.println("plist :"+productList);
                            }
                            //상품 추가했으니 어뎁터 갱신
                            //리사이클러뷰 전체 업데이트 : notifyDataSetChanged
                            //myPageAdapter1.notifyDataSetChanged();
                            init_1();
                        }

                    }
                });
            }
        });

        free_providing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("무료 나눔 눌림");
                where = "무료나눔";
                productList = new ArrayList<>();
                selling.setBackgroundDrawable(getResources().getDrawable(R.drawable.bt_bg_white));
                free_providing.setBackgroundDrawable(getResources().getDrawable(R.drawable.bt_bg_yellow));
                success_deal.setBackgroundDrawable(getResources().getDrawable(R.drawable.bt_bg_white));

                product_Ref = firestore.collection("Product");
                Query query = product_Ref.whereEqualTo("seller_key", mykey)
                        .whereEqualTo("purpose", "무료나눔")
                        .whereEqualTo("success_time", "000000000000")
                        .orderBy("time", Query.Direction.DESCENDING);

                query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(DocumentSnapshot document : task.getResult()){
                                Product product = document.toObject(Product.class);
                                productList.add(product);

                                System.out.println("task :" + document.getId());
                                System.out.println("plist :"+productList);
                            }
                            //상품 추가했으니 어뎁터 갱신
                            //리사이클러뷰 전체 업데이트 : notifyDataSetChanged
                            //myPageAdapter1.notifyDataSetChanged();
                            init_1();
                        }
                    }
                });
            }
        });

        //거래 완료
        success_deal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("거래완료 눌림");
                where = "거래완료";
                productList = new ArrayList<>();
                selling.setBackgroundDrawable(getResources().getDrawable(R.drawable.bt_bg_white));
                free_providing.setBackgroundDrawable(getResources().getDrawable(R.drawable.bt_bg_white));
                success_deal.setBackgroundDrawable(getResources().getDrawable(R.drawable.bt_bg_yellow));
                product_Ref = firestore.collection("Product");

                //제외 커리는 저렇게 orderBy를 해줘야 한다.
                Query query = product_Ref.whereEqualTo("seller_key", mykey)
                        .orderBy("success_time", Query.Direction.ASCENDING)
                        .whereNotIn("success_time", Arrays.asList("000000000000","999999999999"))
                        .orderBy("time", Query.Direction.DESCENDING);

                query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            //DocumentSnapshot document = task.getResult();
                            //System.out.println("ddd:" + document);
                            for(DocumentSnapshot document : task.getResult()){
                                Product product = document.toObject(Product.class);
                                productList.add(product);

                                System.out.println("task :" + document.getId());
                                System.out.println("plist :"+productList);
                            }

                            Query next_query = product_Ref.whereEqualTo("purchaser_key", mykey)
                                    .orderBy("success_time", Query.Direction.ASCENDING)
                                    .whereNotIn("success_time", Arrays.asList("000000000000","999999999999"))
                                    .orderBy("time", Query.Direction.DESCENDING);

                            //사용자가 구매자일 경우도 쿼리 날린다.
                            next_query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        //DocumentSnapshot document = task.getResult();
                                        //System.out.println("ddd:" + document);
                                        for (DocumentSnapshot document : task.getResult()) {
                                            Product product = document.toObject(Product.class);
                                            productList.add(product);

                                            System.out.println("task :" + document.getId());
                                            System.out.println("plist :" + productList);
                                        }

                                        productList.sort(new CompareSuccessTime<>());
                                        init_2();

                                    }
                                }
                            });
                        }

                    }
                });
            }
        });
    }

    void init_1(){
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        myPageAdapter1 = new MyPageAdapter1(productList);
        recyclerView.setAdapter(myPageAdapter1);

        //이미지가 눌렸을 경우
        myPageAdapter1.setOnImgClickListener(new MyPageAdapter1.onImgEventListener() {
            @Override
            public void onItemClick(View v, int pos) {

                System.out.println("상품 키:" + productList.get(pos).key);
                String path = "MyPage";
                Intent intent = new Intent(SettingPage.this, DetailPage.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                intent.putExtra("email", email);
                intent.putExtra("mykey", mykey);
                intent.putExtra("nickname", nickname);
                intent.putExtra("myUniv", myUniv);
                intent.putExtra("productkey", productList.get(pos).key);      //리사이클뷰 인덱스 가져옴
                intent.putExtra("wherefrom", path);
                intent.putExtra("myimg", myimg);
                startActivity(intent);
                finish();

            }
        });



        //팝업 메뉴가 눌렸을 경우
        myPageAdapter1.setOnItemClickListener(new MyPageAdapter1.onItemClickEventListener() {
            @Override
            public void onItemClick(View v, int pos) {



                //팝업 메뉴 객체 생성
                PopupMenu popupMenu = new PopupMenu(SettingPage.this, v);
                //xml파일에 메뉴 정의한 것 가져오기
                MenuInflater inflater = popupMenu.getMenuInflater();
                Menu menu = popupMenu.getMenu();
                //실제 메뉴 정의
                inflater.inflate(R.menu.adapter_mypage_menu, menu);

                //메뉴 클릭이벤트
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        switch (item.getItemId()){

                            //거래완료
                            case R.id.success:
                                selected = "";
                                DocumentReference chattingRef = firestore.collection("ChattingRoom").document(productList.get(pos).key);
                                chattingRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if(task.isSuccessful()){        //송수신이 잘 되었을 경우
                                            DocumentSnapshot document = task.getResult();
                                            chattingRoomInfo = document.toObject(ChattingRoomInfo.class);
                                            int count = 0;
                                            ArrayList<String> tradePeople = new ArrayList<>();
                                            for (count = 0; count < chattingRoomInfo.out_customer_index.size(); count++){

                                                if (chattingRoomInfo.out_customer_index.get(count) == 0 && !(mykey.equals(chattingRoomInfo.customerList.get(count)))){
                                                    //나가지 않은 사람들 중에서 자기 자신이 아닌 사람들
                                                    tradePeople.add(chattingRoomInfo.customer_nicknames.get(count));
                                                }
                                            }//for문 끝
                                            if(tradePeople.size() == 0){        //거래할 대상이 없다.
                                                Toast.makeText(SettingPage.this, "거래할 상대가 없습니다.", Toast.LENGTH_SHORT).show();
                                            }else{
                                                //ArrayList를 String[]으로 변환하여야 한다.
                                                String[] array = new String[tradePeople.size()];
                                                ArrayAdapter<String> adapter = new ArrayAdapter<String>(SettingPage.this, android.R.layout.select_dialog_singlechoice);

                                                for(int i = 0; i < tradePeople.size(); i++){
                                                    array[i] = tradePeople.get(i);
                                                    adapter.add(tradePeople.get(i));
                                                }
                                                Log.d("alert", "dd" + adapter.getItem(0));
                                                //거래할 대상이 있는 경우
                                                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(SettingPage.this);
                                                alertBuilder.setTitle("구매자를 선택하여주세요.");
                                                //alert는 리스트의 경우 메시지를 사용하면 안된다.
                                                //alertBuilder.setMessage("구매자를 선택하여주세요.");


                                                Handler mHandler = new Handler(Looper.getMainLooper());  //Thread 안에 Thread가 사용되기때문에 handler 사용
                                                mHandler.postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {

                                                        alertBuilder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {

                                                                selected = adapter.getItem(i);
                                                                Log.d("alert", "그냥 넘어감");
                                                                AlertDialog.Builder alertBuilder2 = new AlertDialog.Builder(SettingPage.this);
                                                                alertBuilder2.setTitle("안내");
                                                                alertBuilder2.setMessage(selected+ "님과 " + "거래하시겠습니까?");

                                                                alertBuilder2.setPositiveButton("취소",new DialogInterface.OnClickListener(){         //오른쪽버튼
                                                                    public void onClick(DialogInterface dialog,int which){
                                                                        //삭제하지 않음으로 그냥 둔다.
                                                                    }
                                                                });

                                                                alertBuilder2.setNegativeButton("선택", new DialogInterface.OnClickListener() {          //왼쪽버튼
                                                                    @Override
                                                                    public void onClick(DialogInterface dialog, int which) { //사진 삭제

                                                                        if(selected.equals("")){
                                                                            Toast.makeText(SettingPage.this, selected + "선택하여 주세요.", Toast.LENGTH_SHORT).show();
                                                                        }else{

                                                                            successProduct(productList.get(pos), selected);

                                                                        }
                                                                    }
                                                                });

                                                                alertBuilder2.setCancelable(false);  //외부 창 클릭시 꺼짐 막기

                                                                AlertDialog alertDialog2 = alertBuilder2.create();
                                                                alertDialog2.show();

                                                            }
                                                        });


                                                        alertBuilder.setPositiveButton("취소",new DialogInterface.OnClickListener(){         //오른쪽버튼
                                                            public void onClick(DialogInterface dialog,int which){
                                                                //삭제하지 않음으로 그냥 둔다.
                                                            }
                                                        });
                                                        /*
                                                        alertBuilder.setNegativeButton("선택", new DialogInterface.OnClickListener() {          //왼쪽버튼
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) { //사진 삭제

                                                                if(selected.equals("")){
                                                                    Toast.makeText(SettingPage.this, selected + "선택하여 주세요.", Toast.LENGTH_SHORT).show();
                                                                }else{

                                                                    successProduct(productList.get(pos), selected);

                                                                }
                                                            }
                                                        });
                                                        */

                                                        alertBuilder.setCancelable(false);  //외부 창 클릭시 꺼짐 막기
                                                        AlertDialog alertDialog = alertBuilder.create();
                                                        alertDialog.show();

                                                    }
                                                }, 1000);

                                            }
                                        }
                                    }
                                });
                                break;

                            //삭제
                            case R.id.delete:
                                //신경써서 삭제해야할 것 2개
                                //product 정보, 채팅에서 채팅 더 이상 못하게 막아 놓기
                                //시스템에서 버튼 클릭하기 .performClick()  <-이걸로 화면 갱신하기
                                //채팅방정보 -> 채팅 -> 물품등록(999999999999) ->  버튼 클릭 이벤트 발생
                                AlertDialog.Builder dlg = new AlertDialog.Builder(SettingPage.this, R.style.AlertDialogTheme);
                                Handler mHandler = new Handler(Looper.getMainLooper());

                                mHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        dlg.setTitle("물품을 삭제하시겠습니까?");
                                        dlg.setPositiveButton("취소",new DialogInterface.OnClickListener(){
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                            }
                                        }).setNegativeButton("확인", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) { // 채팅방 나감
                                                deleteProduct(productList.get(pos));
                                            }
                                        });
                                        dlg.show();
                                    }
                                }, 0);

                                break;
                        }
                        return false;
                    }
                });
                popupMenu.show();

            }

        });



    }

    void init_2(){

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        myPageAdapter2 = new MyPageAdapter2(productList);
        recyclerView.setAdapter(myPageAdapter2);

        //이미지가 눌렸을 경우
        myPageAdapter2.setOnImgClickListener(new MyPageAdapter2.onImgEventListener() {
            @Override
            public void onItemClick(View v, int pos) {

                System.out.println("상품 키:" + productList.get(pos).key);
                String path = "MyPage";
                Intent intent = new Intent(SettingPage.this, DetailPage.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                intent.putExtra("email", email);
                intent.putExtra("mykey", mykey);
                intent.putExtra("nickname", nickname);
                intent.putExtra("myUniv", myUniv);
                intent.putExtra("productkey", productList.get(pos).key);      //리사이클뷰 인덱스 가져옴
                intent.putExtra("wherefrom", path);
                intent.putExtra("myimg", myimg);
                startActivity(intent);
                finish();

            }
        });

    }


    void setting(){

        emailText.setText(email);
        nicknameText.setText(nickname);

        where = "판매";

        product_Ref = firestore.collection("Product");
        Query query = product_Ref.whereEqualTo("seller_key", mykey)
                .whereEqualTo("purpose", "판매")
                .whereEqualTo("success_time", "000000000000")
                .orderBy("time", Query.Direction.DESCENDING);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    //DocumentSnapshot document = task.getResult();
                    //System.out.println("ddd:" + document);
                    for(DocumentSnapshot document : task.getResult()){
                        Product product = document.toObject(Product.class);
                        productList.add(product);

                        System.out.println("task :" + document.getId());
                        System.out.println("plist :"+productList);
                    }
                    //상품 추가했으니 어뎁터 갱신
                    //리사이클러뷰 전체 업데이트 : notifyDataSetChanged
                    //myPageAdapter1.notifyDataSetChanged();
                    init_1();
                }

            }
        });

    }

    //ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ 이미지 처리 ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ

    void imageView(){         //프로필 사진 변경 및 저장

        imageView.setOnClickListener(new View.OnClickListener() {      //프로필 사진이 눌렸을 경우
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                requestCode = 1;
                startActivityForResult(intent, 1);      //코드 번호로 아래에서 동작한다.

            }
        });

    }

    //갤러리에서 이미지를 가져온다.
    @Override
    protected void onActivityResult(int requstCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Uri uri = data.getData();
            String imgkey = mykey;

            StorageReference deserRef = storageRef.child("profiles").
                    child(imgkey);    //이미지 조회
            UploadTask uploadTask = (UploadTask) deserRef.putFile(uri); //이미지 서버에 uri로 저장
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    user.img = imgkey;
                    user_Ref.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            InputStream inputStream = null;
                            try{
                                inputStream = getContentResolver().openInputStream(uri);
                            }catch (Exception e){

                            }
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            imageView.setImageBitmap(bitmap);
                        }
                    });
                }
            });
        }

    }

    void imagetrigger(){

        user_Ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()) {
                        user = documentSnapshot.toObject(User.class);
                        System.out.println(user.img);
                        if(user.img != null){
                            StorageReference deserRef = storageRef.child("profiles").
                                    child(user.img);    //이미지 조회
                            deserRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    try{
                                        Glide.with(SettingPage.this)
                                                .load(uri)
                                                .override(300, 300)
                                                .thumbnail(0.1f)
                                                .into(imageView);
                                    }catch (Exception e){

                                    }
                                }
                            });
                        }else{//이미지 없음
                            //기본 사진
                        }

                    }
                }
            }
        });
    }

    void profileImageButtonListener(){
        profileImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //팝업 메뉴 객체 생성
                PopupMenu popupMenu = new PopupMenu(SettingPage.this, v);

                //xml파일에 메뉴 정의한 것 가져오기
                MenuInflater inflater = popupMenu.getMenuInflater();
                Menu menu = popupMenu.getMenu();

                //실제 메뉴 정의
                inflater.inflate(R.menu.profile_mypage_menu, menu);


                //메뉴 클릭이벤트
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        switch (item.getItemId()){
                            //수정
                            case R.id.revise:
                                break;

                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });
    }

    //ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ   이미지 끝 ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
    @Override
    public void onBackPressed(){
        //뒤로가기 막기
    }

    void deleteProduct(Product product){
        //시스템에서 버튼 클릭하기 .performClick()  <-이걸로 화면 갱신하기
        //채팅방정보 -> 채팅 -> 물품등록(999999999999) ->  버튼 클릭 이벤트 발생
        DocumentReference chattingRef = firestore.collection("ChattingRoom").document(product.key);
        chattingRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    chattingRoomInfo = document.toObject(ChattingRoomInfo.class);

                    //가져온 정보로 이제 저장
                    String nowTime = Time.nowNewTime();
                    String out_chat = "System" + "/%%/" + "상품이 삭제되었습니다."
                            + "/%%/" + nowTime + "/%%/";
                    chattingRoomInfo.last_time = nowTime;
                    chattingRoomInfo.last_text = out_chat;
                    //1줄 추가
                    chattingRoomInfo.last_index = chattingRoomInfo.last_index + 1;
                    chattingRoomInfo.customerList.add("System/*!%@#!*/delete/1team");       //삭제 시그널 보냄
                    chattingRoomInfo.customer_nicknames.add("System/*!%@#!*/delete/1team");
                    chattingRoomInfo.last_SEE.add(chattingRoomInfo.last_index + 1);
                    chattingRoomInfo.out_customer_index.add(chattingRoomInfo.last_index + 1);

                    firestore.collection("ChattingRoom").document(product.key)
                            .set(chattingRoomInfo)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    //채팅방 저장을 성공하였으니 이제 실시간 DB도 추가
                                    myRef.child("Chatting").child(product.key).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                                            chatInfo = snapshot.getValue(ChatInfo.class);
                                            chatInfo.chatList.add(out_chat);
                                            myRef.child("Chatting").child(product.key).setValue(chatInfo)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            //실시간도 저장 성공 이제 마지막으로 상품에 등록
                                                            product.success_time = "999999999999";
                                                            firestore.collection("Product").document(product.key)
                                                                    .set(product)
                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void unused) {
                                                                            Toast.makeText(SettingPage.this, "삭제 완료", Toast.LENGTH_SHORT).show();
                                                                            if(where.equals("판매")){
                                                                                selling.performClick();
                                                                            }else if(where.equals("무료나눔")){
                                                                                free_providing.performClick();
                                                                            }else{
                                                                                success_deal.performClick();
                                                                            }
                                                                        }
                                                                    });
                                                        }
                                                    });
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }
                            });
                }
            }
        });



    }


    void successProduct(Product product, String nicks){
        //시스템에서 버튼 클릭하기 .performClick()  <-이걸로 화면 갱신하기
        //채팅방정보 -> 채팅 -> 물품등록(999999999999) ->  버튼 클릭 이벤트 발생
        DocumentReference chattingRef = firestore.collection("ChattingRoom").document(product.key);
        chattingRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    chattingRoomInfo = document.toObject(ChattingRoomInfo.class);

                    //가져온 정보로 이제 저장
                    String nowTime = Time.nowNewTime();
                    String out_chat = "System" + "/%%/" + "상품의 거래가 완료되었습니다."
                            + "/%%/" + nowTime + "/%%/";
                    chattingRoomInfo.last_time = nowTime;
                    chattingRoomInfo.last_text = out_chat;
                    //1줄 추가
                    //System/*!%@#!*/success/1team
                    chattingRoomInfo.last_index = chattingRoomInfo.last_index + 1;
                    chattingRoomInfo.customerList.add("System/*!%@#!*/success/1team");       //삭제 시그널 보냄
                    chattingRoomInfo.customer_nicknames.add("System/*!%@#!*/success/1team");
                    chattingRoomInfo.last_SEE.add(chattingRoomInfo.last_index + 1);
                    chattingRoomInfo.out_customer_index.add(chattingRoomInfo.last_index + 1);
                    firestore.collection("ChattingRoom").document(product.key)
                            .set(chattingRoomInfo)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    //채팅방 저장을 성공하였으니 이제 실시간 DB도 추가
                                    myRef.child("Chatting").child(product.key).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                                            chatInfo = snapshot.getValue(ChatInfo.class);
                                            chatInfo.chatList.add(out_chat);
                                            myRef.child("Chatting").child(product.key).setValue(chatInfo)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            //이제 상품 저장
                                                            product.success_time = nowTime;
                                                            product.purchaser = nicks;
                                                            String purchaser_key = "";
                                                            for(int i = 0; i < chattingRoomInfo.customer_nicknames.size(); i++){
                                                                if (nicks.equals(chattingRoomInfo.customer_nicknames.get(i))){
                                                                    purchaser_key = chattingRoomInfo.customerList.get(i);
                                                                }
                                                            }
                                                            product.purchaser_key = purchaser_key;
                                                            firestore.collection("Product").document(product.key)
                                                                    .set(product)
                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void unused) {
                                                                            Toast.makeText(SettingPage.this, "거래 완료", Toast.LENGTH_SHORT).show();
                                                                            if(where.equals("판매")){
                                                                                selling.performClick();
                                                                            }else if(where.equals("무료나눔")){
                                                                                free_providing.performClick();
                                                                            }else{
                                                                                success_deal.performClick();
                                                                            }
                                                                        }
                                                                    });
                                                        }
                                                    });
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }
                            });
                }
            }
        });
    }

}