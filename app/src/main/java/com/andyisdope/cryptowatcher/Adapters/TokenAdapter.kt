package com.andyisdope.cryptowatcher.Adapters

/**
 * Created by Andy on 1/19/2018.
 */
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.preference.PreferenceManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.andyisdope.cryptowatcher.CurrencyDetail
import com.andyisdope.cryptowatcher.R
import com.andyisdope.cryptowatcher.model.Tokens
import com.andyisdope.cryptowatcher.utils.CurrencyFormatter
import com.squareup.picasso.Picasso

import java.io.IOException
import java.text.DecimalFormat
import java.text.NumberFormat

class TokenAdapter(private val mContext: Context, private val mItems: ArrayList<Tokens>) : RecyclerView.Adapter<TokenAdapter.ViewHolder>() {

    private lateinit var list: ArrayList<HashMap<String, Tokens>>
    private val Image_Base_URL = "https://raw.githubusercontent.com/poc19/CryptoWatcher/master/images/"
    private val Data_Base_URL = "https://api.cryptowat.ch"
    val sharedPref = mContext.getSharedPreferences("Favorites", Context.MODE_PRIVATE)


    override fun getItemCount(): Int {
        return mItems.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val settings = PreferenceManager.getDefaultSharedPreferences(mContext)
        val layoutId = R.layout.list_item

        val inflater = LayoutInflater.from(mContext)
        val itemView = inflater.inflate(layoutId, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mItems[position]

        try {
            with(holder)
            {
                //val inputStream = mContext.assets.open(item.Symbol.plus(".png"))
                //val d = Drawable.createFromStream(inputStream, null)
                //holder.tickerImage.setImageDrawable(d)
                var url = Image_Base_URL.plus(item.Symbol.toUpperCase()).plus(".png?raw=true")
                Picasso.with(mContext).load(url)
                        .error(R.drawable.cream).into(holder.tickerImage)
                when (Tokens.TimeFrame) {
                    "Hourly" -> {
                        when {
                            (item.HrChange == "-9999") -> {
                                tickerChange.setTextColor(Color.WHITE)
                                tickerChange.text = "N/A"
                            }
                            item.HrChange.toFloat() <= 0 -> {
                                tickerChange.setTextColor(Color.RED)
                                tickerChange.text = "${CurrencyFormatter.formatterSmall.format(item.HrChange.toFloat())}%"
                            }
                            item.HrChange.toFloat() > 0 -> {
                                tickerChange.setTextColor(Color.GREEN)
                                tickerChange.text = "+${CurrencyFormatter.formatterSmall.format(item.HrChange.toFloat())}%"
                            }
                        }
                    }
                    "Daily" -> {
                        when {
                            (item.TwoChange == "-9999") -> {
                                tickerChange.setTextColor(Color.WHITE)
                                tickerChange.text = "N/A"
                            }
                            item.TwoChange.toFloat() <= 0 -> {
                                tickerChange.setTextColor(Color.RED)
                                tickerChange.text = "${CurrencyFormatter.formatterSmall.format(item.TwoChange.toFloat())}%"
                            }
                            item.TwoChange.toFloat() > 0 -> {
                                tickerChange.setTextColor(Color.GREEN)
                                tickerChange.text = "+${CurrencyFormatter.formatterSmall.format(item.TwoChange.toFloat())}%"
                            }
                        }
                    }
                    "Weekly" -> {
                        when {
                            (item.SevenChange == "-9999") -> {
                                tickerChange.setTextColor(Color.WHITE)
                                tickerChange.text = "N/A"
                            }
                            item.SevenChange.toFloat() <= 0 -> {
                                tickerChange.setTextColor(Color.RED)
                                tickerChange.text = "${CurrencyFormatter.formatterSmall.format(item.SevenChange.toFloat())}%"
                            }
                            item.SevenChange.toFloat() > 0 -> {
                                tickerChange.setTextColor(Color.GREEN)
                                tickerChange.text = "+${CurrencyFormatter.formatterSmall.format(item.SevenChange.toFloat())}%"
                            }
                        }
                    }
                }

                isFavourite.isChecked = item.isFavorite
                tickerSymbol.text = "(" + item.Symbol + ")"
                tickerPrice.text = when {
                    item.CurrentPrice == "-9999" -> "N/A"
                    item.CurrentPrice.toFloat() < .01 -> "$ ${CurrencyFormatter.formatterTiny.format(item.CurrentPrice.toFloat())}"
                    (item.CurrentPrice.toFloat() < 10.0 && item.CurrentPrice.toFloat() > .01) -> "$ ${CurrencyFormatter.formatterSmall.format(item.CurrentPrice.toFloat())}"
                    else -> "$ ${CurrencyFormatter.formatterLarge.format(item.CurrentPrice.toFloat())}"
                }
                tickerPlace.text = "" + item.Place
                tickerName.text = item.Name.toUpperCase()
                Platform.text = item.Symbol
                tickerMarketCap.text =
                        when {
                            (item.MarketCap == "-9999") -> "N/A"
                            (item.MarketCap.toFloat() < 1000000.0) -> CurrencyFormatter.formatterLarge.format(item.MarketCap.toFloat())
                            else -> CurrencyFormatter.formatterSmall.format(item.MarketCap.toFloat() / 1000000) + " M"
                        }
                tickerVolume.text =
                        when {
                            (item.Volume == "-9999") -> "N/A"
                            (item.Volume.toFloat() < 1000000.0) -> CurrencyFormatter.formatterLarge.format(item.Volume.toFloat())
                            else -> CurrencyFormatter.formatterSmall.format(item.Volume.toFloat() / 1000000) + " M"
                        }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        holder.isFavourite.setOnClickListener({
            if(holder.isFavourite.isChecked)
                with(sharedPref.edit()){
                    putString(item.Name, "${item.Symbol}")
                    commit()
                    Toast.makeText(mContext, "Added ${item.Name} refresh to view changes",Toast.LENGTH_SHORT).show()
                }
            else
                with(sharedPref.edit()){
                    remove(item.Name)
                    commit()
                    Toast.makeText(mContext, "Added ${item.Name} refresh to view changes",Toast.LENGTH_SHORT).show()
                }
        })

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
            var intent: Intent = Intent(mContext, CurrencyDetail::class.java)
            intent.putExtra("Currency", item.Name)
            intent.putExtra("Price", item.CurrentPrice)
            intent.putExtra("Image", Image_Base_URL.plus(item.Symbol.toUpperCase()).plus(".png?raw=true"))
            mContext.startActivity(intent)
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
        var tickerVolume: TextView
        var isFavourite: CheckBox

        init {

            tickerImage = mView.findViewById<ImageView>(R.id.tickerIcon) as ImageView
            tickerSymbol = mView.findViewById<TextView>(R.id.tickerSymbol) as TextView
            tickerPrice = mView.findViewById<TextView>(R.id.tickerPrice) as TextView
            tickerChange = mView.findViewById<TextView>(R.id.PriceChange) as TextView
            tickerMarketCap = mView.findViewById<TextView>(R.id.MarketCap) as TextView
            tickerPlace = mView.findViewById<TextView>(R.id.CoinPlace) as TextView
            tickerName = mView.findViewById<TextView>(R.id.tickerName) as TextView
            Platform = mView.findViewById<TextView>(R.id.Platform) as TextView
            tickerVolume = mView.findViewById<TextView>(R.id.tickerVolume) as TextView
            isFavourite = mView.findViewById<CheckBox>(R.id.checkFavorite) as CheckBox
        }
    }

    companion object {

        val ITEM_ID_KEY = "item_id_key"
        val ITEM_KEY = "item_key"
    }
}