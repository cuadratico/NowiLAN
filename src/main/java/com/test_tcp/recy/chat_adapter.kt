package com.test_tcp.recy

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.test_tcp.R

data class chat (val message: String, val usr: Boolean = true)

class chat_adapter(var list: List<chat>): RecyclerView.Adapter<chat_holder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): chat_holder {
        return chat_holder(LayoutInflater.from(parent.context).inflate(R.layout.recy_chat, null))
    }

    override fun onBindViewHolder(holder: chat_holder, position: Int) {
        return holder.ele(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }


    fun update (new_list: List<chat>) {
        this.list = new_list
        notifyItemChanged(this.list.size - 1)
    }
}

