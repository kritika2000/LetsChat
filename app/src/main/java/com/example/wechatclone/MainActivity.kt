package com.example.wechatclone

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

import kotlinx.android.synthetic.main.activity_main.*
import java.util.jar.Manifest
import android.Manifest.permission
import android.Manifest.permission.READ_CONTACTS
import android.provider.ContactsContract
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_main_chat.*
import kotlinx.android.synthetic.main.item_story.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    val  PERMISSIONS_REQUEST_READ_CONTACTS = 100
    val  PERMISSIONS_REQUEST_READ_CAMERA = 200
    val  PERMISSIONS_REQUEST_READ_STORAGE = 300

    var Rid : String=""
    var Sid : String=FirebaseAuth.getInstance().currentUser?.uid.toString()

    var mainChatList = ArrayList<MainChat>()
    var mainStories = ArrayList<Story>()
    override fun onStart() {
        super.onStart()
        grantContactPermission()
        grantCameraPermission()
        grantStoragePermission()

    }

    override fun onBackPressed() {
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.i("User",Sid)

        mainStories.add(Story("Add your story",Sid))

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        startChat.setOnClickListener {
             startActivity(Intent(this,ContactsActivity::class.java))
        }

        val adapter = MainAdapter(mainChatList)
        val storyAdapter = StoryAdapter(mainStories)

            val dbRef = DatabaseUtil.getDatabase().reference

            dbRef.child("Chats").addValueEventListener(object : ValueEventListener {

                override fun onCancelled(p0: DatabaseError) {}

                override fun onDataChange(p0: DataSnapshot) {

                    if (p0.exists()) {

                        for (k in p0.children) {

                            Log.i("MainChat", k.key.toString())

                            if (k.child("senderId").value.toString().equals(Sid)) {
                                Rid = k.child("recieverId").value.toString()
                                val RcName = k.child("recieverName").value.toString()
                                Log.i("name", RcName)
                                if (mainChatList.size == 0) {
                                    mainChatList.add(
                                        MainChat(
                                            Rid,
                                            Sid,
                                            k.child("recieverContact").value.toString(),
                                            RcName
                                        )
                                    )
                                    adapter.notifyDataSetChanged()
                                } else {
                                    if(!mainChatList.filter {j->
                                                j.rcvId == Rid || j.sndId == Rid
                                        }.any()) {
                                            mainChatList.add(
                                                MainChat(
                                                    Rid,
                                                    Sid,
                                                    k.child("recieverContact").value.toString(),
                                                    RcName
                                                )
                                            )
                                            adapter.notifyDataSetChanged()

                                    }
                                }
                            } else if (k.child("recieverId").value.toString().equals(Sid)) {

                                val sender = k.child("senderId").value.toString()

                                Log.i("MainChatReciever", Sid)
                                var sdPhone: String = ""//= k.child("recieverContact").value.toString()
                                Log.i("MainChatSender", sdPhone)
                                dbRef.child("LetsChatUsers").child(sender)
                                    .addListenerForSingleValueEvent(object : ValueEventListener {

                                        override fun onCancelled(p0: DatabaseError) {}

                                        override fun onDataChange(p0: DataSnapshot) {
                                            if (p0.exists()) {

                                                sdPhone = p0.child("phoneNumber").value.toString()
                                                var sdName: String = ""

                                                Log.i("SenderPhone", removeSpaces(sdPhone))
//
                                                val contacts = contentResolver.query(
                                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                                    null,
                                                    null,
                                                    null,
                                                    ContactsContract.Contacts.DISPLAY_NAME + " COLLATE NOCASE ASC"
                                                )
                                                while (contacts!!.moveToNext()) {

                                                    val name =
                                                        contacts.getString(
                                                            contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                                                        )
                                                    val phone =
                                                        contacts.getString(
                                                            contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                                        )
                                                    if (removeSpaces(phone).equals(
                                                            removeSpaces(
                                                                sdPhone
                                                            )
                                                        )
                                                    ) {
                                                        sdName = name
                                                        Log.i("SPhone", removeSpaces(sdPhone))
                                                        Log.i("Phone", removeSpaces(phone))
                                                        break
                                                    }
//

                                                }
                                                Log.i("My_name", "${mainChatList.size}")
                                                if (mainChatList.size == 0) {
                                                    mainChatList.add(
                                                        MainChat(
                                                            Sid,
                                                            sender,
                                                            sdPhone,
                                                            sdName
                                                        )
                                                    )
                                                    adapter.notifyDataSetChanged()
                                                } else {
                                                    if(!(mainChatList.filter {j->
                                                            j.sndId == sender || j.rcvId == sender
                                                        }.any())) {
                                                            mainChatList.add(
                                                                MainChat(
                                                                    Sid,
                                                                    sender,
                                                                    sdPhone,
                                                                    sdName
                                                                )
                                                            )

                                                            adapter.notifyDataSetChanged()
                                                        Log.i("MainChatAdd", k.key.toString())

                                                        }
                                                    }
                                                }
                                            }
                                    })
                            }
                        }
                    }
                }
            })

             RvStories.addItemDecoration(
                 DividerItemDecoration(
                this,
                LinearLayoutManager.VERTICAL
             )
        )

        RvStories.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            this.adapter = storyAdapter
            adapter.notifyDataSetChanged()
        }
        RvMainChats.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            this.adapter = adapter
            adapter.notifyDataSetChanged()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        Log.i("Text","newText")
        menuInflater.inflate(R.menu.menu_main,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.log_out -> {
                val dbref = DatabaseUtil.getDatabase().reference.child("LetsChatUsers")
                dbref.child(FirebaseAuth.getInstance().currentUser?.uid.toString())
                    .child("status")
                    .setValue(false)
                finish()
            }
            R.id.my_profile -> {
                startActivity(Intent(this,MyProfileActivity::class.java))
                FirebaseStorage.getInstance().reference.child("pics/" + FirebaseAuth.getInstance().currentUser?.uid.toString())
                    .downloadUrl.addOnSuccessListener {
                    Picasso.get().load(it)
                        .fit().centerInside()
                        .into(holderStory)
                }
            }
        }
            return super.onOptionsItemSelected(item)

    }

    private fun grantContactPermission() {

        if(checkSelfPermission(android.Manifest.permission.READ_CONTACTS)!=PackageManager.PERMISSION_GRANTED){
            requestPermissions(arrayOf(android.Manifest.permission.READ_CONTACTS),PERMISSIONS_REQUEST_READ_CONTACTS)
        }
}

    private fun grantCameraPermission() {

        if(checkSelfPermission(android.Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA),PERMISSIONS_REQUEST_READ_CAMERA)
        }
    }

    private fun grantStoragePermission() {

        if(checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),PERMISSIONS_REQUEST_READ_STORAGE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        if (grantResults.size > 0) {
            if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Snackbar.make(MainRoot, "Permission Needed", Snackbar.LENGTH_LONG)
                        .setAction("GRANT", View.OnClickListener {
                            grantContactPermission()
                        })
                        .setActionTextColor(Color.parseColor("#03D9F8"))
                        .show()
                }
            } else if (requestCode == PERMISSIONS_REQUEST_READ_CAMERA) {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Snackbar.make(MainRoot, "Permission Needed", Snackbar.LENGTH_LONG)
                        .setAction("GRANT", View.OnClickListener {
                            grantCameraPermission()
                        })
                        .setActionTextColor(Color.parseColor("#03D9F8"))
                        .show()
                }
            } else if (requestCode == PERMISSIONS_REQUEST_READ_STORAGE) {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Snackbar.make(MainRoot, "Permission Needed", Snackbar.LENGTH_LONG)
                        .setAction("GRANT", View.OnClickListener {
                            grantStoragePermission()
                        })
                        .setActionTextColor(Color.parseColor("#03D9F8"))
                        .show()
                }
            }
        }
    }

    private fun FindSenderId(){

        Sid = FirebaseAuth.getInstance().currentUser?.uid as String

    }

    private fun removeSpaces(faulty:String) : String{
        var number=""
        for(k in 0..faulty.length-1){
            if(faulty.get(k)!=' ')
                number += faulty.get(k)
        }
        if(number.length>10)
            return number.substring(3,number.length)
        return number
    }

}
