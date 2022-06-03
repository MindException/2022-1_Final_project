//개발: 이승현

package com.lastproject.used_item_market;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class ChattingListPage extends AppCompatActivity{

    //기본 나의 정보
    String email = "";
    String mykey = "";
    String nickname = "";
    String myUniv = "";
    String myimg = "";

    //Firestore
    private FirebaseFirestore firestore;
    private CollectionReference chatRoomRef;

    //Storage
    private FirebaseStorage storage;            //이미지 저장소
    private StorageReference storageRef;        //정확한 위치

    //위젯
    RecyclerView recyclerView;
    ImageButton imageButton;

    //어뎁터
    RecyclerChatListAdapter recyclerChatListAdapter;

    //채팅방
    ArrayList<ChattingRoomInfo> chattingRoomInfoArrayList = new ArrayList<ChattingRoomInfo>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting_home);

        //기본세팅
        email = getIntent().getStringExtra("email");
        mykey = getIntent().getStringExtra("mykey");
        nickname = getIntent().getStringExtra("nickname");
        myUniv = getIntent().getStringExtra("myUniv");
        myimg = getIntent().getStringExtra("myimg");

        //서버 연동
        firestore = FirebaseFirestore.getInstance();
        chatRoomRef = firestore.collection("ChattingRoom");

        //이미지 서버 연동가능
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        //위젯
        recyclerView = (RecyclerView)findViewById(R.id.chatting_home_list);
        imageButton = (ImageButton)findViewById(R.id.chatting_home_back);

        //리사이클뷰 세팅
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerChatListAdapter = new RecyclerChatListAdapter(chattingRoomInfoArrayList, mykey);
        recyclerChatListAdapter.setOnItemClickListener(new RecyclerChatListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int pos) {
                Intent intent = new Intent(ChattingListPage.this, ChatPage.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("email", email);
                intent.putExtra("mykey", mykey);
                intent.putExtra("nickname", nickname);
                intent.putExtra("myUniv", myUniv);
                intent.putExtra("myimg", myimg);
                intent.putExtra("chatkey", chattingRoomInfoArrayList.get(pos).chat_key);
                startActivity(intent);
                System.exit(0);
            }
        });
        recyclerView.setAdapter(recyclerChatListAdapter);

        Query query = chatRoomRef.whereArrayContains("customerList", mykey)
                .orderBy("last_time", Query.Direction.DESCENDING);
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {      //변동 실시간 수신
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException error) {

                for(DocumentChange dc : snapshots.getDocumentChanges()){     //신기하다

                    //맨 처음은 전부 추가되지만
                    //그 다음부터는 변동된 것만 추가된다.

                    boolean trigger = false;        //이미 추가하면 Arraylist에 추가안하게 한다.

                    ChattingRoomInfo chattingRoomInfo = dc.getDocument().toObject(ChattingRoomInfo.class);
                    for(int i = 0; i < chattingRoomInfoArrayList.size(); i++){
                        //제목과 시간이 같은 것으로 찾을 수 있다.
                        if (chattingRoomInfo.title.equals(chattingRoomInfoArrayList.get(i).title)
                                && chattingRoomInfo.start_time.equals(chattingRoomInfoArrayList.get(i).start_time)){
                            chattingRoomInfoArrayList.set(i,chattingRoomInfo);
                            trigger = true;
                        }
                    }

                    if (trigger != true){       //추가 못하였으니 추가한다.

                        //추가하기 전에 이미 나간 채팅방인지 확인한다.
                        int index = 0;
                        for(index = 0; index < chattingRoomInfo.customerList.size(); index++){
                            if (mykey.equals(chattingRoomInfo.customerList.get(index))){
                                break;
                            }
                        }

                        //0번이어야 나가지 않은 사용자이다.
                        if(chattingRoomInfo.out_customer_index.get(index) == 0){
                            chattingRoomInfoArrayList.add(chattingRoomInfo);    //기존의 것이 없으면  추가
                        }
                    }
                }
                //이거 정렬형태로 가야한다.
                chattingRoomInfoArrayList.sort(new CompareChatList<ChattingRoomInfo>());
                recyclerChatListAdapter.notifyDataSetChanged();

            }
        });

        back();

    }

    void back(){    //뒤로가기

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChattingListPage.this, MainActivity.class);
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

    @Override
    public void onBackPressed(){
        //뒤로가기 막기
    }

}