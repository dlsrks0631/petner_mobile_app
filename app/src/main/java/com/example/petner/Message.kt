package com.example.petner

import android.util.Log

data class Message(
    var message: String?, //message를 담을 변수
    var sendId: String?,   //아이디를 담을 uid 변수
    var otherName: String?,
    var myName: String?
){
    constructor():this("","","", "") {
       Log.d("mess" ,"message item : "  +message + " " + sendId + otherName + myName)
    }
//기본 생성자
}
