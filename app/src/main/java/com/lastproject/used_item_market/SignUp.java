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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

    //패스워드 이미지뷰
    ImageView checkiv;

    //String
    String spassword1;
    String spassword2;
    String sname;

    //trigger 모음
    boolean password_trigger = false;                   //2번째 재확인 비밀번호가 맞는지 확인하는 용
    boolean email_trigger = false;                      //이미 있는 아이디인지 확인용
    boolean nickname_trigger = false;                   //이미 있는 닉네임인지 확인용



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
        checkiv = (ImageView) findViewById(R.id.check_mark);

        //이메일 자동 세팅(xml에서 못 건드리게해서 고정된다.)
        et_email.setText(email);

        //구글 아이디를 검색하여 이미 회원가입이 되어 있는 사용자인지 확인한다.
        try{

            myRef.child("User").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {      //이미 회원가입한 아이디인지 확인한다.

                    for(DataSnapshot ds1 : snapshot.getChildren()){

                        User checkuser = ds1.getValue(User.class);           //아이디가 있는지 확인한다.
                        if(email.equals(checkuser.google_email)){                //아이디가 이미 존재하는 경우

                            Toast.makeText(SignUp.this, "회원가입한 아이디입니다.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(SignUp.this, Login.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            email_trigger = true;
                            startActivity(intent);
                            break;

                        }

                    }//for문 끝

                    if(email_trigger == true){      //중복 이메일 발생하여 액티비티 종료
                        finish();
                    }


                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });



        }catch (Exception e){ }

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

        TextView btn_sign = findViewById(R.id.bt_school_choice);
        btn_sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(password_trigger == false){          //비밀번호가 일치하지 않을 경우

                    Toast.makeText(SignUp.this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();

                    return;     //비밀번호가 일치하지 않는다.
                }

                //닉네임이 있는지 검사한다.
                myRef.child("User").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        sname = et_name.getText().toString();           //닉네임을 가져온다.

                        for(DataSnapshot ds1 : snapshot.getChildren()){     //닉네임 검사

                            User check_user = ds1.getValue(User.class);
                            if(sname.equals(check_user.nickname)){                   //닉네임이 같을 경우

                                nickname_trigger = true;
                                et_name.setText("");
                                et_name.setHint("이미 존재하는 닉네임입니다.");

                            }



                        }//for문 끝

                        if(nickname_trigger == false){       //존재하는 닉네임이 아닐 경우

                            Intent signUp_intent = new Intent(SignUp.this, SignUpMap.class);
                            signUp_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            signUp_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            signUp_intent.putExtra("email", email);
                            signUp_intent.putExtra("password",spassword1);
                            signUp_intent.putExtra("nickname",sname);
                            startActivity(signUp_intent);
                            finish();

                        }
                        nickname_trigger = false;

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });


    }



}