package com.domain.soapstone

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.recycleritem_main.view.*


class MessagesRecyclerAdapter: RecyclerView.Adapter<MessagesRecyclerAdapter.MessagesViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessagesViewHolder {
        return MessagesRecyclerAdapter.MessagesViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.recycleritem_main, parent, false))
    }

    override fun getItemCount(): Int {
        return soapstones.size
    }

    override fun onBindViewHolder(holder: MessagesViewHolder, position: Int) {
        holder.bind(position)
    }

    companion object {
        val soapstones = mutableListOf<Soapstone>()
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

    fun setSoapstones(stonesList: List<Soapstone>){
        soapstones.addAll(stonesList)
    }

    fun addSoapstone(soapstone: Soapstone){
        soapstones.add(soapstone)
    }

    fun clearSoapstones(){
        soapstones.removeAll { true }
    }

    class MessagesViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
        fun bind(index: Int){
            val templateuid = soapstones[index].templateUID
            var message = ""
            templates.forEach {
                if(it.first == templateuid){
                    message = it.second
                }
            }
            view.recycleritem_main_message.text = message
            view.recycleritem_main_user.text = "user: ${soapstones[index].userUID}"
        }
    }
}