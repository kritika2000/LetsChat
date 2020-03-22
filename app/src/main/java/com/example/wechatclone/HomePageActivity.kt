package com.example.wechatclone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_home_page.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class HomePageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        Log.i("homeUser", FirebaseAuth.getInstance().currentUser?.uid.toString())

        val dbref = DatabaseUtil.getDatabase().reference.child("LetsChatUsers")
        var isOnline: Boolean = false
        if (FirebaseAuth.getInstance().currentUser?.uid.toString() != null) {
            dbref.child(FirebaseAuth.getInstance().currentUser?.uid.toString())
                .addValueEventListener(object : ValueEventListener {

                    override fun onCancelled(p0: DatabaseError) {}

                    override fun onDataChange(p0: DataSnapshot) {
                        Log.i("p0", p0.toString())
                        if (p0.exists()) {
                            if (p0.child("status").value == true) {
                                val i = Intent(
                                    this@HomePageActivity,
                                    MainActivity::class.java
                                )
                                i.putExtra("holderName",p0.child("profileName").value.toString())
                                startActivity(i)
                                finish()
                            } else {

                                startActivity(
                                    Intent(
                                        this@HomePageActivity,
                                        SignInActivity::class.java
                                    )
                                )
                                finish()
                            }
                        }
                    }

                })

    }

        createAccount.setOnClickListener {
            startActivity(Intent(this, CreateProfileActivity::class.java))
        }

        LogIn.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }
    }
}
