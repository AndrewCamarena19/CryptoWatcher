package com.andyisdope.cryptowatcher

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.widget.RecyclerView
import android.preference.PreferenceManager
import android.content.*
import com.andyisdope.cryptowatch.Currency
import android.widget.TabHost
import android.support.v7.widget.LinearLayoutManager
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.app.ActivityCompat
import com.andyisdope.cryptowatcher.Services.DataService
import android.content.Intent
import com.andyisdope.cryptowatcher.Adapters.CurrencyAdapter
import com.andyisdope.cryptowatcher.Adapters.TokenAdapter
import com.andyisdope.cryptowatcher.model.Tokens
import java.util.*
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.preference.Preference
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AlertDialog
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.EditText
import java.sql.Time
import kotlin.Comparator
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {


    private lateinit var Refresh: SwipeRefreshLayout
    var mSelectedList: ArrayList<String> = ArrayList()
    private var mCoins: ArrayList<Currency> = ArrayList()
    private var mTokens: ArrayList<Tokens> = ArrayList()
    private var mFavor: ArrayList<Currency> = ArrayList()
    private lateinit var mCoinList: RecyclerView
    private lateinit var mTokenList: RecyclerView
    private lateinit var mFavourites: RecyclerView
    private lateinit var mCoinAdapter: CurrencyAdapter
    private lateinit var mTokenAdapter: TokenAdapter
    private lateinit var mFavAdapter: CurrencyAdapter
    private var networkOk: Boolean = false
    val READ_STORAGE_PERMISSION_REQUEST_CODE = 1
    private var response: String = ""
    private var response2: String = ""
    private lateinit var TimeFrames: Array<String>
    private lateinit var SortBy: Array<String>
    private lateinit var Order: Array<String>
    private lateinit var sharedPref: SharedPreferences


    private val mBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            //val dataItems = intent
            response = intent.getStringExtra(DataService.MY_SERVICE_PAYLOAD)// as Array<Currency>
            displayCoinItems()

        }
    }

    private val mBroadcastReceiver2 = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            //val dataItems = intent
            response2 = intent.getStringExtra(DataService.MY_SERVICE_PAYLOAD)// as Array<Currency>
            displayTokenItems()
            Refresh.isRefreshing = false

        }
    }

    fun checkPermissionForWriteExtertalStorage(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val result = baseContext.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            return result == PackageManager.PERMISSION_GRANTED
        }
        return false
    }

    @Throws(Exception::class)
    fun requestPermissionForWriteExtertalStorage() {
        try {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    READ_STORAGE_PERMISSION_REQUEST_CODE)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }

    }

    fun checkPermissionForReadExtertalStorage(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val result = baseContext.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            return result == PackageManager.PERMISSION_GRANTED
        }
        return false
    }

    @Throws(Exception::class)
    fun requestPermissionForReadExtertalStorage() {
        try {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    READ_STORAGE_PERMISSION_REQUEST_CODE)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (!checkPermissionForReadExtertalStorage())
            requestPermissionForReadExtertalStorage()
        if (!checkPermissionForWriteExtertalStorage())
            requestPermissionForWriteExtertalStorage()
        sharedPref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        initTabs()

        Order = arrayOf("Ascending", "Descending")
        TimeFrames = arrayOf("Hourly", "Daily", "Weekly")
        SortBy = arrayOf("Place", "Alphabet", "Price", "24Hr Volume", "MarketCap", "Hourly", "Daily", "Weekly")

        Currency.SortMethod = "Place"
        Currency.TimeFrame = "Hourly"
        Currency.Order = "Ascending"
        Tokens.SortMethod = "Place"
        Tokens.TimeFrame = "Hourly"
        Tokens.Order = "Ascending"


        mCoinList = findViewById<RecyclerView>(R.id.CoinList) as RecyclerView
        mTokenList = findViewById<RecyclerView>(R.id.TokenList) as RecyclerView
        mFavourites = findViewById<RecyclerView>(R.id.Favorites) as RecyclerView
        Refresh = findViewById<SwipeRefreshLayout>(R.id.RefreshLayout) as SwipeRefreshLayout


        LocalBroadcastManager.getInstance(applicationContext)
                .registerReceiver(mBroadcastReceiver,
                        IntentFilter(DataService.COINS))
        requestData("Coins")

        LocalBroadcastManager.getInstance(applicationContext)
                .registerReceiver(mBroadcastReceiver2,
                        IntentFilter(DataService.TOKENS))
        requestData("Tokens")


        Refresh.setOnRefreshListener {
            requestData("Coins")
            requestData("Tokens")
        }
    }

    private fun requestData(path: String) {
        val intent = Intent(this, DataService::class.java)
        intent.putExtra("Path", path)
        startService(intent)
    }

    private fun sortAdapters(method: String) {
        Currency.SortMethod = method
        Tokens.SortMethod = method
        when (method) {
            "Hourly" -> {
                when (Currency.Order) {
                    "Ascending" -> {
                        mCoins.sortBy { it.HrChange.toFloat() }
                        mTokens.sortBy { it.HrChange.toFloat() }
                        mFavor.sortBy { it.HrChange.toFloat() }

                    }
                    "Descending" -> {
                        mCoins.sortByDescending { it.HrChange.toFloat() }
                        mTokens.sortByDescending { it.HrChange.toFloat() }
                        mFavor.sortByDescending { it.HrChange.toFloat() }

                    }
                }
            }
            "Daily" -> {
                when (Currency.Order) {
                    "Ascending" -> {
                        mCoins.sortBy { it.TwoChange.toFloat() }
                        mTokens.sortBy { it.TwoChange.toFloat() }
                        mFavor.sortBy { it.TwoChange.toFloat() }
                    }
                    "Descending" -> {
                        mCoins.sortByDescending { it.TwoChange.toFloat() }
                        mTokens.sortByDescending { it.TwoChange.toFloat() }
                        mFavor.sortByDescending { it.TwoChange.toFloat() }
                    }
                }
            }
            "Weekly" -> {
                when (Currency.Order) {
                    "Ascending" -> {
                        mCoins.sortBy { it.SevenChange.toFloat() }
                        mTokens.sortBy { it.SevenChange.toFloat() }
                        mFavor.sortBy { it.SevenChange.toFloat() }

                    }
                    "Descending" -> {
                        mCoins.sortByDescending { it.SevenChange.toFloat() }
                        mTokens.sortByDescending { it.SevenChange.toFloat() }
                        mFavor.sortByDescending { it.SevenChange.toFloat() }

                    }
                }
            }
            "Price" -> {
                when (Currency.Order) {
                    "Ascending" -> {
                        mCoins.sortBy { it.CurrentPrice.toFloat() }
                        mTokens.sortBy { it.CurrentPrice.toFloat() }
                        mFavor.sortBy { it.CurrentPrice.toFloat() }

                    }
                    "Descending" -> {
                        mCoins.sortByDescending { it.CurrentPrice.toFloat() }
                        mTokens.sortByDescending { it.CurrentPrice.toFloat() }
                        mFavor.sortByDescending { it.CurrentPrice.toFloat() }

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
                        mCoins.sortBy { it.Volume.toFloat() }
                        mTokens.sortBy { it.Volume.toFloat() }
                        mFavor.sortBy { it.Volume.toFloat() }

                    }
                    "Descending" -> {
                        mCoins.sortByDescending { it.Volume.toFloat() }
                        mTokens.sortByDescending { it.Volume.toFloat() }
                        mFavor.sortByDescending { it.Volume.toFloat() }

                    }
                }
            }
            "MarketCap" -> {
                when (Currency.Order) {
                    "Ascending" -> {
                        mCoins.sortBy { it.MarketCap.toFloat() }
                        mTokens.sortBy { it.MarketCap.toFloat() }
                        mFavor.sortBy { it.MarketCap.toFloat() }

                    }
                    "Descending" -> {
                        mCoins.sortByDescending { it.MarketCap.toFloat() }
                        mTokens.sortByDescending { it.MarketCap.toFloat() }
                        mFavor.sortByDescending { it.MarketCap.toFloat() }

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
        mTokenAdapter = TokenAdapter(this, mTokens)
        mTokenList.adapter = mTokenAdapter
        mTokenList.adapter.notifyDataSetChanged()

        mCoinAdapter = CurrencyAdapter(this, mCoins)
        mCoinList.adapter = mCoinAdapter
        mCoinList.adapter.notifyDataSetChanged()


        mFavAdapter = CurrencyAdapter(this, mFavor)
        mFavourites.adapter = mFavAdapter
        mFavourites.adapter.notifyDataSetChanged()

        Toast.makeText(baseContext, "Sorting ${Currency.SortMethod} in ${Currency.Order} order.", Toast.LENGTH_SHORT).show()

    }

    private fun displayTokenItems() {
        mTokens.clear()
        var temp: Tokens
        var blocks = response2.substringAfter("<tbody>").split("</tr>")
        blocks.take(blocks.size - 1)
                .filter { it.length > 19 }
                .forEach {
                    temp = (createToken(it))
                    if (sharedPref.contains(temp.Name)) {
                        temp.isFavorite = true
                        mFavor.add(toCurrency(temp))
                        mSelectedList.add(temp.Name)
                    }
                    mTokens.add(temp)
                }

        mTokenList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        sortAdapters(Currency.SortMethod)
        displayFavourites()
        //Log.i("Here", mSelectedList.toString())

    }

    private fun displayFavourites() {
        mFavourites.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        mFavAdapter = CurrencyAdapter(this, mFavor)
        mFavourites.adapter = mFavAdapter
        mFavourites.adapter.notifyDataSetChanged()

    }

    private fun displayCoinItems() {
        mCoins.clear()
        mFavor.clear()
        mSelectedList.clear()
        var temp: Currency
        var blocks = response.substringAfter("<tbody>").split("</tr>")
        blocks.take(blocks.size - 1)
                .filter { it.length > 19 }
                .forEach {
                    temp = (createCurrency(it))
                    if (sharedPref.contains(temp.Name)) {
                        temp.isFavorite = true
                        mFavor.add(temp)
                        mSelectedList.add(temp.Name)
                    }
                    mCoins.add(temp)
                }
        Currency.ETH = mCoins[1].CurrentPrice.toFloat()
        Currency.BTC = mCoins[0].CurrentPrice.toFloat()
        mCoinList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }

    override fun onPause() {
        super.onPause()
        //mDataSource.close()
    }

    override fun onResume() {
        super.onResume()
        //mDataSource.open()
    }

    override fun onDestroy() {
        super.onDestroy()

        LocalBroadcastManager.getInstance(applicationContext)
                .unregisterReceiver(mBroadcastReceiver)
        LocalBroadcastManager.getInstance(applicationContext)
                .unregisterReceiver(mBroadcastReceiver2)
    }

    fun initTabs() {
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
        spec.setContent(R.id.Favorites)
        spec.setIndicator("Favorites")
        tabs.addTab(spec)
    }

    private fun toCurrency(Toke: Tokens): Currency {
        return Currency(Toke.Name, Toke.Symbol, Toke.Place, Toke.isFavorite, Toke.Num, Toke.MarketCap, Toke.CurrentPrice, Toke.HrChange, Toke.TwoChange, Toke.SevenChange, Toke.Volume)
    }

    fun createBuilderDialog(array: Array<String>, title: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setItems(array, { dialog, which ->
            sortAdapters(array[which])
        })
        builder.show()
    }

    private fun createSearchDialog() {
        var m_Text: String
        val builder: AlertDialog.Builder = AlertDialog.Builder(this, R.style.ThemeDialog)
        val input = EditText(this)
        input.setTextColor(Color.WHITE)
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setMessage("Enter a Coin/Token name or symbol")
        builder.setView(input)
        builder.setPositiveButton("Tokens") { dialog, which ->
            m_Text = input.text.toString()

            mTokens.firstOrNull { (it.Symbol == m_Text.toUpperCase() || it.Name.trim() == m_Text.toLowerCase()) }
                    ?.let {
                        var intent: Intent = Intent(this, CurrencyDetail::class.java)
                        intent.putExtra("Currency", it.Name)
                        startActivity(intent)
                    } ?: run {
                Toast.makeText(this, "Entry not found", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Coins") { dialog, which ->
            m_Text = input.text.toString()
            mCoins.firstOrNull { (it.Symbol == m_Text.toUpperCase() || it.Name.trim() == m_Text.toLowerCase()) }
                    ?.let {
                        var intent: Intent = Intent(this, CurrencyDetail::class.java)
                        intent.putExtra("Currency", it.Name)
                        startActivity(intent)
                    } ?: run {
                Toast.makeText(this, "Entry not found", Toast.LENGTH_SHORT).show()

            }
        }
        builder.setNeutralButton("Cancel") { dialog, which -> dialog.cancel() }
        var alert: AlertDialog = builder.create()
        alert.show()

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //sorts: alphabet, marketcap, biggest change, price, place
        when (item.itemId) {
            R.id.search -> createSearchDialog()
            R.id.sort -> {
                createBuilderDialog(SortBy, "Select a criteria to sort in ${Currency.Order} order.")
            }
            R.id.time -> {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Select a time frame")
                builder.setItems(TimeFrames, { _, which ->
                    Currency.TimeFrame = TimeFrames[which]
                    Tokens.TimeFrame = TimeFrames[which]
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
                builder.setItems(Order, { _, which ->
                    Currency.Order = Order[which]
                    Tokens.Order = Order[which]
                    Toast.makeText(baseContext, "Sorting ${Currency.SortMethod} in ${Currency.Order} order.", Toast.LENGTH_SHORT).show()
                    sortAdapters(Currency.SortMethod)
                })
                builder.show()
            }
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.actionmenu, menu)
        return true
    }

    private fun createCurrency(block: String): Currency {

        var id = block.substringAfter("<tr id=\"id-")
        id = id.substring(0, id.indexOf("\""))

        var data = block.split("</td")

        var place = data[0].substringAfter("<td class=\"text-center\">").trim()

        var symbol = data[1].substringAfter("<span class=\"currency-symbol\"><a href=\"/currencies/$id/\">")
        symbol = symbol.substring(0, symbol.indexOf("<"))

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
            var temp = hrChange.substringAfter("data-sort=\"")
            temp.substring(0, temp.indexOf("\""))
        }

        var twoChange = data[8].substringAfter("text-right\"")
        twoChange = if (twoChange.length < 5) "?"
        else {
            var temp = twoChange.substringAfter("data-sort=\"")
            temp.substring(0, temp.indexOf("\""))
        }

        var sevenChange = data[9].substringAfter("text-right\"")
        sevenChange = if (sevenChange.length < 5) "?"
        else {
            var temp = sevenChange.substringAfter("data-sort=\"")
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

    private fun createToken(block: String): Tokens {
        var id = block.substringAfter("<tr id=\"id-")
        id = id.substring(0, id.indexOf("\""))

        var platform = block.substringAfter("data-platformsymbol=\"")
        platform = platform.substring(0, platform.indexOf("\""))

        var data = block.split("</td")

        var place = data[0].substringAfter("<td class=\"text-center\">").trim()

        var symbol = data[1].substringAfter("<span class=\"currency-symbol\"><a href=\"/currencies/$id/\">")
        symbol = symbol.substring(0, symbol.indexOf("<"))

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
            var temp = hrChange.substringAfter("data-sort=\"")
            temp.substring(0, temp.indexOf("\""))
        }

        var twoChange = data[8].substringAfter("text-right\"")
        twoChange = if (twoChange.length < 5) "?"
        else {
            var temp = twoChange.substringAfter("data-sort=\"")
            temp.substring(0, temp.indexOf("\""))
        }

        var sevenChange = data[9].substringAfter("text-right\"")
        sevenChange = if (sevenChange.length < 5) "?"
        else {
            var temp = sevenChange.substringAfter("data-sort=\"")
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
}
