package com.example.wechatclone

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.shape.CornerFamily
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_contacts.view.*
import kotlinx.android.synthetic.main.item_contact.view.*

class ContactsAdapter(val Contacts : ArrayList<Contact>) : RecyclerView.Adapter<ContactsAdapter.ContactViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {

        val itemView= LayoutInflater.from(parent.context).inflate(
            R.layout.item_contact,
            parent,
            false
        )

        return ContactViewHolder(itemView)
    }

    override fun getItemCount() : Int { return Contacts.size }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(Contacts[position])
    }

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(contact: Contact)= with(itemView){

            contactName.text=contact.contactName
            contactNumber.text=contact.contactNumber

            Log.i("Contact",contact.contactNumber)



            itemView.setOnClickListener {

                val i=Intent(context,PersonActivity::class.java)

                Log.i("PhoneNumber",contact.contactNumber)

                DatabaseUtil.getDatabase().reference.child("LetsChatUsers")
                    .orderByChild("phoneNumber").equalTo(contact.contactNumber)
                    .addValueEventListener(object : ValueEventListener{

                        override fun onCancelled(p0: DatabaseError) {}

                        override fun onDataChange(p0: DataSnapshot) {

                            Log.i("Print",p0.toString())

                            if(p0.exists()) {
                                i.putExtra("RecieverName",contact.contactName)
                                i.putExtra("ContactNumber",contact.contactNumber)
                                for (k in p0.children){
                                    Log.i("Id", k.key.toString())
                                    i.putExtra("RecieverContact",k.key.toString())
                                }
                                context.startActivity(i)
                            }
                            else{
                                Snackbar.make(this@with,"This contact is not authenticated",Snackbar.LENGTH_LONG)
                                    .setAction("INVITE",View.OnClickListener {

                                        val intent =  Intent(Intent.ACTION_SENDTO);
                                                       intent.setData(Uri.parse("smsto:" + Uri.encode(contact.contactNumber)))
                                                       context.startActivity(intent)
                                    })
                                    .setActionTextColor(Color.CYAN)
                                    .show()
                            }

                        }

                    })

            }

        }
    }
}