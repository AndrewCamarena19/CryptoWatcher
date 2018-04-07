package com.andyisdope.cryptowatcher.database

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import com.andyisdope.cryptowatcher.model.Transaction

@Dao
interface TransactionDAO {

    @Insert
    fun insertAll(vararg toAdd: Transaction)

    @Delete
    fun deleteTrans(toRemove: Transaction)

    @Query("Delete From Transactions")
    fun deleteAll()

    @Query("Select * from Transactions Order By Date")
    fun getAll(): ArrayList<Transaction>

    @Query("Select * from Transactions where Buy = 1")
    fun getAllBuys()

    @Query("Select * from Transactions where Sell = 1")
    fun getAllSells()

    @Query("Select * from Transactions where Coin = :coin")
    fun getCoinTransaction(coin: String)

    @Query("Select SUM(Net) from Transactions")
    fun getNetAssests()

    @Query("Select SUM(Net) from Transactions where Sell = 1")
    fun getSellSum()

    @Query("Select SUM(Net) from Transactions where Buy = 1")
    fun getBuySum()

    @Query("Select SUM(Amount) from Transactions where Coin = :coin")
    fun getCurrentCoins(coin: String)
}