package com.lastproject.used_item_market;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class SellPage extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{

    //기본 나의 정보
    String email = "";
    String mykey = "";
    String nickname = "";
    String myUniv = "";

    //파이어베이스
    FirebaseFirestore firestore;
    CollectionReference productRef;

    //상품관련
    private int limit = 7;         //요청 상품 수
    private boolean isScrolling = false;
    private boolean isLastItemReached = false;
    private DocumentSnapshot lastVisible;       //마지막 스냅샷 커서 저장
    RecyclerView recyclerView;
    RecycleSellAdapter recycleSellAdapter;
    RecyclerView.OnScrollListener onScrollListener;
    SwipeRefreshLayout swipeRefreshLayout;
    List<Product> productList = new ArrayList<Product>();      //여기에 모든 상품들이 들어간다.
    ArrayList<String> productKeyList = new ArrayList<String>();

    //카테고리리
    String category = "모두보기";       //기본값

   @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sell_page);

       //DB 생성
       firestore = FirebaseFirestore.getInstance();
       swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.sellpage_swipelayout);

       //기본세팅
       email = getIntent().getStringExtra("email");
       mykey = getIntent().getStringExtra("mykey");
       nickname = getIntent().getStringExtra("nickname");
       myUniv = getIntent().getStringExtra("myUniv");

       //리사이클 뷰 기초세팅
       recyclerView = (RecyclerView)findViewById(R.id.selllist);
       //recyclerView.setItemAnimator(null);
       recyclerView.setLayoutManager(new LinearLayoutManager(this));
       recycleSellAdapter = new RecycleSellAdapter(productList);
       recycleSellAdapter.setOnItemClickListener(new RecycleSellAdapter.OnItemClickListener() {
           @Override
           public void onItemClick(View v, int pos) {  //리사이클 뷰 가 눌렸을 경우 상세 페이지로 이동

               Intent intent = new Intent(SellPage.this, DetailPage.class);
               intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
               intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
               intent.putExtra("email", email);
               intent.putExtra("mykey", mykey);
               intent.putExtra("nickname", nickname);
               intent.putExtra("myUniv", myUniv);
               intent.putExtra("productkey", productKeyList.get(pos));      //리사이클뷰 인덱스 가져옴
               intent.putExtra("wherefrom", "SellPage");
               startActivity(intent);
               System.exit(0);

           }
       });
       recyclerView.setAdapter(recycleSellAdapter);
       swipeRefreshLayout.setOnRefreshListener(this);

       System.out.println( "대학" + myUniv);

       //쿼리 시작
       productRef = firestore.collection("Product");
       Query query = productRef.whereEqualTo("university", myUniv)
               .whereEqualTo("purpose", "판매").orderBy("time", Query.Direction.DESCENDING)
               .limit(limit);
       query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
           @Override
           public void onComplete(@NonNull Task<QuerySnapshot> task) {
               if (task.isSuccessful()){           //가져오는데 성공
                   if (task.getResult().size() <= 0){          //물건이 없는 경우
                       Toast.makeText(SellPage.this, "상품 없음", Toast.LENGTH_SHORT).show();
                   }else{      //물건이 있다.
                       for(DocumentSnapshot document : task.getResult()){
                           Product product = document.toObject(Product.class);
                           productList.add(product);
                           productKeyList.add(document.getId());
                       }
                       //상품 추가했으니 어뎁터 갱신
                       recycleSellAdapter.notifyDataSetChanged();
                       lastVisible = task.getResult().getDocuments().get(task.getResult().size() - 1);

                       //스크롤 리스너 추가
                       onScrollListener = new RecyclerView.OnScrollListener() {
                           @Override
                           public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                               super.onScrollStateChanged(recyclerView, newState);
                               if(newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
                                   isScrolling = true;
                               }
                           }

                           //스크롤이 limit만큼 내려갈 시 다음 데이터를 limit만큼 읽어서 출력
                           @Override
                           public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                               super.onScrolled(recyclerView, dx, dy);

                               //위치정보 가져오기
                               LinearLayoutManager linearLayoutManager = (LinearLayoutManager)recyclerView.getLayoutManager();

                               int firstVisiblePosition = linearLayoutManager.findFirstVisibleItemPosition();
                               int visibleItemCount = linearLayoutManager.getChildCount();
                               int totalItemCount = linearLayoutManager.getItemCount();

                               //스크롤 조건 시작
                               if(isScrolling && (firstVisiblePosition + visibleItemCount == totalItemCount) && !isLastItemReached ){

                                   isScrolling = false;
                                   //추가 쿼리
                                   Query nextQuery = productRef.whereEqualTo("university", myUniv)
                                           .whereEqualTo("purpose", "판매").orderBy("time", Query.Direction.DESCENDING)
                                           .startAfter(lastVisible).limit(limit);
                                   nextQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                       @Override
                                       public void onComplete(@NonNull Task<QuerySnapshot> nextTask) {
                                           if(nextTask.isSuccessful()){   //가져오는거 성공
                                               if(nextTask.getResult().size() > 0) {
                                                   for(DocumentSnapshot nextDocument : nextTask.getResult()){
                                                       Product product = nextDocument.toObject(Product.class);
                                                       productList.add(product);
                                                       productKeyList.add(nextDocument.getId());
                                                   }
                                                   //어뎁터 또 갱신
                                                   recycleSellAdapter.notifyDataSetChanged();
                                                   lastVisible = nextTask.getResult().getDocuments().get(nextTask.getResult().size() - 1);

                                                   if(nextTask.getResult().size() < limit){      //더 이상 갱신할 필요가 없다.
                                                       isLastItemReached = true;
                                                   }
                                               }
                                           }
                                       }
                                   });

                               }//스크롤 조건 끝

                           }
                       };
                       //스크롤 리스너 끝

                       //스크롤 리스너 추가
                       recyclerView.addOnScrollListener(onScrollListener);

                   }
               }//수신 성공
           }
       });//메인 쿼리



        Back();
    }

    void Back(){
        ImageButton back = (ImageButton)findViewById(R.id.xbtn);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SellPage.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("email", email);
                intent.putExtra("mykey", mykey);
                intent.putExtra("nickname", nickname);
                intent.putExtra("myUniv", myUniv);
                startActivity(intent);
                System.exit(0);
            }
        });
    }

    @Override
    public void onRefresh() {

        //초기화
        isScrolling = true;
        isLastItemReached = false;
        productList = new ArrayList<>();
        productKeyList = new ArrayList<String>();
        //어뎁터를 새로 설치해줘야 한다.
        recycleSellAdapter = new RecycleSellAdapter(productList);
        recycleSellAdapter.setOnItemClickListener(new RecycleSellAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int pos) {  //리사이클 뷰 가 눌렸을 경우 상세 페이지로 이동

                Intent intent = new Intent(SellPage.this, DetailPage.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("email", email);
                intent.putExtra("mykey", mykey);
                intent.putExtra("nickname", nickname);
                intent.putExtra("myUniv", myUniv);
                intent.putExtra("productkey", productKeyList.get(pos));      //리사이클뷰 인덱스 가져옴
                startActivity(intent);
                System.exit(0);

            }
        });
        recyclerView.setAdapter(recycleSellAdapter);

        if (category.equals("모두보기")){

            //카테고리가 정해지지 않은 기본보기
            productRef = firestore.collection("Product");
            Query query =productRef.whereEqualTo("university", myUniv)
                    .whereEqualTo("purpose", "판매").orderBy("time", Query.Direction.DESCENDING)
                    .limit(limit);
            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()){           //가져오는데 성공
                        if (task.getResult().size() <= 0){          //물건이 없는 경우
                            Toast.makeText(SellPage.this, "상품 없음", Toast.LENGTH_SHORT).show();
                        }else{      //물건이 있다.
                            for(DocumentSnapshot document : task.getResult()){
                                Product product = document.toObject(Product.class);
                                productList.add(product);
                                productKeyList.add(document.getId());
                            }
                            //상품 추가했으니 어뎁터 갱신
                            recycleSellAdapter.notifyDataSetChanged();
                            lastVisible = task.getResult().getDocuments().get(task.getResult().size() - 1);

                            //스크롤 리스너 추가
                            onScrollListener = new RecyclerView.OnScrollListener() {
                                @Override
                                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                                    super.onScrollStateChanged(recyclerView, newState);
                                    if(newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
                                        isScrolling = true;
                                    }
                                }

                                //스크롤이 limit만큼 내려갈 시 다음 데이터를 limit만큼 읽어서 출력
                                @Override
                                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                                    super.onScrolled(recyclerView, dx, dy);

                                    //위치정보 가져오기
                                    LinearLayoutManager linearLayoutManager = (LinearLayoutManager)recyclerView.getLayoutManager();

                                    int firstVisiblePosition = linearLayoutManager.findFirstVisibleItemPosition();
                                    int visibleItemCount = linearLayoutManager.getChildCount();
                                    int totalItemCount = linearLayoutManager.getItemCount();

                                    //스크롤 조건 시작
                                    if(isScrolling && (firstVisiblePosition + visibleItemCount == totalItemCount) && !isLastItemReached ){

                                        isScrolling = false;
                                        //추가 쿼리
                                        Query nextQuery = productRef.whereEqualTo("university", myUniv)
                                                .whereEqualTo("purpose", "판매").orderBy("time", Query.Direction.DESCENDING)
                                                .startAfter(lastVisible).limit(limit);
                                        nextQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> nextTask) {
                                                if(nextTask.isSuccessful()){   //가져오는거 성공
                                                    if(nextTask.getResult().size() > 0) {
                                                        for(DocumentSnapshot nextDocument : nextTask.getResult()){
                                                            Product product = nextDocument.toObject(Product.class);
                                                            productList.add(product);
                                                            productKeyList.add(nextDocument.getId());
                                                        }
                                                        //어뎁터 또 갱신
                                                        recycleSellAdapter.notifyDataSetChanged();
                                                        lastVisible = nextTask.getResult().getDocuments().get(nextTask.getResult().size() - 1);

                                                        if(nextTask.getResult().size() < limit){      //더 이상 갱신할 필요가 없다.
                                                            isLastItemReached = true;
                                                        }
                                                    }
                                                }
                                            }
                                        });

                                    }//스크롤 조건 끝

                                }
                            };
                            //스크롤 리스너 끝

                            //스크롤 리스너 추가
                            recyclerView.addOnScrollListener(onScrollListener);

                        }
                    }else{  //수신 실패
                        System.out.println("수신 실패");
                    }
                }
            });//메인 쿼리

            swipeRefreshLayout.setRefreshing(false);        //업데이트 끝



        }else{      //카테고리가 선택이 되었을 경우

            productRef = firestore.collection("Product");
            Query query = productRef.whereEqualTo("university", myUniv)
                    .whereEqualTo("purpose", "판매")
                    .whereEqualTo("category", category)
                    .orderBy("time", Query.Direction.DESCENDING).limit(limit);
            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()){           //가져오는데 성공
                        if (task.getResult().size() <= 0){          //물건이 없는 경우
                            Toast.makeText(SellPage.this, "상품 없음", Toast.LENGTH_SHORT).show();
                        }else{      //물건이 있다.
                            for(DocumentSnapshot document : task.getResult()){
                                Product product = document.toObject(Product.class);
                                productList.add(product);
                                productKeyList.add(document.getId());
                            }
                            //상품 추가했으니 어뎁터 갱신
                            recycleSellAdapter.notifyDataSetChanged();
                            lastVisible = task.getResult().getDocuments().get(task.getResult().size() - 1);

                            //스크롤 리스너 추가
                            onScrollListener = new RecyclerView.OnScrollListener() {
                                @Override
                                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                                    super.onScrollStateChanged(recyclerView, newState);
                                    if(newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
                                        isScrolling = true;
                                    }
                                }

                                //스크롤이 limit만큼 내려갈 시 다음 데이터를 limit만큼 읽어서 출력
                                @Override
                                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                                    super.onScrolled(recyclerView, dx, dy);

                                    //위치정보 가져오기
                                    LinearLayoutManager linearLayoutManager = (LinearLayoutManager)recyclerView.getLayoutManager();

                                    int firstVisiblePosition = linearLayoutManager.findFirstVisibleItemPosition();
                                    int visibleItemCount = linearLayoutManager.getChildCount();
                                    int totalItemCount = linearLayoutManager.getItemCount();

                                    //스크롤 조건 시작
                                    if(isScrolling && (firstVisiblePosition + visibleItemCount == totalItemCount) && !isLastItemReached ){

                                        isScrolling = false;
                                        //추가 쿼리
                                        Query nextQuery = productRef.whereEqualTo("university", myUniv)
                                                .whereEqualTo("purpose", "판매")
                                                .whereEqualTo("category", category)
                                                .orderBy("time", Query.Direction.DESCENDING)
                                                .startAfter(lastVisible).limit(limit);
                                        nextQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> nextTask) {
                                                if(nextTask.isSuccessful()){   //가져오는거 성공
                                                    if(nextTask.getResult().size() > 0) {
                                                        for(DocumentSnapshot nextDocument : nextTask.getResult()){
                                                            Product product = nextDocument.toObject(Product.class);
                                                            productList.add(product);
                                                            productKeyList.add(nextDocument.getId());
                                                        }
                                                        //어뎁터 또 갱신
                                                        recycleSellAdapter.notifyDataSetChanged();
                                                        lastVisible = nextTask.getResult().getDocuments().get(nextTask.getResult().size() - 1);

                                                        if(nextTask.getResult().size() < limit){      //더 이상 갱신할 필요가 없다.
                                                            isLastItemReached = true;
                                                        }
                                                    }
                                                }
                                            }
                                        });

                                    }//스크롤 조건 끝

                                }
                            };
                            //스크롤 리스너 끝

                            //스크롤 리스너 추가
                            recyclerView.addOnScrollListener(onScrollListener);

                        }
                    }else{  //수신 실패
                        System.out.println("수신 실패");
                    }
                }
            });//메인 쿼리

            swipeRefreshLayout.setRefreshing(false);        //업데이트 끝

        }//if문 끝

    }
}