//개발: 이승현

package com.lastproject.used_item_market;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class RecyclerChatListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private List<ChattingRoomInfo> chattingRoomInfoList = new ArrayList<ChattingRoomInfo>();
    private RecyclerChatListAdapter.OnItemClickListener rcListener = null;  //클릭 리스너 변수
    public String mykey;

    //이미지 DB
    private FirebaseStorage storage;            //이미지 저장소
    private StorageReference storageRef;        //정확한 위치에 파일 저장

    private int myindex = 0;

    //생성자
    public RecyclerChatListAdapter(){}

    public RecyclerChatListAdapter(ArrayList<ChattingRoomInfo> chattingRoomInfoArrayList, String mykey){
        this.chattingRoomInfoList = chattingRoomInfoArrayList;
        this.mykey = mykey;
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
    }

    public interface OnItemClickListener            //아이템이 눌린
    {
        void onItemClick(View v, int pos);
    }

    public void setOnItemClickListener(OnItemClickListener rclistener)
    {
        this.rcListener = rclistener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.chatting_home_itme, parent, false);
        RecyclerChatListAdapter.ViewHolderChatRoom viewHolderChatRoom = new RecyclerChatListAdapter.ViewHolderChatRoom(view);

        return viewHolderChatRoom;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        //자신이 나간 채팅방은 구현하지 않는다.

        ChattingRoomInfo chattingRoomInfo = chattingRoomInfoList.get(position);
        //나의 인덱스 번호 구하기
        for(int i = 0; i < chattingRoomInfo.customerList.size(); i++){
            if(chattingRoomInfo.customerList.get(i).equals(mykey)){
                myindex = i;
                break;
            }
        }

        if(chattingRoomInfo.out_customer_index.get(myindex) == 0){       //채팅방을 나가지 않은 경우

            ((RecyclerChatListAdapter.ViewHolderChatRoom)holder).onBind(chattingRoomInfo);

        }
        //채팅



    }

    @Override
    public int getItemCount() {
        return chattingRoomInfoList.size();
    }

    @Override
    public int getItemViewType(int position) {      //리사이클뷰에 재활용을 막는다.
        return position;
    }

    //뷰 홀더더
   class ViewHolderChatRoom extends RecyclerView.ViewHolder{

        View itemview;
        ImageView product_img;
        ImageView profile;
        TextView chattingRoom_counts;
        TextView title;
        TextView time;
        TextView last_text;
        LinearLayout back_red_circle;       //알림 뒤에 빨간색
        TextView alramnum;

        public ViewHolderChatRoom(@NonNull View itemView) {
            super(itemView);
            this.itemview = itemView;

            itemView.setOnClickListener(new View.OnClickListener() {            //클릭은 여기다가 한다.
                @Override
                public void onClick(View v) {

                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION)            //눌렸을 경우
                    {
                        // click event
                        rcListener.onItemClick(v, pos);
                    }

                }
            });

            product_img = (ImageView)itemView.findViewById(R.id.chatting_home_product_img);
            product_img.setImageDrawable(null);
            profile = (ImageView)itemView.findViewById(R.id.chattimg_home_user_profile);
            profile.setImageDrawable(null);
            title = (TextView)itemView.findViewById(R.id.chattimg_home_title);
            title.setSelected(true);
            chattingRoom_counts = (TextView)itemView.findViewById(R.id.chatting_home_peoples);
            time = (TextView)itemView.findViewById(R.id.chatting_home_time);
            last_text = (TextView)itemView.findViewById(R.id.chattimg_home_lasttext);
            back_red_circle = (LinearLayout)itemView.findViewById(R.id.chatting_home_alarmimg);
            alramnum = (TextView)itemView.findViewById(R.id.chatting_home_alarmnum);

        }

        public void onBind(ChattingRoomInfo chattingRoomInfo) {

            //상품 이미지 처리
            if (chattingRoomInfo.product_imgkey != null) {       //상품에 사진이 있는 경우

                StorageReference getRef = storageRef.child("images")
                        .child(chattingRoomInfo.chat_key).child(chattingRoomInfo.product_imgkey);
                getRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {     //사진을 가져온 경우
                    @Override
                    public void onSuccess(Uri uri) {

                        try {

                            Glide.with(itemview)
                                    .load(uri)
                                    .override(150, 150)
                                    .thumbnail(0.1f)
                                    .into(product_img);

                        } catch (Exception e) {
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
            }//상품 이미지 처리 끝


            //프로필 이미지 처리
            StorageReference sellerimgRef = storageRef.child("profiles")
                    .child(chattingRoomInfo.customer_images.get(myindex));
            sellerimgRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {

                    try {

                        Glide.with(itemview)
                                .load(uri)
                                .override(150, 150)
                                .into(profile);

                    } catch (Exception e) {
                    }

                }
            }); //프로필 이미지 처리 끝

            //타이틀
            title.setText(chattingRoomInfo.title);
            //채팅방 인원수
            int count = 0;

            for(int i = 0; i < chattingRoomInfo.customerList.size(); i++){

                if(chattingRoomInfo.out_customer_index.get(i) == 0){            //나가지 않은 인원은 0으로 설정되어 있다.
                    ++count;
                }
            }
            chattingRoom_counts.setText(Integer.toString(count));
            //채팅 마지막 시간
            time.setText(Time.chatTime(chattingRoomInfo.last_time));
            //채팅 마지막 기록
            last_text.setText(chattingRoomInfo.last_text);

            if(chattingRoomInfo.last_text != null){

                String last_chat = chattingRoomInfo.last_text;
                StringTokenizer st = new StringTokenizer(last_chat, "/%%/");
                String temp = st.nextToken();
                if(temp.equals("System")){
                    //시스템이 말함
                    temp = st.nextToken();
                    last_text.setText(temp);
                }else{
                    //사용자가 말함
                    temp = st.nextToken();
                    temp = st.nextToken();
                    last_text.setText(temp);
                }

            }

            //빨간 원 && 안 읽은 메시지
            if(chattingRoomInfo.last_SEE.get(myindex) != chattingRoomInfo.last_index){      //확인하지 않은 채팅이 있는 경우

                int nonSEE = chattingRoomInfo.last_index - chattingRoomInfo.last_SEE.get(myindex);
                //map_btn.setBackgroundDrawable(getResources().getDrawable(R.drawable.bt_bg_blue));
                back_red_circle.setBackground(itemview.getContext().getResources().getDrawable(R.drawable.ci_chattimg_alarm));
                alramnum.setText(Integer.toString(nonSEE));

            }else{      //다 읽은 경우
                back_red_circle.setBackground(new ColorDrawable(Color.TRANSPARENT));        //배경(빨간 원) 지우기
                alramnum.setText("");
            }

        }//onBind 끝
    }
}

/*
c우attimg_home_product_img - 상품 이미지
chattimg_home_user_profile - 유저 프로필
chattimg_home_title - 타이틀
chatting_home_peoples - 채팅방 안에 사람수
chatting_home_time - 시간
chattimg_home_lasttext - 마지막 채팅 기록
chatting_home_alarmimg - 빨간 동그라미 이미지 (java 에서 background 변겅 가능 on off 구현 할수 있겠다)
chatting_home_alarmnum - 빨간 동그라미(알림) 안에 숫자
* */
