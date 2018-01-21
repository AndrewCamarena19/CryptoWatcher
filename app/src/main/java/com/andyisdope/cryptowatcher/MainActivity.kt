package com.andyisdope.cryptowatcher

import android.Manifest
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
import android.content.pm.PackageManager
import android.os.Build
import android.app.Activity
import android.support.v4.app.ActivityCompat
import com.andyisdope.cryptowatcher.Services.DataService
import android.content.Intent
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import android.support.annotation.NonNull
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.storage.FileDownloadTask
import com.google.android.gms.tasks.OnSuccessListener




class MainActivity : AppCompatActivity() {

    private val SIGNIN_REQUEST = 1001
    val MY_GLOBAL_PREFS = "my_global_prefs"
    private val TAG = "MainActivity"
    //var dataItemList = SampleDataProvider.dataItemList


    //var mDataSource: DataSource
    var sRef: StorageReference = FirebaseStorage.getInstance().reference
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
    val READ_STORAGE_PERMISSION_REQUEST_CODE = 1

    private val mBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val dataItems = intent
                    .getParcelableArrayExtra(DataService.MY_SERVICE_PAYLOAD) as Array<Currency>
            Toast.makeText(this@MainActivity,
                    "Received " + dataItems.size + " items from service",
                    Toast.LENGTH_SHORT).show()
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
        if(!checkPermissionForReadExtertalStorage())
            requestPermissionForReadExtertalStorage()
        if(!checkPermissionForWriteExtertalStorage())
            requestPermissionForWriteExtertalStorage()
        //      Code to manage sliding navigation drawer
        //      end of navigation drawer
        val settings = PreferenceManager.getDefaultSharedPreferences(this)
        val grid = false

        val localFile = File.createTempFile("images", "jpg")
        sRef.getFile(localFile)
                .addOnSuccessListener(OnSuccessListener<FileDownloadTask.TaskSnapshot> {
                    // Successfully downloaded data to local file
                    // ...
                }).addOnFailureListener(OnFailureListener {
            // Handle failed download
            // ...
        })

        initTabs()
        mCoinList = findViewById<RecyclerView>(R.id.CoinList) as RecyclerView
        mTokenList = findViewById<RecyclerView>(R.id.TokenList) as RecyclerView
        mFavourites = findViewById<RecyclerView>(R.id.Favorites) as RecyclerView

        displayDataItems(null)

        LocalBroadcastManager.getInstance(applicationContext)
                .registerReceiver(mBroadcastReceiver,
                        IntentFilter(DataService.MY_SERVICE_MESSAGE))

    }

    private fun requestData(path: String) {
        val intent = Intent(this, DataService::class.java)
        intent.putExtra("Path", path)
        startService(intent)
    }


    private fun displayDataItems(category: String?) {
        //change data sources
        //mItemList!!.add(Currency("CAndy", "/media/19633/btc.png", "flow", 1, "Candy", "10000", "100 dong", "15.00", "24Hr"))
        //mItemList!!.add(Currency("DAndy", "/media/20646/eth_logo.png", "cream", 1, "Dandy", "10000", "100 dong", "-15.00", "24Hr"))
        //mItemList!!.add(Currency("Andy", "/media/19782/litecoin-logo.png", "soma", 1, "Andy", "10000", "100 dong", "150.00", "24Hr"))
        //mItemList!!.add(Currency("CAndy", "/media/19684/doge.png", "flow", 1, "Candy", "10000", "100 dong", "15.00", "24Hr"))
        //mItemList!!.add(Currency("DAndy", "/media/351360/zec.png", "cream", 1, "Dandy", "10000", "100 dong", "-15.00", "24Hr"))



        mCoinList!!.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        mTokenList!!.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        mFavourites!!.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        mItemAdapter = CurrencyAdapter(this, mItemList!!)
        mCoinList!!.adapter = mItemAdapter
        mCoinList!!.adapter.notifyDataSetChanged()

        mTokens!!.add(Currency("CAndy", "/media/19684/doge.png", "flow", 1, "Candy", "10000", "100 dong", "15.00", "24Hr"))
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
