package com.test_tcp.recy

import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.test_tcp.R

class chat_holder(view: View): RecyclerView.ViewHolder(view) {


    val usr = view.findViewById<ConstraintLayout>(R.id.usr)
    val usr_mess = view.findViewById<TextView>(R.id.user_l_mess)

    val no_usr = view.findViewById<ConstraintLayout>(R.id.no_usr)
    val no_usr_mess = view.findViewById<TextView>(R.id.user_mess)

    fun ele (chat_data: chat) {

        if (chat_data.usr) {
            no_usr.visibility = View.INVISIBLE
            no_usr_mess.visibility = View.INVISIBLE
            usr_mess.text = chat_data.message
        }else {
            usr.visibility = View.INVISIBLE
            usr_mess.visibility = View.INVISIBLE
            no_usr_mess.text = chat_data.message
        }



    }
}