package com.domain.soapstone

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.google.android.gms.maps.OnMapReadyCallback

import kotlinx.android.synthetic.main.activity_main.*
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.GoogleMap
import kotlinx.android.synthetic.main.fragment_main.*


class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        var mMap: GoogleMap? = null
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val mapFragment = fragment_main_map as SupportMapFragment
        //val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync({googleMap: GoogleMap -> MainActivityFragment.onMapReady(googleMap)})
        //mapFragment.getMapAsync(this)

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney, Australia, and move the camera.
        val sydney = LatLng(-34.0, 151.0)
        mMap?.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap?.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

}
