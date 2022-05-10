package com.lastproject.used_item_market;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RecyclerChatListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private List<ChattingRoomInfo> chattingRoomInfoList = new ArrayList<ChattingRoomInfo>();
    private RecyclePostAdapter.OnItemClickListener mListener = null;  //클릭 리스너 변수

    public interface OnItemClickListener            //아이템이 눌린
    {
        void onItemClick(View v, int pos);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }






}
