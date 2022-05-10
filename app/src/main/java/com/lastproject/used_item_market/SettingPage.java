package com.lastproject.used_item_market;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.InputStream;

public class SettingPage extends AppCompatActivity {

    //기본 나의 정보
    String email = "";
    String mykey = "";
    String nickname = "";
    String myUniv = "";
    String myimg = "";

    //새DB
    private FirebaseFirestore firestore;        //DB
    private CollectionReference UserRef;
    DocumentReference documentReference;

    //이미지DB
    private FirebaseStorage storage;            //이미지 저장소
    private StorageReference storageRef;        //정확한 위치에 파일 저장

    //유저 정보
    private User user;

    //프로필 사진
    ImageView imageView;
    private int requestCode;

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

        //위젯
        imageView = findViewById(R.id.mypage_profile);

        //DB 연동
        firestore = FirebaseFirestore.getInstance();
        UserRef = firestore.collection("User");
        documentReference = UserRef.document(mykey);

        //이미지DB 연동
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

       Back();
       imageButton();
       imagetrigger();
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
                intent.putExtra("myimg", myimg);
                startActivity(intent);
                finish();
            }
        });
    }

    void imageButton(){         //프로필 사진 변경 및 저장

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

    }//갤러리 가져오기 끝

    void imagetrigger(){


        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()) {
                        user = documentSnapshot.toObject(User.class);
                        if(user.img != null){
                            StorageReference deserRef = storageRef.child("profiles").
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





}