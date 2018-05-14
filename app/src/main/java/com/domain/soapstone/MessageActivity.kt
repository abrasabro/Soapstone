package com.domain.soapstone

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_message.*

class MessageActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        var mMap: GoogleMap? = null
        lateinit var instance: MessageActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this
        setContentView(R.layout.activity_message)
        val mapFragment = fragment_message_map as SupportMapFragment
        //mapFragment.getMapAsync({googleMap: GoogleMap -> MessageActivityFragment.onMapReady(googleMap)})
        mapFragment.getMapAsync(MessageActivityFragment.instance)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        MainActivity.mMap = googleMap
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        MessageActivityFragment.instance.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
