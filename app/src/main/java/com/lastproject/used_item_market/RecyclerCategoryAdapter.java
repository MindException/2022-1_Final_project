package com.lastproject.used_item_market;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerCategoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    ArrayList<String> categoryList = new ArrayList<>();
    OnItemClickListener listener = null;


    RecyclerCategoryAdapter(ArrayList<String> categoryList){
        this.categoryList = categoryList;
    }

    public void setOnItemClickListener(OnItemClickListener listener)
    {
        this.listener = listener;
    }

    public interface OnItemClickListener            //아이템이 눌린
    {
        void onItemClick(View v, int pos, TextView textView);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_item, parent, false);


        return new RecyclerCategoryAdapter.ViewHolderCategory(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((RecyclerCategoryAdapter.ViewHolderCategory)holder).onBind(categoryList.get(position));
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    @Override
    public int getItemViewType(int position) {      //리사이클뷰에 재활용을 막는다.
        return position;
    }

    class ViewHolderCategory extends RecyclerView.ViewHolder{

        TextView category;

        public ViewHolderCategory(@NonNull View itemView) {
            super(itemView);
            category = (TextView)itemView.findViewById(R.id.category_item);

            itemView.setOnClickListener(new View.OnClickListener() {            //클릭은 여기다가 한다.
                @Override
                public void onClick(View v) {

                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION)            //눌렸을 경우
                    {
                        // click event
                        listener.onItemClick(v, pos, category);
                    }

                }
            });

        }

        public void onBind(String text){

            category.setText(text);

        }
    }

}
