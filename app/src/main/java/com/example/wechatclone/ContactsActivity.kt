package com.example.wechatclone

import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.opengl.Visibility
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_contacts.*
import java.util.*
import kotlin.collections.ArrayList

class ContactsActivity : AppCompatActivity() {

    val PERMISSION_READ_CONTACTS=1
    var contactList= ArrayList<Contact>()
    var displayContacts = ArrayList<Contact>()
    val adapter=ContactsAdapter(displayContacts)

    override fun onStart() {
        super.onStart()
        grantContactPermission()
    }

    private fun grantContactPermission() {
        if (checkSelfPermission(android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(android.Manifest.permission.READ_CONTACTS),
                PERMISSION_READ_CONTACTS
            )
        } else {
            val contacts = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                null,
                null,
                ContactsContract.Contacts.DISPLAY_NAME + " COLLATE NOCASE ASC"
            )
            while (contacts!!.moveToNext()) {
                val name =
                    contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                val phone =
                    contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                var duplicate = false
                for(i in 0..contactList.size-1) {
                    if (PhoneNumberUtils.compare(contactList.get(i).contactNumber, phone)) {
                        duplicate = true
                    }
                }
                if(!duplicate) {

                    contactList.add(Contact(name, removeSpaces(phone),""))
                    displayContacts.add(Contact(name, removeSpaces(phone),""))

                }
            }

        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        if (requestCode == PERMISSION_READ_CONTACTS) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(RootContact, "Permission Needed", Snackbar.LENGTH_LONG)
                    .setAction("GRANT", View.OnClickListener {
                        grantContactPermission()
                    })
                    .setActionTextColor(Color.parseColor("#03D9F8"))
                    .show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)

        setSupportActionBar(toolbar)

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
        toolbar.setNavigationOnClickListener {
            finish()
        }
        RvShowContacts.apply {
            layoutManager=LinearLayoutManager(this@ContactsActivity)
            adapter=this@ContactsActivity.adapter
            adapter?.notifyDataSetChanged()
        }
        call.setOnClickListener {
            startActivity(Intent(Intent.ACTION_DIAL))
        }
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        menuInflater.inflate(R.menu.menu_contact,menu)
        val searchItem = menu.findItem(R.id.search_contact)
        val searchView = searchItem.actionView as SearchView

        Log.i("Text","newText")

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{

            override fun onQueryTextSubmit(query: String?): Boolean {
                return false;
            }
            override fun onQueryTextChange(newText: String?): Boolean {

                Log.i("Text",newText)

                if(!newText.isNullOrEmpty()){
                    displayContacts.clear()
                    contactList.forEach {
                        if(it.contactName.contains(newText,true))
                            displayContacts.add(it)
                    }
                    adapter.notifyDataSetChanged()
                }
                else{
                    displayContacts.clear()
                    displayContacts.addAll(contactList)
                    adapter.notifyDataSetChanged()
                }
                return true
            }

        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
                return true
    }

}
