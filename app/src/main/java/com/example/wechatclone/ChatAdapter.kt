package com.example.wechatclone

import android.app.AlertDialog
import android.app.AlertDialog.*
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
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
import kotlinx.android.synthetic.main.item_chat.view.*
import java.text.SimpleDateFormat
import java.util.*

val sdf = SimpleDateFormat("HH:mm")
class ChatAdapter(val chatList:ArrayList<Chat>) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
         val itemView=LayoutInflater.from(parent.context).inflate(
             R.layout.item_chat,
             parent,
             false
         )
        return ChatViewHolder(itemView)
    }

    override fun getItemCount(): Int { return chatList.size }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(chatList[position])

    }

    class ChatViewHolder(itemView : View) :  RecyclerView.ViewHolder(itemView){

        fun bind(chat : Chat) = with(itemView){

            showTime.text = chat.lastTime

            senderMessage.text = chat.message

            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)

            if(FirebaseAuth.getInstance().currentUser?.uid.equals(chat.senderId)){
                chatItem.setBackgroundColor(resources.getColor(R.color.colorMessage))
                params.gravity = Gravity.END
            }
            else{
                chatItem.setBackgroundColor(Color.WHITE)
                params.gravity = Gravity.START
            }
            chatItem.layoutParams = params
        }

    }

}