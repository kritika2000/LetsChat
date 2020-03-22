package com.example.wechatclone

import android.app.Activity
import android.app.AlertDialog
import android.app.AlertDialog.*
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.opengl.Visibility
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_chat.view.*
import kotlinx.android.synthetic.main.item_main_chat.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

val sdf_D  = SimpleDateFormat("dd")
val sdf_T = SimpleDateFormat("h:mm")
val sdf_M = SimpleDateFormat("MM")
val sdf_Y = SimpleDateFormat("yyyy")

class MainAdapter(val chatList:ArrayList<MainChat>) : RecyclerView.Adapter<MainAdapter.MainViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        val itemView=LayoutInflater.from(parent.context).inflate(
            R.layout.item_main_chat,
            parent,
            false
        )
        return MainViewHolder(itemView)
    }

    override fun getItemCount(): Int { return chatList.size }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        holder.bind(chatList[position])

    }

    class MainViewHolder(itemView : View) :  RecyclerView.ViewHolder(itemView){

        var List = ArrayList<lastChat>()

        fun bind(chat : MainChat) = with(itemView){

            rcvName.text = chat.recieverName
            var image:Uri

            val Sid = chat.sndId
            val Rid = chat.rcvId

            val s = List.size

            val dbRef = DatabaseUtil.getDatabase().reference

            dbRef.child("Chats").addChildEventListener(object : ChildEventListener {

                override fun onCancelled(p0: DatabaseError) {}

                override fun onChildMoved(p0: DataSnapshot, p1: String?) {}

                override fun onChildChanged(p0: DataSnapshot, p1: String?) {}

                override fun onChildRemoved(p0: DataSnapshot) {}

                override fun onChildAdded(p0: DataSnapshot, p1: String?) {

                    if(p0.exists()){

                        if(Sid.equals(p0.child("senderId").value)&&
                            Rid.equals(p0.child("recieverId").value)||
                            Rid.equals(p0.child("senderId").value)&&
                            Sid.equals(p0.child("recieverId").value)){

                            List.add(
                                lastChat(
                                    p0.child("message").value.toString(),
                                    p0.child("lastTime").value.toString(),
                                    ""
                            )
                            )
                            lastMsg.text = p0.child("message").value.toString()
                            msgUnread.text = ""
                            time.text = p0.child("lastTime").value.toString()
                        }
                    }
                }
            })

               if(chat.sndId!=FirebaseAuth.getInstance().currentUser?.uid) {
                FirebaseStorage.getInstance().reference.child("pics/" + chat.sndId)
                    .downloadUrl.addOnSuccessListener {
                    Picasso.get().load(it)
                        .fit().centerInside()
                        .into(main_image)
                        image = it
                }
            }
            else{
                FirebaseStorage.getInstance().reference.child("pics/" + chat.rcvId)
                    .downloadUrl.addOnSuccessListener {
                    Picasso.get().load(it)
                        .fit().centerInside()
                        .into(main_image)
                    image=it
                }

            }

            itemView.setOnClickListener {

                val i= Intent(context,MainPersonActivity::class.java)

                i.putExtra("RecieverName",chat.recieverName)
                i.putExtra("RcvContact",chat.rcvPhone)
                i.putExtra("RcvId",chat.rcvId)
                i.putExtra("SndId",chat.sndId)

                context.startActivity(i)

            }

            main_image.setOnClickListener {

                val intent = Intent(context,ShowImageActivity::class.java)

                if(chat.sndId!=FirebaseAuth.getInstance().currentUser?.uid){
                    intent.putExtra("Image",chat.sndId)
                }
                else{
                    intent.putExtra("Image",chat.rcvId)
                }
                intent.putExtra("RcvName",chat.recieverName)
                context.startActivity(intent)
            }

        }

    }

}