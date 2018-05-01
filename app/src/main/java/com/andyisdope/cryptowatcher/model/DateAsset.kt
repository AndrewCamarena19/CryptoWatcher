package com.andyisdope.cryptowatcher.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.os.Parcel
import android.os.Parcelable

@Entity(tableName = "Assets")
data class DateAsset(@PrimaryKey() val Date: String,
                     @ColumnInfo(name = "Sum") val Price: Double) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readDouble())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(Date)
        parcel.writeDouble(Price)
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