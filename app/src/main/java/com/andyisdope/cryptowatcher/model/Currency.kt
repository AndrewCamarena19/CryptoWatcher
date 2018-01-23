package com.andyisdope.cryptowatch

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by Andy on 1/18/2018.
 */
data class Currency(val Name: String, val Symbol: String, val Place: Int,
                    val MarketCap: String, val CurrentPrice: String, val HrChange: String, val TwoChange: String, val SevenChange: String, val Volume: String) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readInt(),
            parcel.readString(),
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
        parcel.writeString(MarketCap)
        parcel.writeString(CurrentPrice)
        parcel.writeString(HrChange)
        parcel.writeString(TwoChange)
        parcel.writeString(SevenChange)
        parcel.writeString(Volume)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Currency> {

        var SortMethod: String = ""
        var TimeFrame: String = ""
        var Order: String = "Ascending"

        override fun createFromParcel(parcel: Parcel): Currency {
            return Currency(parcel)
        }

        override fun newArray(size: Int): Array<Currency?> {
            return arrayOfNulls(size)
        }
    }


}