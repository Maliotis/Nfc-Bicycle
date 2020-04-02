package com.example.nfc_.helpers

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.example.nfc_.database.AppDatabase

/**
 * Created by petrosmaliotis on 16/03/2020.
 */
val TAG = DatabaseClient::class.java.simpleName
object DatabaseClient {
    var db: AppDatabase? = null
    var applicationContext: Context? = null
        set(value) {
            if (field == null) {
                field = value
                if (db == null) {
                    db = Room.databaseBuilder(
                            applicationContext!!,
                            AppDatabase::class.java, "database.db"
                        )
                        .enableMultiInstanceInvalidation()
                        .build()
                }
            } else {
                Log.d(TAG, "applicationContext: already set")
            }
        }
}