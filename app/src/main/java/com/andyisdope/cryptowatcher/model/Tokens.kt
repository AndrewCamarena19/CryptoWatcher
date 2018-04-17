package com.andyisdope.cryptowatcher.model

import android.os.Parcel
import android.os.Parcelable
import java.util.*

/**
 * Created by Andy on 1/22/2018.
 */
data class Tokens(val Name: String, val Symbol: String, val Place: Int, val Platform: String, var isFavorite: Boolean, var Num: Double,
             val MarketCap: String, val CurrentPrice: String, val HrChange: String, val TwoChange: String, val SevenChange: String, val Volume: String) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readInt(),
            parcel.readString(),
            parcel.readByte() != 0.toByte(),
            parcel.readDouble(),
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
        parcel.writeString(Platform)
        parcel.writeByte(if (isFavorite) 1 else 0)
        parcel.writeDouble(Num)
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

    companion object CREATOR : Parcelable.Creator<Tokens> {

        var SortMethod: String = "Place"
        var TimeFrame = "Hourly"
        var Order = "Ascending"

        override fun createFromParcel(parcel: Parcel): Tokens {
            return Tokens(parcel)
        }

        override fun newArray(size: Int): Array<Tokens?> {
            return arrayOfNulls(size)
        }
    }

}