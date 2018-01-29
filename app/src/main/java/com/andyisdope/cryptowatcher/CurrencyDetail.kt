package com.andyisdope.cryptowatcher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.Paint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import android.view.MotionEvent
import android.widget.*
import com.andyisdope.cryptowatcher.Services.DataService
import com.andyisdope.cryptowatcher.model.CurrencyDetails
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.CandleStickChart
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.charts.CombinedChart.DrawOrder
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class CurrencyDetail : AppCompatActivity() {

    private var Signal: ArrayList<Float> = ArrayList()
    private var EMA12: ArrayList<Float> = ArrayList()
    private var EMA26: ArrayList<Float> = ArrayList()
    private var MACD: ArrayList<Float> = ArrayList()
    private var Histo: ArrayList<Float> = ArrayList()
    private var mCombined: CombinedChart? = null
    private var mBar: BarChart? = null
    private var mCandle: CandleStickChart? = null
    private var response: String? = ""
    private var CurrencyDeets: ArrayList<CurrencyDetails>? = ArrayList()
    private var entry: TextView? = null
    private var time: TextView? = null
    private var xValues: ArrayList<String>? = ArrayList()
    var formatterLarge: NumberFormat = DecimalFormat("#,###.00")
    private var sigData: LineDataSet? = null

    private val mBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            //val dataItems = intent
            response = intent.getStringExtra(DataService.MY_SERVICE_PAYLOAD)// as Array<Currency>
            Toast.makeText(baseContext,
                    "Candlestick\n" +
                            "24Hr Volume\n" +
                            "MACD w/ Red Signal",
                    Toast.LENGTH_LONG).show()
            parseCoinDetails()

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_currency_detail)
        var c: Calendar = Calendar.getInstance()
        var df: SimpleDateFormat = SimpleDateFormat("yyyyMMdd")
        var today: Int = df.format(c.time).toInt()
        var start: Int = today - 10000
        var curr: String = intent.getStringExtra("Currency")
        title = "${curr.toUpperCase()}"
        entry = findViewById<TextView>(R.id.CurrencyPrice) as TextView
        time = findViewById<TextView>(R.id.PriceTime) as TextView
        initTabs()

        LocalBroadcastManager.getInstance(applicationContext)
                .registerReceiver(mBroadcastReceiver,
                        IntentFilter(DataService.Currency))

        requestData("https://coinmarketcap.com/currencies/${curr.toLowerCase()}/historical-data/?start=$start&end=$today")

    }

    private fun initAllCharts(count: Int) {
        initBar(count)
        initCandle(count)
        initCombined(count)

    }

    fun initTabs() {
        val tabs = findViewById<TabHost>(R.id.DetailTab)
        tabs.setup()
        var spec: TabHost.TabSpec = tabs.newTabSpec("Charts")
        spec.setContent(R.id.Charts)
        spec.setIndicator("Charts")
        tabs.addTab(spec)
        spec = tabs.newTabSpec("Exchanges")
        spec.setContent(R.id.Exchanges)
        spec.setIndicator("Exchanges")
        tabs.addTab(spec)
        spec = tabs.newTabSpec("Alerts")
        spec.setContent(R.id.Alerts)
        spec.setIndicator("Alerts")
        tabs.addTab(spec)
    }

    fun parseUnix(time: Long): String {
        val date = Date(time)
        val sdf = SimpleDateFormat("MM/dd/yy")
        return sdf.format(date)
    }

    private fun initCombined(count: Int) {
        mCombined = findViewById<CombinedChart>(R.id.MACD) as CombinedChart
        mCombined!!.drawOrder = arrayOf(DrawOrder.BAR, DrawOrder.LINE)!!
        val keylistener = object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                entry!!.text = "MACD: ${formatterLarge.format(MACD!![e!!.x.toInt()].toDouble())}," +
                        "S: ${formatterLarge.format(Signal!![e.x.toInt()])}," +
                        "H: ${formatterLarge.format(Histo!![e.x.toInt()])}"

                time!!.text = "${CurrencyDeets!![e!!.x.toInt() + (CurrencyDeets!!.size - 1 - count)].Date}"
            }

            override fun onNothingSelected() {
                entry!!.text = ""
                time!!.text = ""
            }

        }

        mCombined!!.setOnChartValueSelectedListener(keylistener)
        initCombinedChart(count)
    }

    private fun initCombinedChart(count: Int) {
        //mCandle!!.setMaxVisibleValueCount(365)

        // scaling can now only be done on x- and y-axis separately
        mCombined!!.setPinchZoom(false)
        mCombined!!.setDrawGridBackground(false)

        var xAxis: XAxis = mCombined!!.xAxis
        xAxis.isEnabled = true
        xAxis.textColor = Color.WHITE
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 7f // only intervals of 1 day
        xAxis.setValueFormatter({ value, axis ->
            xValues!![(value % xValues!!.size).toInt()]
        })


        var leftAxis: YAxis = mCombined!!.axisRight
//        leftAxis.setEnabled(false);
        leftAxis.setLabelCount(7, false)
        leftAxis.setDrawGridLines(true)
        leftAxis.setDrawAxisLine(true)
        leftAxis.textColor = Color.WHITE

        var rightAxis: YAxis = mCombined!!.axisLeft
        rightAxis.isEnabled = false
//        rightAxis.setStartAtZero(false);

        // setting data
        //mSeekBarX.setProgress(40)
        //mSeekBarY.setProgress(100)

        mCombined!!.legend.isEnabled = false
        mCombined!!.description.isEnabled = false
        setCombinedData(count)
        mCombined!!.setVisibleXRangeMaximum(20f)

    }

    private fun setCombinedData(count: Int) {
        var data = CombinedData()

        generateEMA(count, 12, EMA12)
        generateEMA(count, 26, EMA26)


        data.setData(generateMacD(count))
        data.setData(generateHistogram(count))
        mCombined!!.xAxis.axisMaximum = data.xMax
        mCombined!!.data = data
        mCombined!!.invalidate()
    }

    private fun generateEMA(count: Int, EMA: Int, toAdd: ArrayList<Float>) {
        //{Close - EMA(previous day)} x multiplier + EMA(previous day)
        var size = CurrencyDeets!!.size - 1
        var e = 1
        var sum = 0.0f
        while (e <= EMA) {
            sum += CurrencyDeets!![e + (size - count - EMA)].Close.toFloat()
            e++
        }
        var mult = 2 / (EMA + 1).toFloat()
        var i = 0
        toAdd.add(i, sum / EMA)
        while (i <= count) {
            toAdd.add(i + 1, (CurrencyDeets!![i + (size - count)]!!.Close.toFloat() - toAdd[i]) * mult + toAdd[i])
            //Log.i("EMA", "${(CurrencyDeets!![i + (size - count)]!!.Date)}: ${toAdd[i]}")
            i++
        }

    }

    private fun generateHistogram(count: Int): BarData {
        val yVals1 = ArrayList<BarEntry>()
        var i = 0
        while (i <= count) {
            val value = MACD[i] - Signal[i]
            Histo.add(value)
            yVals1.add(BarEntry(i.toFloat(), value))
            i++
        }

        val set1: BarDataSet
        set1 = BarDataSet(yVals1, null)

        set1.setDrawIcons(false)
        set1.valueTextSize = 0f
        set1.setColors(Color.GRAY)
        return BarData(set1)


    }

    private fun generateSignalData(count: Int, EMA: Int, toAdd: ArrayList<Float>) {
        //{Close - EMA(previous day)} x multiplier + EMA(previous day)
        var size = MACD!!.size - 1
        var e = 1
        var sum = 0.0f
        while (e <= EMA) {
            sum += MACD!![e]
            e++
        }
        var mult = 2 / (EMA + 1).toFloat()
        var i = 0
        toAdd.add(i, sum / EMA)
        while (i <= count) {
            toAdd.add(i + 1, (MACD!![i + (size - count)]!! - toAdd[i]) * mult + toAdd[i])
            i++
        }
    }

    private fun generateMacD(count: Int): LineData {
        //EMA12[i] - EMA26[i]
        var d = LineData()

        var entries: ArrayList<Entry> = ArrayList()


        var i = 0
        while (i <= count) {
            MACD.add(EMA12[i] - EMA26[i])
            entries.add(Entry(i.toFloat(), (EMA12[i] - EMA26[i])))
            Log.i("MACD ", "${EMA12[i]} - ${EMA26[i]} = ${EMA12[i] - EMA26[i]}")
            i++
        }

        var set: LineDataSet = LineDataSet(entries, "Line DataSet")
        set.setColor(Color.BLUE)
        set.setLineWidth(.5f)
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER)
        set.valueTextSize = 0f
        set.setAxisDependency(YAxis.AxisDependency.LEFT)
        generateSignalData(count, 9, Signal)
        generateSignalLine(count)
        d.addDataSet(set)
        d.addDataSet(sigData)

        return d

    }

    private fun generateSignalLine(count: Int) {
        //EMA12[i] - EMA26[i]
        var d = LineData()

        var entries: ArrayList<Entry> = ArrayList()


        var i = 0
        while (i <= count) {
            entries.add(Entry(i.toFloat(), Signal[i]))
            i++
        }

        sigData = LineDataSet(entries, "Line")
        sigData!!.setColor(Color.RED)
        sigData!!.setLineWidth(.5f)
        sigData!!.setMode(LineDataSet.Mode.CUBIC_BEZIER)
        sigData!!.valueTextSize = 0f
        sigData!!.setAxisDependency(YAxis.AxisDependency.LEFT)

    }

    private fun initCandle(count: Int) {
        mCandle = findViewById<CandleStickChart>(R.id.CurrencyChart) as CandleStickChart
        val keylistener = object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                entry!!.text = "O:${formatterLarge.format(CurrencyDeets!![e!!.x.toInt() + (CurrencyDeets!!.size - 1 - count)].Open.toDouble())}," +
                        " C:${formatterLarge.format(CurrencyDeets!![e.x.toInt() + (CurrencyDeets!!.size - 1 - count)].Close.toDouble())}" +
                        ", L:${formatterLarge.format(CurrencyDeets!![e.x.toInt() + (CurrencyDeets!!.size - 1 - count)].Low.toDouble())}"

                time!!.text = "${CurrencyDeets!![e.x.toInt() + (CurrencyDeets!!.size - 1 - count)].Date}"
            }

            override fun onNothingSelected() {
                entry!!.text = ""
                time!!.text = ""
            }

        }

        mCandle!!.setOnChartValueSelectedListener(keylistener)
        initCandleChart(count)

    }

    fun initCandleChart(count: Int) {
        //mCandle!!.setMaxVisibleValueCount(365)

        // scaling can now only be done on x- and y-axis separately
        mCandle!!.setPinchZoom(false)
        mCandle!!.setDrawGridBackground(false)

        var xAxis: XAxis = mCandle!!.xAxis
        xAxis.isEnabled = true
        xAxis.textColor = Color.WHITE
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 7f // only intervals of 1 day
        xAxis.setValueFormatter({ value, axis ->
            xValues!![(value % xValues!!.size).toInt()]
        })


        var leftAxis: YAxis = mCandle!!.axisRight
//        leftAxis.setEnabled(false);
        leftAxis.setLabelCount(7, false)
        leftAxis.setDrawGridLines(true)
        leftAxis.setDrawAxisLine(true)
        leftAxis.textColor = Color.WHITE

        var rightAxis: YAxis = mCandle!!.axisLeft
        rightAxis.isEnabled = false
//        rightAxis.setStartAtZero(false);

        // setting data
        //mSeekBarX.setProgress(40)
        //mSeekBarY.setProgress(100)

        mCandle!!.legend.isEnabled = false
        mCandle!!.description.isEnabled = false
        setCandleData(count)
        mCandle!!.setVisibleXRangeMaximum(20f)

    }

    private fun setCandleData(count: Int) {

        //var prog = (mSeekBarX.getProgress() + 1)

        //entry!!.text = "" +
        //time!!.text = "" + (mSeekBarY.getProgress())

        mCandle!!.resetTracking()
        var size: Int = CurrencyDeets!!.size - 1
        var yVals1: ArrayList<CandleEntry> = ArrayList()
        var i = 0
        while (i <= count) {
            yVals1.add(CandleEntry(
                    i.toFloat(), CurrencyDeets!![i + (size - count)].High.toFloat(),
                    CurrencyDeets!![i + (size - count)].Low.toFloat(),
                    CurrencyDeets!![i + (size - count)].Open.toFloat(),
                    CurrencyDeets!![i + (size - count)].Close.toFloat()
            ))
            i++
        }


        var set1 = CandleDataSet(yVals1, "Data Set")

        set1.setDrawIcons(false)
        set1.axisDependency = YAxis.AxisDependency.LEFT
//        set1.setColor(Color.rgb(80, 80, 80));
        set1.shadowColor = Color.YELLOW
        set1.valueTextColor = Color.WHITE
        set1.shadowWidth = 0.7f
        set1.decreasingColor = Color.RED
        set1.decreasingPaintStyle = Paint.Style.FILL
        set1.increasingColor = Color.GREEN
        set1.increasingPaintStyle = Paint.Style.STROKE
        set1.neutralColor = Color.BLUE
        //set1.setHighlightLineWidth(1f)

        var data = CandleData(set1)

        mCandle!!.data = data
        mCandle!!.invalidate()
    }

    private fun initBar(count: Int) {
        mBar = findViewById<BarChart>(R.id.VolumeChart) as BarChart
        val keylistener = object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                entry!!.text = "Volume: ${formatterLarge.format(e!!.y)} M"
                time!!.text = "${CurrencyDeets!![e.x.toInt() + (CurrencyDeets!!.size - 1 - count)].Date}"
            }

            override fun onNothingSelected() {
                entry!!.text = ""
                time!!.text = ""
            }

        }
        mBar!!.setOnChartValueSelectedListener(keylistener)
        initBarChart(count)
    }

    fun initBarChart(count: Int) {
        var i = 0
        var size = CurrencyDeets!!.size - 1
        while (i <= count) {
            val value = CurrencyDeets!![i + (size - count)].Date
            xValues!!.add(value)
            i++
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
        xAxis.setDrawGridLines(true)
        mBar!!.xAxis.setDrawLabels(true)
        mBar!!.xAxis.setDrawAxisLine(true)
        xAxis.granularity = 7f // only intervals of 1 day
        xAxis.labelCount = 7
        xAxis.setValueFormatter({ value, axis ->
            xValues!![(value % xValues!!.size).toInt()]
        })
        xAxis.textColor = Color.WHITE
        xAxis.isEnabled = true


        //var leftAxis: YAxis = mBar!!.axisLeft
        //leftAxis.setLabelCount(8, false)
        //leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
        //leftAxis.spaceTop = 15f
        //leftAxis.axisMinimum = 0f // this replaces setStartAtZero(true)
//        var leftAxis: YAxis = mBar!!.axisLeft
//        leftAxis.setDrawGridLines(false)
//        leftAxis.setLabelCount(7, false)
//        leftAxis.spaceTop = 0f
//        leftAxis.axisMinimum = 0f // this replaces setStartAtZero(true)
//        leftAxis.textColor = Color.WHITE

        var rightAxis: YAxis = mBar!!.axisRight
        rightAxis.setDrawGridLines(true)
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


        setBarData(count)

        // setting data
        //mSeekBarY.setProgress(50)
        //mSeekBarX.setProgress(12)

        //mSeekBarY.setOnSeekBarChangeListener(this)
        //mSeekBarX.setOnSeekBarChangeListener(this)

        mBar!!.legend.isEnabled = false
        mBar!!.axisLeft.isEnabled = false
        mBar!!.setVisibleXRangeMaximum(20f)


    }

    private fun setBarData(count: Int) {

        var size: Int = CurrencyDeets!!.size - 1
        val yVals1 = ArrayList<BarEntry>()
        var i = 0
        while (i <= count) {
            val value = CurrencyDeets!![i + (size - count)].Volume.toDouble() / 1000000
            yVals1.add(BarEntry(i.toFloat(), value.toFloat()))
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
            data.barWidth = 0.7f

            mBar!!.data = data
        }
        mBar!!.invalidate()


    }

    private fun parseCoinDetails() {
        var blocks = response!!.substringAfter("<tbody>").split("</tr>")
        blocks.take(blocks.size - 1)
                .forEach {
                    CurrencyDeets!!.add(createCurrencyDetail(it))
                }
        CurrencyDeets!!.sortBy { it.Date }
        CurrencyDeets!!.forEach {
            it.Date = (parseUnix(it.Date.toLong()))
        }
        initAllCharts(45)
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

        return CurrencyDetails(unixTime.toString(), high, open, low, close, volume)


    }

    private fun requestData(path: String) {
        val intent = Intent(this, DataService::class.java)
        intent.putExtra("Path", path)
        startService(intent)
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
