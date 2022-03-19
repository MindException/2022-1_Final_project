package com.lastproject.used_item_market;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUp extends AppCompatActivity {

    //구글 이메일
    String email;

    //서버 관련
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    public User userinfo;           //서버에 저장될 사용자의 정보

    //EditText
    EditText et_email;
    EditText et_password1;
    EditText et_password2;
    EditText et_name;

    //String
    String spassword1;
    String spassword2;
    String sname;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);

        //서버 연결
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();

        //인증에 성공한 이메일 값을 받아온다.
        email = getIntent().getStringExtra("email");
        System.out.println(email);

        //EditText 연결
        et_email = findViewById(R.id.email_set);
        et_password1 = findViewById(R.id.password_set);
        et_password2 = findViewById(R.id.password2_set);
        et_name = findViewById(R.id.name_set);

        //이메일 자동 세팅(xml에서 못 건드리게해서 고정된다.)
        et_email.setText(email);








        //뒤로가기 버튼
        cancel();
        //
        sign_up();


    }

    //뒤로가기 버튼
    void cancel(){

        ImageButton cancel_button = findViewById(R.id.back_login);
        cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent signUp_intent = new Intent(SignUp.this, Login.class);
                signUp_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                signUp_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(signUp_intent);
                finish();

            }
        });
    }

    //회원가입 버튼
    void sign_up(){






    }


    //실제 서버에 저장(이건 t-map 구현되면 옮긴다.)
    void put_UserINFO(){





    }


}