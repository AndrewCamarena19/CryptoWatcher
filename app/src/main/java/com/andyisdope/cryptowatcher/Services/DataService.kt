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

    override fun onHandleIntent(intent: Intent?) {
        //        Make the web service request
        val webService = DataWebService.retrofit.create(DataWebService::class.java)
        val call = webService.dataItems(intent!!.getStringExtra("Path"))

        val dataItems: Array<Currency>

        try {
            dataItems = call.execute().body()!!
        } catch (e: IOException) {
            e.printStackTrace()
            Log.i(TAG, "onHandleIntent: " + e.message)
            return
        }

        //        Return the data to MainActivity
        val messageIntent = Intent(MY_SERVICE_MESSAGE)
        messageIntent.putExtra(MY_SERVICE_PAYLOAD, dataItems)
        val manager = LocalBroadcastManager.getInstance(applicationContext)
        manager.sendBroadcast(messageIntent)

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
        val MY_SERVICE_MESSAGE = "DataMessage"
        val MY_SERVICE_PAYLOAD = "DataPayload"
    }

}


