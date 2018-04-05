package com.domain.soapstone

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.support.v4.app.Fragment
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.bottomdrawer_main_nav.*
import kotlinx.android.synthetic.main.bottomdrawer_main_write.*
import kotlinx.android.synthetic.main.fragment_main.*

/**
 * A placeholder fragment containing a simple view.
 */
class MainActivityFragment : Fragment() {

    companion object {
        const val RC_SIGN_IN = 1
        var instance = MainActivityFragment()
        fun showWrite(write: Write){
            instance.showWrite(write)
        }
        fun closeWrite(){
            instance.closeWrite()
        }
    }
    val writes = mutableListOf<Write>()
    val firebaseAuth = FirebaseAuth.getInstance()
    val mainAdapter = MessagesRecyclerAdapter()
    val firebaseDatabase = FirebaseDatabase.getInstance()
    val messagesDatabaseReference = firebaseDatabase.getReference().child("messages")

    val childEventListener: ChildEventListener = object : ChildEventListener{
        override fun onCancelled(p0: DatabaseError?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onChildMoved(p0: DataSnapshot?, p1: String?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onChildChanged(p0: DataSnapshot?, p1: String?) {
            val write = p0?.getValue(Write::class.java)
            if(write != null)
                mainAdapter.updateWrite(write)
        }

        override fun onChildAdded(p0: DataSnapshot?, p1: String?) {
            val write = p0?.getValue(Write::class.java)
            if(write != null) {
                writes.add(write)
                mainAdapter.addWrite(write)
            }
        }

        override fun onChildRemoved(p0: DataSnapshot?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }

    val authStateListener = object : FirebaseAuth.AuthStateListener{
        override fun onAuthStateChanged(p0: FirebaseAuth) {
            val user = p0.currentUser
            if(user != null){
                onSignedInInitialize()
            }else{
                onSignedOutCleanup()
                startActivityForResult(
                        AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setIsSmartLockEnabled(false)
                            .setAvailableProviders(mutableListOf(
                                    AuthUI.IdpConfig.EmailBuilder().build(),
                                    AuthUI.IdpConfig.GoogleBuilder().build()))
                            .build(),
                        RC_SIGN_IN)
            }
        }}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bottomdrawer_main_nav_new.setOnClickListener {
            startActivity(Intent(context, MessageActivity::class.java))
        }
        bottomdrawer_main_nav_signout.setOnClickListener {
            AuthUI.getInstance().signOut(context!!)
        }

        mainAdapter.setWrites(writes)
        (fragment_main_recyclerview).apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = mainAdapter
        }
        instance  = this
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == RC_SIGN_IN){
            if(resultCode == RESULT_OK){
                //signed in
            }
            else if (resultCode == RESULT_CANCELED){
                activity!!.finish()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        firebaseAuth.removeAuthStateListener(authStateListener)
        mainAdapter.clearWrites()
        messagesDatabaseReference.removeEventListener(childEventListener)
    }

    override fun onResume() {
        super.onResume()
        firebaseAuth.addAuthStateListener(authStateListener)
    }

    fun onSignedInInitialize(){
        messagesDatabaseReference.addChildEventListener(childEventListener)
    }

    fun onSignedOutCleanup(){
        mainAdapter.clearWrites()
        messagesDatabaseReference.removeEventListener(childEventListener)
    }

    fun showWrite(write: Write){
        bottomdrawer_main_nav_layout.visibility = View.GONE
        bottomdrawer_main_write_message.text = write.message
        bottomdrawer_main_write_lonlat.text = "Lat:${write.lat}, Lon:${write.lon}"
        bottomdrawer_main_write_address.text = write.address
        bottomdrawer_main_write_good.text = write.ratingGood.toString()
        bottomdrawer_main_write_poor.text = write.ratingPoor.toString()
        bottomdrawer_main_write_layout.visibility = View.VISIBLE
        bottomdrawer_main_write_rategood.setOnClickListener {
            messagesDatabaseReference.child(write.messageUID).child("ratingGood").setValue(write.ratingGood+1)
        }
        bottomdrawer_main_write_ratepoor.setOnClickListener {
            messagesDatabaseReference.child(write.messageUID).child("ratingPoor").setValue(write.ratingPoor+1)
        }
    }

    fun closeWrite(){
        bottomdrawer_main_write_layout.visibility = View.GONE
        bottomdrawer_main_nav_layout.visibility = View.VISIBLE
    }
}
