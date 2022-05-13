package com.lastproject.used_item_market;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import java.util.List;

public class SettingPage extends AppCompatActivity {

    //기본 나의 정보
    String email = "";
    String mykey = "";
    String nickname = "";
    String myUniv = "";

    //위젯 모음
    TextView selling;
    TextView trans_complete;
    TextView www;

    //새DB
    private FirebaseFirestore firestore;        //DB
    private FirebaseStorage storage;            //이미지 저장소
    private StorageReference storageRef;        //정확한 위치에 파일 저장
    CollectionReference product_Ref;
    private DocumentReference user_Ref;
    private User user;
    private CollectionReference UserRef;
    DocumentReference documentReference;

    //이미지 관련 위젯 및 어뎁터
    RecyclerView recyclerView;
    MyPageAdapter1 myPageAdapter1;
    //MyPageAdapter2 myPageAdapter2;
    //MyPageAdapter3 myPageAdapter3;

    // 상품 관련
    List<User> userList = new ArrayList<User>();
    List<Product> productList = new ArrayList<Product>();  //여기에 모든 상품들이 들어간다.
    ArrayList<String> productKeyList = new ArrayList<String>();

    ImageView imageView;
    private int requestCode;

    ImageButton adapterImageButton;
    ImageButton profileImageButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_page);

        //기본세팅
        email = getIntent().getStringExtra("email");
        mykey = getIntent().getStringExtra("mykey");
        nickname = getIntent().getStringExtra("nickname");
        myUniv = getIntent().getStringExtra("myUniv");

        //프로필 사진
        imageView = findViewById(R.id.mypage_profile);

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
        UserRef = firestore.collection("User");
        user_Ref = firestore.collection("User").document(mykey);
        product_Ref = firestore.collection("Product");
        documentReference = UserRef.document(mykey);

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
                intent.putExtra("email", email);
                intent.putExtra("mykey", mykey);
                intent.putExtra("nickname", nickname);
                intent.putExtra("myUniv", myUniv);
                startActivity(intent);
                System.exit(0);
            }
        });
    }

    void mypage_1(){
        // 위젯 연결
        selling = (TextView)findViewById(R.id.selling);
        trans_complete = (TextView)findViewById(R.id.trans_complete);
        www = (TextView)findViewById(R.id.www);


        selling.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("판매 중 눌림");
                user_Ref = firestore.collection("User").document();
                product_Ref = firestore.collection("Product");
                Query query = product_Ref.whereEqualTo("seller_key", mykey)
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
                                productKeyList.add(document.getId());

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

        trans_complete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("거래 완료 눌림");

                user_Ref = firestore.collection("User").document();
                product_Ref = firestore.collection("Product");
                Query query = product_Ref.whereNotEqualTo("success_time", 000000000000)
                        //.whereNotEqualTo("success_time", 999999999999)
                        .orderBy("time", Query.Direction.DESCENDING);

                query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(DocumentSnapshot document : task.getResult()){
                                Product product = document.toObject(Product.class);
                                productList.add(product);
                                productKeyList.add(document.getId());

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

        www.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("*** 눌림");
                // 어댑터 연결
            }
        });
    }

    void init_1(){
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        myPageAdapter1 = new MyPageAdapter1(productList);
        recyclerView.setAdapter(myPageAdapter1);

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
                            //수정
                            case R.id.revise:
                                break;

                            //삭제
                            case R.id.delete:
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
        //myPageAdapter2 = new MyPageAdapter2(productList);
        //recyclerView.setAdapter(myPageAdapter2);
    }

    void init_3(){
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        //myPageAdapter3 = new MyPageAdapter1(productList);
        //recyclerView.setAdapter(myPageAdapter3);
    }


    void setting(){


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
            String nowTime = mykey + Time.nowNewTime();

            StorageReference deserRef = storageRef.child("images").
                    child(nowTime);    //이미지 조회
            UploadTask uploadTask = (UploadTask) deserRef.putFile(uri); //이미지 서버에 uri로 저장
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    user.img = nowTime;
                    UserRef.document(mykey).set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
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


        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()) {
                        user = documentSnapshot.toObject(User.class);
                        System.out.println(user.img);
                        if(user.img != null){
                            StorageReference deserRef = storageRef.child("images").
                                    child(user.img);    //이미지 조회
                            deserRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    try{
                                        Glide.with(SettingPage.this).load(uri).into(imageView);
                                    }catch (Exception e){

                                    }
                                }
                            });
                        }else{//이미지 없음
                            //나중에 기본 사진 추가
                        }

                    }
                }
            }
        });
    }

    //ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ   이미지 끝 ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ

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


}