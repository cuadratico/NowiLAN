package com.test_tcp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.shapes.Shape
import android.net.wifi.WifiManager
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.SearchView
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
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
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.test_tcp.db_info.Companion.saves_list
import com.test_tcp.recy.chat
import com.test_tcp.recy.chat_adapter
import com.test_tcp.recy.saves.saves_adapter
import com.test_tcp.recy.saves.saves_class
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

    private lateinit var noti: ShapeableImageView

    private lateinit var mk: MasterKey
    private lateinit var pref: SharedPreferences

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        delegate.localNightMode = AppCompatDelegate.MODE_NIGHT_YES

        mk = MasterKey.Builder(this)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        pref = EncryptedSharedPreferences.create(this, "ap", mk, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)

        // modificar todos los elemtnos cambiados

        val recy_global = findViewById<RecyclerView>(R.id.recy)

        // top info space
        val back_top = findViewById<ConstraintLayout>(R.id.back_top)
        val close_channel = findViewById<ShapeableImageView>(R.id.close)
        val port_view = findViewById<TextView>(R.id.expre)
        noti = findViewById(R.id.noti)

        // center space
        val back_center = findViewById<ConstraintLayout>(R.id.back_center)

        val back_directions = findViewById<ConstraintLayout>(R.id.back_directions)
        val input_directions = findViewById<TextInputEditText>(R.id.direction)

        val info_connect  = findViewById<TextView>(R.id.info_connect)
        val input_port = findViewById<TextInputEditText>(R.id.port)

        val save_values = findViewById<ShapeableImageView>(R.id.save_values)
        val storage = findViewById<ShapeableImageView>(R.id.storage)

        val init_b = findViewById<ConstraintLayout>(R.id.button)

        val modi = findViewById<ShapeableImageView>(R.id.modi)

        // bottom space
        val back_bot = findViewById<ConstraintLayout>(R.id.back_bot)

        val input_message = findViewById<EditText>(R.id.input_message)
        val send_but = findViewById<ShapeableImageView>(R.id.send)

        // extra
        val info_nowi = findViewById<TextView>(R.id.info_nowi)




        // application features

        // start part

        back_top.visibility = View.INVISIBLE
        back_bot.visibility = View.INVISIBLE

        fun modi_change () {
            if (pref.getBoolean("modi", false)) {
                back_directions.visibility = View.INVISIBLE
                info_connect.visibility = View.VISIBLE
            } else {
                info_connect.visibility = View.INVISIBLE
                val animation = AnimationUtils.loadAnimation(this, R.anim.translate_input_directions)

                animation.setAnimationListener(object: Animation.AnimationListener {
                    override fun onAnimationEnd(p0: Animation?) {
                        input_directions.isEnabled = true
                    }

                    override fun onAnimationRepeat(p0: Animation?) {}

                    override fun onAnimationStart(p0: Animation?) {
                        back_directions.visibility = View.VISIBLE
                        input_directions.isEnabled = false
                    }
                })

                back_directions.startAnimation(animation)

            }
        }

        modi_change()

        modi.setOnClickListener {
            modi.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate_360))
            pref.edit().putBoolean("modi", !pref.getBoolean("modi", false)).commit()
            modi_change()
        }

        // notification part

        fun noti_state () {
            if (pref.getBoolean("noti", false)) {
                noti.setBackgroundResource(R.drawable.noti)
            } else {
                noti.setBackgroundResource(R.drawable.noti_silence)
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED) {
            this.requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100)
        } else {
            noti_state()
        }

        noti.setOnClickListener {
            pref.edit().putBoolean("noti", !pref.getBoolean("noti", false)).commit()
            noti_state()
        }

        // save values part

        var save = false

        fun can_save () {
            if (very_data(input_directions.text.toString(), 1) || very_data(input_port.text.toString())) {
                save_values.setImageResource(R.drawable.save)
                save = true
            } else {
                save_values.setImageResource(R.drawable.no_save)
                save = false
            }
        }

        input_directions.addTextChangedListener { can_save() }
        input_port.addTextChangedListener {  can_save() }

        save_values.setOnClickListener {
            if (save) {
                dialog_login(this, pref) {
                    values_dialog(this@MainActivity, pref, R.drawable.save_local, input_directions.text.toString(), input_port.text.toString()) { values_en, iv ->
                        val db = db_info(this)
                        db.add(values_en, iv)
                    }
                }
            } else {
                Toast.makeText(this, "There are no coincidences to keep", Toast.LENGTH_SHORT).show()
            }
        }

        storage.setOnClickListener {
            dialog_login(this@MainActivity, pref) {
                val db = db_info(this)

                if (db.select()) {

                    val bottom_dialog = BottomSheetDialog(this)
                    val bottom_view = LayoutInflater.from(this).inflate(R.layout.saves_inter, null)

                    val search_usr = bottom_view.findViewById<SearchView>(R.id.search_users)
                    val recy_saves = bottom_view.findViewById<RecyclerView>(R.id.recy)

                    // imprimir los valores en el recycler view y programar el search view

                    bottom_dialog.setContentView(bottom_view)
                    bottom_dialog.window?.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
                    bottom_dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    bottom_dialog.show()

                } else {
                    Toast.makeText(this, "There is no information in the database", Toast.LENGTH_SHORT).show()
                }
            }
        }





        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String?>, grantResults: IntArray, deviceId: Int) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)

        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pref.edit().putBoolean("noti", true).commit()
                noti.setBackgroundResource(R.drawable.noti)

                val noti_manager = this.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                noti_manager.createNotificationChannel(NotificationChannel("Nowi_l_id", "Nowi_l", NotificationManager.IMPORTANCE_MIN))

                Toast.makeText(this, "Notifications activated", Toast.LENGTH_SHORT).show()

                MaterialAlertDialogBuilder(this).apply {
                    setTitle("How can I disable notifications?")
                    setMessage("To turn notifications on or off, simply tap the bell icon in the top right corner (Even if you disable notifications, the notification channel will not be deleted for practicality and optimization)")
                    setPositiveButton("Ok") {_, _ -> }
                }.show()

            } else {
                noti.setBackgroundResource(R.drawable.noti_warning)
                Toast.makeText(this, "The notification permission has not been accepted", Toast.LENGTH_SHORT).show()
            }
        }
    }

}