package com.example.nfc_.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by petrosmaliotis on 2020-01-04.
 */

@Entity
data class User (
    @PrimaryKey(autoGenerate = true) val uid: Int,
    @ColumnInfo(name = "first_name") val firstName: String?,
    @ColumnInfo(name = "last_name") val lastName: String?,
    val logIn: Boolean?,
    val email: String?
    )