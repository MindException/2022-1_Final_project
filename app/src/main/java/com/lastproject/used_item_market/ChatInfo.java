package com.lastproject.used_item_market;

import java.util.ArrayList;

public class ChatInfo {     //실제 채팅

    //    채팅형식
    //    닉네임/%%/채팅내용/%%/채팅시간
    public ArrayList<String> chatList;
    public String start_time;

    public ChatInfo(){}
    public ChatInfo(String seller_key, String seller_nickname){

        chatList = new ArrayList<String>();
        start_time = Time.nowNewTime();
        String first_chat = seller_key + "/%%/" + seller_nickname + "님이 대화방에 참가하셨습니다."
                                    + "/%%/" + start_time;
        chatList.add(first_chat);

    }

}
