//개발: 이승현

package com.lastproject.used_item_market;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class Login extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{

    //기본 나의 정보
    String email = "";
    String mykey = "";
    String nickname = "";
    String myUniv = "";
    String myimg = "";

    SharedPreferences logininfo;
    SharedPreferences.Editor editor;

    //자동 로그인
    private String autologin;    //String 값으로 true이면 자동으로 넘어간다.

    //로그인 버튼들
   TextView google_button;
   TextView login_button;

    //구글로그인 관련 인증증
    private FirebaseAuth auth;                      //파이어 베이스 인증 객체
    private GoogleApiClient googleApiClient;        //구글 API 클라이언트 객체
    private static final int REQ_SIGN_GOOGLE = 100; //구글 로그인 결과 코드
    GoogleSignInOptions googleSignInOptions;

    //사용자가 입력한 이메일과 비밀번호
    EditText et_email;
    EditText et_password;
    public User userinfo;           //서버에 저장될 사용자의 정보

    //새서버
    private FirebaseFirestore firestore;
    private CollectionReference userDocument;

    //텍스트로 정보를 가져옴
    private String semail = "";
    private String spassword = "";

    //비밀번호 암호화
    AES256 aes256;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        //서버 연결
        firestore = FirebaseFirestore.getInstance();
        userDocument = firestore.collection("User");

        //TextView 연결
        et_email = (EditText)findViewById(R.id.email);
        et_password = (EditText)findViewById(R.id.password);
        google_button = (TextView)findViewById(R.id.bt_google);
        login_button = (TextView)findViewById(R.id.bt_login);


        //암호화 객체 생성
        aes256 = new AES256();

        //자동 로그인
        logininfo = getSharedPreferences("setting", MODE_PRIVATE);      //기본 저장 로그인 객체 생성
        editor = logininfo.edit();
        autologin = logininfo.getString("auto", "");                //자동로그인이 되어있느지 가져온다.(두번 째 인자는 실패시 반환값)
        if(autologin.equals("True")){       //자동로그인으로 넘어간다.

            mykey = logininfo.getString("mykey", "");
            email = logininfo.getString("email","");
            nickname = logininfo.getString("nickname", "");
            myUniv = logininfo.getString("myUniv", "");
            myimg = logininfo.getString("myimg", "");
            Intent login_intent = new Intent(Login.this, MainActivity.class);
            login_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            login_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            login_intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            login_intent.putExtra("email", email);
            login_intent.putExtra("mykey", mykey);
            login_intent.putExtra("nickname", nickname);
            login_intent.putExtra("myUniv", myUniv);
            login_intent.putExtra("myimg", myimg);
            Toast.makeText(Login.this, "자동 로그인", Toast.LENGTH_SHORT).show();
            startActivity(login_intent);
            finish();

        }

        googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                .build();

        auth = FirebaseAuth.getInstance();

        //메서드로 정리
        login();
        googleLogin();

    }

    void login(){

        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //입력되어 있는 텍스트를 가져온다.
                semail = et_email.getText().toString();
                spassword = et_password.getText().toString();

                Query loginQuery = userDocument.whereEqualTo("google_email", semail);
                loginQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if(task.isSuccessful()){
                            if(task.getResult().size() > 0){
                                for(QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){    //가져오고 1개 들어있다.
                                    userinfo = queryDocumentSnapshot.toObject(User.class);
                                    mykey = queryDocumentSnapshot.getId();
                                }
                                try {
                                    userinfo.password = aes256.decrypt(userinfo.password);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                if(spassword.equals(userinfo.password)){    //비밀번호가 같을 경우
                                    Intent login_intent = new Intent(Login.this, MainActivity.class);
                                    login_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    login_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    login_intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                    login_intent.putExtra("email", userinfo.google_email);
                                    login_intent.putExtra("mykey", mykey);
                                    login_intent.putExtra("nickname", userinfo.nickname);
                                    login_intent.putExtra("myUniv", userinfo.university);
                                    login_intent.putExtra("myimg", userinfo.img);
                                    editor.putString("auto", "True");
                                    editor.putString("email", userinfo.google_email);
                                    editor.putString("mykey", mykey);
                                    editor.putString("nickname", userinfo.nickname);
                                    editor.putString("myUniv", userinfo.university);
                                    editor.putString("myimg", userinfo.img);
                                    editor.commit();
                                    startActivity(login_intent);
                                    finish();
                                }else{  //비밀번호가 틀릴 경우
                                    spassword = "";
                                    et_password.setText("");
                                    et_password.setHint("비밀번호가 일치하지 않습니다.");
                                }
                            }else{      //아이디가 존재하지 않음
                                //다시 기본 세팅들 초기화
                                semail = "";
                                spassword = "";
                                et_email.setText("");
                                et_email.setHint("일치하는 아이디가 없습니다.");
                                et_password.setText("");
                            }
                        }else{
                            //다시 기본 세팅들 초기화
                            semail = "";
                            spassword = "";
                            //화면도 초기화
                            et_email.setText("");
                            et_password.setText("");
                            Toast.makeText(Login.this, "로그인 오류", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

    }

    void googleLogin(){

        google_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                startActivityForResult(intent, REQ_SIGN_GOOGLE);

            }
        });
    }

    //구글 로그인 인증을 요청할 경유 결과 값을 되돌려 받는 곳이다
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQ_SIGN_GOOGLE){     //결과를 가져온다.

            //여기까지는 잘 들어온다.
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            //지금 여기서 값을 못 가져온다.
            if(result.isSuccess() == true){  //결과가 성공한 경우

                GoogleSignInAccount account = result.getSignInAccount();   //구글로 부터 온 결과가 다 담겨있다.
                resultLogin(account);   //로그인 결과값을 수행하는 메소드


            }else{
            }
        }

    }

    private void resultLogin(GoogleSignInAccount account){

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {  //Task는 인증 결과이다.

                        if(task.isSuccessful()){      //로그인이 성공했으면
                            email = account.getEmail();         //이메일 저장


                            //먼저 구글 아이디가 존재하는지 확인한다.
                            //닉네임을 검사한다.
                            userDocument.whereEqualTo("google_email", email)
                                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if(task.isSuccessful()){        //송수신 성공시
                                        if(task.getResult().size() <= 0){       //아무것도 없어야 중복이 아니다.
                                            Intent signUp_intent = new Intent(Login.this, SignUp.class);
                                            signUp_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            signUp_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            signUp_intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                            signUp_intent.putExtra("email", email);
                                            startActivity(signUp_intent);
                                            //이것을 해줘야 다음에 계정 생성을 할 때도 다시 계정선택이 가능하다.
                                            auth.signOut();
                                            FirebaseAuth.getInstance().signOut();
                                            AuthUI.getInstance().signOut(getApplicationContext());
                                            finish();

                                        }else{
                                            Toast.makeText(Login.this, "Google login", Toast.LENGTH_SHORT).show();
                                            //이것을 해줘야 다음에 계정 생성을 할 때도 다시 계정선택이 가능하다.
                                            auth.signOut();
                                            FirebaseAuth.getInstance().signOut();
                                            AuthUI.getInstance().signOut(getApplicationContext());

                                            //존재하는 정보를 가져온다.
                                            for(QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){    //가져오고 1개 들어있다.
                                                userinfo = queryDocumentSnapshot.toObject(User.class);
                                                mykey = queryDocumentSnapshot.getId();
                                            }

                                            //이제 자동으로 로그인하기 위하여 넘어간다.
                                            Intent login_intent = new Intent(Login.this, MainActivity.class);
                                            login_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            login_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            login_intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                            login_intent.putExtra("email", userinfo.google_email);
                                            login_intent.putExtra("mykey", mykey);
                                            login_intent.putExtra("nickname", userinfo.nickname);
                                            login_intent.putExtra("myUniv", userinfo.university);
                                            login_intent.putExtra("myimg", userinfo.img);
                                            editor.putString("auto", "True");
                                            editor.putString("email", userinfo.google_email);
                                            editor.putString("mykey", mykey);
                                            editor.putString("nickname", userinfo.nickname);
                                            editor.putString("myUniv", userinfo.university);
                                            editor.putString("myimg", userinfo.img);
                                            editor.commit();
                                            startActivity(login_intent);
                                            finish();

                                        }
                                    }else{
                                    }
                                }
                            });

                        }else{

                            Toast.makeText(Login.this, "구글 계정 인증 실패", Toast.LENGTH_SHORT).show();

                        }

                    }
                });
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onBackPressed(){
        //뒤로가기 막기
    }


}