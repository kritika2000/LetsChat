package com.example.wechatclone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_show_image.*

class ShowImageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_image)

        setSupportActionBar(imageToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        imageToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        imageToolbar.setNavigationOnClickListener {
            finish()
        }


        val i = intent

        rcv.text = i.getStringExtra("RcvName")

        FirebaseStorage.getInstance().reference.child("pics/" + i.getStringExtra("Image"))
            .downloadUrl.addOnSuccessListener {
            Picasso.get().load(it)
                .fit().centerInside()
                .into(myImage)

        }
    }
}
