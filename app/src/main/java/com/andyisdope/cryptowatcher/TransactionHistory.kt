package com.andyisdope.cryptowatcher

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_history)

        initViews()
    }

    private fun initViews() {
        CoinName = findViewById(R.id.TransactionCoin)
        BuyBtn = findViewById(R.id.TransctionBuys)
        SellBtn = findViewById(R.id.TransactionSells)
        AllBtn = findViewById(R.id.TransactionsAll)
        StartDate = findViewById(R.id.TransactionDateStart)
        EndDate = findViewById(R.id.TransactionDateEnd)
        TransactionList = findViewById(R.id.TransactionsList)
        SellsTotal = findViewById(R.id.TransactionSellTotals)
        BuysTotal = findViewById(R.id.TransactionBuyTotals)
        SellsTotal = findViewById(R.id.TransactionNetTotals)

    }
}
