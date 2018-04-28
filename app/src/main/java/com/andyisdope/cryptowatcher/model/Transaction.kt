package com.andyisdope.cryptowatcher.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.os.Parcel
import android.os.Parcelable

@Entity(tableName = "Transactions")
data class Transaction(@PrimaryKey() val Date: Long,
                  @ColumnInfo(name = "Coin") val Coin: String,
                  @ColumnInfo(name = "Amount") val Amount: Double,
                  @ColumnInfo(name = "Buy") val Buy: Boolean,
                  @ColumnInfo(name = "Sell") val Sell: Boolean,
                  @ColumnInfo(name = "Price") val Price: Double,
                  @ColumnInfo(name = "Net") val Net: Double) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readLong(),
            parcel.readString(),
            parcel.readDouble(),
            parcel.readByte() != 0.toByte(),
            parcel.readByte() != 0.toByte(),
            parcel.readDouble(),
            parcel.readDouble()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(Date)
        parcel.writeString(Coin)
        parcel.writeDouble(Amount)
        parcel.writeByte(if (Buy) 1 else 0)
        parcel.writeByte(if (Sell) 1 else 0)
        parcel.writeDouble(Price)
        parcel.writeDouble(Net)
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