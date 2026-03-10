package com.test_tcp

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.Fragment
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.FragmentActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import kotlin.toString




fun promt (promt_info: String =  "Authenticate"): BiometricPrompt.PromptInfo {
    return BiometricPrompt.PromptInfo.Builder().apply {
        setTitle(promt_info)
        setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
    }.build()
}

fun recalculate_values (pass: String, pref: SharedPreferences) {
    pref.edit().putString("salt", Base64.getEncoder().withoutPadding().encodeToString(SecureRandom().generateSeed(16))).commit()
    pref.edit().putString("hash", Base64.getEncoder().withoutPadding().encodeToString(MessageDigest.getInstance("SHA256").digest(pass.toByteArray() + Base64.getDecoder().decode(pref.getString("salt", ""))))).commit()

    pref.edit().putString("k_u", pass).commit()
}

fun very (pass: String, pref: SharedPreferences): Boolean {

    if (MessageDigest.isEqual(Base64.getDecoder().decode(pref.getString("hash", "")), MessageDigest.getInstance("SHA256").digest(pass.toByteArray() + Base64.getDecoder().decode(pref.getString("salt", ""))))) {
        recalculate_values(pass, pref)
        return true
    } else {
        return false
    }
}

fun very_data (data: String, port: Int = 0): Boolean {

    if (data.isNotEmpty() && port == 0 && data.toInt() >= 6000) {
        return true
    } else if (data.isNotEmpty() && port == 1 && data.matches(Regex("([0-9]{1,3}\\.){3}[0-9]{1,3}"))){
        return true
    } else {
        return false
    }
}

fun load (context: Context, info: String): Dialog {
    val dialog = Dialog(context)
    val view = LayoutInflater.from(context).inflate(R.layout.load, null)

    val info_load = view.findViewById<TextView>(R.id.info_load)
    info_load.text = info


    dialog.setContentView(view)
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.show()

    return dialog
}

@SuppressLint("MissingInflatedId")
fun block_very (dialog_global: Dialog, contex: Context, pref: SharedPreferences) {

    if (pref.getBoolean("block", false)) {
        val dialog = Dialog(contex)
        val view = LayoutInflater.from(contex).inflate(R.layout.block_view, null)

        val info_ac = view.findViewById<TextView>(R.id.info_ac)
        val time_view = view.findViewById<TextView>(R.id.time)

        val scope = CoroutineScope(Dispatchers.IO).launch {
            for (time in (60 * pref.getInt("multi", 0)).downTo(0)) {
                withContext(Dispatchers.Main) {
                    time_view.text = time.toString()
                }
                delay(1000)
            }
            pref.edit().putBoolean("block", false).commit()

            withContext(Dispatchers.Main) {
                info_ac.text = "Access restored"
                delay(300)
                dialog.dismiss()
            }
            cancel()
        }
        scope.start()

        dialog.setOnDismissListener(object: DialogInterface.OnDismissListener {
            override fun onDismiss(p0: DialogInterface?) {
                scope.cancel()
            }

        })

        dialog.setContentView(view)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    } else {
        dialog_global.show()
    }

}

fun dialog_login (context: FragmentActivity, pref: SharedPreferences, click: () -> Unit) {

    val dialog = Dialog(context)
    val view = LayoutInflater.from(context).inflate(R.layout.pass_auth, null)

    val info = view.findViewById<TextView>(R.id.info_very_pass)
    val opor = view.findViewById<TextView>(R.id.opor)
    val input_pass = view.findViewById<TextInputEditText>(R.id.input_pass)
    val very_progress = view.findViewById<LinearProgressIndicator>(R.id.progress)
    val bits = view.findViewById<TextView>(R.id.bits)
    val bottom = view.findViewById<ShapeableImageView>(R.id.create_pass)

    if (pref.getBoolean("start", false)) {
        opor.text = "/ ".repeat(pref.getInt("opor", 9))
    }

    if (!pref.getBoolean("start", false)) {
        info.text = "Create your password //"
    }

    var bits_count = 0
    input_pass.addTextChangedListener {
        bits_count = entropy(it.toString(), very_progress)
        bits.text = "${if (bits_count > 999.0) {"+999"} else {bits_count}} bits"
    }

    bottom.setOnClickListener {
        if (pref.getBoolean("start", false)) {
            if (input_pass.text!!.isNotEmpty()) {
                if (very(input_pass.text.toString(), pref)) {
                    BiometricPrompt(context, ContextCompat.getMainExecutor(context), object : BiometricPrompt.AuthenticationCallback() {
                            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                super.onAuthenticationSucceeded(result)
                                dialog.dismiss()
                                click()
                            }

                            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                                super.onAuthenticationError(errorCode, errString)
                                Toast.makeText(context, "Authentication error", Toast.LENGTH_SHORT).show()
                            }
                        }).authenticate(promt())
                } else {
                    if ((pref.getInt("opor", 9) - 1) == 0) {
                        pref.edit().putInt("opor", 9).commit()
                        pref.edit().putInt("multi", pref.getInt("multi", 0) + 1).commit()
                        pref.edit().putBoolean("block", true).commit()
                        dialog.dismiss()
                        block_very(dialog, context, pref)
                    } else {
                        input_pass.setText("")
                        pref.edit().putInt("opor", pref.getInt("opor", 9) - 1).commit()
                        opor.text = "/ ".repeat(pref.getInt("opor", 9))
                    }
                }
            } else {
                Toast.makeText(context, "The field is empty", Toast.LENGTH_SHORT).show()
            }
        } else {
            MaterialAlertDialogBuilder(context).apply {
                setTitle("Are you sure you want this to be your password?")
                setMessage("This password can never be changed again")
                setPositiveButton("I'm sure") {_, _ ->

                    BiometricPrompt(context, ContextCompat.getMainExecutor(context), object: BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            super.onAuthenticationSucceeded(result)
                            val load = load(context, "Creating your cryptographic key...")

                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
                                        init(
                                            KeyGenParameterSpec.Builder(input_pass.text.toString(), KeyProperties.PURPOSE_DECRYPT or KeyProperties.PURPOSE_ENCRYPT).apply {
                                                setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                                                setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                                            }.build()
                                        )
                                    }.generateKey()

                                    pref.edit().putBoolean("start", true).commit()
                                    recalculate_values(input_pass.text.toString(), pref)

                                    withContext(Dispatchers.Main) {
                                        input_pass.setText("")
                                        info.text = "Specify your password //"
                                        Toast.makeText(context, "Cryptographic key created", Toast.LENGTH_SHORT).show()
                                        opor.text = "/ ".repeat(pref.getInt("opor", 9))
                                    }

                                } catch (e: Exception) {
                                    pref.edit().putString("k_u", "").commit()
                                    pref.edit().putString("hash", "").commit()
                                    pref.edit().putString("salt", "").commit()
                                    pref.edit().putBoolean("start", false).commit()

                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, "Error creating cryptographic key", Toast.LENGTH_SHORT).show()
                                    }
                                    Log.e("Error creating cryptographic key", e.toString())
                                } finally {
                                    withContext(Dispatchers.Main) {
                                        load.dismiss()
                                    }
                                    cancel()
                                }
                            }
                        }

                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                            super.onAuthenticationError(errorCode, errString)
                            Toast.makeText(context, "Authentication error", Toast.LENGTH_SHORT).show()
                        }

                    } ).authenticate(promt())



                }
                setNegativeButton("let me think") {_, _ -> }
            }.show()
        }
    }


    dialog.setContentView(view)
    dialog.window?.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    block_very(dialog, context, pref)

}



@SuppressLint("MissingInflatedId")
fun values_dialog (context: FragmentActivity, pref: SharedPreferences, icon: Int, ip: String, port: String, click: (encript_values: String, iv: String) -> Unit) {

    val dialog = Dialog(context)
    val view = LayoutInflater.from(context).inflate(R.layout.dialog_saves, null)

    val input_name = view.findViewById<TextInputEditText>(R.id.input_name)
    val input_ip = view.findViewById<TextInputEditText>(R.id.input_ip)
    val input_port = view.findViewById<TextInputEditText>(R.id.input_port)

    val global_bottom = view.findViewById<ShapeableImageView>(R.id.global)

    input_ip.setText(ip)
    input_port.setText(port)

    global_bottom.setImageResource(icon)

    global_bottom.setOnClickListener {
        if (input_name.text!!.isNotEmpty() && ((input_ip.text!!.isEmpty() || very_data(input_ip.text.toString(), 1)) && (input_port.text!!.isEmpty() || very_data(input_port.text.toString())))) {

            BiometricPrompt(context, ContextCompat.getMainExecutor(context), object: BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    val values_global = JSONObject().apply {
                        put("name", input_name.text.toString())
                        put("ip", if (input_ip.text!!.isEmpty()) {"Empty"} else {input_ip.text.toString()})
                        put("port", if (input_port.text!!.isEmpty()) {"Empty"} else {input_port.text.toString()})
                    }.toString()

                    val load = load(context, "Encrypting the values...")

                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

                            val c = Cipher.getInstance("AES/GCM/NoPadding")
                            c.init(Cipher.ENCRYPT_MODE, ks.getKey(pref.getString("k_u", ""), null))

                            click(
                                Base64.getEncoder().withoutPadding()
                                    .encodeToString(c.doFinal(values_global.toByteArray())),
                                Base64.getEncoder().withoutPadding().encodeToString(c.iv)
                            )
                            withContext(Dispatchers.Main) {
                                dialog.dismiss()
                            }

                        } catch (e: Exception) {
                            Log.e("Encrypt_values_error", e.toString())
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Error encrypting values", Toast.LENGTH_SHORT).show()
                            }
                        } finally {
                            pref.edit().putString("k_u", "").commit()
                            withContext(Dispatchers.Main) {
                                load.dismiss()
                            }
                            cancel()
                        }
                    }

                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(context, "Authentication error", Toast.LENGTH_SHORT).show()
                }
            }).authenticate(promt())

        } else {
            Toast.makeText(context, "Some value does not match", Toast.LENGTH_SHORT).show()
        }
    }



    dialog.setContentView(view)
    dialog.window?.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.show()
}