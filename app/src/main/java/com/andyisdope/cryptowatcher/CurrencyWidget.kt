package com.andyisdope.cryptowatcher

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import com.andyisdope.cryptowatcher.Services.CurrencyService



/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in [CurrencyWidgetConfigureActivity]
 */
class CurrencyWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // When the user deletes the widget, delete the preference associated with it.
        for (appWidgetId in appWidgetIds) {
            CurrencyWidgetConfigureActivity.deleteTitlePref(context, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        var widgetID =  intent.getIntExtra("ID", 0)
        if (widgetID != 0) {
            updateAppWidget(context, AppWidgetManager.getInstance(context), widgetID)
        }
    }

    companion object {


        internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager,
                                     appWidgetId: Int) {

            val widgetText = CurrencyWidgetConfigureActivity.loadTitlePref(context, appWidgetId).split(",")

            if (widgetText[0] != "Not Set") {
                CurrencyService.enqueueWork(context, appWidgetId, widgetText[0]+","+widgetText[3]+","+widgetText[4]+","+widgetText[5])
            }
            // Instruct the widget manager to update the widget
        }
    }
}

