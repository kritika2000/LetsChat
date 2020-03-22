package com.example.wechatclone

import android.R.attr
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_story.view.*


class StoryAdapter(val storyList : ArrayList<Story>) : RecyclerView.Adapter<StoryAdapter.StoryViewholder>(){

    val CAMERA_PIC_REQUEST = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewholder {
        val itemView= LayoutInflater.from(parent.context).inflate(
            R.layout.item_story,
            parent,
            false
        )
        return StoryViewholder(itemView)
    }

    override fun getItemCount(): Int {return storyList.size}

    override fun onBindViewHolder(holder: StoryViewholder, position: Int) {
        holder.bind(storyList[position])
    }


    class StoryViewholder(itemView : View) :  RecyclerView.ViewHolder(itemView){

        val CAMERA_PIC_REQUEST = 1

        fun bind(story: Story) = with(itemView) {
                holderName.text = story.storyName
            if(story.storyImage.isNotEmpty())
                FirebaseStorage.getInstance().reference.child("pics/"+story.storyImage)
                    .downloadUrl.addOnSuccessListener {
                    Picasso.get().load(it)
                        .fit().centerInside().into(holderStory)
                }

            holderStory.setOnClickListener {
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
              //  Activity().startActivityForResult(cameraIntent,CAMERA_PIC_REQUEST)\
                context.startActivity(cameraIntent)
            }
        }

    }

//     fun onActivityResult(requestCode : Int,resultCode : Int,data : Intent){
//         if (requestCode == CAMERA_PIC_REQUEST && resultCode == Activity.RESULT_OK) {
//             val photo = attr.data.getExtras().get("data") as Bitmap
//             val it = Intent(conte)
//             .setImageBitmap(photo)
//         }
//     }

}