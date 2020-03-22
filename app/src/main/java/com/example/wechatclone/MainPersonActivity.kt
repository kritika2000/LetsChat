package com.example.wechatclone

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import kotlinx.android.synthetic.main.activity_main_person.*
import kotlinx.android.synthetic.main.activity_main_person.view.*
import java.util.*


class MainPersonActivity : AppCompatActivity() {

    val chatList= ArrayList<Chat>()

    override fun onBackPressed() {
        finish()
    }
    var Rid : String=""
    var Sid : String=FirebaseAuth.getInstance().currentUser?.uid.toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_person)

        Log.i("Me",Sid)

        setSupportActionBar(toolbarmainPerson)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val I = intent
        val rc_name = I.getStringExtra("RecieverName")?.toString()
        val phone_number = I.getStringExtra("RcvContact")?.toString()

        Log.i("Contact",phone_number)

        toolbarmainPerson.PersonName.text = rc_name
        toolbarmainPerson.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
        toolbarmainPerson.setNavigationOnClickListener {
            finish()
        }

        Rid = I.getStringExtra("RcvId")!!.toString()

        if(Rid.equals(Sid))
            Rid = I.getStringExtra("SndId")!!.toString()
        val adapter = ChatAdapter(chatList)
        Log.i("Me",Sid)
        Log.i("MeYou",phone_number)

        val dbRef = DatabaseUtil.getDatabase().reference

        dbRef.child("Chats").addChildEventListener(object : ChildEventListener {

            override fun onCancelled(p0: DatabaseError) {}

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {}

            override fun onChildRemoved(p0: DataSnapshot) {}

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {


                if (Sid.equals(p0.child("recieverId").value)
                ) {

                    if (Rid.equals(p0.child("senderId").value)) {
                        chatList.add(
                            Chat(
                                p0.child("message").value.toString(),
                                Sid,
                                Rid,
                                rc_name.toString(),
                                phone_number.toString(),
                                sdfT.format(Date()),
                                sdfD.format(Date()),
                                sdfM.format(Date()),
                                sdfY.format(Date())
                            )
                        )
                        adapter.notifyDataSetChanged()
                        if(chatList.size>0)
                            RvMessage.smoothScrollToPosition(chatList.size - 1)
                    }
                }
                if(Sid.equals(p0.child("senderId").value.toString())) {

                    if (Rid.equals(p0.child("recieverId").value)) {
                        chatList.add(
                            Chat(
                                p0.child("message").value.toString(),
                                Rid,
                                Sid,
                                rc_name.toString(),
                                phone_number.toString(),
                                sdfT.format(Date()),
                                sdfD.format(Date()),
                                sdfM.format(Date()),
                                sdfY.format(Date())
                            )
                        )
                        adapter.notifyDataSetChanged()
                        if(chatList.size>0)
                            RvMessage.smoothScrollToPosition(chatList.size - 1)
                    }

                }
            }

        })

        sendMessage.setOnClickListener {

            if(!textMessage.text.toString().isEmpty()){

                dbRef.child("Chats").push().setValue(
                    Chat(textMessage.text.toString(),
                        Rid,
                        Sid,
                        rc_name.toString(),
                        phone_number.toString(),
                        sdfT.format(Date()),
                        sdfD.format(Date()),
                        sdfM.format(Date()),
                        sdfY.format(Date())
                    )
                )
                adapter.notifyDataSetChanged()
                if(chatList.size>0)
                    RvMessage.smoothScrollToPosition(chatList.size-1)
            }
            textMessage.text = null
        }

        RvMessage.apply {
            layoutManager = LinearLayoutManager(this@MainPersonActivity)
            this.adapter = adapter
            if(chatList.size>0)
                scrollToPosition(chatList.size - 1)
            adapter.notifyDataSetChanged()
        }

        callPerson.setOnClickListener {
            startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone_number)))
        }

    }
    private fun FindSenderId(){
        Sid = FirebaseAuth.getInstance().currentUser?.uid as String
    }
}
