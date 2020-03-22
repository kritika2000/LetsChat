package com.example.wechatclone

import android.net.Uri

data class User(
    val profileName:String,
    val phoneNumber:String,
    val password:String,
    val status:Boolean,
    val profileImage:String
)