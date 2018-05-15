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
import android.support.v7.app.AlertDialog
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Task
import java.io.IOException
import java.util.*


class MessageActivityFragment : Fragment(), GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener, OnMapReadyCallback {

    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    private val defaultZoom = 20f
    private val mDefaultLocation = LatLng(0.0, 0.0)
    private var mFusedLocationProviderClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MessageActivity.instance)
    private var mLastKnownLocation: Location? = null
    private lateinit var mWriteMarker: Marker
    private val geocoder = Geocoder(MessageActivity.instance, Locale.getDefault())
    private var mAddress = ""
    private lateinit var mMap: GoogleMap
    private var mLocationPermissionGranted: Boolean = false

    companion object {
        lateinit var instance: MessageActivityFragment
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_message, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        instance = this
        super.onViewCreated(view, savedInstanceState)
        fragment_message_write.setOnClickListener {
            makeMessage()
        }
        fragment_message_write.isEnabled = false
    }

    private fun makeMessage() {
        val write = Write(fragment_message_message.text.toString(),
                lat = mWriteMarker.position.latitude,
                lon = mWriteMarker.position.longitude,
                address = "near $mAddress")
        fragment_message_write.isEnabled = false
        val firebaseDatabase = FirebaseDatabase.getInstance()
        val messagesDatabaseReference = firebaseDatabase.reference.child("messages")
        val message = messagesDatabaseReference.push()
        write.messageUID = message.key
        message.setValue(write)
        activity?.finish()
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        return true
    }

    override fun onMapClick(latLng: LatLng?) {
        if (latLng != null)
            putMarker(latLng)
    }

    private fun getLocationPermission() {
        Log.d("getLocationPermission", "function start")
        if (ContextCompat.checkSelfPermission(context!!,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true
            updateLocationUI()
        } else {
            Log.d("getLocationPermission", "do not have location permission")
            ActivityCompat.requestPermissions(activity!!,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        mLocationPermissionGranted = false
        Log.d("onRequestPermissionsRes", "start")
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                Log.d("onRequestPermissionsRes", "PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION")
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("onRequestPermissionsRes", "permission granted")
                    mLocationPermissionGranted = true
                    updateLocationUI()
                } else {
                    Log.d("onRequestPermissionsRes", "call getLocationPermission")
                    errorDialog("Soapstone needs location permissions to complete this action")
                }
            }
        }
    }

    private fun updateLocationUI() {
        try {
            if (mLocationPermissionGranted) {
                mMap.isMyLocationEnabled = true
                getDeviceLocation()
            } else {
                Log.d("updateLocationUI", "lost permission after checking")
                errorDialog("Unexpected loss of permissions")
            }
        } catch (e: SecurityException) {
            Log.d("updateLocationUI", "lost permission after checking")
            errorDialog("Unexpected loss of permissions")
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMarkerClickListener(instance)
        mMap.setOnMapClickListener(instance)
        mMap.uiSettings.isMyLocationButtonEnabled = false
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, defaultZoom))
        getLocationPermission()
    }

    private fun getDeviceLocation() {
        try {
            if (mLocationPermissionGranted) {
                val locationResult = mFusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(activity!!, { task: Task<Location> ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        mLastKnownLocation = task.result
                        if (task.result != null) {
                            val location: Location = task.result
                            val locLatLng = LatLng(location.latitude, location.longitude)
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    locLatLng, defaultZoom))
                            mMap.setLatLngBoundsForCameraTarget(LatLngBounds(locLatLng,
                                    locLatLng))
                            mMap.uiSettings.isZoomControlsEnabled = false
                            mMap.uiSettings.isZoomGesturesEnabled = false
                            val marker = mMap.addMarker(MarkerOptions()
                                    .position(locLatLng))
                            if (marker != null) {
                                mWriteMarker = marker
                                val bitmapFactoryOptions = BitmapFactory.Options()
                                bitmapFactoryOptions.inSampleSize = 6
                                val mapPin = BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(resources, R.drawable.pin, bitmapFactoryOptions))
                                mWriteMarker.setIcon(mapPin)
                                putMarker(mWriteMarker.position)
                                fragment_message_write.isEnabled = true
                            } else {
                                Log.d("getDeviceLocation()", "couldn't add marker to map")
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, defaultZoom))
                                errorDialog("Couldn't add marker to map")
                            }
                        } else {
                            Log.d("getDeviceLocation()", "getLastLocation() was successful with no result")
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, defaultZoom))
                            errorDialog("getLastLocation() was successful with no result")
                        }
                    } else {
                        Log.d("getDeviceLocation()", "getLastLocation() was unsuccessful")
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, defaultZoom))
                        errorDialog("getLastLocation() was unsuccessful")
                    }
                })
            }
        } catch (e: SecurityException) {
            Log.d("getDeviceLocation()", "lost permissions after checking")
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, defaultZoom))
            errorDialog("unexpected loss of permissions")
        }

    }

    private fun putMarker(latLng: LatLng) {
        mWriteMarker.position = latLng

        var addresses = listOf<Address>()
        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
        } catch (e: IOException) {
            Log.d("putMarker", "geocoder network or I/O exception")
            errorDialog("geocoder could not access the network")
        }

        if (addresses.isNotEmpty()) {
            mAddress = ""
            if (addresses[0].getAddressLine(0) != null) {
                mAddress = addresses[0].getAddressLine(0)
                if (addresses[0].getAddressLine(1) != null) {
                    mAddress += addresses[0].getAddressLine(1)
                }
                fragment_message_address.text = mAddress
                return
            }
            if (addresses[0].featureName != null) {
                mAddress = addresses[0].featureName
                fragment_message_address.text = mAddress
                return
            }
        }
        mAddress = "unknown"
        fragment_message_address.text = mAddress
    }

    private fun errorDialog(msg: String = "Error") {
        Log.d("errorDialog", "dialog: $msg")
        val builder = AlertDialog.Builder(MessageActivity.instance)
        builder.setTitle("Error")
        builder.setMessage(msg)
        builder.setPositiveButton("Retry", { _, _: Int ->
            activity?.recreate()
        })
        builder.setNegativeButton("Cancel", { _, _: Int ->
            activity?.finish()
        })
        builder.setOnCancelListener({ errorDialog(msg) })
        builder.create().show()
    }


}