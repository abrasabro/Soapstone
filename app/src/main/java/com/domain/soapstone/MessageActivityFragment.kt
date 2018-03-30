package com.domain.soapstone

import android.content.Intent
import android.support.v4.app.Fragment
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.fragment_message.*

/**
 * A placeholder fragment containing a simple view.
 */
class MessageActivityFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_message, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val templates = mutableListOf(
                Pair(1, "**** ahead"),
                Pair(2, "No **** ahead"),
                Pair(3, "**** required ahead"),
                Pair(40, "be wary of ****"),
                Pair(5, "try ****"),
                Pair(60, "Could this be a ****?"),
                Pair(777, "****!"),
                Pair(80, "Ahh, ****..."))
        val mainAdapter = TemplatesRecyclerAdapter()
        (fragment_message_recyclerview).apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = mainAdapter
        }

    }
}