package com.test_tcp

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class saves (val id: Int, var name: String, var value: String, var iv: String, val type: Int)

class db_info (context: Context): SQLiteOpenHelper(context, "db_info", null, 1) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE db_info (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, value TEXT, iv INTEGER, type TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {}




    fun add (type: Int, ip_po: String, name: String, iv: String) {

        val db = this.writableDatabase

        db.execSQL("INSERT INTO db_info (name, value, iv, type) VALUES (?, ?, ?, ?)", arrayOf(name, ip_po, iv, type))

    }

    fun update (id: Int, ip_po: String, name: String, iv: String) {

        val db = this.writableDatabase

        db.execSQL("UPDATE db_info SET name = ?, value = ?, iv = ? WHERE id = ?", arrayOf(name, ip_po, iv, id))

    }

    fun delete (id: Int) {

        val db = this.writableDatabase

        db.execSQL("DELETE FROM db_info WHERE id = ?", arrayOf(id))

    }


    fun select (type: String): Boolean {

        val db = this.readableDatabase

        val query = db.rawQuery("SELECT * FROM db_info WHERE type = ?", arrayOf(type))

        fun add () {
            saves_list.add(saves(query.getInt(0), query.getString(1), query.getString(2), query.getString(3), query.getInt(4)))
        }

        if (query.moveToFirst()) {
            add()
            while (query.moveToNext()) {
                add()
            }

            return true
        } else {
            return false
        }

    }

    companion object {
        val saves_list = mutableListOf<saves>()
    }
}