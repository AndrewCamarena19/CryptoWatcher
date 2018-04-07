package com.andyisdope.cryptowatcher.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import org.jetbrains.annotations.Nullable
import java.util.*

@Entity(tableName = "Transactions")
data class Transaction(@PrimaryKey() var Date: Date,
                  @ColumnInfo(name = "Coin") var Coin: String,
                  @ColumnInfo(name = "Amount") var Amount: Float,
                  @Nullable @ColumnInfo(name = "Buy") var Buy: Boolean,
                  @Nullable @ColumnInfo(name = "Sell") var Sell: Boolean,
                  @ColumnInfo(name = "Price") var Price: Float,
                  @ColumnInfo(name = "Net") var Net: Float)