package com.andyisdope.cryptowatcher.model

import android.databinding.BaseObservable
import android.databinding.Bindable
import com.andyisdope.cryptowatcher.BR
import java.math.BigDecimal

class CurrencyModel(n: String) : BaseObservable() {

    fun setInvested() {
        invested = (currentPrice * units).setScale(3, BigDecimal.ROUND_HALF_UP)
        notifyPropertyChanged(BR.invested)
    }

    fun setNet(){
        net = (bought + sold).setScale(3, BigDecimal.ROUND_HALF_UP)
        notifyPropertyChanged(BR.net)
    }

    var name: String = n
        @Bindable get() = field
        set(value) {
            field = value
            notifyPropertyChanged(BR.name)
        }

    var units: BigDecimal = BigDecimal(0).setScale(3, BigDecimal.ROUND_HALF_UP)
        @Bindable get() = field
        set(value) {
            field = value
            notifyPropertyChanged(BR.units)
        }
    var usd: BigDecimal = BigDecimal(0).setScale(3, BigDecimal.ROUND_HALF_UP)
        @Bindable get() = field
        set(value) {
            field = value
            notifyPropertyChanged(BR.usd)
        }
    var currentPrice: BigDecimal = BigDecimal(0).setScale(3, BigDecimal.ROUND_HALF_UP)
        @Bindable get() = field
        set(value) {
            field = value
            notifyPropertyChanged(BR.currentPrice)

        }
    var invested: BigDecimal = BigDecimal(0).setScale(3, BigDecimal.ROUND_HALF_UP)
        @Bindable get() = field
        set(value) {
            field = value
            notifyPropertyChanged(BR.invested)

        }
    var bought: BigDecimal = BigDecimal(0).setScale(3, BigDecimal.ROUND_HALF_UP)
        @Bindable get() = field
        set(value) {
            field = value
            notifyPropertyChanged(BR.bought)

        }
    var sold: BigDecimal = BigDecimal(0).setScale(3, BigDecimal.ROUND_HALF_UP)
        @Bindable get() = field
        set(value) {
            field = value
            notifyPropertyChanged(BR.sold)

        }
    var net: BigDecimal = BigDecimal(0).setScale(3, BigDecimal.ROUND_HALF_UP)
        @Bindable get() = field
        set(value) {
            field = value
            notifyPropertyChanged(BR.net)
        }
    var symbol: String = ""
        @Bindable get() = field
        set(value) {
            field = value
            notifyPropertyChanged(BR.symbol)

        }

}