package com.andyisdope.cryptowatcher

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.andyisdope.cryptowatcher.Adapters.TransactionAdapter
import com.andyisdope.cryptowatcher.database.TransactionDatabase
import com.andyisdope.cryptowatcher.model.Transaction
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import java.util.*

//TODO: parse dates into long for range check
//TODO: Complete all transactions button
class TransactionHistory : AppCompatActivity() {

    private lateinit var coinName: TextView
    private lateinit var buyBtn: Button
    private lateinit var sellBtn: Button
    private lateinit var allBtn: Button
    private lateinit var startDate: EditText
    private lateinit var endDate: EditText
    private lateinit var transactionList: RecyclerView
    private lateinit var sellsTotal: TextView
    private lateinit var buysTotal: TextView
    private lateinit var netTotal: TextView
    private lateinit var mTransAdapter: TransactionAdapter
    private lateinit var transactionDB: TransactionDatabase
    private lateinit var transactionsFull: ArrayList<Transaction>
    private lateinit var coinNameString: String
    private lateinit var buyList: List<Transaction>
    private lateinit var sellList: List<Transaction>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_history)
        coinNameString = intent.getStringExtra("Coin")
        initViews()
        initList()
    }

    //Init Recycler Views with Transaction Room lookup
    //Create 3 views, Buys, Sells, All
    private fun initList() {
        transactionDB = TransactionDatabase.getInstance(this)!!
        async(UI) {
            val FullList = async(CommonPool)
            {
                transactionDB!!.TransactionDao().getCoinTransactions(coinNameString)
            }
            val SellNet = async(CommonPool)
            {
                transactionDB!!.TransactionDao().getAllCoinSells(coinNameString)
            }
            val BuyNet = async(CommonPool)
            {
                transactionDB!!.TransactionDao().getAllCoinBuys(coinNameString)
            }
            transactionsFull = ArrayList(FullList.await())

            val selltemp = SellNet.await()
            val buytemp = BuyNet.await()
            sellsTotal.text = "$ $selltemp"
            buysTotal.text = "$ $buytemp"
            if (selltemp + buytemp > 0)
                netTotal.setTextColor(Color.GREEN)
            else
                netTotal.setTextColor(Color.RED)

            netTotal.text = "$ ${selltemp + buytemp}"
            //Runnables to filter lists
            Runnable {
                sellList = transactionsFull
                        .filter { it.Sell }
                        .sortedBy { it.Date }
            }.run()
            Runnable {
                buyList = transactionsFull
                        .filter { it.Buy }
                        .sortedBy { it.Date }
            }.run()


            mTransAdapter = TransactionAdapter(baseContext, transactionsFull)
            transactionList.adapter = mTransAdapter
            transactionList.adapter.notifyDataSetChanged()
        }

    }

    //Set up UI elements
    private fun initViews() {
        coinName = findViewById(R.id.TransactionCoin)
        coinName.text = coinNameString
        buyBtn = findViewById(R.id.TransctionBuys)
        sellBtn = findViewById(R.id.TransactionSells)
        allBtn = findViewById(R.id.TransactionsAll)
        startDate = findViewById(R.id.TransactionDateStart)
        endDate = findViewById(R.id.TransactionDateEnd)
        transactionList = findViewById(R.id.TransactionsList)
        transactionList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        sellsTotal = findViewById(R.id.TransactionSellTotals)
        buysTotal = findViewById(R.id.TransactionBuyTotals)
        netTotal = findViewById(R.id.TransactionNetTotals)

        buysTotal.setTextColor(Color.RED)
        sellsTotal.setTextColor(Color.GREEN)

        //Button to select only buy in certain date range with regex
        buyBtn.setOnClickListener {
            var preParseStart = startDate.text.toString()
            if(preParseStart.contains("^\\d{1,2}\\/\\d{1,2}\\/\\d{4}\$")) {
                var StartLong = Date(preParseStart).time
                var EndLong = endDate.text.toString().toLongOrNull() ?: Long.MAX_VALUE
                var range = buyList
                        .filter { it.Date in StartLong..(EndLong - 1) }

                mTransAdapter = TransactionAdapter(baseContext, ArrayList(range))
                transactionList.adapter = mTransAdapter
                transactionList.adapter.notifyDataSetChanged()
            }
        }
        //Sell Button for date ranges without regex yet
        sellBtn.setOnClickListener {
            var StartLong = startDate.text.toString().toLongOrNull() ?: 0
            var EndLong = endDate.text.toString().toLongOrNull() ?: Long.MAX_VALUE
            var range = sellList
                    .filter { it.Date in StartLong..(EndLong - 1) }

            mTransAdapter = TransactionAdapter(baseContext, ArrayList(range))
            transactionList.adapter = mTransAdapter
            transactionList.adapter.notifyDataSetChanged()
        }

    }

    //Clean up Database
    override fun onDestroy() {
        TransactionDatabase.destroyInstance()
        super.onDestroy()
    }
}
