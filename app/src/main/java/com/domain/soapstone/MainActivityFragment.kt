package com.domain.soapstone

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v4.app.Fragment
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
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
import kotlinx.android.synthetic.main.fragment_main.*


class MainActivityFragment : Fragment(), GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {

    companion object {
        var mMap: GoogleMap? = null
        const val RC_SIGN_IN = 1
        var instance = MainActivityFragment()
        fun showWrite(write: Write){
            instance.showWrite(write)
        }
        fun closeWrite(){
            instance.closeWrite()
        }
        fun onMapReady(googleMap: GoogleMap) {
            mMap = googleMap
            mMap?.setOnMarkerClickListener(instance)
            mMap?.setOnMapClickListener(instance)
            val defaultMapCenter = LatLng(40.045204, -96.803178)
            //mMap?.moveCamera(CameraUpdateFactory.newLatLng(defaultMapCenter).)
            mMap?.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(defaultMapCenter, 1f)))
        }
    }
    val writes = mutableListOf<Write>()
    val writesHashMap = mutableMapOf<String, Write>()
    val firebaseAuth = FirebaseAuth.getInstance()
    val mainAdapter = MessagesRecyclerAdapter()
    val firebaseDatabase = FirebaseDatabase.getInstance()
    val messagesDatabaseReference = firebaseDatabase.getReference().child("messages")
    lateinit var usersDatabaseReference: DatabaseReference
    lateinit var currentUser: User
    lateinit var firebaseUser: FirebaseUser
    var selectedWrite: Write? = null
    lateinit var mapPin: BitmapDescriptor

    val messagesEventListener: ChildEventListener = object : ChildEventListener{
        override fun onCancelled(p0: DatabaseError?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onChildMoved(p0: DataSnapshot?, p1: String?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onChildChanged(p0: DataSnapshot?, p1: String?) {
            val write = p0?.getValue(Write::class.java)
            if(write != null) {
                mainAdapter.updateWrite(write)
                if (write.messageUID == selectedWrite?.messageUID){
                    showWrite(write)
                }
            }
        }

        override fun onChildAdded(p0: DataSnapshot?, p1: String?) {
            val write = p0?.getValue(Write::class.java)
            if(write != null) {
                writes.add(write)
                mainAdapter.addWrite(write)
                val marker = mMap?.addMarker(MarkerOptions()
                        .position(LatLng(write.lat, write.lon))
                        .title(write.message)
                        .icon(mapPin))
                writesHashMap[marker!!.id] = write
                //mMap?.moveCamera(CameraUpdateFactory.newLatLng(LatLng(write.lat, write.lon)))
            }
        }

        override fun onChildRemoved(p0: DataSnapshot?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }

    val usersEventListener: ValueEventListener = object : ValueEventListener{
        override fun onCancelled(p0: DatabaseError?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onDataChange(p0: DataSnapshot?) {
            val user = p0?.getValue(User::class.java)
            if(user != null) {
                currentUser = user
                if(selectedWrite != null) {
                    if (currentUser.goodRatings.contains(selectedWrite!!.messageUID)) {
                        bottomdrawer_main_write_rategood.isEnabled = false
                        //todo: if app loses focus and another message is sent, returns null
                    }
                    if (currentUser.poorRatings.contains(selectedWrite!!.messageUID)) {
                        bottomdrawer_main_write_ratepoor.isEnabled = false
                    }
                }
            }else{
                usersDatabaseReference.setValue(User(firebaseUser.uid))
            }
        }

    }

    val authStateListener = object : FirebaseAuth.AuthStateListener{
        override fun onAuthStateChanged(p0: FirebaseAuth) {
            val user = p0.currentUser
            if(user != null){
                onSignedInInitialize(user)
            }else{
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
        }}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
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
        var bitmapFactoryOptions = BitmapFactory.Options()
        bitmapFactoryOptions.inSampleSize = 6
        mapPin = BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(resources, R.drawable.pin, bitmapFactoryOptions))

        mainAdapter.setWrites(writes)
        (fragment_main_recyclerview).apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = mainAdapter
        }
        instance  = this
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == RC_SIGN_IN){
            if(resultCode == RESULT_OK){
                //signed in
            }
            else if (resultCode == RESULT_CANCELED){
                activity!!.finish()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        firebaseAuth.removeAuthStateListener(authStateListener)
        mainAdapter.clearWrites()
        messagesDatabaseReference.removeEventListener(messagesEventListener)
    }

    override fun onResume() {
        super.onResume()
        firebaseAuth.addAuthStateListener(authStateListener)
        closeWrite()
    }

    fun onSignedInInitialize(user: FirebaseUser){
        firebaseUser = user
        messagesDatabaseReference.addChildEventListener(messagesEventListener)
        usersDatabaseReference = firebaseDatabase.getReference().child("users").child(firebaseUser.uid)
        usersDatabaseReference.addValueEventListener(usersEventListener)
    }

    fun onSignedOutCleanup(){
        mainAdapter.clearWrites()
        messagesDatabaseReference.removeEventListener(messagesEventListener)
    }

    fun showWrite(write: Write){
        selectedWrite = write
        bottomdrawer_main_nav_layout.visibility = View.GONE
        bottomdrawer_main_write_message.text = write.message
        bottomdrawer_main_write_lonlat.text = "Lat:${write.lat}, Lon:${write.lon}"
        bottomdrawer_main_write_address.text = write.address
        bottomdrawer_main_write_good.text = write.ratingGood.toString()
        bottomdrawer_main_write_poor.text = write.ratingPoor.toString()
        bottomdrawer_main_write_layout.visibility = View.VISIBLE
        if(currentUser.goodRatings.contains(write.messageUID)) {
            bottomdrawer_main_write_rategood.isEnabled = false
        }else{
            bottomdrawer_main_write_rategood.isEnabled = true
            bottomdrawer_main_write_rategood.setOnClickListener {
                messagesDatabaseReference.child(write.messageUID).child("ratingGood").setValue(write.ratingGood + 1)
                currentUser.goodRatings.add(write.messageUID)
                usersDatabaseReference.setValue(currentUser)
            }
        }
        if(currentUser.poorRatings.contains(write.messageUID)) {
            bottomdrawer_main_write_ratepoor.isEnabled = false
        }else{
            bottomdrawer_main_write_ratepoor.isEnabled = true
            bottomdrawer_main_write_ratepoor.setOnClickListener {
                messagesDatabaseReference.child(write.messageUID).child("ratingPoor").setValue(write.ratingPoor + 1)
                currentUser.poorRatings.add(write.messageUID)
                usersDatabaseReference.setValue(currentUser)
            }
        }
    }

    fun closeWrite(){
        selectedWrite = null
        bottomdrawer_main_write_layout.visibility = View.GONE
        bottomdrawer_main_nav_layout.visibility = View.VISIBLE
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        if(marker != null) {
            val write = writesHashMap[marker.id]
            if(write != null){
                showWrite(write)
                return true
            }
        }
        return false
    }

    override fun onMapClick(p0: LatLng?) {
        closeWrite()
    }
}
