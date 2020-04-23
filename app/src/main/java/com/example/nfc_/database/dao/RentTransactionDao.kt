package com.example.nfc_.database.dao

import androidx.room.*
import com.example.nfc_.database.entities.RentTransaction


/**
 * Created by petrosmaliotis on 2020-01-04.
 */

@Dao
interface RentTransactionDao {
    @Query("SELECT * FROM renttransaction")
    fun getAll(): List<RentTransaction>

    @Query("SELECT * FROM renttransaction WHERE tid IN (:transactionIds)")
    fun loadByAllIds(transactionIds: IntArray): List<RentTransaction>

    @Query("SELECT * FROM renttransaction WHERE tid LIKE :transactionId LIMIT 1")
    fun findById(transactionId: Int): RentTransaction

    @Update
    fun updateRentTransaction(vararg rentTransaction: RentTransaction)

    @Insert
    fun insertAll(vararg rentTransactions: RentTransaction)

    @Delete
    fun delete(rentTransaction: RentTransaction)

}