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
import android.widget.Toast
import com.andyisdope.cryptowatcher.Adapters.TransactionAdapter
import com.andyisdope.cryptowatcher.database.TransactionDatabase
import com.andyisdope.cryptowatcher.model.Transaction
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import java.math.BigDecimal
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

//TODO: parse dates into long for range check
//TODO: Complete all transactions button
class TransactionHistory : AppCompatActivity() {

    private lateinit var coinName: TextView
    private lateinit var buyBtn: Button
    private lateinit var sellBtn: Button
    private lateinit var allBtn: Button
    private lateinit var startDate: EditText
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

            val selltemp: BigDecimal = BigDecimal(SellNet.await()).setScale(3, BigDecimal.ROUND_HALF_UP)
            val buytemp = BigDecimal(BuyNet.await()).setScale(3, BigDecimal.ROUND_HALF_UP)
            sellsTotal.text = "$ $selltemp"
            buysTotal.text = "$ $buytemp"
            if (selltemp + buytemp > BigDecimal(0.0))
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
        transactionList = findViewById(R.id.TransactionsList)
        transactionList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        sellsTotal = findViewById(R.id.TransactionSellTotals)
        buysTotal = findViewById(R.id.TransactionBuyTotals)
        netTotal = findViewById(R.id.TransactionNetTotals)

        buysTotal.setTextColor(Color.RED)
        sellsTotal.setTextColor(Color.GREEN)

        //Button to select only buy in certain date range with regex
        buyBtn.setOnClickListener {
            var StartLong = parseDate(startDate.text.toString())
            mTransAdapter = if (StartLong > 0) {
                var range = buyList
                        .filter { it.Date in StartLong..(StartLong + 2678400000) }
                TransactionAdapter(baseContext, ArrayList(range))

            } else {
                Toast.makeText(this, "Returning all Buys", Toast.LENGTH_LONG).show()
                TransactionAdapter(baseContext, ArrayList(buyList))
            }

            transactionList.adapter = mTransAdapter
            transactionList.adapter.notifyDataSetChanged()
        }
        //Sell Button for date ranges without regex yet
        sellBtn.setOnClickListener {
            var StartLong = parseDate(startDate.text.toString())
            mTransAdapter = if (StartLong > 0) {
                var range = sellList
                        .filter { it.Date in StartLong..(StartLong + 2678400000) }
                TransactionAdapter(baseContext, ArrayList(range))

            } else {
                Toast.makeText(this, "Returning all Sells", Toast.LENGTH_LONG).show()
                TransactionAdapter(baseContext, ArrayList(sellList))
            }
            transactionList.adapter = mTransAdapter
            transactionList.adapter.notifyDataSetChanged()
        }

        allBtn.setOnClickListener {
            var StartLong = parseDate(startDate.text.toString())
            mTransAdapter = if (StartLong > 0) {
                var range = transactionsFull
                        .filter { it.Date in StartLong..(StartLong + 2678400000) }
                TransactionAdapter(baseContext, ArrayList(range))

            } else {
                Toast.makeText(this, "Returning all Transactions", Toast.LENGTH_LONG).show()
                TransactionAdapter(baseContext, ArrayList(transactionsFull))
            }
            transactionList.adapter = mTransAdapter
            transactionList.adapter.notifyDataSetChanged()
        }

    }

    private fun parseDate(date: String): Long {
        val toParse = date.split("/", "-", ":")
        var result: Long = 0
        if (toParse.size == 2) {
            var month = toParse[0].toIntOrNull() ?: 0
            var year = toParse[1].toIntOrNull() ?: 0
            if (month in 1..12) {
                var dateFormat: DateFormat = SimpleDateFormat("MM,yyyy")
                var dateParsed: Date = dateFormat.parse("$month,$year")
                result = dateParsed.time
            } else {
                Toast.makeText(this, "Invalid Date, month must be between 1-12 with no whitespace", Toast.LENGTH_LONG).show()
            }
        } else
            Toast.makeText(this, "Invalid Date, must have only 1 separator character (/,:,-)", Toast.LENGTH_LONG).show()

        return result
    }

    //Clean up Database
    override fun onDestroy() {
        TransactionDatabase.destroyInstance()
        super.onDestroy()
    }
}
