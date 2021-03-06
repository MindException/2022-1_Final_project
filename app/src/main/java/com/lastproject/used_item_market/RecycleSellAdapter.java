//개발: 이승현

package com.lastproject.used_item_market;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RecycleSellAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public List<Product> productArrayList = new ArrayList<>();
    private FirebaseStorage storage;            //이미지 저장소
    private StorageReference storageRef;        //정확한 위치에 파일 저장

    RecycleSellAdapter(){
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
    }

    RecycleSellAdapter(List<Product> productArrayList){    //새로운 쿼리용 생성자

        this.productArrayList = productArrayList;
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

    }

    //ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ  어뎁터 눌렸울 경우 ㅡㅡㅡㅡㅡㅡㅡㅡㅡ
    public interface OnItemClickListener            //아이템이 눌린
    {
        void onItemClick(View v, int pos);
    }

    private OnItemClickListener mListener = null;  //클릭 리스너 변수

    public void setOnItemClickListener(OnItemClickListener listener)
    {
        this.mListener = listener;
    }
    //ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ 여기까지 ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { // 아이템 뷰를 위한 뷰 객체 생성 후 리턴
        //View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.market_list_itme, parent, false);
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.market_list_itme, parent, false);
        ViewHolderSellProduct viewHolderSellProduct = new ViewHolderSellProduct(view);

        return viewHolderSellProduct;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        ((ViewHolderSellProduct)holder).onBind(productArrayList.get(position), position);
    }

    @Override
    public int getItemCount() {
        return productArrayList.size();
    }

    void addItem(Product data) {      //아이템을 여기다가 집어넣는다.

        productArrayList.add(data);

    }

    //이게 매우 중요 이거 없으면 다 날라간다.

    @Override
    public int getItemViewType(int position) {      //리사이클뷰에 재활용을 막는다.
        return position;
    }

    //이미지 먼저 불러오기
    void preload(Context context, Uri uri){     //이미지를 먼저 불러온다.


    }


    //아이템 저장하는 클래스
    class ViewHolderSellProduct extends RecyclerView.ViewHolder{

        View itemview;

        ImageView iv;
        TextView title;
        TextView price;
        TextView date;
        //ContentResolver cr;

        public ViewHolderSellProduct(@NonNull View itemView) {

            super(itemView);
            this.itemview = itemView;
            //cr = itemView.getContext().getContentResolver();

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

            //아이템 연결
            iv = (ImageView) itemView.findViewById(R.id.market_list_img);
            iv.setImageDrawable(null);
            title = (TextView) itemView.findViewById(R.id.market_list_title);
            title.setSelected(true); // 긴 문장 흘러서 보여줌.
            price = (TextView) itemView.findViewById(R.id.market_list_price);
            price.setSelected(true); // 긴 문장 흘러서 보여줌.
            date = (TextView) itemView.findViewById(R.id.market_list_date);

        }

        public void onBind(Product pdt , int pos){

            title.setText(pdt.title);
            price.setText(Long.toString(pdt.cost) + "원");
            date.setText(Time.productTime(pdt.time));

            //이미지 처리
            if(pdt.pictures.size() != 0){       //상품에 사진이 있는 경우

                String firstimg_key = pdt.pictures.get(0);       //첫 번째 사진만 가져온다.
                StorageReference getRef = storageRef.child("images")
                        .child(pdt.key).child(firstimg_key);
                getRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {     //사진을 가져온 경우
                    @Override
                    public void onSuccess(Uri uri) {

                        try {

                            Glide.with(itemview)
                                    .load(uri)
                                    .override(150, 150)
                                    .thumbnail(0.1f)
                                    .into(iv);

                        }catch (Exception e){

                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
            }//if문 끝








        }
    }

}
