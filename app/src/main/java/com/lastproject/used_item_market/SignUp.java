package com.lastproject.used_item_market;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class SignUp extends AppCompatActivity {

    //구글 이메일
    String email;

    //새서버 관련
    private FirebaseFirestore firestore;
    private CollectionReference userDocument;
    public User userinfo;           //서버에 저장될 사용자의 정보

    //EditText
    EditText et_email;
    EditText et_password1;
    EditText et_password2;
    EditText et_name;

    //패스워드 이미지뷰
    ImageView checkiv;

    //String
    String spassword1;
    String spassword2;
    String sname;

    //trigger 모음
    boolean password_trigger = false;                   //2번째 재확인 비밀번호가 맞는지 확인하는 용

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);

        //서버 연결
        firestore = FirebaseFirestore.getInstance();
        userDocument = firestore.collection("User");

        //인증에 성공한 이메일 값을 받아온다.
        email = getIntent().getStringExtra("email");

        //EditText 연결
        et_email = (EditText) findViewById(R.id.email_set);
        et_password1 = (EditText)findViewById(R.id.password_set);
        et_password2 =(EditText) findViewById(R.id.password2_set);
        et_name = (EditText)findViewById(R.id.name_set);
        checkiv = (ImageView) findViewById(R.id.check_mark);

        //이메일 자동 세팅(xml에서 못 건드리게해서 고정된다.)
        et_email.setText(email);


        //텍스트 갱신
        et_password2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {   //변경될 경우

                //문자열 가져오기
                spassword1 = et_password1.getText().toString();
                spassword2 = et_password2.getText().toString();

                if(spassword1.equals(spassword2)){      //둘다 비밀번호가 일치할 경우

                    password_trigger = true;        //같다
                    checkiv.setImageResource(R.drawable.check);

                }else{                                  //일치하지 않을 경우

                    password_trigger = false;        //같다
                    checkiv.setImageResource(R.drawable.uncheck);

                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        //뒤로가기 버튼
        cancel();
        //
        sign_up();


    }

    //뒤로가기 버튼
    void cancel(){

        ImageButton cancel_button = (ImageButton) findViewById(R.id.back_login);
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

        TextView btn_sign = (TextView) findViewById(R.id.bt_school_choice);
        btn_sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(password_trigger == false){          //비밀번호가 일치하지 않을 경우

                    Toast.makeText(SignUp.this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();

                    return;     //비밀번호가 일치하지 않는다.
                }

                sname = et_name.getText().toString();           //닉네임을 가져온다.

                //닉네임을 검사한다.
                Query query = userDocument.whereEqualTo("nickname", sname);
                query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){        //송수신 성공시
                            if(task.getResult().size() <= 0){       //아무것도 없어야 중복이 아니다.

                                Intent signUp_intent = new Intent(SignUp.this, SignUpMap.class);
                                signUp_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                signUp_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                signUp_intent.putExtra("email", email);
                                signUp_intent.putExtra("password",spassword1);
                                signUp_intent.putExtra("nickname",sname);
                                startActivity(signUp_intent);
                                System.exit(0);

                            }else{
                                Toast.makeText(SignUp.this, "닉네임 중복", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

            }//onClick
        });


    }



}