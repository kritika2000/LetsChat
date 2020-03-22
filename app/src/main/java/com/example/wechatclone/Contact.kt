package com.example.wechatclone

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

@SuppressLint("ParcelCreator")
data class Contact(
    val contactName:String,
    val contactNumber:String,
    val contactImage:String
) : Serializable