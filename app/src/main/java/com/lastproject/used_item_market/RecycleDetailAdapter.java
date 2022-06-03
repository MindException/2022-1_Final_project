//개발: 이승현

package com.lastproject.used_item_market;

import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class RecycleDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    //이미지 가져올때 비트맵 형태로 액티비티에서 가지고 있을 예정
    //이유는 맨 위에 큰 사진이 리사이클 뷰에서 터치할 때마다 바뀌어야 한다.


    ArrayList<Uri> imgArrayList = new ArrayList<>();
    private RecyclePostAdapter.OnItemClickListener mListener = null;  //클릭 리스너 변수

    RecycleDetailAdapter(ArrayList<Uri> list){
        this.imgArrayList = list;
    }

    public interface OnItemClickListener            //아이템이 눌린
    {
        void onItemClick(View v, int pos);
    }

    public void setOnItemClickListener(RecyclePostAdapter.OnItemClickListener listener)
    {
        this.mListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_item_img, parent, false);
        return new RecycleDetailAdapter.ViewHolderDetailProduct(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((RecycleDetailAdapter.ViewHolderDetailProduct)holder).onBind(imgArrayList.get(position));
    }

    @Override
    public int getItemCount() {
        return imgArrayList.size();
    }



    class ViewHolderDetailProduct extends RecyclerView.ViewHolder{

        View itemview;
        ImageView iv;

        public ViewHolderDetailProduct(@NonNull View itemView) {
            super(itemView);
            this.itemview = itemView;

            //아이템 연결
            iv = (ImageView) itemView.findViewById(R.id.img);
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

        public void onBind(Uri uri){

            Glide.with(itemview)
                    .load(uri)
                    .override(150, 150)
                    .thumbnail(0.1f)
                    .into(iv);

        }

    }

}
