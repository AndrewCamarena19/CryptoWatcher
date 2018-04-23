package com.andyisdope.cryptowatcher.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import com.andyisdope.cryptowatcher.model.DateAsset


@Database(entities = [DateAsset::class], version = 1)
abstract class AssetDatabase : RoomDatabase(){

    abstract fun AssetDao(): AssetDAO

    companion object {
        private var Instance: AssetDatabase? = null

        fun getInstance(context: Context): AssetDatabase? {
            if (Instance == null) {
                synchronized(TransactionDatabase::class) {
                    Instance = Room.databaseBuilder(context.applicationContext,
                            AssetDatabase::class.java, "asset.db")
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