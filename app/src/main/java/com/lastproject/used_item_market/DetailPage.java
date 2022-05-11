package com.lastproject.used_item_market;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StreamDownloadTask;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class DetailPage extends AppCompatActivity {

    //기본 나의 정보
    String email = "";
    String mykey = "";
    String nickname = "";
    String myUniv = "";
    String product_key = "";
    String wherefrom = "";

    //파이어베이스
    private FirebaseFirestore firestore;
    private DocumentReference productRef;
    private FirebaseStorage storage;            //이미지 저장소
    private StorageReference storageRef;        //정확한 위치에 파일 저장

    //위젯모음
    ImageButton back_btn;
    TextView title_view;
    TextView purpose_view;
    TextView category_view;
    TextView cost_view;
    TextView text_view;
    TextView map_btn;
    TextView chat_btn;

    //이미지 관련 위젯 및 어뎁터
    RecyclerView recyclerView;
    RecycleDetailAdapter recycleDetailAdapter;
    ArrayList<Bitmap> images = new ArrayList<Bitmap>();
    ImageView mainImg;

    private Product product;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_page);

        //기본세팅
        email = getIntent().getStringExtra("email");
        mykey = getIntent().getStringExtra("mykey");
        nickname = getIntent().getStringExtra("nickname");
        myUniv = getIntent().getStringExtra("myUniv");
        product_key = getIntent().getStringExtra("productkey");
        wherefrom = getIntent().getStringExtra("wherefrom");        //이것으로 다시 돌아간다.

        //위젯 생성
        back_btn = (ImageButton)findViewById(R.id.back_list);
        title_view = (TextView)findViewById(R.id.product_name);
        purpose_view = (TextView)findViewById(R.id.purpose);
        category_view = (TextView)findViewById(R.id.category_name);
        cost_view = (TextView)findViewById(R.id.money);
        text_view = (TextView)findViewById(R.id.detail_text);
        chat_btn = (TextView)findViewById(R.id.go_chatting);
        //map_btn <- 맵은 아직 연결안함

        //이미지 관련 위젯
        recyclerView = (RecyclerView)findViewById(R.id.product_imges);
        mainImg = (ImageView)findViewById(R.id.detail_page_main_img1);


        //서버
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        productRef = firestore.collection("Product").document(product_key);
        productRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {    //문서를 가져오는데 성공

                        product = document.toObject(Product.class);     //상품을 가져온다.
                        if(product.pictures != null){   //이미지가 있을 경우 세팅

                            for(int i = 0; i < product.pictures.size(); i++){       //이미지만큼 가져온다.

                                StorageReference storageReference = storageRef.child("images")
                                        .child(product.pictures.get(i));

                                storageReference.getBytes(1024*1024)
                                        .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                            @Override
                                            public void onSuccess(byte[] bytes) {

                                                Bitmap bitmap = BitmapFactory
                                                        .decodeByteArray(bytes,0, bytes.length);
                                                images.add(bitmap);
                                                System.out.println("비트맵 사이즈" + images.size());
                                                mainImg.setImageBitmap(bitmap);
                                                mainImg.setClipToOutline(true);              //모양에 맞게 사진 자르기
                                                init();
                                            }
                                        });

                            }//for문 끝

                        }//if문끝

                        //나머지 상품정보 세팅


                    } else {        //문서를 가져오는데 실패
                        Toast.makeText(DetailPage.this, "상품 없음", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


        back();
        detailSelect();
        chatting();

    }

    void back(){    //뒤로가기 버튼

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                switch (wherefrom){         //어디로 다시 돌아가야할지 정해야한다.

                    case "SellPage":
                        Intent intent = new Intent(DetailPage.this, SellPage.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("email", email);
                        intent.putExtra("mykey", mykey);
                        intent.putExtra("nickname", nickname);
                        intent.putExtra("myUniv", myUniv);
                        startActivity(intent);
                        System.exit(0);
                        break;
                    case "SharePage":
                        Intent intent2 = new Intent(DetailPage.this, SharePage.class);
                        intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent2.putExtra("email", email);
                        intent2.putExtra("mykey", mykey);
                        intent2.putExtra("nickname", nickname);
                        intent2.putExtra("myUniv", myUniv);
                        startActivity(intent2);
                        System.exit(0);
                        break;
                    case "AllPage":
                        Intent intent3 = new Intent(DetailPage.this, AllPage.class);
                        intent3.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent3.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent3.putExtra("email", email);
                        intent3.putExtra("mykey", mykey);
                        intent3.putExtra("nickname", nickname);
                        intent3.putExtra("myUniv", myUniv);
                        startActivity(intent3);
                        System.exit(0);
                        break;

                }//switch문 끝
            }
        });

    }

    void detailSelect(){    //UI상단 맨 오른쪽 옵션 선택버튼



    }

    void chatting(){        //채팅 방 이동

        chat_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                //오픈 채팅으로할지 개인 채팅으로 할지 정하고 생각하기


            }
        });

    }

    void init(){

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);             //이렇게 하면 수평으로 생성
        recyclerView.setLayoutManager(linearLayoutManager);
        recycleDetailAdapter = new RecycleDetailAdapter(images);
        recycleDetailAdapter.setOnItemClickListener(new RecyclePostAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int pos) {

                //사진 눌리면 메인 사진으로 변환

            }
        });
        recyclerView.setAdapter(recycleDetailAdapter);
        recyclerView.addItemDecoration(new RecyclerDecoration(5));       //간격을 추가한다.



    }

}