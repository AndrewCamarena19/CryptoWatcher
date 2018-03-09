package com.andyisdope.cryptowatcher.Services

import android.app.AppOpsManager
import android.app.IntentService
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.widget.RemoteViews
import com.andyisdope.cryptowatcher.CurrencyWidget
import com.andyisdope.cryptowatcher.CurrencyWidgetConfigureActivity
import com.andyisdope.cryptowatcher.MainActivity
import com.andyisdope.cryptowatcher.R

/**
 * An [IntentService] subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 *
 *
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
class CurrencyService : IntentService("CurrencyService") {

    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            val action = intent.action
            if (UPDATE == action) {
                val param1 = intent.getIntExtra(EXTRA_PARAM1, 0)
                val param2 = intent.getStringExtra(EXTRA_PARAM2)
                handleActionFoo(param1, param2)
            }
        }
    }
    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private fun handleActionFoo(param1: Int, param2: String) {
        var WM: AppWidgetManager = AppWidgetManager.getInstance(this.applicationContext)
        val views = RemoteViews(this.packageName, R.layout.currency_widget)
        var data = parseCurrencyData(param2).split(",")
        //Log.i("Here", data.toString() + param1.toString() + param2)
        views.setTextViewText(R.id.appwidget_text, param2)
        CurrencyWidgetConfigureActivity.saveTitlePref(this, param1, param2 + "," + data)
        views.setTextViewText(R.id.WidgetPrice, data[0])
        views.setTextViewText(R.id.WidgetChange, data[1])
        when
        {
            (data[1].toDouble() < 0) -> views.setTextColor(R.id.WidgetChange, Color.RED)
            else  -> views.setTextColor(R.id.WidgetChange, Color.GREEN)
        }
        val intentSync = Intent(this.applicationContext, CurrencyWidget::class.java)
        intentSync.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE //You need to specify the action for the intent. Right now that intent is doing nothing for there is no action to be broadcasted.
        intentSync.putExtra("ID", param1)
        val pendingSync = PendingIntent.getBroadcast(this.applicationContext, 0, intentSync, PendingIntent.FLAG_UPDATE_CURRENT) //You need to specify a proper flag for the intent. Or else the intent will become deleted.
        views.setOnClickPendingIntent(R.id.widgetRefresh, pendingSync)
        WM.updateAppWidget(param1, views)

    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private fun handleActionBaz(param1: String, param2: String) {
        // TODO: Handle action Baz
        throw UnsupportedOperationException("Not yet implemented")
    }

    companion object {
        // TODO: Rename actions, choose action names that describe tasks that this
        // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
        val UPDATE = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        private val ACTION_BAZ = "com.andyisdope.cryptowatcher.Services.action.BAZ"

        // TODO: Rename parameters
        val EXTRA_PARAM1 = "com.andyisdope.cryptowatcher.Services.extra.PARAM1"
        val EXTRA_PARAM2 = "com.andyisdope.cryptowatcher.Services.extra.PARAM2"

        /**
         * Starts this service to perform action Foo with the given parameters. If
         * the service is already performing a task this action will be queued.
         *
         * @see IntentService
         */
        // TODO: Customize helper method
        fun startActionFoo(context: Context, param1: Int, param2: String) {
                val intent = Intent(context, CurrencyService::class.java)
                intent.action = UPDATE
                intent.putExtra(EXTRA_PARAM1, param1)
                intent.putExtra(EXTRA_PARAM2, param2)
                context.startService(intent)
        }

        fun parseCurrencyData(curr: String): String
        {
            val webService = DataWebService.retrofit.create(DataWebService::class.java)
            val call = webService.getInitial("https://coinmarketcap.com/currencies/$curr/" )
            val resp = call.execute().body()

            //Log.i("Here", resp)
            var price = resp?.substringAfter("<span class=\"price\" data-usd=\"")
            price = price?.substring(0, price.indexOf("\""))

            var change = resp?.substringAfter("(<span data-format-percentage data-format-value=\"")
            change = change?.substring(0, change.indexOf("\""))

            return price+","+change
        }

        /**
         * Starts this service to perform action Baz with the given parameters. If
         * the service is already performing a task this action will be queued.
         *
         * @see IntentService
         */
        // TODO: Customize helper method
        fun startActionBaz(context: Context, param1: String, param2: String) {
            val intent = Intent(context, CurrencyService::class.java)
            intent.action = ACTION_BAZ
            intent.putExtra(EXTRA_PARAM1, param1)
            intent.putExtra(EXTRA_PARAM2, param2)
            context.startService(intent)
        }
    }
}
