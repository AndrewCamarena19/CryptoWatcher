package com.andyisdope.cryptowatcher.Services

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.Context
import android.graphics.Color
import android.widget.RemoteViews
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import android.os.Build
import android.app.*
import android.support.v4.app.NotificationCompat
import android.app.PendingIntent
import android.widget.Toast
import com.andyisdope.cryptowatcher.*
import java.lang.Math.abs
import android.os.PowerManager

class CurrencyService : IntentService("CurrencyService") {
    var formatterSmall: NumberFormat = DecimalFormat("#0.000")
    var formatterLarge: NumberFormat = DecimalFormat("#,###.00")


    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            val action = intent.action
            if (UPDATE == action) {
                val param1 = intent.getIntExtra(WIDGETID, 0)
                val param2 = intent.getStringExtra(WIDGETTEXT)
                val param3 = intent.getStringExtra(WIDGETHIGH)
                val param4 = intent.getStringExtra(WIDGETBOTTOM)
                val param5 = intent.getStringExtra(WIDGETCHANGE)
                handleActionFoo(param1, param2, param3.toFloat(), param4.toFloat(), param5.toFloat())
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private fun handleActionFoo(param1: Int, param2: String, High: Float, Bottom: Float, Change: Float) {
        val currentTime = Calendar.getInstance().time
        val date = Date(currentTime.time)
        val sdf = SimpleDateFormat("HH:mm:ss")
        var WM: AppWidgetManager = AppWidgetManager.getInstance(this.applicationContext)
        val views = RemoteViews(this.packageName, R.layout.currency_widget)
        var data = parseCurrencyData(param2).split(",")
        views.setTextViewText(R.id.appwidget_text, param2.toUpperCase())
        views.setTextViewText(R.id.widgetLast, "Updated: " + sdf.format(date))
        views.setTextViewText(R.id.WidgetPrice, "$ " + formatterLarge.format(data[0].toFloat()))
        when {
            (data[1].toFloat() < 0) -> {
                views.setTextColor(R.id.WidgetChange, Color.RED)
                views.setTextViewText(R.id.WidgetChange, formatterSmall.format(data[1].toFloat()) + "%")

            }
            else -> {
                views.setTextColor(R.id.WidgetChange, Color.GREEN)
                views.setTextViewText(R.id.WidgetChange, "+" + formatterSmall.format(data[1].toFloat()) + "%")

            }
        }
        val intentSync = Intent(this.applicationContext, CurrencyWidget::class.java)
        intentSync.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE //You need to specify the action for the intent. Right now that intent is doing nothing for there is no action to be broadcasted.
        intentSync.putExtra("ID", param1)
        val pendingSync = PendingIntent.getBroadcast(this.applicationContext, param1, intentSync, PendingIntent.FLAG_UPDATE_CURRENT) //You need to specify a proper flag for the intent. Or else the intent will become deleted.
        views.setOnClickPendingIntent(R.id.widgetLast, pendingSync)
        CurrencyWidgetConfigureActivity.saveTitlePref(this, param1, "$param2,$data,$High,$Bottom,$Change")

        if (High != -999.0f && data[0].toFloat() > High) {
            createNotification(param2, data[0], param1, "high")
            CurrencyWidgetConfigureActivity.saveTitlePref(this, param1, "$param2,$data,-999.0f,$Bottom,$Change")
        }
        if (Bottom != -999.0f && data[0].toFloat() < Bottom) {
            createNotification(param2, data[0], param1, "bottom")
            CurrencyWidgetConfigureActivity.saveTitlePref(this, param1, "$param2,$data,$High,-999.0f,$Change")
        }
        if (Change != -999.0f && abs(data[1].toFloat()) > Change) {
            createNotification(param2, data[1], param1, "absolute percent change")
            CurrencyWidgetConfigureActivity.saveTitlePref(this, param1, "$param2,$data,$High,$Bottom,-999.0f")
        }
        WM.updateAppWidget(param1, views)

    }

    private fun createNotification(curr: String, Change: String, WidgetID: Int, Alert: String) {
        val intent = Intent(this, CurrencyDetail::class.java)
        intent.putExtra("Currency", curr)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(this, WidgetID, intent, 0)

        val mNotific = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val name = "CryptoWatcher Alert"
        val desc = "${curr.toUpperCase()} has hit a $Alert of ${formatterLarge.format(Change.toFloat())}"
        val imp = NotificationManager.IMPORTANCE_HIGH
        val ChannelID = "my_channel_01"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mChannel = NotificationChannel(ChannelID, name,
                    imp)
            mChannel.description = desc
            mChannel.lightColor = Color.GREEN
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
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setLights(0xccccff, 1000,1000)
                .build()
        wakeScreen()
        Toast.makeText(this, "Recreate Widget to set new notification", Toast.LENGTH_SHORT).show()
        mNotific.notify(ncode, n)
    }

    private fun wakeScreen()
    {
        val pm = this.getSystemService(Context.POWER_SERVICE) as PowerManager
        val isScreenOn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            pm.isInteractive
        } else {
            pm.isScreenOn
        }
        if (!isScreenOn) {
            val wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE, "MyLock")
            wl.acquire(10000)
            val wl_cpu = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyCpuLock")

            wl_cpu.acquire(10000)
        }
    }

    companion object {
        // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
        val UPDATE = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        private val ACTION_BAZ = "com.andyisdope.cryptowatcher.Services.action.BAZ"

        val WIDGETID = "com.andyisdope.cryptowatcher.Services.extra.WidgetID"
        val WIDGETTEXT = "com.andyisdope.cryptowatcher.Services.extra.WidgetText"
        val WIDGETHIGH = "com.andyisdope.cryptowatcher.Services.extra.WidgetHigh"
        val WIDGETBOTTOM = "com.andyisdope.cryptowatcher.Services.extra.WidgetBottom"
        val WIDGETCHANGE = "com.andyisdope.cryptowatcher.Services.extra.WidgetChange"


        /**
         * Starts this service to perform action Foo with the given parameters. If
         * the service is already performing a task this action will be queued.
         *
         * @see IntentService
         */
        fun startActionFoo(context: Context, param1: Int, param2: String) {
            val intent = Intent(context, CurrencyService::class.java)
            intent.action = UPDATE
            var extras = param2.split(",")
            intent.putExtra(WIDGETID, param1)
            intent.putExtra(WIDGETTEXT, extras[0])
            //Log.i("Here", extras.toString())
            intent.putExtra(WIDGETHIGH, extras[1])
            intent.putExtra(WIDGETBOTTOM, extras[2])
            intent.putExtra(WIDGETCHANGE, extras[3])
            context.startService(intent)
        }

        fun parseCurrencyData(curr: String): String {
            val webService = DataWebService.retrofit.create(DataWebService::class.java)
            val call = webService.getInitial("https://coinmarketcap.com/currencies/$curr/")
            val resp = call.execute().body()

            //Log.i("Here", resp)
            var price = resp?.substringAfter("<span class=\"price\" data-usd=\"")
            price = price?.substring(0, price.indexOf("\""))

            var change = resp?.substringAfter("(<span data-format-percentage data-format-value=\"")
            change = change?.substring(0, change.indexOf("\""))

            return price + "," + change
        }
    }
}
