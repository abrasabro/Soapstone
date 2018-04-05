package com.domain.soapstone

import android.content.Intent
import android.support.v4.app.Fragment
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.fragment_message.*

/**
 * A placeholder fragment containing a simple view.
 */
class MessageActivityFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_message, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragment_message_share.setOnClickListener {
            makeMessage()
        }
    }

    fun makeMessage(){
        val firebaseDatabase = FirebaseDatabase.getInstance()
        val messagesDatabaseReference = firebaseDatabase.getReference().child("messages")
        val message = messagesDatabaseReference.push()
        message.setValue(Write(fragment_message_message.text.toString(), messageUID = message.key))
    }
}