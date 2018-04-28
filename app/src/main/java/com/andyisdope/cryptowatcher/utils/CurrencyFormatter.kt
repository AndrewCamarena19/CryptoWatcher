package com.andyisdope.cryptowatcher.utils

import java.text.DecimalFormat
import java.text.NumberFormat

class CurrencyFormatter {
    companion object {
        val formatterView: NumberFormat = DecimalFormat("#,###.00000")
        val formatterLarge: NumberFormat = DecimalFormat("#,###.000")
        val formatterSmall: NumberFormat = DecimalFormat("#,##0.000")
        val formatterTiny: NumberFormat = DecimalFormat("#0.0##E0")

    }

}