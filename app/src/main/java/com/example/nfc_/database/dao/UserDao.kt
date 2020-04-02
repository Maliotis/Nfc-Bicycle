package com.example.nfc_.database.dao

import androidx.room.*
import com.example.nfc_.database.entities.User
import com.example.nfc_.database.relationships.UserWithRentTransactions

/**
 * Created by petrosmaliotis on 2020-01-04.
 */

@Dao
interface UserDao {
    @Transaction
    @Query("SELECT * FROM  user")
    fun getUsersWithRentTransactions(): List<UserWithRentTransactions>

    @Transaction
    @Query("SELECT * FROM user, renttransaction " +
            "WHERE user.logIn = :logIn " +
            "AND renttransaction.active = :active LIMIT 1")
    fun getUserWithActiveRentTransaction(logIn: Boolean, active: Boolean): UserWithRentTransactions?

    @Transaction
    @Query("SELECT * FROM user WHERE uid IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<UserWithRentTransactions>

    @Query("SELECT * FROM user WHERE email IN (:email)")
    fun getUserByEmail(email: String?): User?

    @Transaction
    @Query("SELECT * FROM user WHERE first_name LIKE :first AND last_name LIKE :last LIMIT 1")
    fun findByName(first: String, last: String): UserWithRentTransactions

    @Transaction
    open fun setLoggedInUser(loggedInUser: User) {
        delete(loggedInUser)
        insertAll(loggedInUser)
    }

    @Insert
    fun insertAll(vararg users: User)

    @Delete
    fun delete(user: User)
}