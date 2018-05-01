package com.andyisdope.cryptowatcher.Adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.andyisdope.cryptowatcher.R
import com.andyisdope.cryptowatcher.model.DateAsset
import com.andyisdope.cryptowatcher.utils.CurrencyFormatter

class USDAdapterItem (private val mContext: Context, private val mItems: ArrayList<DateAsset>) : RecyclerView.Adapter<USDAdapterItem.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {

        val layoutId = R.layout.list_item_usd

        val inflater = LayoutInflater.from(mContext)
        val itemView = inflater.inflate(layoutId, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return mItems.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mItems[position]
        with(holder)
        {
            PortfolioDate.text = item.Date
            PortfolioValue.text = "$ ${CurrencyFormatter.formatterView.format(item.Price)}"
        }

    }

    class ViewHolder(viewHolder: View) : RecyclerView.ViewHolder(viewHolder) {

        var PortfolioDate: TextView
        var PortfolioValue: TextView

        init {
            PortfolioDate = viewHolder.findViewById(R.id.PortfolioDate)
            PortfolioValue = viewHolder.findViewById(R.id.PortfolioAmount)
        }

    }
}