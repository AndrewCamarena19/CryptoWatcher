package com.andyisdope.cryptowatcher.model

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by Andy on 2/6/2018.
 */
data class Market(val market: String, var pair: String, val volBTC: Double, val volUSD: Double, val priceBTC: Double, val priceUSD: Double, val volPer: String, val update: String) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readDouble(),
            parcel.readDouble(),
            parcel.readDouble(),
            parcel.readDouble(),
            parcel.readString(),
            parcel.readString()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(market)
        parcel.writeString(pair)
        parcel.writeDouble(volBTC)
        parcel.writeDouble(volUSD)
        parcel.writeDouble(priceBTC)
        parcel.writeDouble(priceUSD)
        parcel.writeString(volPer)
        parcel.writeString(update)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Market> {
        override fun createFromParcel(parcel: Parcel): Market {
            return Market(parcel)
        }

        override fun newArray(size: Int): Array<Market?> {
            return arrayOfNulls(size)
        }
    }

}