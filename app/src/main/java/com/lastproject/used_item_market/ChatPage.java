package com.lastproject.used_item_market;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class ChatPage extends AppCompatActivity {

    //기본 나의 정보
    String email = "";
    String mykey = "";
    String nickname = "";
    String myUniv = "";
    String myimg = "";
    String chatkey = "";        //채팅방하고 채팅 테이블이 둘다 키값이 같다

    //Firestore
    private FirebaseFirestore firestore;
    private CollectionReference chatRoomRef;

    //RealtimeDatbase
    private FirebaseDatabase database;
    private DatabaseReference myRef;

    //위젯
    RecyclerView recyclerView;
    ImageButton back_button;
    ImageButton insert_button;

    //채팅방 정보
    ChattingRoomInfo chattingRoomInfo;
    ChatInfo chatInfo;

    //채팅
    ArrayList<String> chatList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting);

        //기본세팅
        email = getIntent().getStringExtra("email");
        mykey = getIntent().getStringExtra("mykey");
        nickname = getIntent().getStringExtra("nickname");
        myUniv = getIntent().getStringExtra("myUniv");
        myimg = getIntent().getStringExtra("myimg");
        chatkey = getIntent().getStringExtra("chatkey");

        //위젯 세팅
        recyclerView = (RecyclerView)findViewById(R.id.chatList);
        back_button = (ImageButton)findViewById(R.id.chat_back_btn);
        insert_button = (ImageButton)findViewById(R.id.insert_btn);


        //파이어 베이스 리얼 타임 데이터베이스 연동
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();

        //서버 연동
        firestore = FirebaseFirestore.getInstance();
        chatRoomRef = firestore.collection("ChattingRoom");
        DocumentReference docRef = chatRoomRef.document(chatkey);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {     //채팅방 정보를 가져온다.
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){    //송신 성공
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()){
                        chattingRoomInfo = document.toObject(ChattingRoomInfo.class);       //채팅방 정보 가져옴
                        myRef.child("Chatting").child(chatkey)
                                .addValueEventListener(new ValueEventListener() {    //실시간 채팅
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {//데이터 변화
                                chatList = new ArrayList<>();
                                for(DataSnapshot dataSnapshot : snapshot.getChildren()){    //가져오기
                                    chatInfo = dataSnapshot.getValue(ChatInfo.class);
                                }
                                chatList = chatInfo.chatList;       //채팅을 여기다가 저장

                                //채팅 변동 코딩



                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });//실시간 채팅
                    }
                }//successful
            }
        });//채팅방 끝
        insert();
        back();

    }

    //뒤로가기
    void back(){

        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //나가면서 서버 세팅




            }
        });

    }

    //입력하기
    void insert(){

        insert_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {




            }
        });

    }

}