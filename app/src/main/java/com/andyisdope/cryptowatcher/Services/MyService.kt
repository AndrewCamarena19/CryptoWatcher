package com.andyisdope.cryptowatcher.Services

import android.app.IntentService
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.IBinder
import android.app.PendingIntent
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.Log
import android.widget.RemoteViews
import com.andyisdope.cryptowatcher.NewAppWidget
import com.andyisdope.cryptowatcher.R
import java.util.*


class MyService : IntentService("WidgetService") {
    private lateinit var sharedPref: SharedPreferences
    private lateinit var Favorites: Map<String, Double>

    val webService = DataWebService.retrofit.create(DataWebService::class.java)


    override fun onHandleIntent(p0: Intent) {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(baseContext)
        Favorites = sharedPref.all as Map<String, Double>
        var WM: AppWidgetManager = AppWidgetManager.getInstance(this.applicationContext)
        val allWidgetIds = p0.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS)
        for (widgetId in allWidgetIds) {
            // create some random data
            val number = Random().nextInt(100)


            val remoteViews = RemoteViews(this.applicationContext.packageName, R.layout.new_app_widget)
            Log.i("WidgetExample", number.toString())
            // Set the text
            remoteViews.setTextViewText(R.id.WidgetCurr,
                    "ID: " + widgetId)

            // Register an onClickListener
            val clickIntent = Intent(this.applicationContext,
                    NewAppWidget::class.java)

            clickIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
                    allWidgetIds)

            val pendingIntent = PendingIntent.getBroadcast(
                    applicationContext, 0, clickIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT)
            remoteViews.setOnClickPendingIntent(R.id.WidgetCurr, pendingIntent)
            WM.updateAppWidget(widgetId, remoteViews)
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        // TODO: Return the communication channel to the service.
        throw UnsupportedOperationException("Not yet implemented")
    }
}
