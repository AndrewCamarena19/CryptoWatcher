package com.andyisdope.cryptowatcher

import android.content.*
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import com.andyisdope.cryptowatcher.Adapters.MarketAdapter
import com.andyisdope.cryptowatcher.Services.DataService
import com.andyisdope.cryptowatcher.database.TransactionDatabase
import com.andyisdope.cryptowatcher.model.CurrencyDetails
import com.andyisdope.cryptowatcher.model.Market
import com.andyisdope.cryptowatcher.model.Transaction
import com.andyisdope.cryptowatcher.utils.CurrencyFormatter
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.CandleStickChart
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.charts.CombinedChart.DrawOrder
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.squareup.picasso.Picasso
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class CurrencyDetail : AppCompatActivity() {
    //TODO: Create Master Vault view with pie chart and other graphs
    //TODO: Create functions for ranges and Buttons in transaction view
    private var signal: ArrayList<Float> = ArrayList()
    private var eMA12: ArrayList<Float> = ArrayList()
    private var eMA26: ArrayList<Float> = ArrayList()
    private var mACD: ArrayList<Float> = ArrayList()
    private var histo: ArrayList<Float> = ArrayList()
    private var displayMarkets: ArrayList<Market> = ArrayList()
    private var currencyDeets: ArrayList<CurrencyDetails> = ArrayList()
    private var marketDeets: ArrayList<Market> = ArrayList()
    private var marketPairs: TreeSet<String> = TreeSet()
    private var marketNames: TreeSet<String> = TreeSet()
    private lateinit var marketAdapt: MarketAdapter
    private lateinit var mMarketList: RecyclerView
    private lateinit var response: String
    private lateinit var marketResponse: String
    private lateinit var entry: TextView
    private lateinit var time: TextView
    private lateinit var marketSpinner: Spinner
    private lateinit var pairSpinner: Spinner
    private lateinit var sigData: LineDataSet
    private lateinit var mCombined: CombinedChart
    private lateinit var mBar: BarChart
    private lateinit var mCandle: CandleStickChart
    private lateinit var radioGroup: RadioGroup
    private lateinit var USD: RadioButton
    private lateinit var BTC: RadioButton
    private lateinit var ETH: RadioButton
    private lateinit var toTransactionHistory: Button
    private lateinit var toMainVault: Button
    private lateinit var curr: String
    private lateinit var currSymbol: String
    private var marketLoaded: Boolean = false
    private var xValues: ArrayList<String> = ArrayList()
    private var transactionDB: TransactionDatabase? = null
    private lateinit var vaultPref: SharedPreferences
    private var liquidUSD: Double = 0.0
    private lateinit var liquidText: TextView
    private lateinit var investedTV: TextView
    private lateinit var unitsHeldTV: TextView
    private lateinit var assetsSoldTV: TextView
    private lateinit var currentPriceTV: TextView
    private lateinit var currentNetTV: TextView
    private lateinit var assetsBoughtTV: TextView
    private lateinit var vaultLogo: ImageView
    private lateinit var amountToBuy: EditText
    private lateinit var amountToUSD: EditText
    private lateinit var amountToSell: EditText
    private lateinit var addFunds: EditText
    private lateinit var amountToCurrency: EditText
    private var numberOfCoins: Double = 0.0
    private var invested: Double = 0.0
    private var allBuys: Double = 0.0
    private var allSells: Double = 0.0
    private var currentPrice: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_currency_detail)

        initVariables()
        initSpinners()
        initTabs()
        initRadioGroup()

        LocalBroadcastManager.getInstance(applicationContext)
                .registerReceiver(mBroadcastReceiver,
                        IntentFilter(DataService.Currency))
        LocalBroadcastManager.getInstance(applicationContext)
                .registerReceiver(mBroadcastReceiver2,
                        IntentFilter(DataService.MARKET))


    }

    //Initialize Variables and UI components
    private fun initVariables() {
        val c: Calendar = Calendar.getInstance()
        val df = SimpleDateFormat("yyyyMMdd", Locale.US)
        val today: Int = df.format(c.time).toInt()
        val start: Int = today - 10000
        curr = intent.getStringExtra("Currency")
        currSymbol = intent.getStringExtra("Symbol")
        currentPrice = intent.getStringExtra("Price").toDouble()
        marketLoaded = false
        title = curr.toUpperCase()
        entry = findViewById(R.id.CurrencyPrice)
        time = findViewById(R.id.PriceTime)
        mMarketList = findViewById(R.id.MarketView)
        mMarketList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        //Request data of currency from today and 45 days ago
        requestData("https://coinmarketcap.com/currencies/${curr.toLowerCase()}/historical-data/?start=$start&end=$today")

    }

    //region Broadcast Receivers
    //First receiver for receiving Coin details for Candlestick, LineChart and Combined Chart
    private val mBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            //val dataItems = intent
            response = intent.getStringExtra(DataService.MY_SERVICE_PAYLOAD)// as Array<Currency>
            parseCoinDetails()
        }
    }

    //Second receiver for receiving Market Data on Currency
    private val mBroadcastReceiver2 = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            //val dataItems = intent
            marketResponse = intent.getStringExtra(DataService.MY_SERVICE_PAYLOAD)// as Array<Currency>
            (Toast.makeText(baseContext, "Select an Exchange or Pair to view", Toast.LENGTH_SHORT))
                    .apply { setGravity(Gravity.CENTER, 0, 0) }
                    .apply { view.textAlignment = View.TEXT_ALIGNMENT_CENTER }
                    .show()
            parseMarketDetails()


        }
    }
    //endregion

    //region Market and Chart Views
    //Radio group for Market Data tab for which currency to display in
    private fun initRadioGroup() {
        MarketAdapter.CurrentCurrency = "USD"
        radioGroup = findViewById(R.id.CurrencyRadio)
        USD = findViewById(R.id.RadioUSD)
        BTC = findViewById(R.id.RadioBTC)
        ETH = findViewById(R.id.RadioETH)
        //clear view and use apply
        radioGroup.setOnCheckedChangeListener { _, i ->
            when (i) {
                R.id.RadioUSD -> {
                    MarketAdapter.CurrentCurrency = "USD"
                    marketAdapt = MarketAdapter(baseContext, displayMarkets)
                    mMarketList.adapter = marketAdapt
                    mMarketList.adapter.notifyDataSetChanged()
                }
                R.id.RadioBTC -> {
                    MarketAdapter.CurrentCurrency = "BTC"
                    marketAdapt = MarketAdapter(baseContext, displayMarkets)
                    mMarketList.adapter = marketAdapt
                    mMarketList.adapter.notifyDataSetChanged()
                }
                R.id.RadioETH -> {
                    MarketAdapter.CurrentCurrency = "ETH"
                    marketAdapt = MarketAdapter(baseContext, displayMarkets)
                    mMarketList.adapter = marketAdapt
                    mMarketList.adapter.notifyDataSetChanged()
                }
            }
        }
    }

    //Init Market Spinners to allow user to select a market or currency pair to view
    private fun initSpinners() {
        marketSpinner = findViewById(R.id.MarketSpinner)
        pairSpinner = findViewById(R.id.PairSpinner)

        marketSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                displayMarkets.clear()
                val mark = marketSpinner.getItemAtPosition(p2).toString()
                marketDeets.asSequence()
                        .forEach { if (it.market == mark) displayMarkets.add(it) }

                marketAdapt = MarketAdapter(baseContext, displayMarkets)
                mMarketList.adapter = marketAdapt
                mMarketList.adapter.notifyDataSetChanged()
            }
        }
        pairSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                displayMarkets.clear()
                val mark = pairSpinner.getItemAtPosition(p2).toString()
                marketDeets.asSequence()
                        .forEach { if (it.pair == mark) displayMarkets.add(it) }

                marketAdapt = MarketAdapter(baseContext, displayMarkets)
                mMarketList.adapter = marketAdapt
                mMarketList.adapter.notifyDataSetChanged()
            }
        }
    }

    //Begin to get charts ready for display currently displays 45 most recent days
    private fun initAllCharts(count: Int) {
        if (currencyDeets.size > 45) {
            initBar(count)
            initCandle(count)
            initCombined(count)
        } else {
            //If currency is new then MACD chart cannot be displayed due to lack of data
            Toast.makeText(baseContext, "Currency has less than 45 days of data", Toast.LENGTH_SHORT)
                    .apply { setGravity(Gravity.CENTER, 0, 0) }
                    .apply { view.textAlignment = View.TEXT_ALIGNMENT_CENTER }
                    .show()
            initBar(currencyDeets.size - 1)
            initCandle(currencyDeets.size - 1)
        }
    }

    //Tab initialization and call methods when tab is selected
    private fun initTabs() {
        val tabs = findViewById<TabHost>(R.id.DetailTab)
        tabs.setup()
        with(tabs)
        {
            var spec: TabHost.TabSpec = tabs.newTabSpec("Charts")
            spec.setContent(R.id.Charts)
            spec.setIndicator("Charts")
            addTab(spec)
            spec = tabs.newTabSpec("Exchanges")
            spec.setContent(R.id.Exchanges)
            spec.setIndicator("Exchanges")
            addTab(spec)
            spec = tabs.newTabSpec("Vault")
            spec.setContent(R.id.Vault)
            spec.setIndicator("Vault")
            addTab(spec)
            setOnTabChangedListener { tabID ->
                when (tabID) {
                    "Charts" -> {
                    }
                    "Exchanges" -> {
                        requestMarketData()
                    }
                    "Vault" -> {
                        initDB()
                    }
                }
            }
        }
    }
    //endregion

    //region Load Coin Vault
    //Set Vault UI's display values
    private fun initVaultData() {
        toMainVault = findViewById(R.id.MainVaultBtn)
        toMainVault.setOnClickListener {
            val intent = Intent(this, VaultActivity::class.java)
            startActivity(intent)
        }
        toTransactionHistory = findViewById(R.id.ToTransactionHistory)
        toTransactionHistory.setOnClickListener {
            val intent = Intent(this, TransactionHistory::class.java)
            intent.putExtra("Coin", curr.toUpperCase())
            startActivity(intent)
        }
        vaultPref = baseContext.getSharedPreferences("Coins", Context.MODE_PRIVATE)

        liquidUSD = vaultPref.getString("USD", "0.0").toDouble()
        numberOfCoins = vaultPref.getString(currSymbol, "0.0").toDouble()
        invested = numberOfCoins * currentPrice

        vaultLogo = findViewById(R.id.VaultLogo)

        assetsBoughtTV = findViewById(R.id.VaultAssetsBought)
        assetsBoughtTV.text = "$ ${allBuys * -1}"

        assetsSoldTV = findViewById(R.id.VaultAssetsSold)
        assetsSoldTV.text = "$ $allSells"

        unitsHeldTV = findViewById(R.id.NumberCoins)
        unitsHeldTV.text = "${CurrencyFormatter.formatterView.format(numberOfCoins)}"

        currentPriceTV = findViewById(R.id.VaultCurrentPrice)
        currentPriceTV.text = "$ $currentPrice"

        currentNetTV = findViewById(R.id.VaultNet)
        currentNetTV.text = "$ ${allSells + allBuys}"

        investedTV = findViewById(R.id.VaultInvested)
        investedTV.text = "$ ${CurrencyFormatter.formatterView.format(invested)}"

        liquidText = findViewById(R.id.VaultLiquid)
        liquidText.text = "$ $liquidUSD"

        amountToBuy = findViewById(R.id.VaultBuyCoinsAmount)
        amountToSell = findViewById(R.id.VaultSellCoinsAmount)

        amountToCurrency = findViewById(R.id.VaultBuyCoinsPrice)
        amountToUSD = findViewById(R.id.VaultSellCoinsPrice)

        addFunds = findViewById(R.id.VaultAddFunds)

        //Load image using Picasso
        Picasso.with(applicationContext).load(intent.getStringExtra("Image"))
                .error(R.drawable.cream).into(vaultLogo)

    }

    //Set Listeners for UI
    private fun initVaultButtons() {
        vaultPref.registerOnSharedPreferenceChangeListener { sharedPreferences, _ ->
            numberOfCoins = sharedPreferences.getString(currSymbol, "0").toDouble()
            findViewById<TextView>(R.id.NumberCoins).text = "$numberOfCoins"
            currentNetTV.text = "$ ${allSells + allBuys}"
            assetsSoldTV.text = "$ ${allSells}"
            investedTV.text = "$ ${CurrencyFormatter.formatterView.format(numberOfCoins * currentPrice)}"
            liquidText.text = "$ $liquidUSD"
        }

        findViewById<TextView>(R.id.VaultName).text = curr.toUpperCase()

        //Adds funds to Liquid USD
        addFunds.setOnEditorActionListener { textView, i, _ ->
            val amt = textView.text.toString().toDoubleOrNull()
            var completed = true
            if (amt != null) {
                if (i == EditorInfo.IME_ACTION_DONE && amt > 0) {
                    liquidUSD += amt
                    liquidText.text = "$ $amt"
                    vaultPref.edit().putString("USD", liquidUSD.toString()).apply()
                    completed = false
                    Toast.makeText(this, "Added $ $amt to Liquid Funds", Toast.LENGTH_LONG).show()

                }
            } else {
                Toast.makeText(this, "Enter a valid amount", Toast.LENGTH_LONG).show()
                textView.text = ""
            }
            textView.text = ""
            completed
        }

        //Amount of currency to buy at current price point
        amountToBuy.setOnEditorActionListener { textView, i, _ ->
            var completed = false
            val amt = textView.text.toString().toDoubleOrNull()
            if (amt != null) {
                if (i == EditorInfo.IME_ACTION_DONE && amt > 0) {
                    completed = buyCoins(textView.text.toString().toDouble())
                }
                completed
            } else {
                Toast.makeText(this, "Enter a valid amount", Toast.LENGTH_LONG).show()
                textView.text = ""
                completed
            }
        }

        //Amount of USD to exchange for currency
        amountToUSD.setOnEditorActionListener { tv, i, _ ->
            var completed = false
            val amt = tv.text.toString().toDoubleOrNull()
            if (amt != null) {
                if (i == EditorInfo.IME_ACTION_DONE && amt > 0) {
                    completed = currencyToFiat(tv.text.toString().toDouble())
                }
                completed
            } else {
                Toast.makeText(this, "Enter a valid amount", Toast.LENGTH_LONG).show()
                tv.text = ""
                completed
            }
        }

        //Amount of currency to exchange to USD
        amountToCurrency.setOnEditorActionListener { tv, i, _ ->
            var completed = false
            val amt = tv.text.toString().toDoubleOrNull()
            if (amt != null) {
                if (i == EditorInfo.IME_ACTION_DONE && amt > 0) {
                    completed = fiatToCurrency(tv.text.toString().toDouble())
                }
                completed
            } else {
                Toast.makeText(this, "Enter a valid amount", Toast.LENGTH_LONG).show()
                tv.text = ""
                completed
            }
        }

        //Amount of coins to sell at current price point
        amountToSell.setOnEditorActionListener { tv, i, _ ->
            var completed = false
            val amt = tv.text.toString().toDoubleOrNull()
            if (amt != null) {
                if (i == EditorInfo.IME_ACTION_DONE && amt > 0) {
                    completed = sellCoins(tv.text.toString().toDouble())
                }
                completed
            } else {
                Toast.makeText(this, "Enter a valid amount", Toast.LENGTH_LONG).show()
                tv.text = ""
                completed
            }
        }
    }

    //Method to convert currency to USD must have enough to do so
    private fun currencyToFiat(amt: Double): Boolean {
        var completed = true
        if (amt > numberOfCoins * currentPrice) {
            Toast.makeText(this, "Not enough coins in vault to exchange", Toast.LENGTH_LONG)
                    .apply { setGravity(Gravity.CENTER, 0, 0) }
                    .apply { view.textAlignment = View.TEXT_ALIGNMENT_CENTER }
                    .show()
        } else {
            val numCoins = amt / currentPrice

            //Insert Transaction into Database for tracking
            val t0 = Transaction(Calendar.getInstance().time.time, curr.toUpperCase(), numCoins,
                    false, true, currentPrice, amt)
            transactionDB?.TransactionDao()?.insertAll(t0)

            Toast.makeText(this, "Exchanged $ $amt worth of $curr into USD", Toast.LENGTH_LONG)
                    .apply { setGravity(Gravity.CENTER, 0, 0) }
                    .apply { view.textAlignment = View.TEXT_ALIGNMENT_CENTER }
                    .show()

            allSells += amt
            numberOfCoins -= numCoins
            liquidUSD += amt
            assetsSoldTV.text = "$ $allSells"
            currentNetTV.text = "$ ${allSells + allBuys}"
            //Place current coins and usd into preferences
            vaultPref.edit().putString(currSymbol, CurrencyFormatter.formatterView.format(numberOfCoins)).apply()
            vaultPref.edit().putString("USD", liquidUSD.toString()).apply()
            completed = false
        }
        //If sold all coins remove from preferences to track
        if (numberOfCoins == 0.0)
            vaultPref.edit().remove(currSymbol).apply()
        amountToUSD.setText("")
        return completed
    }

    //USD to Currency method
    private fun fiatToCurrency(amt: Double): Boolean {
        var completed = true
        if (amt > liquidUSD) {
            Toast.makeText(this, "Not enough in vault to exchange, add more funds", Toast.LENGTH_LONG)
                    .apply { setGravity(Gravity.CENTER, 0, 0) }
                    .apply { view.textAlignment = View.TEXT_ALIGNMENT_CENTER }
                    .show()
        } else {
            val numCoins = amt / currentPrice
            //Create transaction record for tracking
            val t0 = Transaction(Calendar.getInstance().time.time, curr.toUpperCase(), numCoins,
                    true, false, currentPrice, (amt) * -1)
            transactionDB?.TransactionDao()?.insertAll(t0)
            allBuys -= amt
            numberOfCoins += numCoins
            liquidUSD -= amt
            //if USD is less than this amount zero it out to avoid tiny fractions
            if (liquidUSD < .00001) liquidUSD = 0.0
            assetsBoughtTV.text = "$ ${allBuys * -1}"
            currentNetTV.text = "$ ${allSells + allBuys}"
            Toast.makeText(this, "Exchanged $amt USD to $numCoins $curr", Toast.LENGTH_SHORT)
                    .apply { setGravity(Gravity.CENTER, 0, 0) }
                    .apply { view.textAlignment = View.TEXT_ALIGNMENT_CENTER }
                    .show()
            vaultPref.edit().putString(currSymbol, CurrencyFormatter.formatterView.format(numberOfCoins)).apply()
            vaultPref.edit().putString("USD", liquidUSD.toString()).apply()
            completed = false
        }
        amountToCurrency.setText("")
        return completed
    }

    //Sell Coins at current price point
    private fun sellCoins(amt: Double): Boolean {
        var completed = true
        if (amt > numberOfCoins) {
            Toast.makeText(this, "Not enough coins in vault to exchange", Toast.LENGTH_LONG)
                    .apply { setGravity(Gravity.CENTER, 0, 0) }
                    .apply { view.textAlignment = View.TEXT_ALIGNMENT_CENTER }
                    .show()
        } else {
            //Create record for tracking
            val t0 = Transaction(Calendar.getInstance().time.time, curr.toUpperCase(), amt,
                    false, true, currentPrice, (amt * currentPrice))
            transactionDB?.TransactionDao()?.insertAll(t0)
            Toast.makeText(this, "Sold $amt coins @$currentPrice for a net of ${currentPrice * amt}", Toast.LENGTH_LONG)
                    .apply { setGravity(Gravity.CENTER, 0, 0) }
                    .apply { view.textAlignment = View.TEXT_ALIGNMENT_CENTER }
                    .show()
            allSells += (currentPrice * amt)
            numberOfCoins -= amt
            liquidUSD += (currentPrice * amt)
            assetsSoldTV.text = "$ $allSells"
            currentNetTV.text = "$ ${allSells + allBuys}"
            vaultPref.edit().putString(currSymbol, CurrencyFormatter.formatterView.format(numberOfCoins)).apply()
            vaultPref.edit().putString("USD", liquidUSD.toString()).apply()
            completed = false
        }

        //If coins are 0 remove from preference tracking
        if (numberOfCoins == 0.0)
            vaultPref.edit().remove(currSymbol).apply()
        amountToSell.setText("")
        return completed
    }

    //Buy coins at price point
    private fun buyCoins(amt: Double): Boolean {
        var completed = true
        if (amt * currentPrice > liquidUSD) {
            Toast.makeText(this, "Not enough in vault to buy, add more funds", Toast.LENGTH_SHORT)
                    .apply { setGravity(Gravity.CENTER, 0, 0) }
                    .apply { view.textAlignment = View.TEXT_ALIGNMENT_CENTER }
                    .show()
        } else {
            val t0 = Transaction(Calendar.getInstance().time.time, curr.toUpperCase(), amt,
                    true, false, currentPrice, (currentPrice * amt) * -1)
            transactionDB?.TransactionDao()?.insertAll(t0)
            allBuys -= currentPrice * amt
            numberOfCoins += amt
            liquidUSD -= amt * currentPrice
            if (liquidUSD < .0001) liquidUSD = 0.0
            assetsBoughtTV.text = "$ ${allBuys * -1}"
            currentNetTV.text = "$ ${allSells + allBuys}"
            Toast.makeText(this, "Bought $amt coins @ $currentPrice for a net of ${currentPrice * -amt}", Toast.LENGTH_LONG)
                    .apply { setGravity(Gravity.CENTER, 0, 0) }
                    .apply { view.textAlignment = View.TEXT_ALIGNMENT_CENTER }
                    .show()
            //Updated USD and Coin amounts
            vaultPref.edit().putString(currSymbol, CurrencyFormatter.formatterView.format(numberOfCoins)).apply()
            vaultPref.edit().putString("USD", liquidUSD.toString()).apply()
            completed = false
        }
        amountToBuy.setText("")
        return completed
    }

    //Load Two database calls using Room and Coroutines to await data
    private fun initDB() {
        transactionDB = TransactionDatabase.getInstance(this)
        async(UI) {
            val buys = async(CommonPool)
            {
                transactionDB!!.TransactionDao().getAllCoinBuys(curr.toUpperCase())
            }
            val sell = async(CommonPool) {
                transactionDB!!.TransactionDao().getAllCoinSells(curr.toUpperCase())
            }
            allBuys = buys.await()
            allSells = sell.await()
            initVaultData()
            initVaultButtons()
        }
    }
    //endregion

    //region Request MarketData
    //Request Market Data and start service to currency data
    //Load only once
    private fun requestMarketData() {
        if (!marketLoaded) {
            val intent = Intent(this, DataService::class.java)
            intent.putExtra("Path", "Market")
            intent.putExtra("Currency", "https://coinmarketcap.com/currencies/${curr.toLowerCase()}/#markets")
            startService(intent)
            marketLoaded = true
        } else
            Toast.makeText(baseContext, "Market Loaded", Toast.LENGTH_SHORT)
                    .apply { setGravity(Gravity.CENTER, 0, 0) }
                    .apply { view.textAlignment = View.TEXT_ALIGNMENT_CENTER }
                    .show()
    }

    //Parse details for Spinners and RecyclerView
    private fun parseMarketDetails() {
        var blocks = marketResponse.substringAfter("<tbody>").split("</tr>")
        blocks.take(blocks.size - 1)
                .forEach {
                    (createMarketDetail(it))
                }
        initMarketView()
    }

    //Set Adapters with Market Data
    private fun initMarketView() {
        marketSpinner.adapter = ArrayAdapter<String>(
                this, R.layout.spinner_layout, marketNames.toList()
        )
        pairSpinner.adapter = ArrayAdapter<String>(
                this, R.layout.spinner_layout, marketPairs.toList()
        )
    }

    //Parse of web scraped Data to get unavailable API data
    private fun createMarketDetail(block: String) {
        var data = block.split("</td>")

        var marketName = data[1].substringAfter("data-sort=\"")
        marketName = marketName.substring(0, marketName.indexOf("\""))

        var pair = data[2].substringAfter("target=\"_blank\">")
        pair = pair.substring(0, pair.indexOf("</a"))

        var volUSD = data[3].substringAfter("data-usd=\"")
        volUSD = volUSD.substring(0, volUSD.indexOf("\""))

        var volBTC = data[3].substringAfter("data-btc=\"")
        volBTC = volBTC.substring(0, volBTC.indexOf("\""))

        var priceUSD = data[4].substringAfter("data-usd=\"")
        priceUSD = priceUSD.substring(0, priceUSD.indexOf("\""))

        var priceBTC = data[4].substringAfter("data-btc=\"")
        priceBTC = priceBTC.substring(0, priceBTC.indexOf("\""))

        var percent = data[5].substringAfter("<span data-format-percentage data-format-value=\"")
        percent = percent.substring(0, percent.indexOf("\""))

        var update = data[6].substringAfter(">")

        marketDeets.add(Market(marketName, pair, volBTC.toDouble(), volUSD.toDouble(), priceBTC.toDouble(), priceUSD.toDouble(), percent, update))
        //If a pair exists AA/BB then don't include BB/AA
        if (!marketPairs.contains(pair)) marketPairs.add(pair.capitalize())
        if (!marketNames.contains(marketName)) marketNames.add(marketName.capitalize())
    }

    //endregion

    //region Combined Chart
    //Initialize Combined Chart for MACD indicator Line and Bar graph
    private fun initCombined(count: Int) {
        mCombined = findViewById(R.id.MACD)
        mCombined.drawOrder = arrayOf(DrawOrder.BAR, DrawOrder.LINE)

        //Listener for when point is touched display info of that point
        val keylistener = object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry, h: Highlight) {
                entry.text = "MACD: ${CurrencyFormatter.formatterLarge.format(mACD[e.x.toInt()].toDouble())}," +
                        "S: ${CurrencyFormatter.formatterLarge.format(signal[e.x.toInt()])}," +
                        "H: ${CurrencyFormatter.formatterLarge.format(histo[e.x.toInt()])}"

                time.text = "${currencyDeets[e.x.toInt() + (currencyDeets.size - 1 - count)].Date}"
            }

            override fun onNothingSelected() {
                entry.text = ""
                time.text = ""
            }

        }

        mCombined.setOnChartValueSelectedListener(keylistener)
        initCombinedChart(count)
    }

    //Set UI of Combined Chart and view window
    private fun initCombinedChart(count: Int) {
        mCombined.setPinchZoom(false)
        mCombined.setDrawGridBackground(false)

        val xAxis: XAxis = mCombined.xAxis
        xAxis.isEnabled = true
        xAxis.textColor = Color.WHITE
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 7f // only intervals of 1 day
        xAxis.setValueFormatter({ value, axis ->
            xValues[(value % xValues.size).toInt()]
        })


        val rightAxis: YAxis = mCombined.axisRight
        rightAxis.setLabelCount(7, false)
        rightAxis.setDrawGridLines(true)
        rightAxis.setDrawAxisLine(true)
        rightAxis.textColor = Color.WHITE

        val leftAxis: YAxis = mCombined.axisLeft
        leftAxis.isEnabled = false

        mCombined.legend.isEnabled = true
        mCombined.legend.textColor = Color.WHITE
        mCombined.description.isEnabled = true
        mCombined.description.textColor = Color.YELLOW
        mCombined.description.textSize = 12f
        mCombined.description.text = "12,26,9 MACD"
        mCombined.description.setPosition(850f, 50f)
        setCombinedData(count)
        mCombined.setVisibleXRangeMaximum(20f)

    }

    //Set Data for two signal lines for MACD generation
    private fun setCombinedData(count: Int) {
        var data = CombinedData()

        //EMA is number of days to take into account
        //Using 12 26 9 day MACD
        generateEMA(count, 12, eMA12)
        generateEMA(count, 26, eMA26)

        data.setData(generateMacD(count))
        data.setData(generateHistogram(count))
        mCombined.xAxis.axisMaximum = data.xMax
        mCombined.data = data
        mCombined.invalidate()
    }

    //Generate an EMA of number of days into arraylist
    private fun generateEMA(count: Int, EMA: Int, toAdd: ArrayList<Float>) {
        //{Close - EMA(previous day)} x multiplier + EMA(previous day)
        val size = currencyDeets.size - 1
        var e = 1
        var sum = 0.0f
        while (e <= EMA) {
            sum += currencyDeets[e + (size - count - EMA)].Close.toFloat()
            e++
        }
        var mult = 2 / (EMA + 1).toFloat()
        var i = 0
        toAdd.add(i, sum / EMA)
        while (i <= count) {
            toAdd.add(i + 1, (currencyDeets[i + (size - count)].Close.toFloat() - toAdd[i]) * mult + toAdd[i])
            i++
        }

    }

    //Generate Histogram bar graph to show difference in Signal Line and MACD Line for trend direction
    private fun generateHistogram(count: Int): BarData {
        val yVals1 = ArrayList<BarEntry>()
        var i = 0
        while (i <= count) {
            val value = mACD[i] - signal[i]
            histo.add(value)
            yVals1.add(BarEntry(i.toFloat(), value))
            i++
        }

        val set1: BarDataSet
        set1 = BarDataSet(yVals1, "Histogram")
        set1.setDrawIcons(false)
        set1.valueTextSize = 0f
        set1.setColors(Color.GRAY)
        return BarData(set1)


    }

    //Signal Line generation from EMA and MACD
    private fun generateSignalData(count: Int, EMA: Int, toAdd: ArrayList<Float>) {
        //{Close - EMA(previous day)} x multiplier + EMA(previous day)
        var size = mACD.size - 1
        var e = 1
        var sum = 0.0f
        while (e <= EMA) {
            sum += mACD[e]
            e++
        }
        var mult = 2 / (EMA + 1).toFloat()
        var i = 0
        toAdd.add(i, sum / EMA)
        while (i <= count) {
            toAdd.add(i + 1, (mACD[i + (size - count)] - toAdd[i]) * mult + toAdd[i])
            i++
        }
    }

    //MACD line generation using 12 day EMA and 26 EMA
    private fun generateMacD(count: Int): LineData {
        //EMA12[i] - EMA26[i]
        val d = LineData()

        val entries: ArrayList<Entry> = ArrayList()

        var i = 0
        while (i <= count) {
            mACD.add(eMA12[i] - eMA26[i])
            entries.add(Entry(i.toFloat(), (eMA12[i] - eMA26[i])))
            i++
        }

        val set = LineDataSet(entries, "MACD")
        with(set)
        {
            color = Color.BLUE
            lineWidth = .5f
            mode = LineDataSet.Mode.CUBIC_BEZIER
            valueTextSize = 0f
            axisDependency = YAxis.AxisDependency.LEFT
        }
        generateSignalData(count, 9, signal)
        generateSignalLine(count)
        d.addDataSet(set)
        d.addDataSet(sigData)
        return d

    }

    //Generate Signal line to indicate shifting trend line
    private fun generateSignalLine(count: Int) {
        //EMA12[i] - EMA26[i]
        var entries: ArrayList<Entry> = ArrayList()


        var i = 0
        while (i <= count) {
            entries.add(Entry(i.toFloat(), signal[i]))
            i++
        }

        sigData = LineDataSet(entries, "Signal Line")
        with(sigData)
        {
            color = Color.RED
            lineWidth = .5f
            mode = LineDataSet.Mode.CUBIC_BEZIER
            valueTextSize = 0f
            axisDependency = YAxis.AxisDependency.LEFT
        }

    }
    //endregion

    //region Candlestick Chart
    //Set up CandlestickChart
    private fun initCandle(count: Int) {
        mCandle = findViewById(R.id.CurrencyChart)
        val keylistener = object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry, h: Highlight) {
                entry.text = "O:${CurrencyFormatter.formatterLarge.format(currencyDeets[e.x.toInt() + (currencyDeets.size - 1 - count)].Open.toDouble())}," +
                        " C:${CurrencyFormatter.formatterLarge.format(currencyDeets[e.x.toInt() + (currencyDeets.size - 1 - count)].Close.toDouble())}" +
                        ", L:${CurrencyFormatter.formatterLarge.format(currencyDeets[e.x.toInt() + (currencyDeets.size - 1 - count)].Low.toDouble())}"

                time.text = "${currencyDeets[e.x.toInt() + (currencyDeets.size - 1 - count)].Date}"
            }

            override fun onNothingSelected() {
                entry.text = ""
                time.text = ""
            }

        }

        mCandle.setOnChartValueSelectedListener(keylistener)
        initCandleChart(count)

    }

    //Set up look of chart
    private fun initCandleChart(count: Int) {
        mCandle.setPinchZoom(false)
        mCandle.setDrawGridBackground(false)

        val xAxis: XAxis = mCandle.xAxis
        with(xAxis)
        {
            isEnabled = true
            textColor = Color.WHITE
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 7f // only intervals of 1 day
            setValueFormatter({ value, axis ->
                xValues[(value % xValues.size).toInt()]
            })
        }


        val leftAxis: YAxis = mCandle.axisRight
        with(leftAxis)
        {
            setLabelCount(7, false)
            setDrawGridLines(true)
            setDrawAxisLine(true)
            textColor = Color.WHITE
        }

        val rightAxis: YAxis = mCandle.axisLeft
        rightAxis.isEnabled = false

        with(mCandle)
        {
            legend.isEnabled = false
            description.isEnabled = true
            description.text = "45 Day CandleStick"
            description.textColor = Color.YELLOW
            description.textSize = 12f
            description.setPosition(900f, 50f)
            setPinchZoom(false)
        }
        setCandleData(count)
        mCandle.setVisibleXRangeMaximum(20f)

    }

    //Create data points for number of days count
    private fun setCandleData(count: Int) {

        mCandle.resetTracking()
        val size: Int = currencyDeets.size - 1
        val yVals1: ArrayList<CandleEntry> = ArrayList()
        var i = 0
        while (i <= count) {
            yVals1.add(CandleEntry(
                    i.toFloat(), currencyDeets[i + (size - count)].High.toFloat(),
                    currencyDeets[i + (size - count)].Low.toFloat(),
                    currencyDeets[i + (size - count)].Open.toFloat(),
                    currencyDeets[i + (size - count)].Close.toFloat()
            ))
            i++
        }

        var set1 = CandleDataSet(yVals1, "Data Set")
        with(set1)
        {
            setDrawIcons(false)
            axisDependency = YAxis.AxisDependency.LEFT
            shadowColor = Color.YELLOW
            valueTextColor = Color.WHITE
            shadowWidth = 0.7f
            decreasingColor = Color.RED
            decreasingPaintStyle = Paint.Style.FILL
            increasingColor = Color.GREEN
            increasingPaintStyle = Paint.Style.STROKE
            neutralColor = Color.BLUE
        }

        var data = CandleData(set1)

        mCandle.data = data
        mCandle.invalidate()
    }
    //endregion

    //region Bar Chart
    //24Hour Volume Chart
    private fun initBar(count: Int) {
        mBar = findViewById<BarChart>(R.id.VolumeChart) as BarChart
        val keylistener = object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry, h: Highlight) {
                entry.text = "Volume: ${CurrencyFormatter.formatterLarge.format(e.y)} M"
                time.text = "${currencyDeets[e.x.toInt() + (currencyDeets.size - 1 - count)].Date}"
            }

            override fun onNothingSelected() {
                entry.text = ""
                time.text = ""
            }

        }
        mBar.setOnChartValueSelectedListener(keylistener)
        initBarChart(count)
    }

    //Set up look and set date values for x axis
    private fun initBarChart(count: Int) {
        var i = 0
        var size = currencyDeets.size - 1
        while (i <= count) {
            val value = currencyDeets[i + (size - count)].Date
            xValues.add(value)
            i++
        }

        with(mBar)
        {
            setDrawBarShadow(false)
            setDrawValueAboveBar(false)
            description.isEnabled = true
            description.text = "24Hr Volume"
            description.textColor = Color.YELLOW
            description.textSize = 12f
            description.setPosition(850f, 50f)
            setMaxVisibleValueCount(365)
            setPinchZoom(false)
            setDrawGridBackground(false)
            xAxis.setDrawLabels(true)
            xAxis.setDrawAxisLine(true)
        }

        val xAxis: XAxis = mBar.xAxis
        with(xAxis)
        {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(true)
            granularity = 7f
            labelCount = 7
            setValueFormatter({ value, _ ->
                xValues[(value % xValues.size).toInt()]
            })
            textColor = Color.WHITE
            isEnabled = true
        }

        val rightAxis: YAxis = mBar.axisRight
        with(rightAxis)
        {
            setDrawGridLines(true)
            setLabelCount(7, false)
            spaceTop = 0f
            axisMinimum = 0f // this replaces setStartAtZero(true)
            textColor = Color.WHITE
        }

        setBarData(count)
        mBar.legend.isEnabled = false
        mBar.axisLeft.isEnabled = false
        mBar.setVisibleXRangeMaximum(20f)

    }

    //Load up data for each bar date
    private fun setBarData(count: Int) {

        var size: Int = currencyDeets.size - 1
        val yVals1 = ArrayList<BarEntry>()
        var i = 0
        while (i <= count) {
            val value = currencyDeets[i + (size - count)].Volume.toDouble() / 1000000
            yVals1.add(BarEntry(i.toFloat(), value.toFloat()))
            i++
        }

        val set1: BarDataSet
        //If data is available
        if (mBar.data != null && mBar.data.dataSetCount > 0) {
            set1 = (mBar.data.getDataSetByIndex(0)) as BarDataSet
            set1.values = yVals1
            mBar.data.notifyDataChanged()
            mBar.notifyDataSetChanged()
        } else {
            set1 = BarDataSet(yVals1, null)
            set1.setDrawIcons(false)
            set1.setColors(Color.GRAY)

            val dataSets = ArrayList<IBarDataSet>()
            dataSets.add(set1)

            val data = BarData(dataSets)
            data.setValueTextSize(0f)
            data.barWidth = 0.7f

            mBar.data = data
        }
        mBar.invalidate()


    }
    //endregion

    //region Parse and Create Coin Details
    //Parse coin payload from service
    private fun parseCoinDetails() {
        var blocks = response.substringAfter("<tbody>").split("</tr>")
        blocks.take(blocks.size - 1)
                .forEach {
                    currencyDeets.add(createCurrencyDetail(it))
                }
        currencyDeets.sortBy { it.Date }
        currencyDeets.forEach {
            it.Date = (parseUnix(it.Date.toLong()))
        }
        //if more than 45 days of data exists use 45 other wise use as many days as it has
        if (blocks.size >= 45)
            initAllCharts(45)
        else
            initAllCharts(blocks.size)
    }

    //Create Currency structure from parsed details
    private fun createCurrencyDetail(block: String): CurrencyDetails {
        var data = block.substringAfter("<tr class=\"text-right\">").split("</td>")
        //data index: date,open,high,low,close,volume,marketcap

        var date = data[0].substringAfter(">").replace("</td>", "")
        var dateFormat: DateFormat = SimpleDateFormat("MMM dd, yyyy")
        var dateParsed: Date = dateFormat.parse(date)
        var unixTime: Long = dateParsed.time

        var open = data[1].substringAfter("<td data-format-fiat data-format-value=\"")
        open = open.substring(0, open.indexOf("\""))

        var high = data[2].substringAfter("<td data-format-fiat data-format-value=\"")
        high = high.substring(0, high.indexOf("\""))

        var low = data[3].substringAfter("<td data-format-fiat data-format-value=\"")
        low = low.substring(0, low.indexOf("\""))

        var close = data[4].substringAfter("<td data-format-fiat data-format-value=\"")
        close = close.substring(0, close.indexOf("\""))

        var volume = data[5].substringAfter("<td data-format-market-cap data-format-value=\"")
        volume = volume.substring(0, volume.indexOf("\""))

        return CurrencyDetails(unixTime.toString(), high, open, low, close, volume)
    }

    //Request data method for service endpoing
    private fun requestData(path: String) {
        val intent = Intent(this, DataService::class.java)
        intent.putExtra("Path", path)
        startService(intent)
    }

    //Convert millis time into MM/DD/YY format
    fun parseUnix(time: Long): String {
        val date = Date(time)
        val sdf = SimpleDateFormat("MM/dd/yy")
        return sdf.format(date)
    }
    //endregion

    //region Activity Lifecycle

    //Clean up Transaction and broadcasters
    override fun onDestroy() {
        TransactionDatabase.destroyInstance()
        super.onDestroy()

        LocalBroadcastManager.getInstance(applicationContext)
                .unregisterReceiver(mBroadcastReceiver)
        LocalBroadcastManager.getInstance(applicationContext)
                .unregisterReceiver(mBroadcastReceiver2)
    }
    //endregion
}