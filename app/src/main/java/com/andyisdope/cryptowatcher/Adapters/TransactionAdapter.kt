package com.andyisdope.cryptowatcher.Adapters

import android.content.Context
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import com.andyisdope.cryptowatcher.R
import com.andyisdope.cryptowatcher.model.Transaction
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

class TransactionAdapter(private val mContext: Context, private val mItems: ArrayList<Transaction>) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    var formatterLarge: NumberFormat = DecimalFormat("#,###.00")
    var formatterSmall: NumberFormat = DecimalFormat("#,##0.00")
    var formatterTiny: NumberFormat = DecimalFormat("#0.0##E0")

    fun parseUnix(time: Long): String {
        val date = Date(time)
        val sdf = SimpleDateFormat("MM/dd/yy")
        return sdf.format(date)
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {

        val layoutId = R.layout.list_item_transaction

        val inflater = LayoutInflater.from(mContext)
        val itemView = inflater.inflate(layoutId, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return mItems.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mItems[position]
        Log.i("Database", item.toString())
        with(holder)
        {
            TransactionDate.text = parseUnix(item.Date)
            TransactionSell.isChecked = item.Sell
            TransactionBuy.isChecked = item.Buy
            TransactionPrice.text = when {
                item.Price < .01 -> "$ ${formatterTiny.format(item.Price)}"
                (item.Price < 10.0 && item.Price > .01) -> "$ ${formatterSmall.format(item.Price)}"
                else -> "$ ${formatterLarge.format(item.Price)}"
            }
            TransactionAmount.text = when {
                item.Amount < .01 -> "${formatterTiny.format(item.Amount)}"
                (item.Amount < 10.0 && item.Amount > .01) -> "${formatterSmall.format(item.Amount)}"
                else -> "${formatterLarge.format(item.Amount)}"
            }
            when (item.Sell) {
                true -> {
                    TransactionNet.text = when {
                        abs(item.Net) < .01 -> "$ ${formatterTiny.format(item.Net)}"
                        (abs(item.Net) < 10.0 && abs(item.Net) > .01) -> "$ ${formatterSmall.format(item.Net)}"
                        else -> "$ ${formatterLarge.format(item.Net)}"
                    }
                    TransactionNet.setTextColor(Color.GREEN)
                }
                false -> {
                    TransactionNet.text = when {
                        abs(item.Net) < .01 -> "$ ${formatterTiny.format(item.Net)}"
                        (abs(item.Net) < 10.0 && abs(item.Net) > .01) -> "$ ${formatterSmall.format(item.Net)}"
                        else -> "$ ${formatterLarge.format(item.Net)}"
                    }
                    TransactionNet.setTextColor(Color.RED)
                }
            }
        }

    }


    class ViewHolder(mView: View) : RecyclerView.ViewHolder(mView) {
        var TransactionDate: TextView
        var TransactionBuy: RadioButton
        var TransactionSell: RadioButton
        var TransactionPrice: TextView
        var TransactionAmount: TextView
        var TransactionNet: TextView

        init {
            TransactionDate = mView.findViewById(R.id.TransactionItemDate)
            TransactionBuy = mView.findViewById(R.id.TransactionItemBuy)
            TransactionSell = mView.findViewById(R.id.TransactionItemSell)
            TransactionPrice = mView.findViewById(R.id.TransactionItemPrice)
            TransactionAmount = mView.findViewById(R.id.TransactionItemAmount)
            TransactionNet = mView.findViewById(R.id.TransactionItemNet)

        }

    }
}