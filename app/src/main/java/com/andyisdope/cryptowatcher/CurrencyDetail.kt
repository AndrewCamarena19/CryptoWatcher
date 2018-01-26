package com.andyisdope.cryptowatcher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import com.andyisdope.cryptowatcher.Services.DataService
import com.andyisdope.cryptowatcher.model.CurrencyDetails
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.CandleStickChart
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.charts.CombinedChart.DrawOrder
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class CurrencyDetail : AppCompatActivity() {

    private var mCombined: CombinedChart? = null
    private var mBar: BarChart? = null
    private var mCandle: CandleStickChart? = null
    private var response: String? = ""
    private var CurrencyDeets: ArrayList<CurrencyDetails>? = ArrayList()

    private val mBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            //val dataItems = intent
            response = intent.getStringExtra(DataService.MY_SERVICE_PAYLOAD)// as Array<Currency>
            Toast.makeText(baseContext,
                    "Received Coin Details",
                    Toast.LENGTH_SHORT).show()
            parseCoinDetails()

        }
    }

    private fun parseCoinDetails() {
        var blocks = response!!.substringAfter("<tbody>").split("</tr>")
        blocks.take(blocks.size-1)
                .forEach{
                    CurrencyDeets!!.add(createCurrencyDetail(it))
                }
        initBarChart()
    }

    private fun createCurrencyDetail(block: String): CurrencyDetails {
        var data = block.substringAfter("<tr class=\"text-right\">").split("</td>")
        //data index: date,open,high,low,close,volume,marketcap

        var date = data[0].substringAfter(">").replace("</td>", "")
        var dateFormat: DateFormat = SimpleDateFormat("MMM dd, yyyy")
        var dateParsed: Date = dateFormat!!.parse(date)
        var unixTime: Long = dateParsed.time

        var open = data[1].substringAfter("<td data-format-fiat data-format-value=\"")
        open = open.substring(0, open.indexOf("\""))

        var high = data[2].substringAfter("<td data-format-fiat data-format-value=\"")
        high = high.substring(0, high.indexOf("\""))

        var low = data[3].substringAfter("<td data-format-fiat data-format-value=\"")
        low = low.substring(0, low.indexOf("\""))

        var close = data[4].substringAfter("<td data-format-fiat data-format-value=\"")
        close = close.substring(0, close.indexOf("\""))

        var volume = data[5].substringAfter("<td data-format-market-cap data-format-value=\"")
        volume = volume.substring(0, volume.indexOf("\""))

        //Log.i("Entry", volume)

        return CurrencyDetails(unixTime.toString(), open, high, low, close, volume)


    }
    private fun requestData(path: String) {
        val intent = Intent(this, DataService::class.java)
        intent.putExtra("Path", path)
        startService(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_currency_detail)
        var c: Calendar = Calendar.getInstance()

        var df: SimpleDateFormat = SimpleDateFormat("yyyyMMdd")
        var today: Int = df.format(c.time).toInt()
        var start: Int = today - 10000

        mCandle = findViewById<CandleStickChart>(R.id.CurrencyChart) as CandleStickChart
        mBar = findViewById<BarChart>(R.id.VolumeChart) as BarChart
        mCombined = findViewById<CombinedChart>(R.id.MACD) as CombinedChart
        mCombined!!.drawOrder = arrayOf(DrawOrder.BAR, DrawOrder.LINE)!!

        LocalBroadcastManager.getInstance(applicationContext)
                .registerReceiver(mBroadcastReceiver,
                        IntentFilter(DataService.Currency))

        requestData("https://coinmarketcap.com/currencies/bitcoin/historical-data/?start=$start&end=$today")

    }

    fun parseUnix(time: Long): String {
        val date = Date(time)
        val sdf = SimpleDateFormat("MM/dd/yyyy")
        return sdf.format(date)
    }

    fun initBarChart() {

        var xValues: ArrayList<String>? = ArrayList()

        CurrencyDeets!!.forEach {xValues!!.add(parseUnix(it.Date.toLong()))
        }
        //xValues!!.add(parseUnix(it.Date.toLong()))

        mBar!!.setDrawBarShadow(false)
        mBar!!.setDrawValueAboveBar(false)
        mBar!!.description.isEnabled = false

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        mBar!!.setMaxVisibleValueCount(365)

        // scaling can now only be done on x- and y-axis separately
        mBar!!.setPinchZoom(false)

        mBar!!.setDrawGridBackground(false)
        // mBar!!setDrawYLabels(false)

        var xAxis: XAxis = mBar!!.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 7f // only intervals of 1 day
        xAxis.labelCount = 7
        xAxis.setValueFormatter({ value, axis ->
            xValues!![(value % xValues.size).toInt()]
        })
        xAxis.textColor = Color.WHITE


        //var leftAxis: YAxis = mBar!!.axisLeft
        //leftAxis.setLabelCount(8, false)
        //leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
        //leftAxis.spaceTop = 15f
        //leftAxis.axisMinimum = 0f // this replaces setStartAtZero(true)

        var rightAxis: YAxis = mBar!!.axisRight
        rightAxis.setDrawGridLines(false)
        rightAxis.setLabelCount(7, false)
        rightAxis.spaceTop = 0f
        rightAxis.axisMinimum = 0f // this replaces setStartAtZero(true)
        rightAxis.textColor = Color.WHITE

//        var l: Legend = mBar!!.legend
//        l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
//        l.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
//        l.orientation = Legend.LegendOrientation.HORIZONTAL
//        l.setDrawInside(false)
//        l.form = Legend.LegendForm.SQUARE
//        l.formSize = 9f
//        l.textSize = 11f
//        l.xEntrySpace = 4f
        // l.setExtra(ColorTemplate.VORDIPLOM_COLORS, new String[] { "abc",
        // "def", "ghj", "ikl", "mno" })
        // l.setCustom(ColorTemplate.VORDIPLOM_COLORS, new String[] { "abc",
        // "def", "ghj", "ikl", "mno" })


        setData(CurrencyDeets!!.size)

        // setting data
        //mSeekBarY.setProgress(50)
        //mSeekBarX.setProgress(12)

        //mSeekBarY.setOnSeekBarChangeListener(this)
        //mSeekBarX.setOnSeekBarChangeListener(this)

        mBar!!.legend.isEnabled = false
        mBar!!.axisLeft.isEnabled = false
        mBar!!.setVisibleXRangeMaximum(20f)



    }

    private fun setData(count: Int) {


        val yVals1 = ArrayList<BarEntry>()
        var i = 0
        while (i < count) {
            val value = CurrencyDeets!![i].Volume.toFloat()/1000000
            yVals1.add(BarEntry(i.toFloat(), value))
            i++
        }

        val set1: BarDataSet

        if (mBar!!.data != null && mBar!!.data.dataSetCount > 0) {
            set1 = (mBar!!.data.getDataSetByIndex(0)) as BarDataSet
            set1.values = yVals1
            mBar!!.data.notifyDataChanged()
            mBar!!.notifyDataSetChanged()
        } else {
            set1 = BarDataSet(yVals1, null)

            set1.setDrawIcons(false)

            set1.setColors(Color.GRAY)

            val dataSets = ArrayList<IBarDataSet>()
            dataSets.add(set1)

            val data = BarData(dataSets)
            data.setValueTextSize(0f)
            data.barWidth = 0.9f

            mBar!!.data = data
        }
        mBar!!.invalidate()


    }

    override fun onPause() {
        super.onPause()
        //mDataSource.close()
    }

    override fun onResume() {
        super.onResume()
        //mDataSource.open()
    }

    override fun onDestroy() {
        super.onDestroy()

        LocalBroadcastManager.getInstance(applicationContext)
                .unregisterReceiver(mBroadcastReceiver)
    }
}
