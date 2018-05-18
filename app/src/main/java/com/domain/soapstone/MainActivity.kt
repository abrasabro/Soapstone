package com.domain.soapstone

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.GoogleMap


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val mapFragment = fragment_main_map as SupportMapFragment
        mapFragment.getMapAsync({ googleMap: GoogleMap -> MainActivityFragment.onMapReady(googleMap) })
    }

}
