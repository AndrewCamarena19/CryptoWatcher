package com.andyisdope.cryptowatcher.Services

/**
 * Created by Andy on 1/21/2018.
 */

import android.app.IntentService
import android.content.Intent
import android.net.Uri
import android.support.v4.content.LocalBroadcastManager
import android.util.Log

import com.andyisdope.cryptowatch.Currency
import com.andyisdope.cryptowatcher.utils.HttpHelper
import com.google.gson.Gson

import java.io.IOException

class DataService : IntentService("DataService") {

    override fun onHandleIntent(intent: Intent) {
        //        Make the web service request
        val webService = DataWebService.retrofit.create(DataWebService::class.java)
        when(intent.getStringExtra("Path"))
        {
            "Tokens" ->
            {
                val call = webService.getInitial("https://coinmarketcap.com/tokens/views/all/")
                val resp = call.execute().body()
                val messageIntent = Intent(TOKENS)
                messageIntent.putExtra(MY_SERVICE_PAYLOAD, resp)
                val manager = LocalBroadcastManager.getInstance(applicationContext)
                manager.sendBroadcast(messageIntent)
            }
            "Coins"->
            {
                val call = webService.getInitial("https://coinmarketcap.com/coins/views/all/" )
                val resp = call.execute().body()
                val messageIntent = Intent(COINS)
                messageIntent.putExtra(MY_SERVICE_PAYLOAD, resp)
                val manager = LocalBroadcastManager.getInstance(applicationContext)
                manager.sendBroadcast(messageIntent)
            }
            "Market" ->
            {
                val call = webService.getInitial(intent.getStringExtra("Currency"))
                val resp = call.execute().body()
                val messageIntent = Intent(MARKET)
                messageIntent.putExtra(MY_SERVICE_PAYLOAD, resp)
                val manager = LocalBroadcastManager.getInstance(applicationContext)
                manager.sendBroadcast(messageIntent)
            }
            else ->
            {
                val call = webService.getInitial(intent.getStringExtra("Path"))
                val resp = call.execute().body()
                val messageIntent = Intent(Currency)
                messageIntent.putExtra(MY_SERVICE_PAYLOAD, resp)
                val manager = LocalBroadcastManager.getInstance(applicationContext)
                manager.sendBroadcast(messageIntent)
            }
        }


    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "onCreate")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy")
    }

    companion object {

        val TAG = "DataService"
        val TOKENS = "TOKENS"
        val COINS = "COINS"
        val Currency = "Currency"
        val MY_SERVICE_PAYLOAD = "DataPayload"
        val MARKET = "Market"
    }

}


