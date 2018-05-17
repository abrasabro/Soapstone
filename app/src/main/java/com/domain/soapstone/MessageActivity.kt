package com.domain.soapstone

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.android.gms.maps.SupportMapFragment
import kotlinx.android.synthetic.main.activity_message.*

class MessageActivity : AppCompatActivity() {

    companion object {
        lateinit var instance: MessageActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        instance = this
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)
        val mapFragment = fragment_message_map as SupportMapFragment
        mapFragment.getMapAsync(MessageActivityFragment.instance)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        MessageActivityFragment.instance.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
