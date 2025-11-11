package com.test_tcp.recy

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.test_tcp.R
import com.test_tcp.db_info.Companion.saves_list
import com.test_tcp.saves

class saves_adapter (var list: List<saves>): RecyclerView.Adapter<saves_holder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): saves_holder {
        return saves_holder(LayoutInflater.from(parent.context).inflate(R.layout.recy_saves, null))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: saves_holder, position: Int) {
        return holder.elements(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }


    fun update () {
        this.list = saves_list
        notifyDataSetChanged()
    }
}