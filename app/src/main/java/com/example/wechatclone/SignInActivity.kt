package com.example.wechatclone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_sign_in.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

var ans:Boolean=false
class SignInActivity : AppCompatActivity() {
    val dbRef=FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
       val mAuth = FirebaseAuth.getInstance().currentUser
           super.onCreate(savedInstanceState)
           setContentView(R.layout.activity_sign_in)

           var passwordKey: String = ""
           var phoneKey: String = ""
           LogIn.setOnClickListener {
               if (phoneNo.text.toString().length == 10
                   && Signin_password.text.toString().length >= 8
               ) {
                   AccountExists()
               } else
                   Snackbar.make(
                       RootSignIn,
                       "Invalid phone number or password",
                       Snackbar.LENGTH_LONG
                   ).show()
           }
       }

    private fun AccountExists() {
        getAllKeys()
    }
    private fun getAllKeys(){

        val lcuDbRef=dbRef.child("LetsChatUsers")
        lcuDbRef.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                for (ds in p0.children){
                    Log.i("Key",ds.key)
                    lcuDbRef.child(ds.key.toString()).addValueEventListener(object : ValueEventListener{
                        override fun onCancelled(p0: DatabaseError) {}
                        override fun onDataChange(p0: DataSnapshot) {
                            if (p0.child("phoneNumber").value.toString().equals(phoneNo.text.toString()) &&
                                p0.child("password").value.toString().equals(Signin_password.text.toString())
                            ){
                                val dbref = DatabaseUtil.getDatabase().reference.child("LetsChatUsers")
                                dbref.child(FirebaseAuth.getInstance().currentUser?.uid.toString())
                                    .child("status")
                                    .setValue(true)
                                startActivity(Intent(this@SignInActivity,MainActivity::class.java))
                                finish()
                                ans=true
                            }
                            else{
                                GlobalScope.launch(Dispatchers.Main) {
                                    Snackbar.make(RootSignIn,"Account with these credentials does'nt exist",Snackbar.LENGTH_LONG).show()
                                }
                            }
                        }

                    })
                }
            }

        })

    }
}