package com.andyisdope.cryptowatcher.Services

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import android.content.Context
import android.content.SharedPreferences
import android.os.AsyncTask
import android.util.Log
import com.andyisdope.cryptowatcher.database.AssetDatabase
import com.andyisdope.cryptowatcher.model.DateAsset
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*

class PortfolioService : JobService() {

    override fun onStopJob(p0: JobParameters?): Boolean {
        Log.i("Jobb", "Canelling yobs")
        return false
    }

    override fun onStartJob(p0: JobParameters?): Boolean {
        Log.i("Jobb", "Starting yobs")
        var pTask = PortfolioTask(this)
        pTask.execute(p0)
        return true
    }

    class PortfolioTask(job: JobService) : AsyncTask<JobParameters, Void, JobParameters>() {

        val adf = SimpleDateFormat("MM/dd/YYYY")
        private lateinit var currentTime: Date
        private var js: JobService = job

        override fun doInBackground(vararg p0: JobParameters): JobParameters? {

            var context = js.applicationContext
            currentTime = Calendar.getInstance().time
            val CoinsPref: SharedPreferences = context.getSharedPreferences("CoinNames", Context.MODE_PRIVATE)
            val TotalWorth: SharedPreferences = context.getSharedPreferences("TotalWorth", Context.MODE_PRIVATE)
            val USD: SharedPreferences = context.getSharedPreferences("Coins", Context.MODE_PRIVATE)
            val AssetDB = AssetDatabase.getInstance(context)
            TotalWorth.edit().clear().apply()
            var total = USD.getString("USD", "0.0").toDouble()
            CoinsPref.all.entries.forEach {
                val call = CurrencyService.webService.getInitial("https://api.coinmarketcap.com/v1/ticker/${it.key}/")
                val resp = call.execute().body()
                var json = JSONArray(resp)
                total += json.getJSONObject(0)["price_usd"].toString().toDouble() * CoinsPref.getString(it.key, "0.0").toDouble()
            }
            val date = DateAsset(adf.format(Date(currentTime.time)).toString(), total)
            AssetDB?.AssetDao()?.insertAll(date)
            Log.i("Jobb", date.toString())
            return p0[0]
        }

        override fun onPostExecute(result: JobParameters) {
            super.onPostExecute(result)
            js.jobFinished(result, false)
            Log.i("Jobb", "all done")
        }
    }
}
//4076.35702