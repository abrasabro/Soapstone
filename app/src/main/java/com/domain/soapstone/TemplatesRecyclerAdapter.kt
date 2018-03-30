package com.domain.soapstone

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.recycleritem_message.*
import kotlinx.android.synthetic.main.recycleritem_message.view.*


class TemplatesRecyclerAdapter : RecyclerView.Adapter<TemplatesRecyclerAdapter.TemplatesViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TemplatesViewHolder {
        return TemplatesViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.recycleritem_message, parent, false))
    }

    override fun getItemCount(): Int {
        return templates.size
    }

    override fun onBindViewHolder(holder: TemplatesViewHolder, position: Int) {
        holder.bind(position)
    }

    companion object {
        val templates = mutableListOf(
                Pair(1, "**** ahead"),
                Pair(2, "No **** ahead"),
                Pair(3, "**** required ahead"),
                Pair(40, "be wary of ****"),
                Pair(5, "try ****"),
                Pair(60, "Could this be a ****?"),
                Pair(777, "****!"),
                Pair(80, "Ahh, ****..."))
    }

    class TemplatesViewHolder(var view: View) : RecyclerView.ViewHolder(view){

        fun bind(index: Int){
            view.recycleritem_message_template.text = templates[index].second
            view.recycleritem_message_template.setOnClickListener {
                makeMessage(templates[index].first)
            }
        }
        fun makeMessage(templateUID: Int){
            val firebaseDatabase = FirebaseDatabase.getInstance()
            val messagesDatabaseReference = firebaseDatabase.getReference().child("messages")
            messagesDatabaseReference.push().setValue(Soapstone(MainActivityFragment.currentuser, templateUID))
        }
    }
}