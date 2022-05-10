package com.lastproject.used_item_market;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

public class ChattingRoomInfo {

    //무조건 0번 인덱스는 판매자 본인으로 해서 만들기
    //자신이 속한 채팅방 찾을 경우 메서드는 .whereIn을 사용하면 된다.
    public List<String> customerList;               //사용자들의 키값을 가지고 있는다.
    public List<String> customer_nicknames;          //사용자들의 닉네임 값을 가지고 있는다.
    public List<String> customer_images;            //사용자들의 이미지 키값을 가지고 있는다.
    public List<Integer> out_customer_index;        //나간 사용자의 인덱스 번호를 기억한다.
    public String start_time;                       //채팅의 처음 시작 시간을 정한다.
    public String last_time;                        //채팅의 마지막 시간을 저장한다.
    public String chat_key;                         //Raaltime-Database에서의 키갑을 가지고 있는다.

    //채팅방 만들때 주의할 점!!
    //프로필 사진 불러올 경우에 이미지가 있으면 onSuccessListener에서 코딩하고
    //실패한 경우에는 onFailureListener에서 작업하는 방식으로 코딩한다.


    public ChattingRoomInfo(){}

    public ChattingRoomInfo(String seller_key, String nickname, String time){

        customerList = new ArrayList<String>();
        customer_nicknames = new ArrayList<String>();
        customer_images = new ArrayList<String>();
        customerList.add(seller_key);
        customer_nicknames.add(nickname);
        start_time = time;

    }


}
