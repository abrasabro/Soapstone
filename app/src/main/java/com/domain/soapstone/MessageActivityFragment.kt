package com.domain.soapstone

import android.support.v4.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_message.*
import android.support.v4.app.ActivityCompat
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.support.v4.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.OnCompleteListener
import java.io.IOException
import java.util.*


class MessageActivityFragment : Fragment(), GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener, OnMapReadyCallback {

    val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    val DEFAULT_ZOOM = 20f
    val mDefaultLocation = LatLng(0.0, 0.0)
    var mFusedLocationProviderClient : FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MessageActivity.instance)
    var mLastKnownLocation: Location? = null
    lateinit var mWriteMarker: Marker
    val geocoder = Geocoder(MessageActivity.instance, Locale.getDefault())
    var mAddress = ""

    companion object {
        var mLocationPermissionGranted: Boolean = false
        var mMap: GoogleMap? = null
        lateinit var instance: MessageActivityFragment
        fun onMapReady(googleMap: GoogleMap) {
            mMap = googleMap
            mMap?.setOnMarkerClickListener(instance)
            mMap?.setOnMapClickListener(instance)
            val sydney = LatLng(-34.0, 151.0)
            mMap?.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
            //mMap?.moveCamera(CameraUpdateFactory.newLatLng(sydney))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_message, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragment_message_write.setOnClickListener {
            makeMessage()
        }
        instance  = this
    }

    fun makeMessage(){
        val write = Write(fragment_message_message.text.toString(),
                lat = mWriteMarker.position.latitude,
                lon = mWriteMarker.position.longitude,
                address = "near $mAddress")
        fragment_message_message.text.clear()
        fragment_message_write.isEnabled = false
        //fragment_message_write.text = "Writing.."
        val firebaseDatabase = FirebaseDatabase.getInstance()
        val messagesDatabaseReference = firebaseDatabase.getReference().child("messages")
        val message = messagesDatabaseReference.push()
        write.messageUID = message.key
        message.setValue(write)
        //fragment_message_write.text = "Written!"

    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        return true
    }

    override fun onMapClick(latLng: LatLng?) {
        if(latLng != null)
            putMarker(latLng)
    }

    private fun getLocationPermission() {
        /*
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */
        if (ContextCompat.checkSelfPermission(context!!,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true
            updateLocationUI()
        } else {
            ActivityCompat.requestPermissions(activity!!,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        mLocationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true
                }
            }
        }
        updateLocationUI()
    }

    fun updateLocationUI(){
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap?.setMyLocationEnabled(true)
                mMap?.getUiSettings()?.isMyLocationButtonEnabled = true
                getDeviceLocation()
            } else {
                mMap?.setMyLocationEnabled(false)
                mMap?.getUiSettings()?.isMyLocationButtonEnabled = false
                getLocationPermission()
            }
        } catch (e: SecurityException) {

        }


    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap?.setOnMarkerClickListener(instance)
        mMap?.setOnMapClickListener(instance)
        val sydney = LatLng(-34.0, 151.0)
        mMap?.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        //mMap?.moveCamera(CameraUpdateFactory.newLatLng(sydney))
        getLocationPermission()
        mMap?.uiSettings?.isMyLocationButtonEnabled = false
    }

    private fun getDeviceLocation() {
        /*
     * Get the best and most recent location of the device, which may be null in rare
     * cases when a location is not available.
     */
        try {
            if (mLocationPermissionGranted) {
                val locationResult = mFusedLocationProviderClient.getLastLocation()
                locationResult.addOnCompleteListener(activity!!, object : OnCompleteListener<Location> {
                    override fun onComplete(task: Task<Location>) {
                        if (task.isSuccessful) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.result
                            if(mLastKnownLocation != null){
                                mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        LatLng(mLastKnownLocation!!.getLatitude(),
                                                mLastKnownLocation!!.getLongitude()), DEFAULT_ZOOM))
                                mMap?.setLatLngBoundsForCameraTarget(LatLngBounds(LatLng(mLastKnownLocation!!.getLatitude(),
                                        mLastKnownLocation!!.getLongitude()),
                                        LatLng(mLastKnownLocation!!.getLatitude(),
                                                mLastKnownLocation!!.getLongitude())))
                                mMap?.uiSettings?.isZoomControlsEnabled = false
                                mMap?.uiSettings?.isZoomGesturesEnabled = false
                                val marker = mMap?.addMarker(MarkerOptions()
                                        .position(LatLng(mLastKnownLocation!!.getLatitude(),
                                                mLastKnownLocation!!.getLongitude())))
                                if(marker != null) {
                                    mWriteMarker = marker
                                    var bitmapFactoryOptions = BitmapFactory.Options()
                                    bitmapFactoryOptions.inSampleSize = 6
                                    val mapPin = BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(resources, R.drawable.pin, bitmapFactoryOptions))
                                    mWriteMarker.setIcon(mapPin)
                                    putMarker(mWriteMarker.position)
                                }
                        }} else {
                            mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM))
                            mMap?.getUiSettings()?.isMyLocationButtonEnabled = false
                        }
                    }
                })
            }
        } catch (e: SecurityException) {

        }

    }

    fun putMarker(latLng: LatLng){
        mWriteMarker.position = latLng

        var addresses = listOf<Address>()
        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
        }catch (e: IOException){

        }


        if(addresses.size > 0){
            mAddress = ""
            if(addresses[0].getAddressLine(0) != null) {
                mAddress = addresses[0].getAddressLine(0)
                if(addresses[0].getAddressLine(1) != null) {
                    mAddress += addresses[0].getAddressLine(1)
                }
                fragment_message_address.text = mAddress
                return
            }
            if(addresses[0].featureName != null){
                mAddress = addresses[0].featureName
                fragment_message_address.text = mAddress
                return
            }
        }
        mAddress = "unknown"
        fragment_message_address.text = mAddress
    }



}