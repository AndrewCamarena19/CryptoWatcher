package com.andyisdope.cryptowatcher.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import com.andyisdope.cryptowatcher.model.Transaction

@Database(entities = [Transaction::class], version = 1)
abstract class TransactionDatabase : RoomDatabase() {

    abstract fun TransactionDao(): TransactionDAO

    companion object {
        private var Instance: TransactionDatabase? = null

        fun getInstance(context: Context): TransactionDatabase? {
            if (Instance == null) {
                synchronized(TransactionDatabase::class) {
                    Instance = Room.databaseBuilder(context.applicationContext,
                            TransactionDatabase::class.java, "weather.db")
                            .allowMainThreadQueries()
                            .build()
                }
            }
            return Instance
        }

        fun destroyInstance() {
            Instance = null
        }
    }
}