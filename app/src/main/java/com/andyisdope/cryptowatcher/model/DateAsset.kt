package com.andyisdope.cryptowatcher.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.ColumnInfo
import android.os.Parcel
import android.os.Parcelable

@Entity(tableName = "Assets")
data class DateAsset(@PrimaryKey() val Date: Long,
                     @ColumnInfo(name = "Sum") val Price: Float) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readLong(),
            parcel.readFloat()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(Date)
        parcel.writeFloat(Price)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DateAsset> {
        override fun createFromParcel(parcel: Parcel): DateAsset {
            return DateAsset(parcel)
        }

        override fun newArray(size: Int): Array<DateAsset?> {
            return arrayOfNulls(size)
        }
    }
}