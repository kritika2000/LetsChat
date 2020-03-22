package com.example.wechatclone

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_create_profile.*
import kotlinx.android.synthetic.main.activity_create_profile.view.*
import kotlinx.android.synthetic.main.activity_create_profile.*
import kotlinx.android.synthetic.main.activity_create_profile.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap
var Ans:Boolean=false
class CreateProfileActivity : AppCompatActivity() {
    override fun onStart() {
        super.onStart()
        if(FirebaseAuth.getInstance().currentUser!=null)
            finish()
    }
    private val GALLERY = 1
    private val REQUEST_CODE = 2

    lateinit var pictureDialog:AlertDialog.Builder
    val bytes = ByteArrayOutputStream()
    val auth by lazy { FirebaseAuth.getInstance() }

    var users=HashMap<String,User>()

    lateinit var storedVerificationId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_profile)

        save.setOnClickListener {
            if(profile_name.text.toString().isEmpty())
                tilName.profile_name.setError("This field can't be empty")
            if(phoneNo.text.toString().length<10)
                tilPhone.phoneNo.setError("This field can't be empty")
            if(profile_password.text.toString().length<8)
                tilpassWord.profile_password.setError("Password must contains atleast 8 characters")
            if(profile_name.text.toString().isNotEmpty()
                &&phoneNo.text.toString().length==10
                &&profile_password.text.toString().length>=8) {
                AccountExists()
            }

        }
        if(!profile_name.text.toString().isEmpty())
            tilName.profile_name.setError(null)
        if(phoneNo.text.toString().isNotEmpty())
            tilPhone.phoneNo.setError(null)
    }

    private fun AccountExists() {

        val dbRef= DatabaseUtil.getDatabase()
        dbRef.reference.child("LetsChatUsers").orderByChild("phoneNumber").equalTo(phoneNo.text.toString()).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                if (!p0.exists()){
                    Ans=true
                    Submit(save)
                }
                else {
                    Ans = false
                    root.showSnack("This number is already in use")
                }

            }

        })
    }

    private fun showPictureDialog() {
        pictureDialog = AlertDialog.Builder(this)
        pictureDialog.setTitle("Profile photo")
        val pictureDialogItems = arrayOf("Gallery","Remove photo")
        pictureDialog.setSingleChoiceItems(pictureDialogItems,-1,
            DialogInterface.OnClickListener { dialog, which ->
                when (which) {
                    0 -> choosePhotoFromGallary()
                    1 -> RemovePhoto()
                }
            })
        pictureDialog.show()
    }
    //Remove photo
    private fun RemovePhoto() {
        profile_image.setImageResource(R.drawable.default_profile_photo)
    }
    // Gallery
    fun choosePhotoFromGallary() {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galleryIntent, GALLERY)
            pictureDialog.create().dismiss()

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
        profile_image.setImageBitmap(myBitmap)

    }

    companion object {
        private val IMAGE_DIRECTORY = "/demonuts"
    }
    fun SelectImage(view: View) {
        showPictureDialog()
    }

    fun Submit(view: View) {
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signInWithPhoneAuthCredential(credential)
            }
            override fun onVerificationFailed(e: FirebaseException) {
                if (e is FirebaseAuthInvalidCredentialsException) {
                    root.showSnack("${e.localizedMessage}")
                } else if (e is FirebaseTooManyRequestsException) {
                    root.showSnack("${e.localizedMessage}")
                }
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                storedVerificationId = verificationId
                var resendToken = token
            }
        }
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            "+91" + phoneNo.text.toString(), // Phone number to verify
            60, // Timeout duration
            TimeUnit.SECONDS, // Unit of timeout
            this, // Activity (for callback binding)
            callbacks
        ) // OnVerificationStateChangedCallbacks
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {

        var imageUri:Uri? = null

        auth.signInWithCredential(credential)

            .addOnCompleteListener(this) { task ->
                Log.d("TAG", "signInWithCredential:success")
                val snack= Snackbar.make(root,"Account Created Succesfully", Snackbar.LENGTH_LONG)
                val textView = snack.view.findViewById(R.id.snackbar_text) as TextView
                textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_circle_black_24dp, 0, 0, 0);
                textView.gravity=Gravity.CENTER
                val storageRef=FirebaseStorage.getInstance().reference.child("pics/${FirebaseAuth.getInstance().currentUser?.uid}")
                val image=bytes.toByteArray()
                val upload=storageRef.putBytes(image)
                upload.addOnCompleteListener {
                    if(it.isSuccessful){
                        storageRef.downloadUrl.addOnCompleteListener {
                            it.result?.let {
                                imageUri=it
                            }
                        }
                    }
                }
                snack.show()

                val dbRef= DatabaseUtil.getDatabase()

                    dbRef.reference.child("LetsChatUsers").child(FirebaseAuth.getInstance().currentUser?.uid.toString()).setValue(
                        User(
                            "${profile_name.text.toString()}",
                            "${phoneNo.text.toString()}",
                            "${profile_password.text.toString()}",
                            true,
                             "${FirebaseAuth.getInstance().currentUser?.uid}"
                            )
                    )
                GlobalScope.launch(Dispatchers.Main) {
                    delay(2000)
                    val i = Intent(this@CreateProfileActivity,MainActivity::class.java)
                    i.putExtra("holderName",profile_name.text.toString())
                    startActivity(i)
                }

                return@addOnCompleteListener
            }
            .addOnFailureListener {
                root.showSnack("Error : ${it.localizedMessage}")
            }
    }
    private fun View.showSnack(SnackTitle:String){
        val snack = Snackbar.make(this, SnackTitle, Snackbar.LENGTH_LONG)
        snack.animationMode = Snackbar.ANIMATION_MODE_SLIDE
        snack.show()
    }
}




