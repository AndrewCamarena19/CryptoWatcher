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
import android.widget.*
import com.andyisdope.cryptowatcher.Services.CurrencyService


/**
 * The configuration screen for the [CurrencyWidget] AppWidget.
 */
class CurrencyWidgetConfigureActivity : Activity() {
    private var mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var mAppWidgetText: Spinner
    private lateinit var HighPrice: EditText
    private lateinit var BottomPrice: EditText
    private lateinit var AbsoluteChange: EditText
    private lateinit var HighPriceBox: CheckBox
    private lateinit var BottomPriceBox: CheckBox
    private lateinit var AbsoluteChangeBox: CheckBox
    private var selected: String = ""
    private lateinit var sharedPref: SharedPreferences

    private var spinnerOnclick = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(p0: AdapterView<*>?) {
        }

        override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
            selected = mAppWidgetText.getItemAtPosition(p2).toString()
            //CurrencyService.startActionFoo(context,appWidgetId, widgetText)

            Toast.makeText(baseContext, selected, Toast.LENGTH_SHORT).show()

        }
    }

    private fun initUI()
    {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        HighPrice = findViewById<EditText>(R.id.WidgetHighSet) as EditText
        BottomPrice = findViewById<EditText>(R.id.WidgetBottomSet) as EditText
        AbsoluteChange = findViewById<EditText>(R.id.WidgetAbsoluteSet) as EditText
        mAppWidgetText = findViewById<Spinner>(R.id.appwidget_text) as Spinner
        findViewById<View>(R.id.add_button).setOnClickListener(mOnClickListener)

        HighPriceBox = findViewById<CheckBox>(R.id.WidgetHighPrice) as CheckBox
        HighPriceBox.setOnCheckedChangeListener { compoundButton, b ->
            when(b)
            {
                true -> HighPrice.visibility = View.VISIBLE
                false -> HighPrice.visibility = View.GONE
            }
        }
        BottomPriceBox = findViewById<CheckBox>(R.id.WidgetBottomPrice) as CheckBox
        BottomPriceBox.setOnCheckedChangeListener { compoundButton, b ->
            when(b)
            {
                true -> BottomPrice.visibility = View.VISIBLE
                false -> BottomPrice.visibility = View.GONE
            }
        }
        AbsoluteChangeBox = findViewById<CheckBox>(R.id.WidgetAbsoluteChange) as CheckBox
        AbsoluteChangeBox.setOnCheckedChangeListener { compoundButton, b ->
            when(b)
            {
                true -> AbsoluteChange.visibility = View.VISIBLE
                false -> AbsoluteChange.visibility = View.GONE
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

        //saveTitlePref(context, mAppWidgetId, CurrencyName)

        // It is the responsibility of the configuration activity to update the app widget
        //val appWidgetManager = AppWidgetManager.getInstance(context)
        //Log.i("Here", "in config buton")
        var HP = -999.0f
        var BP = -999.0f
        var CP = -999.0f
        if(HighPriceBox.isChecked)
        {
            HP = HighPrice.text.toString().toFloat()
        }
        if(BottomPriceBox.isChecked)
        {
            BP = BottomPrice.text.toString().toFloat()
        }
        if(AbsoluteChangeBox.isChecked)
        {
            CP = AbsoluteChange.text.toString().toFloat()
        }

        CurrencyService.startActionFoo(context, mAppWidgetId, "$CurrencyName,$HP,$BP,$CP")


        // Make sure we pass back the original appWidgetId
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

