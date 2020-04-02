package com.example.nfc_.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.nfc_.database.dao.RentTransactionDao
import com.example.nfc_.database.dao.UserDao
import com.example.nfc_.database.entities.RentTransaction
import com.example.nfc_.database.entities.User

/**
 * Created by petrosmaliotis on 2020-01-04.
 */

@Database(entities = arrayOf(User::class, RentTransaction::class), version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun rentTransaction(): RentTransactionDao
}