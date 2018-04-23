package com.andyisdope.cryptowatcher.Adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.andyisdope.cryptowatcher.R
import com.andyisdope.cryptowatcher.model.Asset
import com.github.mikephil.charting.charts.PieChart

class AssetAdapter(private val mContext: Context, private val mItems: ArrayList<Asset>) : RecyclerView.Adapter<AssetAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {

        val layoutId = R.layout.list_item_share

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
            vaultAssetName.text = item.assetName
            vaultAssetHolding.text = item.assetHolding.toString()
        }

    }

    class ViewHolder(var viewHolder: View) : RecyclerView.ViewHolder(viewHolder) {

        var vaultAssetName: TextView
        var vaultAssetHolding: TextView

        init {
            vaultAssetName = viewHolder.findViewById(R.id.VaultShareAsset)
            vaultAssetHolding = viewHolder.findViewById(R.id.VaultShareHoldings)
        }

    }
}