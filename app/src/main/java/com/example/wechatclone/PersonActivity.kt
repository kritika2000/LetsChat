package com.example.wechatclone

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.inputmethod.InputMethodManager
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

import kotlinx.android.synthetic.main.activity_person.*
import kotlinx.android.synthetic.main.activity_person.view.*
import kotlinx.android.synthetic.main.item_chat.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

val sdfD  = SimpleDateFormat("dd")
val sdfT = SimpleDateFormat("HH:mm")
val sdfM = SimpleDateFormat("MM")
val sdfY = SimpleDateFormat("yyyy")
class PersonActivity : AppCompatActivity() {

    val chatList= ArrayList<Chat>()

    var Rid : String=""
    var Sid : String=FirebaseAuth.getInstance().currentUser?.uid.toString()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_person)

        Log.i("Me",Sid)

        setSupportActionBar(toolbarPerson)

        val adapter = ChatAdapter(chatList)
        val I = intent

        Rid = I.getStringExtra("RecieverContact")!!
        supportActionBar?.setDisplayShowTitleEnabled(false)

        toolbarPerson.PersonName.text = I.getStringExtra("RecieverName")
        toolbarPerson.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
        toolbarPerson.setNavigationOnClickListener {
            finish()
        }

        val dbRef = DatabaseUtil.getDatabase().reference

                dbRef.child("Chats").addChildEventListener(object : ChildEventListener{

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
                                I.getStringExtra("RecieverName")!!.toString(),
                                I.getStringExtra("ContactNumber")!!.toString(),
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
                                I.getStringExtra("RecieverName")!!.toString(),
                                I.getStringExtra("ContactNumber")!!.toString(),
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
                        I.getStringExtra("RecieverName")!!.toString(),
                        I.getStringExtra("ContactNumber")!!.toString(),
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
            layoutManager = LinearLayoutManager(this@PersonActivity)
            this.adapter = adapter
            adapter.notifyDataSetChanged()
        }

        callPerson.setOnClickListener {
            startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + I.getStringExtra("ContactNumber"))))
        }

    }

    private fun removeSpaces(faulty:String) : String{
        var number=""
        for(k in 0..faulty.length-1){
            if(faulty.get(k)!=' ')
                number += faulty.get(k)
        }
        return number
    }


}
