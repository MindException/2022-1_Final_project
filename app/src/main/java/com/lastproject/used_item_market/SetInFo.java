package com.lastproject.used_item_market;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SetInFo extends AppCompatActivity {

    //이전으로 부터 받아온 유저정보 저장
    String email = "";
    String password = "";
    String nickname = "";

    //학교 정보저장
    String university = "";          //대학교
    String latitude = "";            //위도
    String longtitude = "";          //경도

    //새서버 관련
    private FirebaseFirestore firestore;


    public User userinfo;                  //서버에 저장될 사용자의 정보
    public Map<String, Object> universityinfo = new HashMap<>();

    TextView uv_name;
    TextView signup_button;

   @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_in_fo);

       //서버 연결
       firestore = FirebaseFirestore.getInstance();

       email = getIntent().getStringExtra("email");
       password = getIntent().getStringExtra("password");
       nickname = getIntent().getStringExtra("nickname");
       university = getIntent().getStringExtra("university");
       latitude = getIntent().getStringExtra("latitude");
       longtitude = getIntent().getStringExtra("longtitude");

       //view 연결
       uv_name = (TextView)findViewById(R.id.university);
       uv_name.setText(university);
       signup_button = (TextView) findViewById(R.id.bt_login);

       signup();

    }

    void signup(){

       signup_button.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {

               //저장할 객체 생성
               userinfo = new User(email, password, nickname, university);
               //universityinfo = new University(university, latitude, longtitude);
               universityinfo.put("university", university);
               universityinfo.put("latitude", latitude);
               universityinfo.put("longtitude", longtitude);

               //비밀번호 변환(AES256)
               AES256 aes256 = new AES256();
               try {
                   userinfo.password = aes256.encrypt(userinfo.password);
               } catch (Exception e) {
                   e.printStackTrace();
               }
               //새저장
               firestore.collection("User").add(userinfo);
               firestore.collection("University").document(university).set(universityinfo);

               Intent signUp_intent = new Intent(SetInFo.this, Login.class);
               signUp_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
               signUp_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
               startActivity(signUp_intent);
               System.exit(0);

           }
       });


    }






}