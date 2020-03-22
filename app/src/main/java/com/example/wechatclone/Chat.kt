package com.example.wechatclone

data class Chat(
    val message:String,
    val recieverId:String,
    val senderId:String,
    val recieverName:String,
    val recieverContact:String,
    val lastTime : String,
    val lastDay:String,
    val lastMon:String,
    val lastYear:String
)