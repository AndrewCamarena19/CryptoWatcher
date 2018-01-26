package com.andyisdope.cryptowatcher.model

import android.os.Parcel
import android.os.Parcelable
import com.andyisdope.cryptowatcher.CurrencyDetail

/**
 * Created by Andy on 1/25/2018.
 */
data class CurrencyDetails(var Date: String, var High: String, var Open: String, var Low: String, var Close: String, var Volume: String) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(Date)
        parcel.writeString(High)
        parcel.writeString(Open)
        parcel.writeString(Low)
        parcel.writeString(Close)
        parcel.writeString(Volume)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CurrencyDetails> {
        override fun createFromParcel(parcel: Parcel): CurrencyDetails {
            return CurrencyDetails(parcel)
        }

        override fun newArray(size: Int): Array<CurrencyDetails?> {
            return arrayOfNulls(size)
        }
    }

}