package com.lastproject.used_item_market;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RecyclerChatListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private List<ChattingRoomInfo> chattingRoomInfoList = new ArrayList<ChattingRoomInfo>();
    private RecyclerChatListAdapter.OnItemClickListener rcListener = null;  //클릭 리스너 변수

    public interface OnItemClickListener            //아이템이 눌린
    {
        void onItemClick(View v, int pos);
    }

    public void setOnItemClickListener(RecyclerChatListAdapter.OnItemClickListener rclistener)
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
        viewHolderChatRoom.setIsRecyclable(false);       //글라이드 상황에 따라 넣기
        return viewHolderChatRoom;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((RecyclerChatListAdapter.ViewHolderChatRoom)holder).onBind(chattingRoomInfoList.get(position));
    }

    @Override
    public int getItemCount() {
        return chattingRoomInfoList.size();
    }

    //뷰 홀더더
   class ViewHolderChatRoom extends RecyclerView.ViewHolder{

        public ViewHolderChatRoom(@NonNull View itemView) {
            super(itemView);

        }

        public void onBind(ChattingRoomInfo chattingRoomInfo){

        }

    }
}

/*
chattimg_home_product_img - 상품 이미지
chattimg_home_user_profile - 유저 프로필
chattimg_home_title - 타이틀
chatting_home_peoples - 채팅방 안에 사람수
chatting_home_time - 시간
chattimg_home_lasttext - 마지막 채팅 기록
chatting_home_alarmimg - 빨간 동그라미 이미지 (java 에서 background 변겅 가능 on off 구현 할수 있겠다)
chatting_home_alarmnum - 빨간 동그라미(알림) 안에 숫자
* */
