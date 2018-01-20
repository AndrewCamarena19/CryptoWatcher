package com.andyisdope.cryptowatch

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by Andy on 1/18/2018.
 */
data class Currency(val Name: String, val ImageURL: String, val Symbol: String, val Place: Int,
                        val FullName: String, val MarketCap: String, val CurrentPrice: String, val Change: String, val TimeFrame: String) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readInt(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(Name)
        parcel.writeString(ImageURL)
        parcel.writeString(Symbol)
        parcel.writeInt(Place)
        parcel.writeString(FullName)
        parcel.writeString(MarketCap)
        parcel.writeString(CurrentPrice)
        parcel.writeString(Change)
        parcel.writeString(TimeFrame)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Currency> {
        override fun createFromParcel(parcel: Parcel): Currency {
            return Currency(parcel)
        }

        override fun newArray(size: Int): Array<Currency?> {
            return arrayOfNulls(size)
        }
    }

}