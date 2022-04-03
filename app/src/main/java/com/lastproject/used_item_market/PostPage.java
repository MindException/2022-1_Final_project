package com.lastproject.used_item_market;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class PostPage extends AppCompatActivity {

    String mykey;
    String myUniv;
    String email;
    String nickname;

    //스피너만 따로
    Spinner purposeSpinner;          //모집인원
    Spinner categorySpinner;         //년도

    //가져온 스피너 결과 값
    String result_purpose = "";
    String result_category = "";

    //DB 관련
    private FirebaseDatabase database;
    private DatabaseReference myRef;

    //이미지 관련
    ArrayList<String> imgarray = new ArrayList<String>();       //이미지 이진화 모음
    int requestCode;
    private RecyclerView rv;
    public RecyclePostAdapter adapter;
    TextView img_countView;                                //사진 개수

    //위젯 모음
    TextView done_btn;                      //작성하기
    TextView back_btn;                      //뒤로가기
    EditText et_title;                      //제목
    EditText et_cash;                      //가격
    EditText et_text;                       //내용



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_page);

        //기본세팅
        email = getIntent().getStringExtra("email");
        mykey = getIntent().getStringExtra("mykey");
        nickname = getIntent().getStringExtra("nickname");
        myUniv = getIntent().getStringExtra("myUniv");

        //파이어 베이스 데이터베이스 연동
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();

        rv = (RecyclerView)findViewById(R.id.prouduct_recycleview);
        img_countView = (TextView)findViewById(R.id.count_img);

        //위젯
        back_btn = (TextView)findViewById(R.id.pg_xbtn);
        done_btn = (TextView)findViewById(R.id.pg_done);
        et_title = (EditText) findViewById(R.id.title_text);
        et_cash = (EditText) findViewById(R.id.et_cash);
        et_text = (EditText) findViewById(R.id.text);

        back();
        done();
        setPurposeSpinner();
        setCategorySpinner();
        saveImage();
    }

    public void back(){
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(PostPage.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("email", email);
                intent.putExtra("mykey", mykey);
                intent.putExtra("nickname", nickname);
                intent.putExtra("myUniv", myUniv);
                startActivity(intent);
            }
        });
    }

    void done(){ // 체크버튼 클릭 시 게시글 작성 완료
        done_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String nowTime = Time.nowTime();        //작성 시간을 저장
                String title = et_title.getText().toString();
                String cash = et_cash.getText().toString();
                String text = et_text.getText().toString();

                System.out.println(cash);

                if(!title.equals("") && !cash.equals("") && !text.equals("")){        //공백으로 받는게 없어야 한다.

                    try {       //여기서

                        Long icash = Long.parseLong(cash);     //한번
                        Product savepd = new Product(mykey,nickname,myUniv,title,icash,result_purpose,
                                result_category,"", text, nowTime);

                        if(imgarray.size() != 0){   //이미지가 있어야 이미지를 추가한다.
                            savepd.pictures = new ArrayList<String>();
                            for(int i = 0; i < imgarray.size(); i++){

                                savepd.pictures.add(imgarray.get(i));

                            }
                        }

                        //다 저장되었으니 이제 서버에 저장
                        myRef.child("Product").push().setValue(savepd);

                        //저장되었으니 인탠트로 넘어간다.
                        Intent intent = new Intent(PostPage.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("email", email);
                        intent.putExtra("mykey", mykey);
                        intent.putExtra("nickname", nickname);
                        intent.putExtra("myUniv", myUniv);
                        startActivity(intent);

                    }catch (Exception e){

                        et_cash.setText("");
                        et_cash.setHint("가격을 입력하세요.");

                    }
                }//if문 끝
            }
        });
    }

    void setPurposeSpinner(){
        purposeSpinner = (Spinner) findViewById(R.id.purpose);
        ArrayAdapter ppAdapter = ArrayAdapter.createFromResource(this,R.array.purpose, android.R.layout.simple_spinner_dropdown_item);
        ppAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); //선택목록이 나타날 때 사용할 레이아웃 지정
        purposeSpinner.setAdapter(ppAdapter);  //스피너에 어댑터 적용

        purposeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                result_purpose = purposeSpinner.getSelectedItem().toString(); // 스피너 선택값 가져오기
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    void setCategorySpinner(){
        categorySpinner = (Spinner) findViewById(R.id.category);
        ArrayAdapter cgAdapter = ArrayAdapter.createFromResource(this,R.array.category, android.R.layout.simple_spinner_dropdown_item);
        cgAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); //선택목록이 나타날 때 사용할 레이아웃 지정
        categorySpinner.setAdapter(cgAdapter);  //스피너에 어댑터 적용

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                result_category = categorySpinner.getSelectedItem().toString(); // 스피너 선택값 가져오기
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    void saveImage(){

        ImageButton bt_saveimg = (ImageButton)findViewById(R.id.add_img_btn);
        bt_saveimg.setOnClickListener(new View.OnClickListener() {      //사진 추가를 위해 이미지가 눌렸을 경우
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                requestCode = 1;
                startActivityForResult(intent, 1);      //코드 번호로 아래에서 동작한다.

            }
        });
    }

    //갤러리에서 가져온 이미지 저장(완성)
    @Override
    protected void onActivityResult(int requstCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (resultCode == RESULT_OK) {


            Uri uri = data.getData();
            InputStream inputStream = null;
            try {
                inputStream = getContentResolver().openInputStream(uri);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);        //비트맵으로 가져온다.
            ByteArrayOutputStream stream1 = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.WEBP, 100, stream1);
            byte[] img1Byte = stream1.toByteArray();
            String img = ParseIMG.byteArrayToBinaryString(img1Byte); //(성공)

            if(imgarray.size() < 10){       //사진을 10개까지 받는다.
                //배열에 저장
                imgarray.add(img);
                //사진 개수 카운트
                img_countView.setText("사진 추가(" + imgarray.size() + "/10)");
                //여기서 리사이클 뷰 실행
                init();
            }

        }
    }

    //리사이클 뷰 동작
    void init(){

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);             //이렇게 하면 수평으로 생성
        rv.setLayoutManager(linearLayoutManager);
        adapter = new RecyclePostAdapter();
        for(int i = 0; i < imgarray.size(); i++){
            adapter.addItem(imgarray.get(i));
        }
        rv.addItemDecoration(new RecyclerDecoration(5));       //간격을 추가한다.
        rv.setAdapter(adapter);

    }


}