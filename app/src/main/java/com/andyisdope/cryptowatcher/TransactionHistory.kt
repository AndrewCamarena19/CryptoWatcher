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

    private lateinit var CoinName: TextView
    private lateinit var BuyBtn: Button
    private lateinit var SellBtn: Button
    private lateinit var AllBtn: Button
    private lateinit var StartDate: EditText
    private lateinit var EndDate: EditText
    private lateinit var TransactionList: RecyclerView
    private lateinit var SellsTotal: TextView
    private lateinit var BuysTotal: TextView
    private lateinit var NetTotal: TextView
    private lateinit var mTransAdapter: TransactionAdapter
    private lateinit var TransactionDB: TransactionDatabase
    private lateinit var TransactionsFull: ArrayList<Transaction>
    private lateinit var CoinNameString: String
    private lateinit var BuyList: List<Transaction>
    private lateinit var SellList: List<Transaction>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_history)
        CoinNameString = intent.getStringExtra("Coin")
        initViews()
        initList()
    }

    private fun initList() {
        TransactionDB = TransactionDatabase.getInstance(this)!!
        async(UI) {
            val FullList = async(CommonPool)
            {
                TransactionDB!!.TransactionDao().getCoinTransactions(CoinNameString)
            }
            val SellNet = async(CommonPool)
            {
                TransactionDB!!.TransactionDao().getAllCoinSells(CoinNameString)
            }
            val BuyNet = async(CommonPool)
            {
                TransactionDB!!.TransactionDao().getAllCoinBuys(CoinNameString)
            }
            TransactionsFull = ArrayList(FullList.await())

            val selltemp = SellNet.await()
            val buytemp = BuyNet.await()
            SellsTotal.text = "$ $selltemp"
            BuysTotal.text = "$ $buytemp"
            if (selltemp + buytemp > 0)
                NetTotal.setTextColor(Color.GREEN)
            else
                NetTotal.setTextColor(Color.RED)

            NetTotal.text = "$ ${selltemp + buytemp}"
            Log.i("Database", TransactionsFull.toString())

            Runnable {
                SellList = TransactionsFull
                        .filter { it.Sell }
                        .sortedBy { it.Date }
            }.run()
            Runnable {
                BuyList = TransactionsFull
                        .filter { it.Buy }
                        .sortedBy { it.Date }
            }.run()


            mTransAdapter = TransactionAdapter(baseContext, TransactionsFull)
            TransactionList.adapter = mTransAdapter
            TransactionList.adapter.notifyDataSetChanged()
        }

    }

    private fun initViews() {
        CoinName = findViewById(R.id.TransactionCoin)
        CoinName.text = CoinNameString
        BuyBtn = findViewById(R.id.TransctionBuys)
        SellBtn = findViewById(R.id.TransactionSells)
        AllBtn = findViewById(R.id.TransactionsAll)
        StartDate = findViewById(R.id.TransactionDateStart)
        EndDate = findViewById(R.id.TransactionDateEnd)
        TransactionList = findViewById(R.id.TransactionsList)
        TransactionList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        SellsTotal = findViewById(R.id.TransactionSellTotals)
        BuysTotal = findViewById(R.id.TransactionBuyTotals)
        NetTotal = findViewById(R.id.TransactionNetTotals)

        BuysTotal.setTextColor(Color.RED)
        SellsTotal.setTextColor(Color.GREEN)

        BuyBtn.setOnClickListener {
            var preParseStart = StartDate.text.toString()
            if(preParseStart.contains("^\\d{1,2}\\/\\d{1,2}\\/\\d{4}\$")) {
                var StartLong = Date(preParseStart).time
                var EndLong = EndDate.text.toString().toLongOrNull() ?: Long.MAX_VALUE
                var range = BuyList
                        .filter { it.Date in StartLong..(EndLong - 1) }

                mTransAdapter = TransactionAdapter(baseContext, ArrayList(range))
                TransactionList.adapter = mTransAdapter
                TransactionList.adapter.notifyDataSetChanged()
            }
        }

        SellBtn.setOnClickListener {
            var StartLong = StartDate.text.toString().toLongOrNull() ?: 0
            var EndLong = EndDate.text.toString().toLongOrNull() ?: Long.MAX_VALUE
            var range = SellList
                    .filter { it.Date in StartLong..(EndLong - 1) }

            mTransAdapter = TransactionAdapter(baseContext, ArrayList(range))
            TransactionList.adapter = mTransAdapter
            TransactionList.adapter.notifyDataSetChanged()
        }

    }

    override fun onDestroy() {
        TransactionDatabase.destroyInstance()
        super.onDestroy()
    }
}
