package com.lastproject.used_item_market;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class RecyclePostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    //상품 이미지(Uri)를 가져온다.
    private ArrayList<Uri> productArrayList = new ArrayList<Uri>();
    private OnItemClickListener mListener = null;  //클릭 리스너 변수

    public interface OnItemClickListener            //아이템이 눌린
    {
        void onItemClick(View v, int pos);
    }

    public void setOnItemClickListener(OnItemClickListener listener)
    {
        this.mListener = listener;
    }

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

    void addItem(Uri data) {      //아이템을 여기다가 집어넣는다.

        productArrayList.add(data);

    }


    class ViewHolderPostProduct extends RecyclerView.ViewHolder{

        ImageView iv;
        ContentResolver cr;

        public ViewHolderPostProduct(@NonNull View itemView) {
            super(itemView);

            //아이템 연결
            iv = (ImageView) itemView.findViewById(R.id.img);
            cr = itemView.getContext().getContentResolver();

            itemView.setOnClickListener(new View.OnClickListener() {            //클릭은 여기다가 한다.
                @Override
                public void onClick(View v) {

                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION)            //눌렸을 경우
                    {
                        // click event
                        mListener.onItemClick(v, pos);
                    }

                }
            });


        }

        //실제 데이터 추가
       public void onBind(Uri data){

            try {

                Bitmap bmp = MediaStore.Images.Media.getBitmap(cr, data);
                iv.setImageBitmap(bmp);
                iv.setClipToOutline(true);              //모양에 맞게 사진 자르기

            }catch (Exception e){
                System.out.println("view holder binding 실패");
            }

        }
    }


}
