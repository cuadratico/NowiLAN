package com.test_tcp

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class saves (val id: Int, var json: String, var iv: String)

class db_info (context: Context): SQLiteOpenHelper(context, "db_global", null, 1) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE db_saves (id INTEGER PRIMARY KEY AUTOINCREMENT, json TEXT, iv TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {}




    fun add (json: String, iv: String) {

        val db = this.writableDatabase

        db.execSQL("INSERT INTO db_saves (json, iv) VALUES (?, ?)", arrayOf(json, iv))

    }

    fun update (id: Int, json: String, iv: String) {

        val db = this.writableDatabase

        db.execSQL("UPDATE db_saves SET json = ?, iv = ? WHERE id = ?", arrayOf(json, iv, id))

    }

    fun delete (id: Int) {

        val db = this.writableDatabase

        db.execSQL("DELETE FROM db_info WHERE id = ?", arrayOf(id))

    }


    fun select (): Boolean {

        val db = this.readableDatabase

        val query = db.rawQuery("SELECT * FROM db_saves", null)

        fun add () {
            saves_list = saves_list.plus(saves(query.getInt(0), query.getString(1), query.getString(2)))
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
        var saves_list = listOf<saves>()
    }
}