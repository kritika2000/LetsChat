package com.example.wechatclone

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_my_profile.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.IOException

class MyProfileActivity : AppCompatActivity() {

    val dbRef=FirebaseDatabase.getInstance().reference

    private val GALLERY = 1

    val bytes = ByteArrayOutputStream()
    lateinit var imageUri:Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)
        Ptoolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
         Log.i("Id On Execution",dbRef.child("LetsChatUsers").key.toString())
        Ptoolbar.setNavigationOnClickListener {
            finish()
        }
        val userKey=FirebaseAuth.getInstance().currentUser?.uid

        Log.i("Current user",userKey.toString())


        dbRef.child("LetsChatUsers").child(userKey.toString()).addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(p0: DataSnapshot) {

                Log.i("Current User details", p0.child(userKey.toString()).toString())

                profileName.text = p0.child("profileName").value.toString()
                profilePhone.text = p0.child("phoneNumber").value.toString()

                val storageRef=FirebaseStorage.getInstance().reference.child("pics/" +
                        "${FirebaseAuth.getInstance().currentUser?.uid}"
                )

                Log.i("Storage",storageRef.toString())

                storageRef.downloadUrl.addOnSuccessListener {
                    Picasso.get()
                        .load(it)
                        .fit()
                        .centerInside()
                        .into(viewImage)
                    Log.i("Imageuri",it.toString())
                }.addOnFailureListener {
                    Toast.makeText(this@MyProfileActivity,"Image can't be loaded",Toast.LENGTH_SHORT).show()
                }
            }

        })

        viewImage.setOnClickListener {

            choosePhotoFromGallary()
        }

    }


    private fun RemovePhoto() {
        viewImage.setImageResource(R.drawable.default_profile_photo)
    }

    fun choosePhotoFromGallary() {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, GALLERY)
    }


    public override fun onActivityResult(requestCode:Int, resultCode:Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GALLERY&&resultCode== Activity.RESULT_OK)
        {
            if (data != null)
            {
                val contentURI = data.data
                try
                {
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)
                    val path = saveImage(bitmap)
                    Toast.makeText(this, "Image Saved!", Toast.LENGTH_SHORT).show()

                }
                catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT).show()
                }

            }

        }

    }

    fun saveImage(myBitmap: Bitmap) {

        myBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        viewImage.setImageBitmap(myBitmap)

        val storageRef=FirebaseStorage.getInstance().reference.child("pics/${FirebaseAuth.getInstance().currentUser?.uid}")
        val image=bytes.toByteArray()
        val upload=storageRef.putBytes(image)
        upload.addOnCompleteListener {

            Log.i("Storage",storageRef.toString())

            if(it.isSuccessful){
                storageRef.downloadUrl.addOnCompleteListener {
                    it.result?.let {
                        imageUri=it
                        Log.i("Imageuri",imageUri.toString())
                    }
                }
            }
        }
    }
}
