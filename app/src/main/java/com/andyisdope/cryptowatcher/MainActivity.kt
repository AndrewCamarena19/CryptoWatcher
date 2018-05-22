package com.andyisdope.cryptowatcher

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.*
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.InputType
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TabHost
import android.widget.Toast
import com.andyisdope.cryptowatcher.model.Currency
import com.andyisdope.cryptowatcher.Adapters.CurrencyAdapter
import com.andyisdope.cryptowatcher.Adapters.TokenAdapter
import com.andyisdope.cryptowatcher.R.id.Favorites
import com.andyisdope.cryptowatcher.Services.DataService
import com.andyisdope.cryptowatcher.Services.PortfolioService
import com.andyisdope.cryptowatcher.model.Tokens
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView


class MainActivity : AppCompatActivity() {

    private lateinit var mAdView: AdView
    private lateinit var refresh: SwipeRefreshLayout
    private var mSelectedList: ArrayList<String> = ArrayList()
    private var mCoins: ArrayList<Currency> = ArrayList()
    private var mTokens: ArrayList<Tokens> = ArrayList()
    private var mFavor: ArrayList<Currency> = ArrayList()
    private lateinit var mCoinList: RecyclerView
    private lateinit var mTokenList: RecyclerView
    private lateinit var mFavourites: RecyclerView
    private lateinit var mCoinAdapter: CurrencyAdapter
    private lateinit var mTokenAdapter: TokenAdapter
    private lateinit var mFavAdapter: CurrencyAdapter
    private var response: String = ""
    private var response2: String = ""
    private lateinit var timeFrames: Array<String>
    private lateinit var sortBy: Array<String>
    private lateinit var order: Array<String>
    private lateinit var sharedPref: SharedPreferences
    private lateinit var pricingPref: SharedPreferences
    private lateinit var coinsPref: SharedPreferences
    private lateinit var coinNames: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Banner AdView
        mAdView = findViewById(R.id.adView)
        mAdView.loadAd(AdRequest.Builder().build())

        //Schedule portfolio tracking job
        scheduleJob()
        initTabs()
        initVariables()

        //New call to services on swipe refresh
        refresh.setOnRefreshListener {
            requestData("Coins")
            requestData("Tokens")
        }
    }

    //Initialize Views and User Preferences
    private fun initVariables() {
        //Load Preferences to start tracking coin data
        coinNames = applicationContext.getSharedPreferences("CoinNames", Context.MODE_PRIVATE)
        sharedPref = applicationContext.getSharedPreferences("Favorites", Context.MODE_PRIVATE)
        pricingPref = applicationContext.getSharedPreferences("Prices", Context.MODE_PRIVATE)
        coinsPref = applicationContext.getSharedPreferences("Coins", Context.MODE_PRIVATE)

        //Arrays of options menu data
        order = arrayOf("Ascending", "Descending")
        timeFrames = arrayOf("Hourly", "Daily", "Weekly")
        sortBy = arrayOf("Place", "Alphabet", "Price", "24Hr Volume", "MarketCap", "Hourly", "Daily", "Weekly")

        //Initial ordering of coin and token data
        Currency.SortMethod = "Place"
        Currency.TimeFrame = "Hourly"
        Currency.Order = "Ascending"
        Tokens.SortMethod = "Place"
        Tokens.TimeFrame = "Hourly"
        Tokens.Order = "Ascending"

        //UI recyclerViews
        mCoinList = findViewById(R.id.CoinList)
        mTokenList = findViewById(R.id.TokenList)
        mFavourites = findViewById(Favorites)
        refresh = findViewById(R.id.RefreshLayout)

        //Broadcast receivers to start and display coins and tokens
        LocalBroadcastManager.getInstance(applicationContext)
                .registerReceiver(mBroadcastReceiver,
                        IntentFilter(DataService.COINS))
        LocalBroadcastManager.getInstance(applicationContext)
                .registerReceiver(mBroadcastReceiver2,
                        IntentFilter(DataService.TOKENS))

        //Initial requests for Token and coin data
        requestData("Coins")
        requestData("Tokens")
    }

    //Scheduled job to track portfolio performance in Room taken every 6 hours persisted on reboot
    private fun scheduleJob() {
        val comp = ComponentName(applicationContext, PortfolioService::class.java)
        val builder: JobInfo = JobInfo.Builder(1111, comp)
                .setPeriodic(21600000)//21600000
                .setPersisted(true)
                .build()
        val scheduler: JobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        scheduler.schedule(builder)
    }

    //region Broadcasts and Data methods
    //First Broadcast receiver for coin items to kick off displaying Coins
    private val mBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            //val dataItems = intent
            response = intent.getStringExtra(DataService.MY_SERVICE_PAYLOAD)// as Array<Currency>
            displayCoinItems()

        }
    }
    //Second Broadcast receiver for token items to kick of displaying Tokens
    private val mBroadcastReceiver2 = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            //val dataItems = intent
            response2 = intent.getStringExtra(DataService.MY_SERVICE_PAYLOAD)// as Array<Currency>
            displayTokenItems()
            refresh.isRefreshing = false

        }
    }

    //Helper method to call Retrofit Service use Path intent string as URL endpoint
    //Or keyword to get hardcoded endpoing
    private fun requestData(path: String) {
        val intent = Intent(this, DataService::class.java)
        intent.putExtra("Path", path)
        startService(intent)
    }

    //Sort the Arrayadapters for recyclerviews based on ordering criteria
    private fun sortAdapters(method: String) {
        Currency.SortMethod = method
        Tokens.SortMethod = method
        when (method) {
            "Hourly" -> {
                when (Currency.Order) {
                    "Ascending" -> {
                        mCoins.sortBy { it.HrChange.toDouble() }
                        mTokens.sortBy { it.HrChange.toDouble() }
                        mFavor.sortBy { it.HrChange.toDouble() }

                    }
                    "Descending" -> {
                        mCoins.sortByDescending { it.HrChange.toDouble() }
                        mTokens.sortByDescending { it.HrChange.toDouble() }
                        mFavor.sortByDescending { it.HrChange.toDouble() }

                    }
                }
            }
            "Daily" -> {
                when (Currency.Order) {
                    "Ascending" -> {
                        mCoins.sortBy { it.TwoChange.toDouble() }
                        mTokens.sortBy { it.TwoChange.toDouble() }
                        mFavor.sortBy { it.TwoChange.toDouble() }
                    }
                    "Descending" -> {
                        mCoins.sortByDescending { it.TwoChange.toDouble() }
                        mTokens.sortByDescending { it.TwoChange.toDouble() }
                        mFavor.sortByDescending { it.TwoChange.toDouble() }
                    }
                }
            }
            "Weekly" -> {
                when (Currency.Order) {
                    "Ascending" -> {
                        mCoins.sortBy { it.SevenChange.toDouble() }
                        mTokens.sortBy { it.SevenChange.toDouble() }
                        mFavor.sortBy { it.SevenChange.toDouble() }

                    }
                    "Descending" -> {
                        mCoins.sortByDescending { it.SevenChange.toDouble() }
                        mTokens.sortByDescending { it.SevenChange.toDouble() }
                        mFavor.sortByDescending { it.SevenChange.toDouble() }

                    }
                }
            }
            "Price" -> {
                when (Currency.Order) {
                    "Ascending" -> {
                        mCoins.sortBy { it.CurrentPrice.toDouble() }
                        mTokens.sortBy { it.CurrentPrice.toDouble() }
                        mFavor.sortBy { it.CurrentPrice.toDouble() }

                    }
                    "Descending" -> {
                        mCoins.sortByDescending { it.CurrentPrice.toDouble() }
                        mTokens.sortByDescending { it.CurrentPrice.toDouble() }
                        mFavor.sortByDescending { it.CurrentPrice.toDouble() }

                    }
                }
            }
            "Place" -> {
                when (Currency.Order) {
                    "Ascending" -> {
                        mCoins.sortBy { it.Place }
                        mTokens.sortBy { it.Place }
                        mFavor.sortBy { it.Place }

                    }
                    "Descending" -> {
                        mCoins.sortByDescending { it.Place }
                        mTokens.sortByDescending { it.Place }
                        mFavor.sortByDescending { it.Place }

                    }
                }
            }
            "24Hr Volume" -> {
                when (Currency.Order) {
                    "Ascending" -> {
                        mCoins.sortBy { it.Volume.toDouble() }
                        mTokens.sortBy { it.Volume.toDouble() }
                        mFavor.sortBy { it.Volume.toDouble() }

                    }
                    "Descending" -> {
                        mCoins.sortByDescending { it.Volume.toDouble() }
                        mTokens.sortByDescending { it.Volume.toDouble() }
                        mFavor.sortByDescending { it.Volume.toDouble() }

                    }
                }
            }
            "MarketCap" -> {
                when (Currency.Order) {
                    "Ascending" -> {
                        mCoins.sortBy { it.MarketCap.toDouble() }
                        mTokens.sortBy { it.MarketCap.toDouble() }
                        mFavor.sortBy { it.MarketCap.toDouble() }

                    }
                    "Descending" -> {
                        mCoins.sortByDescending { it.MarketCap.toDouble() }
                        mTokens.sortByDescending { it.MarketCap.toDouble() }
                        mFavor.sortByDescending { it.MarketCap.toDouble() }

                    }
                }
            }
            "Alphabet" -> {
                when (Currency.Order) {
                    "Ascending" -> {
                        mCoins.sortBy { it.Name }
                        mTokens.sortBy { it.Name }
                        mFavor.sortBy { it.Name }

                    }
                    "Descending" -> {
                        mCoins.sortByDescending { it.Name }
                        mTokens.sortByDescending { it.Name }
                        mFavor.sortByDescending { it.Name }

                    }
                }
            }
        }

        //Update all RecyclerViews with same ordering scheme
        mTokenAdapter = TokenAdapter(this, mTokens)
        mTokenList.adapter = mTokenAdapter
        mTokenList.adapter.notifyDataSetChanged()

        mCoinAdapter = CurrencyAdapter(this, mCoins)
        mCoinList.adapter = mCoinAdapter
        mCoinList.adapter.notifyDataSetChanged()


        mFavAdapter = CurrencyAdapter(this, mFavor)
        mFavourites.adapter = mFavAdapter
        mFavourites.adapter.notifyDataSetChanged()

        //Let user know how adapters were sorted
        Toast.makeText(baseContext, "Sorting ${Currency.SortMethod} in ${Currency.Order} order.", Toast.LENGTH_SHORT).show()

    }
    //endregion

    //region UI and Dialog methods
    //Set Tab info and layout
    private fun initTabs() {
        val tabs = findViewById<TabHost>(R.id.CTlist)
        tabs.setup()
        var spec: TabHost.TabSpec = tabs.newTabSpec("Coin List")
        spec.setContent(R.id.CoinList)
        spec.setIndicator("Coin List")
        tabs.addTab(spec)
        spec = tabs.newTabSpec("Token List")
        spec.setContent(R.id.TokenList)
        spec.setIndicator("Token List")
        tabs.addTab(spec)
        spec = tabs.newTabSpec("Favorites")
        spec.setContent(Favorites)
        spec.setIndicator("Favorites")
        tabs.addTab(spec)
    }

    //Dialog builder for different sorting, or display info
    private fun createBuilderDialog(array: Array<String>, title: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setItems(array, { _, which ->
            sortAdapters(array[which])
        })
        builder.show()
    }

    //Search dialog for token or coin, searches on Symbol or full Name
    private fun createSearchDialog() {
        var mText: String
        val builder: AlertDialog.Builder = AlertDialog.Builder(this, R.style.ThemeDialog)
        val input = EditText(this)
        input.setTextColor(Color.WHITE)
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setMessage("Enter a Coin/Token name or symbol")
        builder.setView(input)
        builder.setPositiveButton("Tokens") { _, _ ->
            mText = input.text.toString()

            mTokens.firstOrNull { (it.Symbol == mText.toUpperCase() || it.Name.trim() == mText.toLowerCase()) }
                    ?.let {
                        val intent = Intent(this, CurrencyDetail::class.java)
                        intent.putExtra("Currency", it.Name)
                        startActivity(intent)
                    } ?: run {
                Toast.makeText(this, "Entry not found", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Coins") { _, _ ->
            mText = input.text.toString()
            mCoins.firstOrNull { (it.Symbol == mText.toUpperCase() || it.Name.trim() == mText.toLowerCase()) }
                    ?.let {
                        val intent = Intent(this, CurrencyDetail::class.java)
                        intent.putExtra("Currency", it.Name)
                        startActivity(intent)
                    } ?: run {
                Toast.makeText(this, "Entry not found", Toast.LENGTH_SHORT).show()

            }
        }
        builder.setNeutralButton("Cancel") { dialog, _ -> dialog.cancel() }
        val alert: AlertDialog = builder.create()
        alert.show()

    }

    //Set action bar items, Search, Sort, TimeFrame, Ordering
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //sorts: alphabet, marketcap, biggest change, price, place
        when (item.itemId) {
            R.id.search -> createSearchDialog()
            R.id.sort -> {
                createBuilderDialog(sortBy, "Select a criteria to sort in ${Currency.Order} order.")
            }
            R.id.time -> {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Select a time frame")
                builder.setItems(timeFrames, { _, which ->
                    Currency.TimeFrame = timeFrames[which]
                    Tokens.TimeFrame = timeFrames[which]
                    Toast.makeText(baseContext, "Current time frame: ${Currency.TimeFrame}", Toast.LENGTH_SHORT).show()
                    mCoinList.adapter.notifyDataSetChanged()
                    mTokenList.adapter.notifyDataSetChanged()
                    mFavourites.adapter.notifyDataSetChanged()

                })
                builder.show()
            }
            R.id.order -> {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Select an order direction")
                builder.setItems(order, { _, which ->
                    Currency.Order = order[which]
                    Tokens.Order = order[which]
                    Toast.makeText(baseContext, "Sorting ${Currency.SortMethod} in ${Currency.Order} order.", Toast.LENGTH_SHORT).show()
                    sortAdapters(Currency.SortMethod)
                })
                builder.show()
            }
        }
        return true
    }

    //Inflate the ActionBar
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.actionmenu, menu)
        return true
    }
    //endregion

    //region Lifecycle methods
    //Unregister Broadcast recievers on destory for leaks
    override fun onDestroy() {
        super.onDestroy()

        LocalBroadcastManager.getInstance(applicationContext)
                .unregisterReceiver(mBroadcastReceiver)
        LocalBroadcastManager.getInstance(applicationContext)
                .unregisterReceiver(mBroadcastReceiver2)
    }
    //endregion

    //region Currency Methods
    //Convert Token to Currency Function
    private fun toCurrency(Toke: Tokens): Currency {
        return Currency(Toke.Name, Toke.Symbol, Toke.Place, Toke.isFavorite, Toke.Num, Toke.MarketCap, Toke.CurrentPrice, Toke.HrChange, Toke.TwoChange, Toke.SevenChange, Toke.Volume)
    }

    //Parse through data request payload block by block
    //Webscraped to avoid API limits and info not available through API
    private fun createCurrency(block: String): Currency {

        var id = block.substringAfter("<tr id=\"id-")
        id = id.substring(0, id.indexOf("\""))

        val data = block.split("</td>")
        val place = data[0].substringAfter("<td class=\"text-center\">").trim()

        var symbol = data[2].substringAfter("col-symbol\">")
        //symbol = symbol.substring(0, symbol.indexOf(","))

        var marketCap = data[3].substringAfter("data-sort=\"")
        marketCap = marketCap.substring(0, marketCap.indexOf("\""))

        var currPrice = data[4].substringAfter("data-sort=\"")
        currPrice = currPrice.substring(0, currPrice.indexOf("\""))

        var volume = data[6].substringAfter("data-sort=\"")
        volume = volume.substring(0, volume.indexOf("\""))

        var hrChange = data[7].substringAfter("text-right\"")
        hrChange = if (hrChange.length < 5) "?"
        else {
            val temp = hrChange.substringAfter("data-sort=\"")
            temp.substring(0, temp.indexOf("\""))
        }

        var twoChange = data[8].substringAfter("text-right\"")
        twoChange = if (twoChange.length < 5) "?"
        else {
            val temp = twoChange.substringAfter("data-sort=\"")
            temp.substring(0, temp.indexOf("\""))
        }

        var sevenChange = data[9].substringAfter("text-right\"")
        sevenChange = if (sevenChange.length < 5) "?"
        else {
            val temp = sevenChange.substringAfter("data-sort=\"")
            temp.substring(0, temp.indexOf("\""))
        }
        if (marketCap == "0") marketCap = "-9999"
        if (currPrice == "0") currPrice = "-9999"
        if (hrChange == "-0.0001") hrChange = "-9999"
        if (twoChange == "-0.0001") twoChange = "-9999"
        if (sevenChange == "-0.0001") sevenChange = "-9999"
        if (volume == "-1" || volume == "NONE" || volume == "None") volume = "-9999"
        return Currency(id, symbol, Integer.parseInt(place), false, 0.0, marketCap, currPrice, hrChange, twoChange, sevenChange, volume)
    }

    //Parse Coins and display them, if a coin is in preferences keep track for favorites
    //And portfolio tracking
    private fun displayCoinItems() {
        mCoins.clear()
        mFavor.clear()
        mSelectedList.clear()
        var temp: Currency
        val blocks = response.substringAfter("<tbody>").split("</tr>")
        blocks.take(blocks.size - 1)
                .filter { it.length > 19 }
                .forEach {
                    temp = (createCurrency(it))
                    if (sharedPref.contains(temp.Name)) {
                        temp.isFavorite = true
                        mFavor.add(temp)
                        mSelectedList.add(temp.Name)
                    }
                    if (coinsPref.contains(temp.Symbol)) {
                        pricingPref.edit().putString(temp.Symbol, temp.CurrentPrice).apply()
                        coinNames.edit().putString(temp.Name, coinsPref.getString(temp.Symbol, "0")).apply()
                    }
                    mCoins.add(temp)
                }
        Currency.ETH = mCoins[1].CurrentPrice.toDouble()
        Currency.BTC = mCoins[0].CurrentPrice.toDouble()
        mCoinList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }
    //endregion

    //region Token and Favorites methods
    //Parse through data request payload block by block
    //Webscraped to avoid API limits and info not available through API
    private fun createToken(block: String): Tokens {
        var id = block.substringAfter("<tr id=\"id-")
        id = id.substring(0, id.indexOf("\""))

        var platform = block.substringAfter("data-platformsymbol=\"")
        platform = platform.substring(0, platform.indexOf("\""))

        val data = block.split("</td>")

        val place = data[0].substringAfter("<td class=\"text-center\">").trim()

        var symbol = data[2].substringAfter("col-symbol\">")
        //symbol = symbol.substring(0, symbol.indexOf("<"))

        var marketCap = data[3].substringAfter("data-sort=\"")
        marketCap = marketCap.substring(0, marketCap.indexOf("\""))
        //if(marketCap.contains("+")) marketCap = marketCap.replace("+", "")

        var currPrice = data[4].substringAfter("data-sort=\"")
        currPrice = currPrice.substring(0, currPrice.indexOf("\""))
        //if(currPrice.contains("+")) currPrice = currPrice.replace("+", "")

        var volume = data[6].substringAfter("data-sort=\"")
        volume = volume.substring(0, volume.indexOf("\""))
        //if(volume.contains("+")) volume = volume.replace("+", "")

        var hrChange = data[7].substringAfter("text-right\"")
        hrChange = if (hrChange.length < 5) "?"
        else {
            val temp = hrChange.substringAfter("data-sort=\"")
            temp.substring(0, temp.indexOf("\""))
        }

        var twoChange = data[8].substringAfter("text-right\"")
        twoChange = if (twoChange.length < 5) "?"
        else {
            val temp = twoChange.substringAfter("data-sort=\"")
            temp.substring(0, temp.indexOf("\""))
        }

        var sevenChange = data[9].substringAfter("text-right\"")
        sevenChange = if (sevenChange.length < 5) "?"
        else {
            val temp = sevenChange.substringAfter("data-sort=\"")
            temp.substring(0, temp.indexOf("\""))
        }

        if (marketCap == "0") marketCap = "-9999"
        if (currPrice == "0") currPrice = "-9999"
        if (hrChange == "-0.0001") hrChange = "-9999"
        if (twoChange == "-0.0001") twoChange = "-9999"
        if (sevenChange == "-0.0001") sevenChange = "-9999"
        if (volume == "-1" || volume == "NONE" || volume == "None") volume = "-9999"

        return Tokens(id, symbol, Integer.parseInt(place), platform, false, 0.0, marketCap, currPrice, hrChange, twoChange, sevenChange, volume)
    }

    //Parse Tokens and display them, if a coin is in preferences keep track for favorites
    //And portfolio tracking
    private fun displayTokenItems() {
        mTokens.clear()
        var temp: Tokens
        val blocks = response2.substringAfter("<tbody>").split("</tr>")
        blocks.take(blocks.size - 1)
                .filter { it.length > 19 }
                .forEach {
                    temp = (createToken(it))
                    if (sharedPref.contains(temp.Name)) {
                        temp.isFavorite = true
                        mFavor.add(toCurrency(temp))
                        mSelectedList.add(temp.Name)
                    }
                    if (coinsPref.contains(temp.Symbol)) {
                        pricingPref.edit().putString(temp.Symbol, temp.CurrentPrice).apply()
                        coinNames.edit().putString(temp.Name, coinsPref.getString(temp.Symbol, "0")).apply()
                    }
                    mTokens.add(temp)
                }

        mTokenList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        sortAdapters(Currency.SortMethod)
        displayFavourites()
    }

    //Add all currencies that are favored to that tab
    private fun displayFavourites() {
        mFavourites.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        mFavAdapter = CurrencyAdapter(this, mFavor)
        mFavourites.adapter = mFavAdapter
        mFavourites.adapter.notifyDataSetChanged()

    }
    //endregion
}