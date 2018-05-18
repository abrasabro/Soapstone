package com.domain.soapstone

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.BitmapFactory
import android.support.v4.app.Fragment
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.bottomdrawer_main_nav.*
import kotlinx.android.synthetic.main.bottomdrawer_main_write.*


class MainActivityFragment : Fragment(), GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {

    companion object {
        lateinit var mMap: GoogleMap
        const val RC_SIGN_IN = 1
        lateinit var instance: MainActivityFragment
        fun onMapReady(googleMap: GoogleMap) {
            mMap = googleMap
            mMap.setOnMarkerClickListener(instance)
            mMap.setOnMapClickListener(instance)
            val defaultMapCenter = LatLng(40.045204, -96.803178)
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(defaultMapCenter, 1f)))
        }
    }

    val writesHashMap = mutableMapOf<String, Write>()
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val messagesDatabaseReference = firebaseDatabase.reference.child("messages")
    lateinit var usersDatabaseReference: DatabaseReference
    lateinit var currentUser: User
    lateinit var firebaseUser: FirebaseUser
    var selectedWrite: Write? = null
    lateinit var mapPin: BitmapDescriptor

    //manages the data for each Write in the database
    private val messagesEventListener: ChildEventListener = object : ChildEventListener {
        override fun onCancelled(error: DatabaseError?) {
            Log.d("messagesEventListener", "onCancelled, ${error?.details}")
            errorDialog("Error communicating with Firebase servers \n" + error?.details)
            //This method will be triggered in the event that this listener either failed at the server, or is removed as a result of the security and Firebase rules.
        }

        override fun onChildMoved(dataSnapshot: DataSnapshot?, previousChildName: String?) {
            return
            //This method is triggered when a child location's priority changes. See setPriority(Object) and Ordered Data for more information on priorities and ordering data.
            //should never be called as all entries should be ordered by key and never change
        }

        //called on ratings change
        override fun onChildChanged(dataSnapshot: DataSnapshot?, previousChildName: String?) {
            val write = dataSnapshot?.getValue(Write::class.java)
            if (write != null) {
                if (write.messageUID == selectedWrite?.messageUID) {
                    showWrite(write)
                }
                writesHashMap.forEach { key: String, value: Write ->
                    if (value.messageUID == write.messageUID) {
                        writesHashMap[key] = write
                    }
                }
            }
        }

        //called for every message at the start and for any new messages added
        override fun onChildAdded(dataSnapshot: DataSnapshot?, previousChildName: String?) {
            val write = dataSnapshot?.getValue(Write::class.java)
            if (write != null) {
                val marker = mMap.addMarker(MarkerOptions()
                        .position(LatLng(write.lat, write.lon))
                        .title(write.message)
                        .icon(mapPin))
                writesHashMap[marker.id] = write
            }
        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot?) {
            return
            //only called when a message is removed while the app is running
        }

    }

    //manages user UID and ratings
    private val userEventListener: ValueEventListener = object : ValueEventListener {
        override fun onCancelled(error: DatabaseError?) {
            Log.d("userEventListener", "onCancelled, ${error?.details}")
            errorDialog("Error communicating with Firebase servers \n" + error?.details)
        }

        //called immediately with the contents of the current user and every time the user's data changes (after rating)
        override fun onDataChange(snapshot: DataSnapshot?) {
            val user = snapshot?.getValue(User::class.java)
            if (user != null) {
                currentUser = user
                val write = selectedWrite
                if (write != null) {
                    if (currentUser.goodRatings.contains(write.messageUID)) {
                        bottomdrawer_main_write_rategood.isEnabled = false
                    }
                    if (currentUser.poorRatings.contains(write.messageUID)) {
                        bottomdrawer_main_write_ratepoor.isEnabled = false
                    }
                }
            } else {//first time user
                usersDatabaseReference.setValue(User(firebaseUser.uid))
            }
        }

    }

    //called when the user signs in or out
    private val authStateListener = { auth: FirebaseAuth ->
        val user = auth.currentUser
        if (user != null) {
            onSignedInInitialize(user)
        } else {
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
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        instance = this
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomdrawer_main_nav_new.setOnClickListener {
            startActivity(Intent(context, MessageActivity::class.java))
        }
        bottomdrawer_main_nav_signout.setOnClickListener {
            AuthUI.getInstance().signOut(context!!)
        }
        val bitmapFactoryOptions = BitmapFactory.Options()
        bitmapFactoryOptions.inSampleSize = 6
        mapPin = BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(resources, R.drawable.pin, bitmapFactoryOptions))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                //signed in
            } else if (resultCode == RESULT_CANCELED) {
                activity?.finish()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        firebaseAuth.removeAuthStateListener(authStateListener)
        messagesDatabaseReference.removeEventListener(messagesEventListener)
    }

    override fun onResume() {
        super.onResume()
        firebaseAuth.addAuthStateListener(authStateListener)
        closeWrite()
    }

    private fun onSignedInInitialize(user: FirebaseUser) {
        firebaseUser = user
        //verify google map has been loaded asynchronously
        try {
            mMap.isMyLocationEnabled
        } catch (e: UninitializedPropertyAccessException) {
            Log.d("onSignedInInitialize", "googlemap not ready before firebase")
            errorDialog("Error creating Google Map")
        }
        messagesDatabaseReference.addChildEventListener(messagesEventListener)
        usersDatabaseReference = firebaseDatabase.reference.child("users").child(firebaseUser.uid)
        usersDatabaseReference.addValueEventListener(userEventListener)
    }

    private fun onSignedOutCleanup() {
        messagesDatabaseReference.removeEventListener(messagesEventListener)
    }

    fun showWrite(write: Write) {
        selectedWrite = write
        bottomdrawer_main_nav_layout.visibility = View.GONE
        bottomdrawer_main_write_message.text = write.message
        bottomdrawer_main_write_address.text = write.address
        bottomdrawer_main_write_good.text = write.ratingGood.toString()
        bottomdrawer_main_write_poor.text = write.ratingPoor.toString()
        bottomdrawer_main_write_layout.visibility = View.VISIBLE
        if (currentUser.goodRatings.contains(write.messageUID)) {
            bottomdrawer_main_write_rategood.isEnabled = false
        } else {
            bottomdrawer_main_write_rategood.isEnabled = true
            bottomdrawer_main_write_rategood.setOnClickListener {
                messagesDatabaseReference.child(write.messageUID).child("ratingGood").setValue(write.ratingGood + 1)
                currentUser.goodRatings.add(write.messageUID)
                usersDatabaseReference.setValue(currentUser)
                bottomdrawer_main_write_rategood.isEnabled = false
            }
        }
        if (currentUser.poorRatings.contains(write.messageUID)) {
            bottomdrawer_main_write_ratepoor.isEnabled = false
        } else {
            bottomdrawer_main_write_ratepoor.isEnabled = true
            bottomdrawer_main_write_ratepoor.setOnClickListener {
                messagesDatabaseReference.child(write.messageUID).child("ratingPoor").setValue(write.ratingPoor + 1)
                currentUser.poorRatings.add(write.messageUID)
                usersDatabaseReference.setValue(currentUser)
                bottomdrawer_main_write_ratepoor.isEnabled = false
            }
        }
    }

    private fun closeWrite() {
        selectedWrite = null
        bottomdrawer_main_write_layout.visibility = View.GONE
        bottomdrawer_main_nav_layout.visibility = View.VISIBLE
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        if (marker != null) {
            val write = writesHashMap[marker.id]
            if (write != null) {
                showWrite(write)
                return true
            }
        }
        return false
    }

    override fun onMapClick(latLng: LatLng?) {
        closeWrite()
    }

    private fun errorDialog(msg: String = "Error") {
        Log.d("errorDialog", "dialog: $msg")
        val builder = AlertDialog.Builder(MessageActivity.instance)
        builder.setTitle("Error")
        builder.setMessage(msg)
        builder.setPositiveButton("Retry", { _, _: Int ->
            activity?.recreate()
        })
        builder.setNegativeButton("Quit", { _, _: Int ->
            activity?.finish()
        })
        builder.setOnCancelListener({ errorDialog(msg) })
        builder.create().show()
    }
}
