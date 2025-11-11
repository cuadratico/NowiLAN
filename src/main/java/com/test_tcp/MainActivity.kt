package com.test_tcp

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatButton
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textfield.TextInputLayout
import com.test_tcp.R
import com.test_tcp.db_info.Companion.saves_list
import com.test_tcp.recy.chat
import com.test_tcp.recy.chat_adapter
import com.test_tcp.recy.saves_adapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.ServerSocket
import java.net.Socket
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec

class MainActivity : AppCompatActivity() {

    companion object {
        var update = false
    }
    private lateinit var channel: Socket
    private lateinit var adapter: chat_adapter
    private lateinit var dialog: Dialog
    private lateinit var dialog_auth: Dialog
    private lateinit var adapter_saves: saves_adapter
    private lateinit var load_dialog: Dialog
    private lateinit var dialog_pass: BottomSheetDialog


    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        delegate.localNightMode = AppCompatDelegate.MODE_NIGHT_YES


        val mk = MasterKey.Builder(this)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        val pref = EncryptedSharedPreferences.create(this, "ap", mk, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)

        val chat_list = mutableListOf<chat>()

        val recy = findViewById<RecyclerView>(R.id.recy)


        val wifi_icon = findViewById<ShapeableImageView>(R.id.wifi_icon)
        val wifi_reca = findViewById<AppCompatButton>(R.id.wifi_reca)

        val back_top = findViewById<ConstraintLayout>(R.id.back_top)
        val port_exp = findViewById<TextView>(R.id.expre)
        val close = findViewById<ShapeableImageView>(R.id.close)
        close.visibility = View.INVISIBLE

        val back_center = findViewById<ConstraintLayout>(R.id.back_center)

        val create_pass = findViewById<ShapeableImageView>(R.id.create_pass)

        val back_direction = findViewById<TextInputLayout>(R.id.back_direction)

        val input_direction = findViewById<EditText>(R.id.direction)
        val save_direction = findViewById<ShapeableImageView>(R.id.save_directions)
        val ips = findViewById<ShapeableImageView>(R.id.ips)
        save_direction.visibility = View.INVISIBLE

        val input_port = findViewById<EditText>(R.id.port)
        val save_ports = findViewById<ShapeableImageView>(R.id.save_ports)
        val ports = findViewById<ShapeableImageView>(R.id.ports)
        save_ports.visibility = View.INVISIBLE

        val button = findViewById<AppCompatButton>(R.id.button)
        val modi = findViewById<ShapeableImageView>(R.id.modi)

        val back_bot = findViewById<ConstraintLayout>(R.id.back_bot)

        val send = findViewById<ShapeableImageView>(R.id.send)
        val input_message = findViewById<EditText>(R.id.message)
        send.visibility = View.INVISIBLE


        wifi_icon.visibility = View.INVISIBLE
        wifi_reca.visibility = View.INVISIBLE

        back_top.visibility = View.INVISIBLE

        back_bot.visibility = View.INVISIBLE


        val wifi_manager = this.getSystemService(Context.WIFI_SERVICE) as WifiManager

        if (wifi_manager.wifiState != 3){
            back_center.visibility = View.INVISIBLE
            wifi_icon.visibility = View.VISIBLE
            wifi_reca.visibility = View.VISIBLE
        }

        wifi_reca.setOnClickListener {
            if (wifi_manager.wifiState == 3){
                back_center.visibility = View.VISIBLE
                wifi_icon.visibility = View.INVISIBLE
                wifi_reca.visibility = View.INVISIBLE
            }else {
                Toast.makeText(this, "You still don't have access to the Wi-Fi network", Toast.LENGTH_SHORT).show()
            }
        }


        fun button_version () {
            ports.visibility = View.VISIBLE
            if (pref.getBoolean("sta", false)) {
                button.text = "Connect"
                back_direction.visibility = View.VISIBLE
                ips.visibility = View.VISIBLE
            } else {
                back_direction.visibility = View.INVISIBLE
                ips.visibility = View.INVISIBLE
                button.text = "Create"
            }
            if (!pref.getBoolean("start", false)) {
                ips.visibility = View.INVISIBLE
                ports.visibility = View.INVISIBLE
            } else {
                create_pass.visibility = View.INVISIBLE
            }

        }

        button_version()



        modi.setOnClickListener {
            pref.edit().putBoolean("sta", !pref.getBoolean("sta", false)).commit()
            button_version()
        }

        val update_all = lifecycleScope.launch(Dispatchers.IO, start = CoroutineStart.LAZY) {

            while (true) {

                if (update) {
                    update = false
                    withContext(Dispatchers.Main) {
                        adapter_saves.update()
                        if (saves_list.isEmpty()) {
                            dialog_pass.dismiss()
                            Toast.makeText(this@MainActivity, "The list is empty", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                delay(50)
            }
        }


        val scope = lifecycleScope.launch (Dispatchers.IO, start = CoroutineStart.LAZY){

            try {
                if (!pref.getBoolean("sta", false)) {
                    val scoket = ServerSocket(input_port.text.toString().toInt())

                    channel = scoket.accept()
                } else {
                    channel = Socket(input_direction.text.toString(), input_port.text.toString().toInt())
                }
                withContext(Dispatchers.Main) {
                    dialog.dismiss()
                    input_direction.setText("")
                    input_port.setText("")
                    port_exp.text = channel.inetAddress.toString().replace("/", "")
                    send.visibility = View.VISIBLE
                    close.visibility = View.VISIBLE
                }
            }catch (e: Exception) {
                Log.e("Error", e.toString())
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Connection interrupted", Toast.LENGTH_SHORT).show()
                    recreate()
                }
            }

            while (true) {

                try {
                    if (channel.isConnected && channel.inputStream.read() != 0) {
                        val buffer = ByteArray(2048)

                        val read = channel.inputStream.read(buffer)

                        chat_list.add(chat(String(buffer, 0, read, Charsets.UTF_8), false))

                        Log.e("lista", chat_list.toString())
                        withContext(Dispatchers.Main) {
                            adapter.update(chat_list)
                            recy.scrollToPosition(chat_list.size)
                        }
                    }

                } catch (e: Exception) {
                    Log.e("Error", e.toString())
                    channel.close()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Connection interrupted", Toast.LENGTH_SHORT).show()
                        recreate()
                    }
                }

                delay(50)
            }
        }

        input_direction.addTextChangedListener { ip ->
            if (pref.getBoolean("start", false) && ip.toString().isNotEmpty() && very_data(ip.toString(), 1)) {
                save_direction.visibility = View.VISIBLE
            } else {
                save_direction.visibility = View.INVISIBLE
            }
        }

        input_port.addTextChangedListener { ports ->
            if (pref.getBoolean("start", false) && ports.toString().isNotEmpty() && very_data(ports.toString())) {
                save_ports.visibility = View.VISIBLE
            } else {
                save_ports.visibility = View.INVISIBLE
            }
        }

        button.setOnClickListener {
            if (input_port.text.isNotEmpty() && very_data(input_port.text.toString())) {

                if ((pref.getBoolean("sta", false) && very_data(input_direction.text.toString(), 1)) || !pref.getBoolean("sta", false)) {


                    val promt = BiometricPrompt.PromptInfo.Builder()
                        .setTitle("Port Authentication")
                        .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                        .build()


                    BiometricPrompt(this, ContextCompat.getMainExecutor(this), object : BiometricPrompt.AuthenticationCallback() {
                            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                super.onAuthenticationSucceeded(result)

                                dialog = Dialog(this@MainActivity)
                                val view = LayoutInflater.from(this@MainActivity).inflate(R.layout.connection_delay, null)

                                val progress_bar = view.findViewById<ProgressBar>(R.id.progress)
                                progress_bar.isActivated = true

                                dialog.setContentView(view)
                                dialog.setCancelable(false)
                                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                                dialog.show()

                                scope.start()

                                back_center.visibility = View.INVISIBLE

                                back_top.visibility = View.VISIBLE
                                back_bot.visibility = View.VISIBLE

                                adapter = chat_adapter(chat_list)

                                recy.adapter = adapter
                                recy.layoutManager = LinearLayoutManager(this@MainActivity)
                            }
                        }).authenticate(promt)
                }else {
                    Toast.makeText(this, "IP problems", Toast.LENGTH_SHORT).show()
                }

            } else {
                Toast.makeText(this, "Port problems", Toast.LENGTH_SHORT).show()
            }
        }

        send.setOnClickListener {
            lifecycleScope.launch (Dispatchers.IO) {
                channel.outputStream.write(" ${input_message.text.toString()}".toByteArray())
                cancel()
            }
            chat_list.add(chat(input_message.text.toString()))
            adapter.update(chat_list)
            recy.scrollToPosition(chat_list.size)
            input_message.text.clear()
        }

        close.setOnClickListener {

            MaterialAlertDialogBuilder(this).apply {
                setTitle("Are you sure you want to close the connection?")
                setMessage("If you close the connection, all chat information will be deleted.")
                setPositiveButton("Close") {_, _ ->
                    scope.cancel()
                    channel.close()
                    recreate()
                }
                setNegativeButton("Keep") {_, _ ->}
            }.show()
        }

        fun very_pass (): Pair<AppCompatButton, EditText> {

            dialog_auth = Dialog(this)
            val view = LayoutInflater.from(this).inflate(R.layout.pass_auth, null)

            val input_pass = view.findViewById<EditText>(R.id.input_pass)
            val progress = view.findViewById<LinearProgressIndicator>(R.id.progress)
            val very = view.findViewById<AppCompatButton>(R.id.create_pass)
            very.text = "Verify"

            input_pass.addTextChangedListener { data ->
                entropy(data.toString(), progress)
            }

            dialog_auth.setContentView(view)
            dialog_auth.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog_auth.show()

            return Pair(very, input_pass)
        }

        create_pass.setOnClickListener {

            val (create, input_pass) = very_pass()

            create.setOnClickListener {
                create.isEnabled = false
                input_pass.isEnabled = false
                if (input_pass.text.isNotEmpty()) {
                    val load = load(this, "Creating the cryptographic key")
                    try {
                        lifecycleScope.launch (Dispatchers.IO) {
                            val ks = KeyGenParameterSpec.Builder(input_pass.text.toString(), KeyProperties.PURPOSE_DECRYPT or KeyProperties.PURPOSE_ENCRYPT).apply {
                                setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                                setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                            }.build()

                            val kg = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
                                init(ks)
                            }

                            kg.generateKey()
                            pref.edit().putString("salt", Base64.getEncoder().withoutPadding().encodeToString(SecureRandom().generateSeed(16))).commit()
                            pref.edit().putString("hash", Base64.getEncoder().withoutPadding().encodeToString(MessageDigest.getInstance("SHA256").digest(input_pass.text.toString().toByteArray() + Base64.getDecoder().decode(pref.getString("salt", ""))))).commit()

                            Log.e("hash", Base64.getDecoder().decode(pref.getString("hash", "")).toString())

                            pref.edit().putBoolean("start", true).commit()

                            withContext(Dispatchers.Main) {
                                button_version()
                                load.dismiss()
                                dialog_auth.dismiss()
                            }
                            cancel()
                        }

                    } catch (e: Exception) {
                        Log.e("Error", e.toString())
                        Toast.makeText(this, "The operation could not be completed", Toast.LENGTH_SHORT).show()
                        pref.edit().putString("salt", "").commit()
                        pref.edit().putString("hash", "").commit()

                        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

                        if (ks.getKey(input_pass.text.toString(), null) != null) {
                            ks.deleteEntry(input_pass.text.toString())
                        }
                    }

                }
            }
        }



        fun very_db (type: Int) {

            val scope = CoroutineScope(Dispatchers.IO).launch (start = CoroutineStart.LAZY){
                val db = db_info(applicationContext)
                if (db.select(type.toString()) || saves_list.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        load_dialog = load(this@MainActivity, "decrypting the information")
                    }
                    try {
                        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

                        for (position in 0..saves_list.size - 1) {
                            val (id, name, value, iv, type) = saves_list[position]

                            val c = Cipher.getInstance("AES/GCM/NoPadding")
                            c.init(Cipher.DECRYPT_MODE, ks.getKey(pref.getString("k_u", ""), null), GCMParameterSpec(128, Base64.getDecoder().decode(iv)))

                            saves_list[position].value = String(c.doFinal(Base64.getDecoder().decode(value.toByteArray())))

                        }

                        withContext(Dispatchers.Main) {
                            Log.e("pass", saves_list.toString())
                            adapter_saves = saves_adapter(saves_list)
                            dialog_auth.dismiss()
                            dialog_pass = dialog_de(this@MainActivity, pref, adapter_saves, update_all)
                        }

                    } catch (e: Exception) {
                        Log.e("Error", e.toString())
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@MainActivity, "The information could not be decrypted", Toast.LENGTH_SHORT).show()
                            pref.edit().putString("k_u", "").commit()
                        }
                    } finally {
                        load_dialog.dismiss()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "The database is empty", Toast.LENGTH_SHORT).show()
                    }
                }

                cancel()
            }

            scope.start()

        }

        ips.setOnClickListener {
            val (very, input) = very_pass()

            very.setOnClickListener {
                if (very(this, input.text.toString(), pref)) {
                    very_db(1)
                    update_all.start()
                }
            }
        }

        ports.setOnClickListener {
            val (very, input) = very_pass()
            very.setOnClickListener {
                if (very(this, input.text.toString(), pref)) {
                    very_db(0)
                    update_all.start()
                }
            }
        }




        save_ports.setOnClickListener {
            val (very, input) = very_pass()

            very.setOnClickListener {
                if (very(this, input.text.toString(), pref)) {
                    save_values(this, pref, "Default name", 0, input_port.text.toString(), dialog_auth)
                }
            }
        }

        save_direction.setOnClickListener {
            val (very, input) = very_pass()

            very.setOnClickListener {
                if (very(this, input.text.toString(), pref)) {
                    save_values(this, pref, "Default name", 1, input_direction.text.toString(), dialog_auth)
                }
            }
        }


        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}