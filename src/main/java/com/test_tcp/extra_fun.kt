package com.test_tcp

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.test_tcp.db_info.Companion.saves_list
import com.test_tcp.recy.saves_adapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import kotlin.toString


fun load (context: Activity, text: String): Dialog {

    val dialog = Dialog(context)
    val view = LayoutInflater.from(context).inflate(R.layout.connection_delay, null)

    val progress = view.findViewById<ProgressBar>(R.id.progress)
    val info_text = view.findViewById<TextView>(R.id.text)

    progress.isActivated = true
    info_text.text = text

    dialog.setContentView(view)
    dialog.setCancelable(false)
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.show()

    return dialog
}

fun dialog_de (context: Activity, pref: SharedPreferences, adapter: saves_adapter, coroutine: Job): BottomSheetDialog {

    val dialog = BottomSheetDialog(context)
    val view = LayoutInflater.from(context).inflate(R.layout.saves_inter, null)

    val recy = view.findViewById<RecyclerView>(R.id.recy)

    Log.e("lista", saves_list.toString())
    recy.adapter = adapter
    recy.layoutManager = LinearLayoutManager(context)

    dialog.setContentView(view)
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.show()

    dialog.setOnDismissListener(object: DialogInterface.OnDismissListener {
        override fun onDismiss(dialog: DialogInterface?) {
            coroutine.cancel()
            pref.edit().putString("k_u", "").commit()
            saves_list.clear()
        }

    })

    return dialog

}




@RequiresApi(Build.VERSION_CODES.O)
fun very (context: Context, pass: String, pref: SharedPreferences): Boolean {

    if (MessageDigest.isEqual(Base64.getDecoder().decode(pref.getString("hash", "")), MessageDigest.getInstance("SHA256").digest(pass.toByteArray() + Base64.getDecoder().decode(pref.getString("salt", ""))))) {

        pref.edit().putString("salt", Base64.getEncoder().withoutPadding().encodeToString(SecureRandom().generateSeed(16))).commit()
        pref.edit().putString("hash", Base64.getEncoder().withoutPadding().encodeToString(MessageDigest.getInstance("SHA256").digest(pass.toByteArray() + Base64.getDecoder().decode(pref.getString("salt", ""))))).commit()

        pref.edit().putString("k_u", pass).commit()

        return true
    } else {
        Toast.makeText(context, "The password is incorrect", Toast.LENGTH_SHORT).show()
        return false
    }
}



@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("MissingInflatedId")


fun save_values (context: Activity, pref: SharedPreferences, name: String, type: Int, value: String, dialog: Dialog) {

    try {
        val db = db_info(context)
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

        val c = Cipher.getInstance("AES/GCM/NoPadding")
        c.init(Cipher.ENCRYPT_MODE, ks.getKey(pref.getString("k_u", ""), null))

        db.add(type, Base64.getEncoder().withoutPadding().encodeToString(c.doFinal(value.toByteArray())), name, Base64.getEncoder().withoutPadding().encodeToString(c.iv))

        dialog.dismiss()
        Toast.makeText(context, "Saved value", Toast.LENGTH_SHORT).show()

    } catch (e: Exception) {
        Log.e("Error", e.toString())
        Toast.makeText(context, "The information could not be saved", Toast.LENGTH_SHORT).show()
    } finally {
        pref.edit().putString("k_u", "").commit()
    }
}

fun very_data (data: String, port: Int = 0): Boolean {

    if (port == 0 && data.toInt() >= 6000) {
        return true
    } else if (port == 1 && data.matches(Regex("([0-9]{1,3}\\.){3}[0-9]{1,3}"))){
        return true
    } else {
        return false
    }
}