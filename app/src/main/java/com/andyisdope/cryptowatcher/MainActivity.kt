package com.andyisdope.cryptowatcher

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import android.support.v4.content.LocalBroadcastManager
import com.andyisdope.cryptowatcher.Services.MyService
import android.support.v7.widget.RecyclerView
import android.preference.PreferenceManager
import android.content.*
import android.support.v4.widget.DrawerLayout
import android.widget.ListView
import com.andyisdope.cryptowatch.Currency
import android.widget.TabHost
import android.support.v7.widget.LinearLayoutManager






class MainActivity : AppCompatActivity() {

    private val SIGNIN_REQUEST = 1001
    val MY_GLOBAL_PREFS = "my_global_prefs"
    private val TAG = "MainActivity"
    //var dataItemList = SampleDataProvider.dataItemList
    private val JSON_URL = "https://www.cryptocompare.com/"

    //var mDataSource: DataSource
    var mItemList: ArrayList<Currency>? = ArrayList()
    var mTokens: ArrayList<Currency>? = ArrayList()
    var mDrawerLayout: DrawerLayout? = null
    var mDrawerList: ListView? = null
    var mCategories: Array<String>? = null
    var mCoinList: RecyclerView? = null
    var mTokenList: RecyclerView? = null
    var mFavourites: RecyclerView? = null
    var mItemAdapters: CurrencyAdapter? = null
    var mItemAdapter: CurrencyAdapter? = null
    var networkOk: Boolean = false

    private val mBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val dataItems = intent
                    .getParcelableArrayExtra(MyService.MY_SERVICE_PAYLOAD) as Array<Currency>
            Toast.makeText(this@MainActivity,
                    "Received " + dataItems.size + " items from service",
                    Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //      Code to manage sliding navigation drawer
        //      end of navigation drawer
        val settings = PreferenceManager.getDefaultSharedPreferences(this)
        val grid = false

        initTabs()
        mCoinList = findViewById<RecyclerView>(R.id.CoinList) as RecyclerView
        mTokenList = findViewById<RecyclerView>(R.id.TokenList) as RecyclerView
        mFavourites = findViewById<RecyclerView>(R.id.Favorites) as RecyclerView

        displayDataItems(null)

        LocalBroadcastManager.getInstance(applicationContext)
                .registerReceiver(mBroadcastReceiver,
                        IntentFilter(MyService.MY_SERVICE_MESSAGE))

    }

    private fun displayDataItems(category: String?) {
        //change data sources
        mItemList!!.add(Currency("CAndy", "www.internet", "flow", 1, "Candy", "10000", "100 dong", "15.00", "24Hr"))
        mItemList!!.add(Currency("DAndy", "www.internet", "cream", 1, "Dandy", "10000", "100 dong", "-15.00", "24Hr"))
        mItemList!!.add(Currency("Andy", "www.internet", "soma", 1, "Andy", "10000", "100 dong", "150.00", "24Hr"))
        mItemList!!.add(Currency("CAndy", "www.internet", "flow", 1, "Candy", "10000", "100 dong", "15.00", "24Hr"))
        mItemList!!.add(Currency("DAndy", "www.internet", "cream", 1, "Dandy", "10000", "100 dong", "-15.00", "24Hr"))
        mItemList!!.add(Currency("Andy", "www.internet", "soma", 1, "Andy", "10000", "100 dong", "150.00", "24Hr"))
        mItemList!!.add(Currency("CAndy", "www.internet", "flow", 1, "Candy", "10000", "100 dong", "15.00", "24Hr"))
        mItemList!!.add(Currency("DAndy", "www.internet", "cream", 1, "Dandy", "10000", "100 dong", "-15.00", "24Hr"))
        mItemList!!.add(Currency("Andy", "www.internet", "soma", 1, "Andy", "10000", "100 dong", "150.00", "24Hr"))
        mItemList!!.add(Currency("CAndy", "www.internet", "flow", 1, "Candy", "10000", "100 dong", "15.00", "24Hr"))
        mItemList!!.add(Currency("DAndy", "www.internet", "cream", 1, "Dandy", "10000", "100 dong", "-15.00", "24Hr"))
        mItemList!!.add(Currency("Andy", "www.internet", "soma", 1, "Andy", "10000", "100 dong", "150.00", "24Hr"))

        mCoinList!!.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        mTokenList!!.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        mFavourites!!.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        mItemAdapter = CurrencyAdapter(this, mItemList!!)
        mCoinList!!.adapter = mItemAdapter
        mCoinList!!.adapter.notifyDataSetChanged()

        mTokens!!.add(Currency("BAndy", "www.internet", "flow", 1, "Bandy", "10000", "100 dong", "105.00", "24Hr"))
        mItemAdapters = CurrencyAdapter(this, mTokens!!)
        mTokenList!!.adapter = mItemAdapters
        mTokenList!!.adapter.notifyDataSetChanged()
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
    }

    fun initTabs()
    {
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
        when (item.itemId) {
            R.id.search -> Toast.makeText(this, "Search selected", Toast.LENGTH_SHORT).show()
            R.id.sort -> Toast.makeText(this, "Sort selected", Toast.LENGTH_SHORT).show()
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
}
