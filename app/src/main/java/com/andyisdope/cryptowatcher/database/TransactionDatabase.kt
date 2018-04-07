package com.andyisdope.cryptowatcher.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.content.Context
import com.andyisdope.cryptowatcher.model.Transaction

@Database(entities = [Transaction::class], version = 1)
@TypeConverters(DateConverter::class)
abstract class TransactionDatabase : RoomDatabase() {

    abstract fun TransactionDao(): TransactionDAO

    companion object {
        private var Instance: TransactionDatabase? = null

        fun getInstance(context: Context): TransactionDatabase? {
            Instance ?: synchronized(TransactionDatabase::class)
            {
                Instance = Room.databaseBuilder(context.applicationContext,
                        TransactionDatabase::class.java, "trans.db")
                        .allowMainThreadQueries()
                        .build()
            }
            return Instance
        }

        fun destroyInstance() {
            Instance = null
        }
    }
}