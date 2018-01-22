package com.andyisdope.cryptowatcher.Adapters

/**
 * Created by Andy on 1/19/2018.
 */
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.preference.PreferenceManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.andyisdope.cryptowatch.Currency
import com.andyisdope.cryptowatcher.R
import com.squareup.picasso.Picasso

import java.io.IOException

class CurrencyAdapter(private val mContext: Context, private val mItems: ArrayList<Currency>) : RecyclerView.Adapter<CurrencyAdapter.ViewHolder>() {

    private val list: ArrayList<HashMap<String, Currency>>? = null
    private val Image_Base_URL = "https://raw.githubusercontent.com/poc19/CryptoWatcher/master/images/"
    private val Data_Base_URL = "https://api.cryptowat.ch"

    override fun getItemCount(): Int {
        return mItems.size
    }

    private var prefsListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val settings = PreferenceManager.getDefaultSharedPreferences(mContext)
        prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key -> Log.i("preferences", "onSharedPreferenceChanged: " + key) }
        settings.registerOnSharedPreferenceChangeListener(prefsListener)

        val layoutId = R.layout.list_item

        val inflater = LayoutInflater.from(mContext)
        val itemView = inflater.inflate(layoutId, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mItems[position]

        try {
            //val inputStream = mContext.assets.open(item.Symbol.plus(".png"))
            //val d = Drawable.createFromStream(inputStream, null)
            //holder.tickerImage.setImageDrawable(d)
            var url = Image_Base_URL.plus(item.Symbol.toUpperCase()).plus(".png?raw=true")
            Picasso.with(mContext).load(url).error(R.drawable.cream).into(holder.tickerImage)
            holder.tickerSymbol.text = "("+item.Symbol+")"
            holder.tickerPrice.text = item.CurrentPrice
            when
            {
                (item.Change == "?") -> holder.tickerChange.setTextColor(Color.WHITE)
                item.Change.toDouble() <= 0 -> holder.tickerChange.setTextColor(Color.RED)
                item.Change.toDouble() > 0 -> holder.tickerChange.setTextColor(Color.GREEN)
            }
            holder.tickerChange.text = item.Change
            holder.tickerMarketCap.text = item.MarketCap
            holder.tickerPlace.text = "" + item.Place
            holder.tickerName.text = item.Name.toUpperCase()
            holder.Platform.text = item.Symbol

        } catch (e: IOException) {
            e.printStackTrace()
        }

        holder.mView.setOnClickListener {
            //create activity for a single ticker with different viewports
                           Toast.makeText(mContext, "You selected " + item.Name,
                                    Toast.LENGTH_SHORT).show()
            //                String itemId = item.getItemId();
            //val intent = Intent(mContext, DetailActivity::class.java)
            //intent.putExtra(ITEM_KEY, item)
            //mContext.startActivity(intent)
        }

        holder.mView.setOnLongClickListener {
            Toast.makeText(mContext, "You long clicked " + item.Name,
                    Toast.LENGTH_SHORT).show()
            false
        }
    }

    class ViewHolder(var mView: View) : RecyclerView.ViewHolder(mView) {

        var tickerImage: ImageView
        var tickerSymbol: TextView
        var tickerPrice: TextView
        var tickerChange: TextView
        var tickerMarketCap: TextView
        var tickerPlace: TextView
        var tickerName: TextView
        var Platform: TextView

        init {

            tickerImage = mView.findViewById<ImageView>(R.id.tickerIcon) as ImageView
            tickerSymbol = mView.findViewById<TextView>(R.id.tickerSymbol) as TextView
            tickerPrice = mView.findViewById<TextView>(R.id.tickerPrice) as TextView
            tickerChange = mView.findViewById<TextView>(R.id.PriceChange) as TextView
            tickerMarketCap = mView.findViewById<TextView>(R.id.MarketCap) as TextView
            tickerPlace = mView.findViewById<TextView>(R.id.CoinPlace) as TextView
            tickerName =  mView.findViewById<TextView>(R.id.tickerName) as TextView
            Platform = mView.findViewById<TextView>(R.id.Platform) as TextView

        }
    }

    companion object {

        val ITEM_ID_KEY = "item_id_key"
        val ITEM_KEY = "item_key"
    }
}