package com.andyisdope.cryptowatcher.Adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.andyisdope.cryptowatch.Currency
import com.andyisdope.cryptowatcher.R
import com.andyisdope.cryptowatcher.model.Market
import com.andyisdope.cryptowatcher.utils.CurrencyFormatter
import java.text.DecimalFormat
import java.text.NumberFormat

/**
 * Created by Andy on 2/6/2018.
 */
class MarketAdapter(private val mContext: Context, private val mItems: ArrayList<Market>) : RecyclerView.Adapter<MarketAdapter.ViewHolder>() {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mItems[position]
        with(holder)
        {

            MarketName.text = item.market + "     "
            PairName.text = item.pair + "     "
            Updated.text = item.update
            VolPer.text = "${CurrencyFormatter.formatterSmall.format(item.volPer.toFloat())}%"

            when (CurrentCurrency) {
                "USD" -> {
                    VolNum.text = "$ ${CurrencyFormatter.formatterLarge.format(item.volUSD)}"
                    PriceNum.text = "$ ${CurrencyFormatter.formatterLarge.format(item.priceUSD)}"
                }
                "ETH" -> {
                    VolNum.text = "${CurrencyFormatter.formatterLarge.format(item.volUSD/ Currency.ETH)} ETH "
                    PriceNum.text = "${CurrencyFormatter.formatterLarge.format(item.priceUSD/ Currency.ETH)} ETH "
                }
                "BTC" -> {
                    VolNum.text = "${CurrencyFormatter.formatterLarge.format(item.volUSD/Currency.BTC)} BTC "
                    PriceNum.text = "${CurrencyFormatter.formatterLarge.format(item.priceUSD/Currency.BTC)} BTC "
                }
            }

            holder.mView.setOnClickListener {
                //create activity for a single ticker with different viewports
                Toast.makeText(mContext, "You selected " + item.market,
                        Toast.LENGTH_SHORT).show()
                //                String itemId = item.getItemId();
                //val intent = Intent(mContext, DetailActivity::class.java)
                //intent.putExtra(ITEM_KEY, item)
                //mContext.startActivity(intent)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {

        val layoutId = R.layout.market_item

        val inflater = LayoutInflater.from(mContext)
        val itemView = inflater.inflate(layoutId, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return mItems.size
    }


    class ViewHolder(var mView: View) : RecyclerView.ViewHolder(mView) {

        var MarketName: TextView
        var Updated: TextView
        var PairName: TextView
        var VolNum: TextView
        var VolPer: TextView
        var PriceNum: TextView

        init {
            MarketName = mView.findViewById<TextView>(R.id.MarketName) as TextView
            Updated = mView.findViewById<TextView>(R.id.Updated) as TextView
            PairName = mView.findViewById<TextView>(R.id.PairName) as TextView
            VolNum = mView.findViewById<TextView>(R.id.VolumeNum) as TextView
            VolPer = mView.findViewById<TextView>(R.id.VolPer) as TextView
            PriceNum = mView.findViewById<TextView>(R.id.PriceNum) as TextView

        }
    }

    companion object {
        var CurrentCurrency = "USD"
    }
}