package com.lastproject.used_item_market;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;

public class PostPage extends AppCompatActivity {
    //스피너만 따로
    Spinner purposeSpinner;          //모집인원
    Spinner categorySpinner;            //년도
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_page);

        Back();
        Done();
        setPurposeSpinner();
        setCategorySpinner();
    }

    void Back(){
        ImageButton back = (ImageButton)findViewById(R.id.xbtn);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PostPage.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    void Done(){ // 체크버튼 클릭 시 게시글 작성 완료
        ImageButton done = (ImageButton)findViewById(R.id.done);

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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
                String pp = purposeSpinner.getSelectedItem().toString(); // 스피너 선택값 가져오기
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
                String cg = categorySpinner.getSelectedItem().toString(); // 스피너 선택값 가져오기
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
}