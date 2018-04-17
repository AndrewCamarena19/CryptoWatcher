package com.andyisdope.cryptowatcher.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.os.Parcel
import android.os.Parcelable
import org.jetbrains.annotations.Nullable
import java.util.*

@Entity(tableName = "Transactions")
data class Transaction(@PrimaryKey() val Date: Long,
                  @ColumnInfo(name = "Coin") val Coin: String,
                  @ColumnInfo(name = "Amount") val Amount: Float,
                  @ColumnInfo(name = "Buy") val Buy: Boolean,
                  @ColumnInfo(name = "Sell") val Sell: Boolean,
                  @ColumnInfo(name = "Price") val Price: Float,
                  @ColumnInfo(name = "Net") val Net: Float) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readLong(),
            parcel.readString(),
            parcel.readFloat(),
            parcel.readByte() != 0.toByte(),
            parcel.readByte() != 0.toByte(),
            parcel.readFloat(),
            parcel.readFloat()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(Date)
        parcel.writeString(Coin)
        parcel.writeFloat(Amount)
        parcel.writeByte(if (Buy) 1 else 0)
        parcel.writeByte(if (Sell) 1 else 0)
        parcel.writeFloat(Price)
        parcel.writeFloat(Net)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Transaction> {
        override fun createFromParcel(parcel: Parcel): Transaction {
            return Transaction(parcel)
        }

        override fun newArray(size: Int): Array<Transaction?> {
            return arrayOfNulls(size)
        }
    }
}