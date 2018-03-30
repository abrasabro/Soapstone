package com.domain.soapstone

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
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.fragment_message.*

/**
 * A placeholder fragment containing a simple view.
 */
class MainActivityFragment : Fragment() {

    companion object {
        const val RC_SIGN_IN = 1
        var currentuser = ""
    }
    val soapstones = mutableListOf<Soapstone>()
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
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onChildAdded(p0: DataSnapshot?, p1: String?) {
            val soapstone = p0?.getValue(Soapstone::class.java)
            if(soapstone != null) {
                soapstones.add(soapstone)
                mainAdapter.addSoapstone(soapstone)
                mainAdapter.notifyDataSetChanged()
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
                onSignedInInitialize(user.displayName?:"")
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
        fragment_main_new.setOnClickListener {
            startActivity(Intent(context, MessageActivity::class.java))
        }

        mainAdapter.setSoapstones(soapstones)
        (fragment_main_recyclerview).apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = mainAdapter
        }
    }

    override fun onPause() {
        super.onPause()
        firebaseAuth.removeAuthStateListener(authStateListener)
        mainAdapter.clearSoapstones()
        mainAdapter.notifyDataSetChanged()
        messagesDatabaseReference.removeEventListener(childEventListener)
    }

    override fun onResume() {
        super.onResume()
        firebaseAuth.addAuthStateListener(authStateListener)
    }

    fun onSignedInInitialize(username: String){
        currentuser = username
        messagesDatabaseReference.addChildEventListener(childEventListener)
    }

    fun onSignedOutCleanup(){
        currentuser = ""
        mainAdapter.clearSoapstones()
        mainAdapter.notifyDataSetChanged()
        messagesDatabaseReference.removeEventListener(childEventListener)
    }
}
