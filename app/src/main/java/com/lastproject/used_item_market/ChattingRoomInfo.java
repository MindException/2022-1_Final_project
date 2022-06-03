//개발: 이승현

package com.lastproject.used_item_market;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

public class ChattingRoomInfo {

    //무조건 0번 인덱스는 판매자 본인으로 해서 만들기
    //자신이 속한 채팅방 찾을 경우 메서드는 .whereIn을 사용하면 된다.
    public List<String> customerList;               //사용자들의 키값을 가지고 있는다.
    public List<String> customer_nicknames;         //사용자들의 닉네임 값을 가지고 있는다.
    public List<String> customer_images;            //사용자들의 이미지 키값을 가지고 있는다.
    public List<Integer> out_customer_index;        //나간 사용자의 인덱스 번호를 기억한다.(안나가면 0번)
    public String start_time;                       //채팅의 처음 시작 시간을 정한다.
    public String last_time;                        //채팅의 마지막 시간을 저장한다.
    public String chat_key;                         //Realtime-Database에서의 키값을 가지고 있는다.
    public String product_imgkey;                   //상품 이미지
    public List<Integer> last_SEE;                  //사용자들이 마지막으로 읽은 채팅의 인덱스번호를 저장한다.
    public int last_index;                          //제일 마지막으로 보낸 채팅의 행을 저장
    public String last_text;                        //제일 마지막으로 보낸 문자
    public String title;                            //상품 제목

    //상품이 삭제된 경우
    //last_text를 물품 삭제 기준으로 삼는다.
    //앞에 오는 "System/%%/상품삭제/%%/상품이 삭제되었습니다." 이걸로 조건문을 줄 수 있다.


    //채팅방 만들때 주의할 점!!
    //프로필 사진 불러올 경우에 이미지가 있으면 onSuccessListener에서 코딩하고
    //실패한 경우에는 onFailureListener에서 작업하는 방식으로 코딩한다.

    //채팅 읽은 위치 만드는 방법
    //리사이클뷰에서 스크롤 인덱스 번호를 가져온다. - SellPage 참조
    //인덱스 번호를 최대값으로 저장한다.
    //last_index - last_SEE 값으로 몇 개의 채팅을 안 읽었는지 계산한다
    //마지막 읽은 위치는 채팅방에서 나갈때 저장하도록 한다.

    //상품이 삭제되거나 거래가 종료된 경우 key 값에 다른 것을 추가한다.
    // 거래가 완료된 경우:    System/*!%@#!*/success/1team
    // 삭제된 경우:          System/*!%@#!*/delete/1team




    public ChattingRoomInfo(){}

    public ChattingRoomInfo(String seller_key, String nickname, String time, String title){

        customerList = new ArrayList<String>();
        customer_nicknames = new ArrayList<String>();
        customer_images = new ArrayList<String>();
        customerList.add(seller_key);
        customer_nicknames.add(nickname);
        start_time = time;
        last_time = time;
        last_SEE = new ArrayList<Integer>();
        this.title = title;
        out_customer_index = new ArrayList<Integer>();

    }


}
