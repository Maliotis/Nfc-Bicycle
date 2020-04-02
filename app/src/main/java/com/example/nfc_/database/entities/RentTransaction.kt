package com.example.nfc_.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

/**
 * Created by petrosmaliotis on 2020-01-04.
 */

@Entity
data class RentTransaction (
    @PrimaryKey(autoGenerate = true) val tid: Int,
    val active: Boolean?,
    val amount: Double?,
    val date: String?,
    val userCreatorId: Int
)