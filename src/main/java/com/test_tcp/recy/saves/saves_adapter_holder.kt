package com.test_tcp.recy.saves

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.android.material.imageview.ShapeableImageView
import com.test_tcp.R
import com.test_tcp.db_info
import com.test_tcp.db_info.Companion.saves_list
import com.test_tcp.saves
import com.test_tcp.very_data
import java.security.KeyStore
import java.util.Base64
import javax.crypto.Cipher

class saves_adapter (var list: List<saves>, val edit: (saves) -> Unit, val delete: (saves) -> Unit, val copy: (saves) -> Unit): RecyclerView.Adapter<saves_holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): saves_holder {
        return saves_holder(LayoutInflater.from(parent.context).inflate(R.layout.recy_saves, null))
    }

    override fun onBindViewHolder(holder: saves_holder, position: Int) {
        return holder.elements(list[position], edit, delete, copy)
    }

    override fun getItemCount(): Int {
        return list.size
    }


    fun update (new_list: List<saves>) {
        DiffUtil.calculateDiff(diff_ui_save(list, new_list)).dispatchUpdatesTo(this)
        this.list = saves_list
    }
}

class diff_ui_save (val past_list: List<saves>, val new_list: List<saves>): DiffUtil.Callback() {

    override fun getOldListSize(): Int = past_list.size

    override fun getNewListSize(): Int = new_list.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean = past_list[oldItemPosition].id == new_list[newItemPosition].id

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean = past_list[oldItemPosition] == new_list[newItemPosition]

}


class saves_holder(view: View): RecyclerView.ViewHolder(view) {

    // type 0 = ports - type 1 = ips


    val type = view.findViewById<ShapeableImageView>(R.id.type)
    // val name = view.findViewById<TextView>(R.id.name)
    // val ip_po = view.findViewById<TextView>(R.id.ip_po)

    val edit_button = view.findViewById<ShapeableImageView>(R.id.edit)
    val delete_button = view.findViewById<ShapeableImageView>(R.id.delete)

    // val copy_button = view.findViewById<ShapeableImageView>(R.id.copy)

    fun elements (saves_data: saves, edit: (saves) -> Unit, delete: (saves) -> Unit, copy: (saves) -> Unit) {

         /*if (saves_data.type == 0) {
            type.setImageResource(R.drawable.ports)
        }

          */

        // name.text = saves_data.name
        // ip_po.text = saves_data.value


        edit_button.setOnClickListener {
            edit(saves_data)
        }

        delete_button.setOnLongClickListener(object: View.OnLongClickListener {
            override fun onLongClick(v: View?): Boolean {
                delete(saves_data)
                return true
            }
        })

        /*copy_button.setOnClickListener {
            copy(saves_data)
        }
         */
    }
}