package com.andyisdope.cryptowatcher.Services

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.widget.RemoteViews
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import android.os.Build
import android.support.v4.app.NotificationManagerCompat
import android.R.attr.name
import android.app.*
import android.support.v4.app.NotificationCompat
import android.app.PendingIntent
import com.andyisdope.cryptowatcher.*


/**
 * An [IntentService] subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 *
 *
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
class CurrencyService : IntentService("CurrencyService") {
    var formatterSmall: NumberFormat = DecimalFormat("#0.000")
    var formatterLarge: NumberFormat = DecimalFormat("#,###.00")


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
        val currentTime = Calendar.getInstance().time
        val date = Date(currentTime.time)
        val sdf = SimpleDateFormat("HH:mm:ss")
        var WM: AppWidgetManager = AppWidgetManager.getInstance(this.applicationContext)
        val views = RemoteViews(this.packageName, R.layout.currency_widget)
        var data = parseCurrencyData(param2).split(",")
        //Log.i("Here", data.toString() + param1.toString() + param2)
        views.setTextViewText(R.id.appwidget_text, param2.toUpperCase())
        views.setTextViewText(R.id.widgetLast,"Updated: " + sdf.format(date))
        CurrencyWidgetConfigureActivity.saveTitlePref(this, param1, param2 + "," + data)
        views.setTextViewText(R.id.WidgetPrice, "$ " + formatterLarge.format(data[0].toFloat()))
        when
        {
            (data[1].toFloat() < 0) ->
            {
                views.setTextColor(R.id.WidgetChange, Color.RED)
                views.setTextViewText(R.id.WidgetChange, formatterSmall.format(data[1].toFloat()) + "%")

            }
            else  ->
            {
                views.setTextColor(R.id.WidgetChange, Color.GREEN)
                views.setTextViewText(R.id.WidgetChange, "+" + formatterSmall.format(data[1].toFloat()) + "%")

            }
        }
        val intentSync = Intent(this.applicationContext, CurrencyWidget::class.java)
        intentSync.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE //You need to specify the action for the intent. Right now that intent is doing nothing for there is no action to be broadcasted.
        intentSync.putExtra("ID", param1)
        val pendingSync = PendingIntent.getBroadcast(this.applicationContext, param1, intentSync, PendingIntent.FLAG_UPDATE_CURRENT) //You need to specify a proper flag for the intent. Or else the intent will become deleted.
        views.setOnClickPendingIntent(R.id.widgetLast, pendingSync)
        createNotification(param2, data[0], param1)
        WM.updateAppWidget(param1, views)

    }

    private fun createNotification(curr: String, price: String, WidgetID: Int)
    {

        val intent = Intent(this, CurrencyDetail::class.java)
        intent.putExtra("Currency", curr)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(this, WidgetID, intent, 0)

        val mNotific = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val name = "CryptoWatcher Alert"
        val desc = "${curr.toUpperCase()} has hit price ${formatterLarge.format(price.toFloat())}"
        val imp = NotificationManager.IMPORTANCE_HIGH
        val ChannelID = "my_channel_01"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mChannel = NotificationChannel(ChannelID, name,
                    imp)
            mChannel.description = desc
            mChannel.lightColor = Color.CYAN
            mChannel.canShowBadge()
            mChannel.setShowBadge(true)
            mNotific.createNotificationChannel(mChannel)
        }

        val ncode = 101
        val n = NotificationCompat.Builder(this, ChannelID)
                .setContentTitle(name)
                .setContentText(desc)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                .setNumber(5)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

        mNotific.notify(ncode, n)
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
