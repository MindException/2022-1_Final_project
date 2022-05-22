package com.lastproject.used_item_market;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerCategoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    ArrayList<String> categoryList = new ArrayList<>();


    RecyclerCategoryAdapter(ArrayList<String> categoryList){
        this.categoryList = categoryList;
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

    class ViewHolderCategory extends RecyclerView.ViewHolder{

        TextView category;

        public ViewHolderCategory(@NonNull View itemView) {
            super(itemView);
            category = (TextView)itemView.findViewById(R.id.category_item);

        }

        public void onBind(String text){
            category.setText(text);
        }
    }

}
