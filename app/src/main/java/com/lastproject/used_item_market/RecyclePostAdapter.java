package com.lastproject.used_item_market;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclePostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    //상품 이미지(이진 스트링)를 가져온다.
    private ArrayList<String> productArrayList = new ArrayList<String>();


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_item_img, parent, false);
        return new ViewHolderPostProduct(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        ((ViewHolderPostProduct)holder).onBind(productArrayList.get(position));

    }

    @Override
    public int getItemCount() {
        return productArrayList.size();
    }

    void addItem(String data) {      //아이템을 여기다가 집어넣는다.

        productArrayList.add(data);

    }


    class ViewHolderPostProduct extends RecyclerView.ViewHolder{

        ImageView iv;

        public ViewHolderPostProduct(@NonNull View itemView) {
            super(itemView);
            //아이템 연결
            iv = (ImageView) itemView.findViewById(R.id.img);

        }

        //실제 데이터 추가
       public void onBind(String img){

            Bitmap bmp;
            byte[] bytes = ParseIMG.binaryStringToByteArray(img);
            bmp = BitmapFactory.decodeByteArray(bytes, 0 , bytes.length);
            iv.setImageBitmap(bmp);
            iv.setClipToOutline(true);              //모양에 맞게 사진 자르기

        }
    }






}
