package com.andyisdope.cryptowatcher.model

import android.os.Parcel
import android.os.Parcelable

data class Asset(val assetName: String, val assetHolding: Float) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readFloat()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(assetName)
        parcel.writeFloat(assetHolding)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Asset> {
        override fun createFromParcel(parcel: Parcel): Asset {
            return Asset(parcel)
        }

        override fun newArray(size: Int): Array<Asset?> {
            return arrayOfNulls(size)
        }
    }
}