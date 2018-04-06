package com.domain.soapstone

import android.support.v4.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_message.*


class MessageActivityFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_message, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragment_message_write.setOnClickListener {
            makeMessage()
        }
    }

    fun makeMessage(){
        val write = Write(fragment_message_message.text.toString())
        fragment_message_message.text.clear()
        fragment_message_write.isEnabled = false
        fragment_message_write.text = "Writing.."
        val firebaseDatabase = FirebaseDatabase.getInstance()
        val messagesDatabaseReference = firebaseDatabase.getReference().child("messages")
        val message = messagesDatabaseReference.push()
        write.messageUID = message.key
        message.setValue(write)
        fragment_message_write.text = "Written!"

    }
}