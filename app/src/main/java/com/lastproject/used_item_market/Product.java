//개발: 이승현

package com.lastproject.used_item_market;

import java.util.List;

//실제 서버에 저장되는 중고물품이다.
public class Product {

    public String key = "";             //상품 키
    public String seller = "";          //판매자 키
    public String seller_key = "";      //판매자 이름
    public String university = "";      //대학 이름
    public String title = "";           //게시글 제목
    public Long cost;                    //가격
    public String purpose;              //거래 목적 (판매와 무료나눔만 존재)
    public String category;             //카테고리
    public String destination_longtitude;   //거래 장소 경도
    public String destination_latitude;     //거래 장소 위도
    public String text;                 //내용
    public String time;                 //작성시간
    public String purchaser_key;        //구매자 키
    public String purchaser;            //구매자 이름
    //사진 이미지 저장
    public List<String> pictures;        //사진들(저장된 이름들 저장)
    //추후 거래 완료 시
    public String success_time = "000000000000";          //기본 값 0000년 00월 00일 00시 00분
    // 여기가 999999999999 가들어가면 사용자가 삭제한 데이터 값이다.

    public Product(){}

    //작성 포멧멧
   public Product(String seller, String seller_key, String university, String title, Long cost,
                   String purpose, String category, String text, String time){

        this.seller = seller;
        this.seller_key = seller_key;
        this.university = university;
        this.title = title;
        this.cost = cost;
        this.purpose = purpose;
        this.category = category;
        this.text = text;
        this.time = time;

    }

    //기본 입력 판매자, 판매자키, 대학
    public Product(String seller, String seller_key, String university){

        this.seller = seller;
        this.seller_key = seller_key;
        this.university = university;

    }

}
