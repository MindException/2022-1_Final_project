package com.lastproject.used_item_market;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class RecyclerChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    ChattingRoomInfo chattingRoomInfo;
    String mykey;
    List<String> chatting = new ArrayList<>();

    //이미지 DB
    private FirebaseStorage storage;            //이미지 저장소
    private StorageReference storageRef;        //정확한 위치에 파일 저장

    RecyclerChatAdapter(List<String> chatting, String mykey, ChattingRoomInfo chattingRoomInfo){
        this.mykey = mykey;
        this.chatting = chatting;
        this.chattingRoomInfo = chattingRoomInfo;
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        /*
            여기서 StringTokenizer로 분기처리하기
            시스템이면 0,
            상대방이면 1,
            자기자신이면 2,
        */
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        int viewType_trigger = 0;
        StringTokenizer stringTokenizer = new StringTokenizer(chatting.get(viewType), "/%%/");
        String key = stringTokenizer.nextToken();
        if(key.equals("System")){
            //시스템 뷰 홀더
            View view = inflater.inflate(R.layout.chatting_item_system, parent, false);
            RecyclerChatAdapter.ViewHolderSystem viewHolderSystem = new RecyclerChatAdapter.ViewHolderSystem(view);
            return viewHolderSystem;
        }else if(key.equals(mykey)){
            //본인 뷰 홀더
            View view = inflater.inflate(R.layout.chatting_iitem_i, parent, false);
            RecyclerChatAdapter.ViewHolderMe viewHolderMe = new RecyclerChatAdapter.ViewHolderMe(view);
            return viewHolderMe;

        }else{
            //상대 뷰 홀더
            View view = inflater.inflate(R.layout.chatting_item_you, parent, false);
            RecyclerChatAdapter.ViewHolderNotMe viewHolderNotMe = new RecyclerChatAdapter.ViewHolderNotMe(view);
            return viewHolderNotMe;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof ViewHolderSystem){     //시스템
            ((RecyclerChatAdapter.ViewHolderSystem)holder).onBind(chatting.get(position));
        }else if(holder instanceof ViewHolderMe){   //본인
            ((RecyclerChatAdapter.ViewHolderMe)holder).onBind(chatting.get(position));
        }else{  //상대방
            ((RecyclerChatAdapter.ViewHolderNotMe)holder).onBind(chatting.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return chatting.size();
    }

    @Override
    public int getItemViewType(int position) {      //리사이클뷰에 재활용을 막는다.
        return position;
    }

    //채팅 포멧 예시: 키값/%%/닉네임/%%/채팅내용/%%/채팅시간

    //시스템(완)
    class ViewHolderSystem extends RecyclerView.ViewHolder{

        TextView alarm;

        public ViewHolderSystem(@NonNull View itemView) {
            super(itemView);
            alarm = (TextView)itemView.findViewById(R.id.System_alarm);
        }

        public void onBind(String chat) {
            StringTokenizer st = new StringTokenizer(chat, "/%%/");
            String key = st.nextToken();
            String msg = st.nextToken();
            alarm.setText(msg);
        }
    }

    //상대방
    class ViewHolderNotMe extends RecyclerView.ViewHolder{

        ImageView profile;
        TextView nicknameView;
        TextView chatView;
        TextView time;
        View itemview;

        public ViewHolderNotMe(@NonNull View itemView) {
            super(itemView);

            this.itemview = itemView;

            profile = (ImageView)itemView.findViewById(R.id.chattimg_item_you_profile);
            nicknameView = (TextView)itemView.findViewById(R.id.chattimg_item_you_neme);
            chatView = (TextView)itemView.findViewById(R.id.chattimg_item_you_text);
            time = (TextView)itemView.findViewById(R.id.chatting_item_you_time);

        }

        public void onBind(String chat) {

            StringTokenizer st = new StringTokenizer(chat, "/%%/");
            String key = st.nextToken();
            String nickname = st.nextToken();
            String text = st.nextToken();
            String chat_time = st.nextToken();

            //인덱스 저장
            int index = 0;
            for(int i = 0; i < chattingRoomInfo.customerList.size(); i++){
                if(key.equals(chattingRoomInfo.customerList.get(i))){
                    index = i;
                    break;
                }
            }

            //프로필 이미지 처리
            StorageReference sellerimgRef = storageRef.child("profiles")
                    .child(chattingRoomInfo.customer_images.get(index));
            sellerimgRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {

                    try {

                        Glide.with(itemview)
                                .load(uri)
                                .override(150, 150)
                                .into(profile);

                    } catch (Exception e) {
                        System.out.println("view holder binding 실패");
                    }

                }
            }); //프로필 이미지 처리 끝

            //기본세팅
            nicknameView.setText(nickname);
            chatView.setText(text);
            time.setText(chat_time);

        }
    }

    //본인
    class ViewHolderMe extends RecyclerView.ViewHolder{

        ImageView profile;
        TextView nicknameView;
        TextView chatView;
        TextView time;
        View itemView;

        public ViewHolderMe(@NonNull View itemView) {
            super(itemView);

            this.itemView = itemView;
            profile = (ImageView)itemView.findViewById(R.id.chattimg_item_i_profile);
            nicknameView = (TextView)itemView.findViewById(R.id.chattimg_item_i_neme);
            chatView = (TextView)itemView.findViewById(R.id.chattimg_item_i_text);
            time = (TextView)itemView.findViewById(R.id.chatting_item_i_time);

        }

        public void onBind(String chat) {

            StringTokenizer st = new StringTokenizer(chat, "/%%/");
            String key = st.nextToken();
            String nickname = st.nextToken();
            String text = st.nextToken();
            String chat_time = st.nextToken();

            //인덱스 저장
            int index = 0;
            for(int i = 0; i < chattingRoomInfo.customerList.size(); i++){
                if(key.equals(chattingRoomInfo.customerList.get(i))){
                    index = i;
                    break;
                }
            }

            //프로필 이미지 처리
            StorageReference sellerimgRef = storageRef.child("profiles")
                    .child(chattingRoomInfo.customer_images.get(index));
            sellerimgRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {

                    try {

                        Glide.with(itemView)
                                .load(uri)
                                .override(150, 150)
                                .into(profile);

                    } catch (Exception e) {
                        System.out.println("view holder binding 실패");
                    }

                }
            }); //프로필 이미지 처리 끝

            //기본세팅
            nicknameView.setText(nickname);
            chatView.setText(text);
            time.setText(chat_time);

        }
    }

}
