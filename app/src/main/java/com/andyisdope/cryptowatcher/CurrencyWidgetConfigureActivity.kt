package com.andyisdope.cryptowatcher

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.*
import com.andyisdope.cryptowatcher.Services.CurrencyService


/**
 * The configuration screen for the [CurrencyWidget] AppWidget.
 */
class CurrencyWidgetConfigureActivity : Activity() {
    private var mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var mAppWidgetText: Spinner
    private lateinit var highPrice: EditText
    private lateinit var bottomPrice: EditText
    private lateinit var absoluteChange: EditText
    private lateinit var highPriceBox: CheckBox
    private lateinit var bottomPriceBox: CheckBox
    private lateinit var absoluteChangeBox: CheckBox
    private var selected: String = ""
    private lateinit var sharedPref: SharedPreferences

    //Spinner to select currency widget
    private var spinnerOnclick = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(p0: AdapterView<*>?) {
        }

        override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
            selected = mAppWidgetText.getItemAtPosition(p2).toString()
            Toast.makeText(baseContext, selected, Toast.LENGTH_SHORT).show()

        }
    }

    //Configuration Screen for widget
    private fun initUI() {
        sharedPref = baseContext.getSharedPreferences("Favorites", Context.MODE_PRIVATE)

        //If a price passes a high, bottom or absolute change in either direction notify user if set
        highPrice = findViewById(R.id.WidgetHighSet)
        bottomPrice = findViewById(R.id.WidgetBottomSet)
        absoluteChange = findViewById(R.id.WidgetAbsoluteSet)
        mAppWidgetText = findViewById(R.id.appwidget_text)
        findViewById<View>(R.id.add_button).setOnClickListener(mOnClickListener)

        highPriceBox = findViewById(R.id.WidgetHighPrice)
        highPriceBox.setOnCheckedChangeListener { _, b ->
            when (b) {
                true -> highPrice.visibility = View.VISIBLE
                false -> highPrice.visibility = View.GONE
            }
        }
        bottomPriceBox = findViewById(R.id.WidgetBottomPrice)
        bottomPriceBox.setOnCheckedChangeListener { _, b ->
            when (b) {
                true -> bottomPrice.visibility = View.VISIBLE
                false -> bottomPrice.visibility = View.GONE
            }
        }
        absoluteChangeBox = findViewById(R.id.WidgetAbsoluteChange)
        absoluteChangeBox.setOnCheckedChangeListener { _, b ->
            when (b) {
                true -> absoluteChange.visibility = View.VISIBLE
                false -> absoluteChange.visibility = View.GONE
            }
        }


        var mList = sharedPref.all.keys.toList()
        var adapters = ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, mList)

        mAppWidgetText.onItemSelectedListener = spinnerOnclick
        adapters.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mAppWidgetText.adapter = adapters

    }

    private var mOnClickListener: View.OnClickListener = View.OnClickListener {
        val context = this@CurrencyWidgetConfigureActivity

        // When the button is clicked, store the string locally
        val CurrencyName = selected


        // It is the responsibility of the configuration activity to update the app widget
        var HP = -999.0
        var BP = -999.0
        var CP = -999.0
        if (highPriceBox.isChecked) {
            HP = highPrice.text.toString().toDouble()
        }
        if (bottomPriceBox.isChecked) {
            BP = bottomPrice.text.toString().toDouble()
        }
        if (absoluteChangeBox.isChecked) {
            CP = absoluteChange.text.toString().toDouble()
        }
        //First update call issued manually
        CurrencyService.enqueueWork(context, mAppWidgetId, "$CurrencyName,$HP,$BP,$CP")


        // Make sure we pass back the original appWidgetId
        //Only a single notification will be issued, user must create new widget for new notification
        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId)
        setResult(Activity.RESULT_OK, resultValue)
        Toast.makeText(this, "Press Updated time to refresh \n" +
                "Each notification will only be used once", Toast.LENGTH_LONG).show()
        finish()
    }

    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        setContentView(R.layout.currency_widget_configure)

        Toast.makeText(this, "Add a currency to favorites to populate list", Toast.LENGTH_SHORT).show()
        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(Activity.RESULT_CANCELED)
        initUI()


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

