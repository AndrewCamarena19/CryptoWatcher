package com.andyisdope.cryptowatcher.model

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by Andy on 2/6/2018.
 */
data class Market(val market: String, var pair: String, val volBTC: Float, val volUSD: Float, val priceBTC: Float, val priceUSD: Float, val volPer: String, val update: String) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readFloat(),
            parcel.readFloat(),
            parcel.readFloat(),
            parcel.readFloat(),
            parcel.readString(),
            parcel.readString()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(market)
        parcel.writeString(pair)
        parcel.writeFloat(volBTC)
        parcel.writeFloat(volUSD)
        parcel.writeFloat(priceBTC)
        parcel.writeFloat(priceUSD)
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