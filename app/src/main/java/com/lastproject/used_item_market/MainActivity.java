package com.lastproject.used_item_market;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    //기본 나의 정보
    String email = "";
    String mykey = "";
    String nickname = "";
    String myUniv = "";
    String myimg = "";

    //위젯 모음
    TextView new_title;
    TextView one;
    TextView two;
    TextView three;
    TextView five;
    Spinner univspinner;

    //새DB
    private FirebaseFirestore firestore;        //DB
    private CollectionReference UserRef;
    DocumentReference documentReference;
    CollectionReference product_Ref;
    CollectionReference univRef;

    //이미지DB
    private FirebaseStorage storage;            //이미지 저장소
    private StorageReference storageRef;        //정확한 위치에 파일 저장

    //이미지 관련 위젯 및 어뎁터
    RecyclerView recyclerView;
    RecyclerView recyclerView2;
    NewProductAdapter newProductAdapter;
    CostListAdapter costListAdapter;
    ArrayList<Bitmap> images = new ArrayList<Bitmap>();
    ImageView mainImg;

    // 상품 관련
    List<Product> productList = new ArrayList<Product>();  //여기에 모든 상품들이 들어간다.
    ArrayList<String> productKeyList = new ArrayList<String>();
    private int limit = 7;         //요청 상품 수

    //대학 저장
    ArrayList<String> univNames = new ArrayList<>();
    ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lobby);

        //기본세팅
        email = getIntent().getStringExtra("email");
        mykey = getIntent().getStringExtra("mykey");
        nickname = getIntent().getStringExtra("nickname");
        myUniv = getIntent().getStringExtra("myUniv");
        myimg = getIntent().getStringExtra("myimg");

        //DB 연동
        firestore = FirebaseFirestore.getInstance();

        //리사이클러 뷰 관련 위젯
        recyclerView = (RecyclerView)findViewById(R.id.lobby_recyclerHorizon);
        recyclerView2 = (RecyclerView)findViewById(R.id.cost_recycler);

        //먼저 대학으로 설정한다.
        univspinner = (Spinner)findViewById(R.id.univ_spinner);
        univRef = firestore.collection("University");
        univRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){        //수송신 성공
                    if(task.getResult().size() > 0){        //대학이 넘어온다.

                        for(DocumentSnapshot document : task.getResult()){
                            University university = document.toObject(University.class);
                            univNames.add(university.university);
                        }
                        arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item,
                                univNames);
                        univspinner.setAdapter(arrayAdapter);

                        //원래 자신의 대학교로 초기세팅을 해준다.
                        univspinner.setSelection(getIndex(univspinner, myUniv));

                        //스피너 선택될 경우 대학을 옮긴다.
                        univspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                                myUniv = univspinner.getSelectedItem().toString();          //대학 선택
                                product();
                                costlist();
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> adapterView) {

                            }
                        });

                    }
                }
            }
        });
        Sell();
        Share();
        All();
        chat();
        Post();
        Setting();
    }

    void Sell(){ //판매 버튼 클릭 시 화면 이동
        ImageButton sell = (ImageButton)findViewById(R.id.Sell);
        Glide.with(this).load(R.raw.ic_lobby_maket).into(sell);
        sell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SellPage.class);
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
            }
        });
    }

    void Share(){ //무료나눔 버튼 클릭 시 화면 이동
        ImageButton share = (ImageButton)findViewById(R.id.share);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SharePage.class);
                startActivity(intent);
            }
        });
    }
    void All(){ //모두보기 버튼 클릭 시 화면 이동
        ImageButton all = (ImageButton)findViewById(R.id.Show);
        Glide.with(this).load(R.raw.ic_lobby_all).into(all);
        all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AllPage.class);
                startActivity(intent);
            }
        });
    }
    void Post(){ //작성 버튼 클릭 시 화면 이동
        ImageButton post = (ImageButton)findViewById(R.id.addbtn);

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, PostPage.class);
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
            }
        });
    }

    void chat(){    //채팅 목록 화면으로 이동한다.
        ImageButton chat = (ImageButton)findViewById(R.id.chat);
        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ChattingListPage.class);
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
            }
        });

    }

    void Setting(){ //설정 버튼 클릭 시 화면 이동
        ImageButton set = (ImageButton)findViewById(R.id.setbtn);

        set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SettingPage.class);
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
            }
        });
    }

    @Override
    public void onBackPressed(){
        //뒤로가기 막기
    }

    void product(){
        new_title = (TextView)findViewById(R.id.textImg);

        product_Ref = firestore.collection("Product");
        Query query = product_Ref.whereEqualTo("university", myUniv)
                .whereEqualTo("success_time", "000000000000")
                .orderBy("time", Query.Direction.DESCENDING)
                .limit(limit);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    System.out.println("qqq : " + query);
                    for(DocumentSnapshot document : task.getResult()){
                        Product product = document.toObject(Product.class);
                        productList.add(product);
                        productKeyList.add(document.getId());

                        System.out.println("www :" + document.getId()); // limit값인 7 개 잘 가져옴. 순서도 맞음
                    }
                    init();
                }
            }
        });
    }

    void init(){
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        newProductAdapter = new NewProductAdapter(productList);
        recyclerView.setAdapter(newProductAdapter);

        newProductAdapter.setOnItemClickListener(new NewProductAdapter.onItemClickEventListener() {
            @Override
            public void onItemClick(View v, int pos) {
                System.out.println(pos +"번째 아이템 눌림");

                System.out.println("상품 키:" + productKeyList.get(pos));
                String path = "MainActivity";
                Intent intent = new Intent(MainActivity.this, DetailPage.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                intent.putExtra("email", email);
                intent.putExtra("mykey", mykey);
                intent.putExtra("nickname", nickname);
                intent.putExtra("myUniv", myUniv);
                intent.putExtra("productkey", productKeyList.get(pos));      //리사이클뷰 인덱스 가져옴
                intent.putExtra("wherefrom", path);
                intent.putExtra("myimg", myimg);
                startActivity(intent);
                finish();
            }
        });
    }

    void costlist(){
        one = (TextView)findViewById(R.id.oneover);
        two = (TextView)findViewById(R.id.twoover);
        three = (TextView)findViewById(R.id.threeover);
        five = (TextView)findViewById(R.id.fiveover);

        one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                productList = new ArrayList<>();
                productKeyList = new ArrayList<>();

                one.setBackgroundDrawable(getResources().getDrawable(R.drawable.mypage_chack_view_round));
                two.setBackgroundDrawable(getResources().getDrawable(R.drawable.mypage_nochack_view_round));
                three.setBackgroundDrawable(getResources().getDrawable(R.drawable.mypage_nochack_view_round));
                five.setBackgroundDrawable(getResources().getDrawable(R.drawable.mypage_nochack_view_round));

                product_Ref = firestore.collection("Product");
                Query query = product_Ref.whereEqualTo("university", myUniv)  // 대학 물품이므로 대학이 같아야함
                        .orderBy("cost", Query.Direction.ASCENDING)
                        .whereGreaterThanOrEqualTo("cost", 10000) // 1만원대 이므로 1만원이상이고
                        .whereLessThan("cost", 20000)
                        .limit(limit); // 2만원보다 작아야한다.
                //.orderBy("time", Query.Direction.DESCENDING); // 최신순으로 정렬

                query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            System.out.println("1만원대");
                            if (task.getResult().size() <= 0){          //물건이 없는 경우
                                Toast.makeText(MainActivity.this, "1만원 대 상품 없음", Toast.LENGTH_SHORT).show();
                            }else {
                                for (DocumentSnapshot document : task.getResult()) {
                                    Product product = document.toObject(Product.class);
                                    productList.add(product);
                                    productKeyList.add(document.getId());

                                    System.out.println("Product 키값 :" + document.getId());
                                    System.out.println("plist :" + productList);
                                }
                            }
                            productList.sort(new CompareSuccessTime<Product>());
                            init_2();
                        }
                    }
                });
            }
        });

        two.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                productList = new ArrayList<>();
                productKeyList = new ArrayList<>();

                one.setBackgroundDrawable(getResources().getDrawable(R.drawable.mypage_nochack_view_round));
                two.setBackgroundDrawable(getResources().getDrawable(R.drawable.mypage_chack_view_round));
                three.setBackgroundDrawable(getResources().getDrawable(R.drawable.mypage_nochack_view_round));
                five.setBackgroundDrawable(getResources().getDrawable(R.drawable.mypage_nochack_view_round));

                product_Ref = firestore.collection("Product");
                Query query = product_Ref.whereEqualTo("university", myUniv)  // 대학 물품이므로 대학이 같아야함
                        .orderBy("cost", Query.Direction.ASCENDING)
                        .whereGreaterThanOrEqualTo("cost", 20000) // 2만원대 이므로 2만원이상이고
                        .whereLessThan("cost", 30000)
                        .limit(limit); // 3만원보다 작아야한다.
                //.orderBy("time", Query.Direction.DESCENDING); // 최신순으로 정렬

                query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            System.out.println("2만원대");
                            if (task.getResult().size() <= 0){          //물건이 없는 경우
                                Toast.makeText(MainActivity.this, "2만원 대 상품 없음", Toast.LENGTH_SHORT).show();
                            }else {
                                for (DocumentSnapshot document : task.getResult()) {
                                    Product product = document.toObject(Product.class);
                                    productList.add(product);
                                    productKeyList.add(document.getId());

                                    System.out.println("Product 키값 :" + document.getId());
                                    System.out.println("plist :" + productList);
                                }
                            }
                            productList.sort(new CompareSuccessTime<Product>());
                            init_2();
                        }
                    }
                });
            }
        });

        three.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                productList = new ArrayList<>();
                productKeyList = new ArrayList<>();

                one.setBackgroundDrawable(getResources().getDrawable(R.drawable.mypage_nochack_view_round));
                two.setBackgroundDrawable(getResources().getDrawable(R.drawable.mypage_nochack_view_round));
                three.setBackgroundDrawable(getResources().getDrawable(R.drawable.mypage_chack_view_round));
                five.setBackgroundDrawable(getResources().getDrawable(R.drawable.mypage_nochack_view_round));

                product_Ref = firestore.collection("Product");
                Query query = product_Ref.whereEqualTo("university", myUniv)  // 대학 물품이므로 대학이 같아야함
                        .orderBy("cost", Query.Direction.ASCENDING)
                        .whereGreaterThanOrEqualTo("cost", 30000) // 3만원대 이므로 3만원이상이고
                        .whereLessThan("cost", 40000)
                        .limit(limit); // 4만원보다 작아야한다.
                //.orderBy("time", Query.Direction.DESCENDING); // 최신순으로 정렬

                query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            System.out.println("3만원대");
                            if (task.getResult().size() <= 0){          //물건이 없는 경우
                                Toast.makeText(MainActivity.this, "3만원 대 상품 없음", Toast.LENGTH_SHORT).show();
                            }else {
                                for (DocumentSnapshot document : task.getResult()) {
                                    Product product = document.toObject(Product.class);
                                    productList.add(product);
                                    productKeyList.add(document.getId());

                                    System.out.println("Product 키값 :" + document.getId());
                                    System.out.println("plist :" + productList);
                                }
                            }
                            productList.sort(new CompareSuccessTime<Product>());
                            init_2();
                        }
                    }
                });
            }
        });

        five.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                productList = new ArrayList<>();
                productKeyList = new ArrayList<>();

                one.setBackgroundDrawable(getResources().getDrawable(R.drawable.mypage_nochack_view_round));
                two.setBackgroundDrawable(getResources().getDrawable(R.drawable.mypage_nochack_view_round));
                three.setBackgroundDrawable(getResources().getDrawable(R.drawable.mypage_nochack_view_round));
                five.setBackgroundDrawable(getResources().getDrawable(R.drawable.mypage_chack_view_round));

                product_Ref = firestore.collection("Product");
                Query query = product_Ref.whereEqualTo("university", myUniv)  // 대학 물품이므로 대학이 같아야함
                        .orderBy("cost", Query.Direction.ASCENDING)
                        .whereGreaterThanOrEqualTo("cost", 50000)// 5만원이상
                        .limit(limit);
                //.orderBy("time", Query.Direction.DESCENDING); // 최신순으로 정렬

                query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            System.out.println("5만원이상");
                            if (task.getResult().size() <= 0){          //물건이 없는 경우
                                Toast.makeText(MainActivity.this, "5만원 이상 상품 없음", Toast.LENGTH_SHORT).show();
                            }else {
                                for (DocumentSnapshot document : task.getResult()) {
                                    Product product = document.toObject(Product.class);
                                    productList.add(product);
                                    productKeyList.add(document.getId());

                                    System.out.println("Product 키값 :" + document.getId());
                                    System.out.println("plist :" + productList);
                                }
                            }
                            productList.sort(new CompareSuccessTime<Product>());
                            init_2();
                        }
                    }
                });
            }
        });
    }

    void init_2(){
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView2.setLayoutManager(linearLayoutManager);
        costListAdapter = new CostListAdapter(productList);
        recyclerView2.setAdapter(costListAdapter);

        costListAdapter.setOnItemClickListener(new CostListAdapter.onItemClickEventListener() {
            @Override
            public void onItemClick(View v, int pos) {
                System.out.println(pos +"번째 아이템 눌림");

                System.out.println("상품 키:" + productKeyList.get(pos));
                String path = "MainActivity";
                Intent intent = new Intent(MainActivity.this, DetailPage.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                intent.putExtra("email", email);
                intent.putExtra("mykey", mykey);
                intent.putExtra("nickname", nickname);
                intent.putExtra("myUniv", myUniv);
                intent.putExtra("productkey", productKeyList.get(pos));      //리사이클뷰 인덱스 가져옴
                intent.putExtra("wherefrom", path);
                intent.putExtra("myimg", myimg);
                startActivity(intent);
                finish();
            }
        });
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

}