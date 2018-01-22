package com.andyisdope.cryptowatcher.model

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by Andy on 1/22/2018.
 */
class Tokens(val Name: String, val Symbol: String, val Place: Int, val Platform: String,
             val MarketCap: String, val CurrentPrice: String, val Change: String, val TimeFrame: String) : Parcelable {
    constructor(parcel: Parcel) : this(
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
        parcel.writeString(Symbol)
        parcel.writeInt(Place)
        parcel.writeString(Platform)
        parcel.writeString(MarketCap)
        parcel.writeString(CurrentPrice)
        parcel.writeString(Change)
        parcel.writeString(TimeFrame)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Tokens> {
        override fun createFromParcel(parcel: Parcel): Tokens {
            return Tokens(parcel)
        }

        override fun newArray(size: Int): Array<Tokens?> {
            return arrayOfNulls(size)
        }
    }
}