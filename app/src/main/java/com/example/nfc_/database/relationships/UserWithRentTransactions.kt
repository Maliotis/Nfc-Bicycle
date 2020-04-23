package com.example.nfc_.database.relationships

import androidx.room.Embedded
import androidx.room.Relation
import com.example.nfc_.database.entities.RentTransaction
import com.example.nfc_.database.entities.User

/**
 * Created by petrosmaliotis on 2020-01-04.
 */

data class UserWithRentTransactions(
    @Embedded val user: User,
    @Relation(parentColumn = "uid", entityColumn = "userCreatorId")
    val rentTransactions: MutableList<RentTransaction>
)