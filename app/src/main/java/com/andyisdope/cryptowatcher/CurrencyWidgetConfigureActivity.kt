package com.andyisdope.cryptowatcher

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import android.widget.ArrayAdapter
import com.andyisdope.cryptowatcher.Services.CurrencyService


/**
 * The configuration screen for the [CurrencyWidget] AppWidget.
 */
class CurrencyWidgetConfigureActivity : Activity() {
    private var mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var mAppWidgetText: Spinner
    private var selected: String = ""
    private lateinit var sharedPref: SharedPreferences

    private var spinnerOnclick = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(p0: AdapterView<*>?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
            selected = mAppWidgetText.getItemAtPosition(p2).toString()
            //CurrencyService.startActionFoo(context,appWidgetId, widgetText)

            Toast.makeText(baseContext, selected, Toast.LENGTH_SHORT).show()

        }
    }
    private var mOnClickListener: View.OnClickListener = View.OnClickListener {
        val context = this@CurrencyWidgetConfigureActivity

        // When the button is clicked, store the string locally
        val widgetText = selected
        saveTitlePref(context, mAppWidgetId, widgetText)

        // It is the responsibility of the configuration activity to update the app widget
        val appWidgetManager = AppWidgetManager.getInstance(context)
        Log.i("Here", "in config buton")
        CurrencyService.startActionFoo(context,mAppWidgetId, widgetText)


        // Make sure we pass back the original appWidgetId
        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId)
        setResult(Activity.RESULT_OK, resultValue)
        Toast.makeText(this, "Press Updated time to refresh", Toast.LENGTH_SHORT).show()
        finish()
    }

    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        Toast.makeText(this, "Add a currency to favorites to populate list", Toast.LENGTH_SHORT).show()
        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(Activity.RESULT_CANCELED)
        sharedPref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        var mList = sharedPref.all.keys.toList()
        //Log.i("Here", sharedPref.all.keys.toString())
        var adapters = ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, mList)
        setContentView(R.layout.currency_widget_configure)
        mAppWidgetText = findViewById<Spinner>(R.id.appwidget_text) as Spinner
        findViewById<View>(R.id.add_button).setOnClickListener(mOnClickListener)
        mAppWidgetText.onItemSelectedListener = spinnerOnclick
        adapters.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mAppWidgetText.adapter = adapters



        // Find the widget id from the intent.
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }


        //mAppWidgetText.setText(loadTitlePref(this@CurrencyWidgetConfigureActivity, mAppWidgetId))
    }

    companion object {

        private val PREFS_NAME = "com.andyisdope.cryptowatcher.CurrencyWidget"
        private val PREF_PREFIX_KEY = "appwidget_"

        // Write the prefix to the SharedPreferences object for this widget
        internal fun saveTitlePref(context: Context, appWidgetId: Int, text: String) {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
            prefs.putString(PREF_PREFIX_KEY + appWidgetId, text)
            prefs.apply()
        }

        // Read the prefix from the SharedPreferences object for this widget.
        // If there is no preference saved, get the default from a resource
        internal fun loadTitlePref(context: Context, appWidgetId: Int): String {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0)
            val titleValue = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null)
            return titleValue ?: "Not Set"
        }

        internal fun deleteTitlePref(context: Context, appWidgetId: Int) {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
            prefs.remove(PREF_PREFIX_KEY + appWidgetId)
            prefs.apply()
        }
    }
}

