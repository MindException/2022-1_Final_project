//개발: 이승현

package com.lastproject.used_item_market;

public class User {         //사용자 테이블이다.

    public String google_email;             //인증 받은 구글 이메일
    public String password;                 //비밀번호
    public String nickname;                 //별명
    public String university;               //소속한 대학
    public String img;                      //이진 파일로 이미지가 들어가 있다.

    //유저의 이미지는 profile 테이블에 만들어 놓을 예정
    //키 값은 사용자의 키값과 동일하게 만들고 수정을 통하여 이미지 정보만 계속 바꿀 예정이다. <- 이렇게 해야 채팅에서 이미지를 좀 더
    //쉽게 가져다가 쓸 수 있다.


    User(){}        //기본 생성자

    User(String google_email, String password, String nickname, String univetsity){

        this.google_email = google_email;
        this.password = password;
        this.nickname = nickname;
        this.university = univetsity;

    }






}
