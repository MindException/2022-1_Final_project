package com.lastproject.used_item_market;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import java.util.List;

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
    EditText chatView;

    //채팅방 정보
    ChattingRoomInfo chattingRoomInfo;
    ChatInfo chatInfo;

    //채팅
    List<String> chatList = new ArrayList<>();

    //리사이클러뷰 관련
    RecyclerChatAdapter adapter;
    LinearLayoutManager linearLayoutManager;

    //나의 인덱스 번호(채팅방)
    int myindex = 1000;    //1000은 초기화 값

    //현재 여기서의 마지막 본 행
    int readLastIndex = 0;
    int nowReadIndex = 0;

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
        chatView = (EditText) findViewById(R.id.insert_chat_editText);

        //파이어 베이스 리얼 타임 데이터베이스 연동
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();

        //리사이클뷰 레이아웃
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

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

                        if(myindex == 1000){        //인덱스 번호를 이미 찾은 경우
                            //인덱스 번호 찾기
                            for(int i = 0; i < chattingRoomInfo.customerList.size(); i++){
                                if(mykey.equals(chattingRoomInfo.customerList.get(i))){
                                    myindex = i;
                                    break;
                                }
                            }
                            readLastIndex = chattingRoomInfo.last_SEE.get(myindex);
                        }

                        //마지막 본 인덱스 업데이트
                        recyclerView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                            @Override
                            public void onScrollChange(View view, int i, int i1, int i2, int i3) {

                                //스크롤하면서 읽은 행(제일 마지막 기준)
                                nowReadIndex = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                                System.out.println("현재 위치: " +  nowReadIndex);
                                if(readLastIndex < nowReadIndex){           //더 읽었기 때문에 기준을 표시한다.
                                    readLastIndex = nowReadIndex;
                                    //마지막 본 인덱스 변화
                                    chattingRoomInfo.last_SEE.set(myindex, readLastIndex);

                                    //서버에 저장 - 자신이 마지막까지 읽은 부분을 저장(성공)
                                    DocumentReference update_LastSEE = firestore.collection("ChattingRoom")
                                            .document(chattingRoomInfo.chat_key);
                                    update_LastSEE.update("last_SEE", chattingRoomInfo.last_SEE).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            System.out.println("마지막 읽은 횟수 업데이트");
                                        }
                                    });


                                }

                            }
                        });

                        myRef.child("Chatting").child(chatkey)
                                .addValueEventListener(new ValueEventListener() {    //실시간 채팅
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {//데이터 변화
                                chatList = new ArrayList<>();
                                chatInfo = snapshot.getValue(ChatInfo.class);
                                chatList = chatInfo.chatList;       //채팅을 여기다가 저장

                                /*
                                //어뎁터 설정
                                adapter = new RecyclerChatAdapter(chatList, mykey, chattingRoomInfo);
                                recyclerView.setAdapter(adapter);
                                */

                                //리사이클 뷰 보던 위치 고정 세팅
                                linearLayoutManager.scrollToPosition(readLastIndex);       //채팅 마지막에 보던 위치로 이동
                                //리사이클 뷰에 맨 상이 시작 위치가 된다.


                                //자신이 읽은 부분 위치로 리사이클뷰 이동만하면 끝
                                if(chatInfo.chatList.size() - 2 == nowReadIndex){       //마지막 채팅 보고 있는데 채팅이 추가된 경우

                                    //위치

                                }else{      //위에 채팅 보고 있다가 채팅이 추가된 경우

                                    Toast.makeText(ChatPage.this, "새로운 채팅", Toast.LENGTH_SHORT).show();
                                    //위치

                                }
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
                Intent intent = new Intent(ChatPage.this, ChattingListPage.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("email", email);
                intent.putExtra("mykey", mykey);
                intent.putExtra("nickname", nickname);
                intent.putExtra("myUniv", myUniv);
                intent.putExtra("myimg", myimg);
                startActivity(intent);
                System.exit(0);
            }
        });

    }

    //입력하기
    void insert(){

        insert_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String chat_text = chatView.getText().toString();
                String nowTime = Time.nowNewTime();

                if(!chat_text.equals("")) {      //채팅을 입력한 경우

                    String chat = mykey + "/%%/" + nickname + "/%%/"
                            + chat_text + "/%%/" + nowTime;

                    chatInfo.chatList.add(chat);        //채팅 배열에 추가
                    //실시간 데이터베이스에 채팅 먼저 저장
                    myRef.child("Chatting").child(chatkey).setValue(chatInfo)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    //채팅 저장에 성공한 경우
                                    chattingRoomInfo.last_time = nowTime;   //마지막 시간 저장
                                    chattingRoomInfo.last_text = chat;      //마지막 채팅 저장
                                    //채팅 추가되니 -1 하지 말아라
                                    chattingRoomInfo.last_index = chatList.size() -1 ;
                                    chattingRoomInfo.last_SEE.set(myindex, chatList.size() - 1);

                                    DocumentReference setDocRef = chatRoomRef.document(chatkey);
                                    setDocRef.set(chattingRoomInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {        //저장에 성공한 경우

                                            chatView.setText("");       //성공했으니 채팅입력 창 초기화

                                        }
                                    });
                                }
                            });
                }

            }
        });

    }

}