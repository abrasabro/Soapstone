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
        return writes.size
    }

    override fun onBindViewHolder(holder: MessagesViewHolder, position: Int) {
        holder.bind(position)
    }

    companion object {
        val writes = mutableListOf<Write>()
    }

    fun setWrites(writesList: List<Write>){
        writes.addAll(writesList)
        notifyDataSetChanged()
    }

    fun addWrite(write: Write){
        writes.add(write)
        notifyDataSetChanged()
    }

    fun clearWrites(){
        writes.removeAll { true }
        notifyDataSetChanged()
    }

    fun updateWrite(write: Write){
        for(i in 0 until writes.size){
            if(writes[i].messageUID == write.messageUID){
                writes[i] = write
            }
        }
        notifyDataSetChanged()
    }

    class MessagesViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
        fun bind(index: Int){
            view.recycleritem_main_message.text = writes[index].message
            view.recycleritem_main_toggle.setOnCheckedChangeListener { buttonView, isChecked ->
                if(isChecked){
                    MainActivityFragment.showWrite(writes[index])
                }
                else{
                    MainActivityFragment.closeWrite()
                }
            }
        }
    }
}