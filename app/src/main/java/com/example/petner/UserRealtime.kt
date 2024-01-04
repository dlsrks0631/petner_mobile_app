package com.example.petner

data class UserRealtime(
    var name: String,
    var id: String,
    var uId: String
){
    constructor(): this("","","")
}

