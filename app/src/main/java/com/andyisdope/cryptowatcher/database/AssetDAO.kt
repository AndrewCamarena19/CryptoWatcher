package com.andyisdope.cryptowatcher.database

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.andyisdope.cryptowatcher.model.DateAsset


@Dao
interface AssetDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg toAdd: DateAsset)

    @Query("Delete From Assets")
    fun deleteAll()

    @Query("Select * from Assets Order By Date")
    fun getAll(): List<DateAsset>

}