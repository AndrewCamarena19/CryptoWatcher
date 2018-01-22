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


class MainActivity : AppCompatActivity() {

    private val SIGNIN_REQUEST = 1001
    val MY_GLOBAL_PREFS = "my_global_prefs"
    private val TAG = "MainActivity"
    //var dataItemList = SampleDataProvider.dataItemList


    //var mDataSource: DataSource
    var mCoins: ArrayList<Currency>? = ArrayList()
    var mTokens: ArrayList<Tokens>? = ArrayList()
    var mCoinList: RecyclerView? = null
    var mTokenList: RecyclerView? = null
    var mFavourites: RecyclerView? = null
    var mCoinAdapter: CurrencyAdapter? = null
    var mTokenAdapter: TokenAdapter? = null
    var mFavAdapter: CurrencyAdapter? = null
    var networkOk: Boolean = false
    val READ_STORAGE_PERMISSION_REQUEST_CODE = 1
    var response: String = ""
    var response2: String = ""

    private val mBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            //val dataItems = intent
            response = intent.getStringExtra(DataService.MY_SERVICE_PAYLOAD)// as Array<Currency>
            Toast.makeText(baseContext,
                    "Received Coins",
                    Toast.LENGTH_SHORT).show()
            displayCoinItems()

        }
    }

    private val mBroadcastReceiver2 = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            //val dataItems = intent
            response2 = intent.getStringExtra(DataService.MY_SERVICE_PAYLOAD)// as Array<Currency>
            Toast.makeText(baseContext,
                    "Received Tokens",
                    Toast.LENGTH_SHORT).show()
            displayTokenItems()

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
        //      Code to manage sliding navigation drawer
        //      end of navigation drawer
        val settings = PreferenceManager.getDefaultSharedPreferences(this)
        val grid = false
        initTabs()
        mCoinList = findViewById<RecyclerView>(R.id.CoinList) as RecyclerView
        mTokenList = findViewById<RecyclerView>(R.id.TokenList) as RecyclerView
        mFavourites = findViewById<RecyclerView>(R.id.Favorites) as RecyclerView

        LocalBroadcastManager.getInstance(applicationContext)
                .registerReceiver(mBroadcastReceiver,
                        IntentFilter(DataService.COINS))
        requestData("https://coinmarketcap.com/coins/views/all/")

        LocalBroadcastManager.getInstance(applicationContext)
                .registerReceiver(mBroadcastReceiver2,
                        IntentFilter(DataService.TOKENS))
        requestData("https://coinmarketcap.com/tokens/views/all/")
    }

    private fun requestData(path: String) {
        val intent = Intent(this, DataService::class.java)
        intent.putExtra("Path", path)
        startService(intent)
    }

    private fun displayTokenItems() {
        var blocks = response2.substringAfter("<tbody>").split("</tr>")
        blocks.take(548)
                .filter { it.length > 19 }
                .forEach { mTokens!!.add(createToken(it)) }


        mTokenList!!.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        mTokenAdapter = TokenAdapter(this, mTokens!!)
        mTokenList!!.adapter = mTokenAdapter
        mTokenList!!.adapter.notifyDataSetChanged()
    }

    //mFavourites!!.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)


    private fun displayCoinItems() {

        var blocks = response.substring(64118).split("</tr>")
        blocks.take(894)
                .filter { it.length > 19 }
                .forEach { mCoins!!.add(createCurrency(it)) }


        mCoinList!!.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        mCoinAdapter = CurrencyAdapter(this, mCoins!!)
        mCoinList!!.adapter = mCoinAdapter
        mCoinList!!.adapter.notifyDataSetChanged()
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


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //sorts: alphabet, marketcap, biggest change, price, place
        when (item.itemId) {
            R.id.search -> Toast.makeText(this, "Search selected", Toast.LENGTH_SHORT).show()
            R.id.sort -> {




                Toast.makeText(this, "Sort selected", Toast.LENGTH_SHORT).show()
            }
            R.id.currency -> Toast.makeText(this, "Currency selected", Toast.LENGTH_SHORT).show()
            R.id.time -> Toast.makeText(this, "Time selected", Toast.LENGTH_SHORT).show()
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

        var symbol = block.substringAfter("<span class=\"currency-symbol\"><a href=\"/currencies/$id/\">")
        symbol = symbol.substring(0, symbol.indexOf("<"))

        var place = block.substringAfter("<td class=\"text-center\">")
        place = place.substring(0, place.indexOf("<")).replace("\n", "").trim()

        var marketCap = block.substringAfter("class=\"no-wrap market-cap text-right\" data-usd=\"")
        marketCap = marketCap.substring(0, marketCap.indexOf("\""))

        var currPrice = block.substringAfter("class=\"price\" data-usd=\"")
        currPrice = currPrice.substring(0, currPrice.indexOf("\""))

        var volume = block.substringAfter("class=\"volume\" data-usd=\"")
        volume = volume.substring(0, volume.indexOf("\""))

        var hrChange = block.substringAfter("no-wrap percent-1h").substringAfter("data-usd=\"")
        hrChange = hrChange.substring(0, hrChange.indexOf("\""))

        var twoChange = block.substringAfter("no-wrap percent-24h").substringAfter("data-usd=\"")
        twoChange = twoChange.substring(0, twoChange.indexOf("\""))

        var sevenChange = block.substringAfter("no-wrap percent-7d").substringAfter("data-usd=\"")
        sevenChange = sevenChange.substring(0, sevenChange.indexOf("\""))

        return Currency(id, symbol, Integer.parseInt(place), marketCap, currPrice, hrChange, "1hr")
    }

    private fun createToken(block: String): Tokens {
        var id = block.substringAfter("<tr id=\"id-")
        id = id.substring(0, id.indexOf("\""))

        var symbol = block.substringAfter("<span class=\"currency-symbol\"><a href=\"/currencies/$id/\">")
        symbol = symbol.substring(0, symbol.indexOf("<"))

        var place = block.substringAfter("<td class=\"text-center\">")
        place = place.substring(0, place.indexOf("<")).replace("\n", "").trim()

        var platform = block.substringAfter("data-platformsymbol=\"")
        platform = platform.substring(0, platform.indexOf("\""))

        var marketCap = block.substringAfter("class=\"no-wrap market-cap text-right\" data-usd=\"")
        marketCap = marketCap.substring(0, marketCap.indexOf("\""))

        var currPrice = block.substringAfter("class=\"price\" data-usd=\"")
        currPrice = currPrice.substring(0, currPrice.indexOf("\""))

        var volume = block.substringAfter("class=\"volume\" data-usd=\"")
        volume = volume.substring(0, volume.indexOf("\""))

        var hrChange = block.substringAfter("no-wrap percent-1h").substringAfter("data-usd=\"")
        hrChange = hrChange.substring(0, hrChange.indexOf("\""))

        var twoChange = block.substringAfter("no-wrap percent-24h").substringAfter("data-usd=\"")
        twoChange = twoChange.substring(0, twoChange.indexOf("\""))

        var sevenChange = block.substringAfter("no-wrap percent-7d").substringAfter("data-usd=\"")
        sevenChange = sevenChange.substring(0, sevenChange.indexOf("\""))

        return Tokens(id, symbol, Integer.parseInt(place), platform, marketCap, currPrice, hrChange, "1hr")
    }
}
