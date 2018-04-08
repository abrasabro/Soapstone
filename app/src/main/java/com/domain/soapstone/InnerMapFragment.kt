package com.domain.soapstone

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.domain.soapstone.R.layout.fragment_map
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.fragment_map.*
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment






class InnerMapFragment : Fragment(), OnMapReadyCallback{

    private var mMap: GoogleMap? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney, Australia, and move the camera.
        val sydney = LatLng(-34.0, 151.0)
        mMap?.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap?.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

}