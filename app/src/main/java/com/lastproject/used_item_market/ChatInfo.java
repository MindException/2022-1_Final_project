//개발: 이승현

package com.lastproject.used_item_market;

import java.util.ArrayList;
import java.util.List;

public class ChatInfo {     //실제 채팅

    //    채팅형식
    //    나의 키값/%%/닉네임/%%/채팅내용/%%/채팅시간
    public List<String> chatList = null;
    public String start_time = null;

    public ChatInfo(){}
    public ChatInfo(String seller_nickname){

        chatList = new ArrayList<String>();
        start_time = Time.nowNewTime();
        String first_chat = "System" + "/%%/" + seller_nickname + "님이 대화방에 참가하셨습니다."
                                    + "/%%/" + start_time + "/%%/";
        chatList.add(first_chat);

    }

}
