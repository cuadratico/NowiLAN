package com.test_tcp.recy

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.shapes.Shape
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.android.material.imageview.ShapeableImageView
import com.test_tcp.MainActivity.Companion.update
import com.test_tcp.R
import com.test_tcp.db_info
import com.test_tcp.db_info.Companion.saves_list
import com.test_tcp.saves
import com.test_tcp.very
import com.test_tcp.very_data
import java.security.KeyStore
import java.util.Base64
import javax.crypto.Cipher

class saves_holder(view: View): RecyclerView.ViewHolder(view) {

    // type 0 = ports - type 1 = ips


    val type = view.findViewById<ShapeableImageView>(R.id.type)
    val name = view.findViewById<TextView>(R.id.name)
    val ip_po = view.findViewById<TextView>(R.id.ip_po)

    val edit = view.findViewById<ShapeableImageView>(R.id.edit)
    val delete = view.findViewById<ShapeableImageView>(R.id.delete)

    val copy = view.findViewById<ShapeableImageView>(R.id.copy)
    @RequiresApi(Build.VERSION_CODES.O)
    fun elements (saves_data: saves) {

        if (saves_data.type == 0) {
            type.setImageResource(R.drawable.ports)
        }

        name.text = saves_data.name
        ip_po.text = saves_data.value

        val context = name.context

        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val mk = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        val pref = EncryptedSharedPreferences.create(context, "ap", mk, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)

        val db = db_info(context)

        edit.setOnClickListener {

            val dialog = Dialog(context)
            val view = LayoutInflater.from(context).inflate(R.layout.dialog_saves, null)

            val dialog_type = view.findViewById<ShapeableImageView>(R.id.type)

            val input_name = view.findViewById<EditText>(R.id.input_name)

            val input_value = view.findViewById<EditText>(R.id.input_ip_po)

            val edit_all = view.findViewById<ShapeableImageView>(R.id.edit)

            if (saves_data.type == 0) {
                dialog_type.setImageResource(R.drawable.ports)
                input_value.hint = "Ports"
            }
            input_name.setText(saves_data.name)
            input_value.setText(saves_data.value)

            edit_all.setOnClickListener {
                if (input_value.text.isNotEmpty() && input_name.text.isNotEmpty() && very_data(input_value.text.toString(), saves_data.type)) {
                    val c = Cipher.getInstance("AES/GCM/NoPadding")
                    c.init(Cipher.ENCRYPT_MODE, ks.getKey(pref.getString("k_u", ""), null))

                    db.update(saves_data.id, Base64.getEncoder().withoutPadding().encodeToString(c.doFinal(input_value.text.toString().toByteArray())), input_name.text.toString(), Base64.getEncoder().withoutPadding().encodeToString(c.iv))

                    val position = saves_list.indexOfFirst { it.id == saves_data.id }
                    saves_list[position].name = input_name.text.toString()
                    saves_list[position].value = input_value.text.toString()

                    update = true
                    dialog.dismiss()
                } else {
                    Toast.makeText(context, "Error with write fields", Toast.LENGTH_SHORT).show()
                }
            }



            dialog.setContentView(view)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.show()
        }

        delete.setOnLongClickListener(object: View.OnLongClickListener {
            override fun onLongClick(v: View?): Boolean {
                db.delete(saves_data.id)
                saves_list.removeIf { it.id == saves_data.id }
                update = true
                return true
            }
        })

        copy.setOnClickListener {
            val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

            manager.setPrimaryClip(ClipData.newPlainText("value", saves_data.value))
        }



    }
}