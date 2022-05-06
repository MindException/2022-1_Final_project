package com.lastproject.used_item_market;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class DetailPage extends AppCompatActivity {

    //기본 나의 정보
    String email = "";
    String mykey = "";
    String nickname = "";
    String myUniv = "";
    String product_key = "";

    //파이어베이스
    FirebaseFirestore firestore;
    CollectionReference userDocument;
    CollectionReference productRef;


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

        System.out.println(product_key);


    }
}