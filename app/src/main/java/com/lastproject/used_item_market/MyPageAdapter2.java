package com.lastproject.used_item_market;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class MyPageAdapter2 extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    public List<Product> productArrayList = new ArrayList<>();
    private FirebaseStorage storage;            //이미지 저장소
    private StorageReference storageRef;        //정확한 위치에 파일 저장

    //클릭 리스너
    public interface onImgEventListener {
        void onItemClick(View v, int pos);
    }
    private onImgEventListener mImgClickListener;

    public void setOnImgClickListener(onImgEventListener listener){
        this.mImgClickListener = listener;
    }

    //생성자
    public MyPageAdapter2() { }

    MyPageAdapter2(List<Product> productArrayList){    //새로운 쿼리용 생성자

        this.productArrayList = productArrayList;
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mypage_list_item2, parent, false);
        return new MyPageAdapter2.MyPageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((MyPageAdapter2.MyPageViewHolder)holder).onBind(productArrayList.get(position));
    }

    @Override
    public int getItemCount() {
        return productArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {      //리사이클뷰에 재활용을 막는다.
        return position;
    }

    //뷰홀더
    class MyPageViewHolder extends RecyclerView.ViewHolder {

        View itemview;
        ImageView iv;
        TextView title;
        TextView price;
        TextView date;

        public MyPageViewHolder(@NonNull View itemView) {
            super(itemView);

            this.itemview = itemView;

            //아이템 연결
            iv = (ImageView) itemView.findViewById(R.id.mypage_list_img);
            iv.setOnClickListener(new View.OnClickListener() {     //팝업 메뉴 리스너
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if(pos != RecyclerView.NO_POSITION){
                        if(mImgClickListener != null){
                            mImgClickListener.onItemClick(v, pos);
                        }
                    }
                }
            });
            title = (TextView) itemView.findViewById(R.id.mypage_list_title);
            title.setSelected(true); // 긴 문장 흘러서 보여줌.
            price = (TextView) itemView.findViewById(R.id.mypage_list_price);
            date = (TextView) itemView.findViewById(R.id.mypage_list_date);

        }

        public void onBind(Product pdt){
            title.setText(pdt.title);
            price.setText(Long.toString(pdt.cost) + "원");
            date.setText(Time.productTime(pdt.time));

            String path = "images/"+pdt.key+"/"+pdt.pictures.get(0).toString();
            StorageReference imgRef = storageRef.child(path);
            imgRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {

                    try {

                        Glide.with(itemview)
                                .load(uri)
                                .override(150, 150)
                                .into(iv);

                    }catch (Exception e){
                        System.out.println("view holder binding 실패");
                    }


                }
            });

        }

    }


}
