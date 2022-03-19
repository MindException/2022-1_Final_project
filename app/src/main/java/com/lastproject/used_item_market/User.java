package com.lastproject.used_item_market;

public class User {         //사용자 테이블이다.

    public String google_email;             //인증 받은 구글 이메일
    public String password;                 //비밀번홓
    public String nickname;                 //별명
    public String univetsity;               //소속한 대학
    public String img;                      //이진 파일로 이미지가 들어가 있다.


    User(){}        //기본 생성자

    User(String google_email, String password, String nickname){

        this.google_email = google_email;
        this.password = password;
        this.nickname = nickname;

    }






}
