package com.lastproject.used_item_market;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

public class NewProductAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    public List<Product> productArrayList = new ArrayList<>();
    private FirebaseStorage storage;            //이미지 저장소
    private StorageReference storageRef;        //정확한 위치에 파일 저장

    NewProductAdapter(List<Product> productArrayList){    //새로운 쿼리용 생성자

        this.productArrayList = productArrayList;
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

    }
    public NewProductAdapter() {
    }

    //ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ  어뎁터 눌렸울 경우 ㅡㅡㅡㅡㅡㅡㅡㅡㅡ
    public interface onItemClickEventListener            //아이템이 눌린
    {
        void onItemClick(View v, int pos);
    }

    private onItemClickEventListener mItemClickListener = null;  //클릭 리스너 변수

    public void setOnItemClickListener(onItemClickEventListener listener)
    {
        this.mItemClickListener = listener;
    }
    //ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ 여기까지 ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.horizon_rectcler_item, parent, false);

        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((ProductViewHolder)holder).onBind(productArrayList.get(position));
    }

    @Override
    public int getItemCount() {
        return productArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {      //리사이클뷰에 재활용을 막는다.
        return position;
    }

    void addItem(Product data) {      //아이템을 여기다가 집어넣는다.

        productArrayList.add(data);

    }

    class ProductViewHolder extends RecyclerView.ViewHolder {

        View itemview;
        ImageView iv;
        TextView title;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemview = itemView;

            //아이템 연결
            iv = (ImageView) itemView.findViewById(R.id.recyclerHorizon_item);
            iv.setImageDrawable(null);
            title = (TextView) itemView.findViewById(R.id.textImg);
            title.setSelected(true); // 긴 문장 흘러서 보여줌.

            itemView.setOnClickListener(new View.OnClickListener() {            //클릭은 여기다가 한다.
                @Override
                public void onClick(View v) {

                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION)            //눌렸을 경우
                    {
                        // click event
                        mItemClickListener.onItemClick(v, pos);
                    }

                }
            });
        }

        public void onBind(Product pdt){

            title.setText(pdt.title);

            //이미지 처리
            if(pdt.pictures.size() != 0){       //상품에 사진이 있는 경우

                String firstimg_key = pdt.pictures.get(0);       //첫 번째 사진만 가져온다.
                StorageReference getRef = storageRef.child("images")
                        .child(pdt.key)
                        .child(firstimg_key);
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
                            System.out.println("view holder binding 실패");
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        System.out.println("상품 사진 통신 실패");

                    }
                });
            }//if문 끝
        }

    }
}
