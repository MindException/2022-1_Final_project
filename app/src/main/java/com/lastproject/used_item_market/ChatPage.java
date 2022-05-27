package com.lastproject.used_item_market;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
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
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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

    //이미지 DB
    private FirebaseStorage storage;            //이미지 저장소
    private StorageReference storageRef;        //정확한 위치에 파일 저장

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
    int nowReadIndex = -1;

    int addTextMsg = 0;

    //처음 한 번만 동작
    Boolean trigger = false;

    int num = 0;

    ArrayList<Bitmap> bitmaps = new ArrayList<>();
    ImageView profile;

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

        //이미지
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        //리사이클뷰 레이아웃
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setNestedScrollingEnabled(false);

        //서버 연동
        firestore = FirebaseFirestore.getInstance();
        chatRoomRef = firestore.collection("ChattingRoom");
        DocumentReference docRef = chatRoomRef.document(chatkey);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {

                //데이터의 정보가 변동이 있을 때마다 가져온다.
                chattingRoomInfo = value.toObject(ChattingRoomInfo.class);
                System.out.println("됨?");
                setting(storageRef);    //세팅

            }//onEvent() 끝
        });//리스너 끝

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

    void setting(StorageReference storageReference){        //이미지를 먼저 가져와서 세팅해야 됨으로 재귀로 가야한다.

        if(bitmaps == null || bitmaps.size() >= chattingRoomInfo.customer_images.size()){
            //사진을 다 불러왔으니까 채팅에 대하여 세팅한다.

            //어뎁터 장착
            if(trigger == false){       //제일 처음 한 번만 작성
                adapter = new RecyclerChatAdapter(chatList, mykey, chattingRoomInfo, bitmaps);
                recyclerView.setAdapter(adapter);
                trigger = true;
            }

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
                    if(chattingRoomInfo.last_SEE.get(myindex) < nowReadIndex){           //더 읽었기 때문에 기준을 표시한다.
                        //마지막 본 인덱스 변화
                        chattingRoomInfo.last_SEE.set(myindex, nowReadIndex);

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
            //읽은 표시 끝

            //채팅
            myRef.child("Chatting").child(chatkey)
                    .addValueEventListener(new ValueEventListener() {    //실시간 채팅
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {//데이터 변화
                            chatList = new ArrayList<>();
                            chatInfo = snapshot.getValue(ChatInfo.class);
                            chatList = chatInfo.chatList;       //채팅을 여기다가 저장

                            //어뎁터 정보 추가
                            adapter.chatting = chatList;
                            if(adapter.chattingRoomInfo != chattingRoomInfo){       //새로운 사용자가 입장하여 채팅방 정보를 갱신한 경우
                                //이거 함수화
                                adapter.newCustomer(chattingRoomInfo);
                            }
                            adapter.notifyDataSetChanged();     //그 자리 그대로 있는다.

                            if(nowReadIndex == -1){     //맨 처음 위치 초기화
                                nowReadIndex = chattingRoomInfo.last_SEE.get(myindex);
                                linearLayoutManager.scrollToPosition(nowReadIndex);
                            }

                            //자신이 읽은 부분 위치로 리사이클뷰 이동만하면 끝
                            if(chatInfo.chatList.size() - 2 == nowReadIndex){       //마지막 채팅 보고 있는데 채팅이 추가된 경우
                                //위치
                                linearLayoutManager.scrollToPosition(nowReadIndex + 1);     //새로운 채팅으로 리사이클뷰 내리기
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });//실시간 채팅



        }else{      //사진을 다 불러오지 못하여 사진을 불러오기 위하여 재귀한다.

            StorageReference imgRef = storageReference.child("profiles")
                    .child(chattingRoomInfo.customer_images.get(bitmaps.size()));
            System.out.println(chattingRoomInfo.customer_images.get(bitmaps.size()));
            imgRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {        //재귀가 잘 동작한다.

                    System.out.println(uri);

                    //인덱스 저장
                    int index = 0;
                    for(int i = 0; i < chattingRoomInfo.customerList.size(); i++){
                        if(mykey.equals(chattingRoomInfo.customerList.get(i))){
                            index = i;
                            break;
                        }
                    }
                    if(bitmaps.size() == index){    //같을 경우

                        View imgView = getLayoutInflater().inflate(R.layout.chatting_iitem_i, null, false);
                        profile = (ImageView)imgView.findViewById(R.id.chattimg_item_i_profile);


                    }else{      //틀릴 경우

                        View imgView = getLayoutInflater().inflate(R.layout.chatting_item_you, null, false);
                        profile = (ImageView)imgView.findViewById(R.id.chattimg_item_you_profile);

                    }

                        //비트맵으로 저장한다.
                        Glide.with(profile)
                            .asBitmap()
                            .load(uri)
                            .override(150, 150)
                            .into(new SimpleTarget<Bitmap>(){
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {

                                    Bitmap bitmap = resource;
                                    bitmaps.add(bitmap);
                                    System.out.println(bitmap.toString());
                                    System.out.println("사이즈:" + bitmaps.size());
                                    //다시 재귀한다.
                                    setting(storageReference);

                                }
                            });

                }
            });
        }
    }

    @Override
    protected void onStop(){
        super.onStop();
        bitmaps.clear();
        bitmaps = null;
    }

}