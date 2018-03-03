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
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.*
import com.andyisdope.cryptowatcher.Adapters.MarketAdapter
import com.andyisdope.cryptowatcher.Services.DataService
import com.andyisdope.cryptowatcher.model.CurrencyDetails
import com.andyisdope.cryptowatcher.model.Market
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.CandleStickChart
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.charts.CombinedChart.DrawOrder
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
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
    private var DisplayMarkets: ArrayList<Market> = ArrayList()
    private var CurrencyDeets: ArrayList<CurrencyDetails> = ArrayList()
    private var MarketDeets: ArrayList<Market> = ArrayList()
    private var MarketPairs: TreeSet<String> = TreeSet()
    private var MarketNames: TreeSet<String> = TreeSet()
    private lateinit var MarketAdapt: MarketAdapter
    private lateinit var mMarketList: RecyclerView
    private lateinit var response: String
    private lateinit var marketResponse: String
    private lateinit var entry: TextView
    private lateinit var time: TextView
    private lateinit var MarketSpinner: Spinner
    private lateinit var PairSpinner: Spinner
    private lateinit var sigData: LineDataSet
    private lateinit var mCombined: CombinedChart
    private lateinit var mBar: BarChart
    private lateinit var mCandle: CandleStickChart
    private lateinit var radioGroup: RadioGroup
    private lateinit var USD: RadioButton
    private lateinit var BTC: RadioButton
    private lateinit var ETH: RadioButton
    private lateinit var curr: String
    private var marketLoaded: Boolean = false
    private var MarketIndex: Int = 0
    private var PairIndex: Int = 0
    private var xValues: ArrayList<String> = ArrayList()
    private val formatterLarge: NumberFormat = DecimalFormat("#,###.00")

    private val mBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            //val dataItems = intent
            response = intent.getStringExtra(DataService.MY_SERVICE_PAYLOAD)// as Array<Currency>
            parseCoinDetails()
        }
    }

    private val mBroadcastReceiver2 = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            //val dataItems = intent
            marketResponse = intent.getStringExtra(DataService.MY_SERVICE_PAYLOAD)// as Array<Currency>
            Toast.makeText(baseContext, "Select an Exchange or Pair to view", Toast.LENGTH_SHORT).show()
            parseMarketDetails()


        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_currency_detail)
        val c: Calendar = Calendar.getInstance()
        val df: SimpleDateFormat = SimpleDateFormat("yyyyMMdd")
        val today: Int = df.format(c.time).toInt()
        val start: Int = today - 10000
        curr = intent.getStringExtra("Currency")
        marketLoaded = false
        title = "${curr.toUpperCase()}"
        entry = findViewById<TextView>(R.id.CurrencyPrice) as TextView
        time = findViewById<TextView>(R.id.PriceTime) as TextView
        mMarketList = findViewById<RecyclerView>(R.id.MarketView) as RecyclerView
        mMarketList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)


        initSpinners()
        initTabs()
        initRadioGroup()

        LocalBroadcastManager.getInstance(applicationContext)
                .registerReceiver(mBroadcastReceiver,
                        IntentFilter(DataService.Currency))
        LocalBroadcastManager.getInstance(applicationContext)
                .registerReceiver(mBroadcastReceiver2,
                        IntentFilter(DataService.MARKET))

        requestData("https://coinmarketcap.com/currencies/${curr.toLowerCase()}/historical-data/?start=$start&end=$today")

    }

    private fun initRadioGroup() {
        MarketAdapter.CurrentCurrency = "USD"
        radioGroup = findViewById<RadioGroup>(R.id.CurrencyRadio) as RadioGroup
        USD = findViewById<RadioButton>(R.id.RadioUSD) as RadioButton
        BTC = findViewById<RadioButton>(R.id.RadioBTC) as RadioButton
        ETH = findViewById<RadioButton>(R.id.RadioETH) as RadioButton

        radioGroup.setOnCheckedChangeListener { radioGroup, i ->
            when (i) {
                R.id.RadioUSD -> {
                    MarketAdapter.CurrentCurrency = "USD"
                    MarketAdapt = MarketAdapter(baseContext, DisplayMarkets)
                    mMarketList.adapter = MarketAdapt
                    mMarketList.adapter.notifyDataSetChanged()
                }
                R.id.RadioBTC -> {
                    MarketAdapter.CurrentCurrency = "BTC"
                    MarketAdapt = MarketAdapter(baseContext, DisplayMarkets)
                    mMarketList.adapter = MarketAdapt
                    mMarketList.adapter.notifyDataSetChanged()
                }
                R.id.RadioETH -> {
                    MarketAdapter.CurrentCurrency = "ETH"
                    MarketAdapt = MarketAdapter(baseContext, DisplayMarkets)
                    mMarketList.adapter = MarketAdapt
                    mMarketList.adapter.notifyDataSetChanged()
                }
            }
        }
    }

    private fun initSpinners() {
        MarketSpinner = findViewById<Spinner>(R.id.MarketSpinner) as Spinner
        PairSpinner = findViewById<Spinner>(R.id.PairSpinner) as Spinner

        MarketSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                DisplayMarkets.clear()
                var mark = MarketSpinner.getItemAtPosition(p2).toString()
                MarketDeets.asSequence()
                        .forEach { if (it.market == mark) DisplayMarkets.add(it) }

                MarketAdapt = MarketAdapter(baseContext, DisplayMarkets)
                mMarketList.adapter = MarketAdapt
                mMarketList.adapter.notifyDataSetChanged()
            }
        }
        PairSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                DisplayMarkets.clear()
                var mark = PairSpinner.getItemAtPosition(p2).toString()
                MarketDeets.asSequence()
                        .forEach { if (it.pair == mark) DisplayMarkets.add(it) }

                MarketAdapt = MarketAdapter(baseContext, DisplayMarkets)
                mMarketList.adapter = MarketAdapt
                mMarketList.adapter.notifyDataSetChanged()
            }
        }
    }

    private fun initAllCharts(count: Int) {
        if(CurrencyDeets.size > 45) {
            initBar(count)
            initCandle(count)
            initCombined(count)
        }
        else {
            Toast.makeText(baseContext, "Currency has less than 45 days of data", Toast.LENGTH_SHORT).show()
            initBar(CurrencyDeets.size-1)
            initCandle(CurrencyDeets.size-1)
        }
    }

    fun initTabs() {
        val tabs = findViewById<TabHost>(R.id.DetailTab)
        tabs.setup()
        with(tabs)
        {
            var spec: TabHost.TabSpec = tabs.newTabSpec("Charts")
            spec.setContent(R.id.Charts)
            spec.setIndicator("Charts")
            addTab(spec)
            spec = tabs.newTabSpec("Exchanges")
            spec.setContent(R.id.Exchanges)
            spec.setIndicator("Exchanges")
            addTab(spec)
            spec = tabs.newTabSpec("Alerts")
            spec.setContent(R.id.Alerts)
            spec.setIndicator("Alerts")
            addTab(spec)
            setOnTabChangedListener { tabID ->
                when (tabID) {
                    "Charts" -> {
                    }
                    "Exchanges" -> {
                        requestMarketData()
                    }
                    "Alerts" -> {
                    }
                }
            }
        }
    }

    private fun requestMarketData() {
        if (!marketLoaded) {
            val intent = Intent(this, DataService::class.java)
            intent.putExtra("Path", "Market")
            intent.putExtra("Currency", "https://coinmarketcap.com/currencies/${curr.toLowerCase()}/#markets")
            startService(intent)
            marketLoaded = true
        } else
            Toast.makeText(baseContext, "Market Loaded", Toast.LENGTH_SHORT).show()
    }

    private fun parseMarketDetails() {
        var blocks = marketResponse.substringAfter("<tbody>").split("</tr>")
        blocks.take(blocks.size - 1)
                .forEach {
                    (createMarketDetail(it))
                }
        initMarketView()
    }

    private fun initMarketView() {
        MarketSpinner.adapter = ArrayAdapter<String>(
                this, R.layout.spinner_layout, MarketNames.toList()
        )
        PairSpinner.adapter = ArrayAdapter<String>(
                this, R.layout.spinner_layout, MarketPairs.toList()
        )
    }

    private fun createMarketDetail(block: String) {
        var data = block.split("</td>")
        //data index: date,open,high,low,close,volume,marketcap

        var marketName = data[1].substringAfter("\">").replace("</td>", "")
        marketName = marketName.substring(0, marketName.indexOf("<"))

        var pair = data[2].substringAfter("target=\"_blank\">")
        pair = pair.substring(0, pair.indexOf("</a"))

        var volUSD = data[3].substringAfter("data-usd=\"")
        volUSD = volUSD.substring(0, volUSD.indexOf("\""))

        var volBTC = data[3].substringAfter("data-btc=\"")
        volBTC = volBTC.substring(0, volBTC.indexOf("\""))

        var priceUSD = data[4].substringAfter("data-usd=\"")
        priceUSD = priceUSD.substring(0, priceUSD.indexOf("\""))

        var priceBTC = data[4].substringAfter("data-btc=\"")
        priceBTC = priceBTC.substring(0, priceBTC.indexOf("\""))

        var percent = data[5].substringAfter("<span data-format-percentage data-format-value=\"")
        percent = percent.substring(0, percent.indexOf("\""))

        var update = data[6].substringAfter(">")

        MarketDeets.add(Market(marketName, pair, volBTC.toFloat(), volUSD.toFloat(), priceBTC.toFloat(), priceUSD.toFloat(), percent, update))
        Log.i("Market", MarketDeets[MarketDeets.size - 1].toString())
        if (!MarketPairs.contains(pair)) MarketPairs.add(pair.capitalize())
        if (!MarketNames.contains(marketName)) MarketNames.add(marketName.capitalize())
    }

    //region Combined Chart
    private fun initCombined(count: Int) {
        mCombined = findViewById<CombinedChart>(R.id.MACD) as CombinedChart
        mCombined.drawOrder = arrayOf(DrawOrder.BAR, DrawOrder.LINE)

        val keylistener = object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry, h: Highlight) {
                entry.text = "MACD: ${formatterLarge.format(MACD[e.x.toInt()].toDouble())}," +
                        "S: ${formatterLarge.format(Signal[e.x.toInt()])}," +
                        "H: ${formatterLarge.format(Histo[e.x.toInt()])}"

                time.text = "${CurrencyDeets[e.x.toInt() + (CurrencyDeets.size - 1 - count)].Date}"
            }

            override fun onNothingSelected() {
                entry.text = ""
                time.text = ""
            }

        }

        mCombined.setOnChartValueSelectedListener(keylistener)
        initCombinedChart(count)
    }

    private fun initCombinedChart(count: Int) {
        mCombined.setPinchZoom(false)
        mCombined.setDrawGridBackground(false)

        var xAxis: XAxis = mCombined.xAxis
        xAxis.isEnabled = true
        xAxis.textColor = Color.WHITE
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 7f // only intervals of 1 day
        xAxis.setValueFormatter({ value, axis ->
            xValues[(value % xValues.size).toInt()]
        })


        var rightAxis: YAxis = mCombined.axisRight
        rightAxis.setLabelCount(7, false)
        rightAxis.setDrawGridLines(true)
        rightAxis.setDrawAxisLine(true)
        rightAxis.textColor = Color.WHITE

        var leftAxis: YAxis = mCombined.axisLeft
        leftAxis.isEnabled = false

        mCombined.legend.isEnabled = true
        mCombined.legend.textColor = Color.WHITE
        mCombined.description.isEnabled = true
        mCombined.description.textColor = Color.YELLOW
        mCombined.description.textSize = 12f
        mCombined.description.text = "12,26,9 MACD"
        mCombined.description.setPosition(850f, 50f)
        setCombinedData(count)
        mCombined.setVisibleXRangeMaximum(20f)

    }

    private fun setCombinedData(count: Int) {
        var data = CombinedData()

        generateEMA(count, 12, EMA12)
        generateEMA(count, 26, EMA26)


        data.setData(generateMacD(count))
        data.setData(generateHistogram(count))
        mCombined.xAxis.axisMaximum = data.xMax
        mCombined.data = data
        mCombined.invalidate()
    }

    private fun generateEMA(count: Int, EMA: Int, toAdd: ArrayList<Float>) {
        //{Close - EMA(previous day)} x multiplier + EMA(previous day)
        var size = CurrencyDeets.size - 1
        var e = 1
        var sum = 0.0f
        while (e <= EMA) {
            sum += CurrencyDeets[e + (size - count - EMA)].Close.toFloat()
            e++
        }
        var mult = 2 / (EMA + 1).toFloat()
        var i = 0
        toAdd.add(i, sum / EMA)
        while (i <= count) {
            toAdd.add(i + 1, (CurrencyDeets[i + (size - count)].Close.toFloat() - toAdd[i]) * mult + toAdd[i])
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
        set1 = BarDataSet(yVals1, "Histogram")
        set1.setDrawIcons(false)
        set1.valueTextSize = 0f
        set1.setColors(Color.GRAY)
        return BarData(set1)


    }

    private fun generateSignalData(count: Int, EMA: Int, toAdd: ArrayList<Float>) {
        //{Close - EMA(previous day)} x multiplier + EMA(previous day)
        var size = MACD.size - 1
        var e = 1
        var sum = 0.0f
        while (e <= EMA) {
            sum += MACD[e]
            e++
        }
        var mult = 2 / (EMA + 1).toFloat()
        var i = 0
        toAdd.add(i, sum / EMA)
        while (i <= count) {
            toAdd.add(i + 1, (MACD[i + (size - count)] - toAdd[i]) * mult + toAdd[i])
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
            //Log.i("MACD ", "${EMA12[i]} - ${EMA26[i]} = ${EMA12[i] - EMA26[i]}")
            i++
        }

        var set: LineDataSet = LineDataSet(entries, "MACD")
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

        sigData = LineDataSet(entries, "Signal Line")
        sigData.setColor(Color.RED)
        sigData.setLineWidth(.5f)
        sigData.setMode(LineDataSet.Mode.CUBIC_BEZIER)
        sigData.valueTextSize = 0f
        sigData.setAxisDependency(YAxis.AxisDependency.LEFT)

    }
    //endregion

    //region Candlestick Chart
    private fun initCandle(count: Int) {
        mCandle = findViewById<CandleStickChart>(R.id.CurrencyChart) as CandleStickChart
        val keylistener = object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry, h: Highlight) {
                entry.text = "O:${formatterLarge.format(CurrencyDeets[e.x.toInt() + (CurrencyDeets.size - 1 - count)].Open.toDouble())}," +
                        " C:${formatterLarge.format(CurrencyDeets[e.x.toInt() + (CurrencyDeets.size - 1 - count)].Close.toDouble())}" +
                        ", L:${formatterLarge.format(CurrencyDeets[e.x.toInt() + (CurrencyDeets.size - 1 - count)].Low.toDouble())}"

                time.text = "${CurrencyDeets[e.x.toInt() + (CurrencyDeets.size - 1 - count)].Date}"
            }

            override fun onNothingSelected() {
                entry.text = ""
                time.text = ""
            }

        }

        mCandle.setOnChartValueSelectedListener(keylistener)
        initCandleChart(count)

    }

    fun initCandleChart(count: Int) {
        //mCandle.setMaxVisibleValueCount(365)

        // scaling can now only be done on x- and y-axis separately
        mCandle.setPinchZoom(false)
        mCandle.setDrawGridBackground(false)

        var xAxis: XAxis = mCandle.xAxis
        with(xAxis)
        {
            isEnabled = true
            textColor = Color.WHITE
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 7f // only intervals of 1 day
            setValueFormatter({ value, axis ->
                xValues[(value % xValues.size).toInt()]
            })
        }


        var leftAxis: YAxis = mCandle.axisRight
        with(leftAxis)
        {
            setLabelCount(7, false)
            setDrawGridLines(true)
            setDrawAxisLine(true)
            textColor = Color.WHITE
        }

        var rightAxis: YAxis = mCandle.axisLeft
        rightAxis.isEnabled = false

        with(mCandle)
        {
            legend.isEnabled = false
            description.isEnabled = true
            description.text = "45 Day CandleStick"
            description.textColor = Color.YELLOW
            description.textSize = 12f
            description.setPosition(900f, 50f)
            setPinchZoom(false)
        }
        setCandleData(count)
        mCandle.setVisibleXRangeMaximum(20f)

    }

    private fun setCandleData(count: Int) {

        //var prog = (mSeekBarX.getProgress() + 1)

        //entry.text = "" +
        //time.text = "" + (mSeekBarY.getProgress())

        mCandle.resetTracking()
        var size: Int = CurrencyDeets.size - 1
        var yVals1: ArrayList<CandleEntry> = ArrayList()
        var i = 0
        while (i <= count) {
            yVals1.add(CandleEntry(
                    i.toFloat(), CurrencyDeets[i + (size - count)].High.toFloat(),
                    CurrencyDeets[i + (size - count)].Low.toFloat(),
                    CurrencyDeets[i + (size - count)].Open.toFloat(),
                    CurrencyDeets[i + (size - count)].Close.toFloat()
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

        mCandle.data = data
        mCandle.invalidate()
    }
    //endregion

    //region Bar Chart
    private fun initBar(count: Int) {
        mBar = findViewById<BarChart>(R.id.VolumeChart) as BarChart
        val keylistener = object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry, h: Highlight) {
                entry.text = "Volume: ${formatterLarge.format(e.y)} M"
                time.text = "${CurrencyDeets[e.x.toInt() + (CurrencyDeets.size - 1 - count)].Date}"
            }

            override fun onNothingSelected() {
                entry.text = ""
                time.text = ""
            }

        }
        mBar.setOnChartValueSelectedListener(keylistener)
        initBarChart(count)
    }

    fun initBarChart(count: Int) {
        var i = 0
        var size = CurrencyDeets.size - 1
        while (i <= count) {
            Log.i("Date", CurrencyDeets[i + (size - count)].Date)
            val value = CurrencyDeets[i + (size - count)].Date
            xValues.add(value)
            i++
        }

        with(mBar)
        {
            setDrawBarShadow(false)
            setDrawValueAboveBar(false)
            description.isEnabled = true
            description.text = "24Hr Volume"
            description.textColor = Color.YELLOW
            description.textSize = 12f
            description.setPosition(850f, 50f)
            setMaxVisibleValueCount(365)
            setPinchZoom(false)
            setDrawGridBackground(false)
            xAxis.setDrawLabels(true)
            xAxis.setDrawAxisLine(true)
        }

        var xAxis: XAxis = mBar.xAxis

        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(true)
        xAxis.granularity = 7f // only intervals of 1 day
        xAxis.labelCount = 7
        xAxis.setValueFormatter({ value, axis ->
            xValues[(value % xValues.size).toInt()]
        })
        xAxis.textColor = Color.WHITE
        xAxis.isEnabled = true


        //var leftAxis: YAxis = mBar.axisLeft
        //leftAxis.setLabelCount(8, false)
        //leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
        //leftAxis.spaceTop = 15f
        //leftAxis.axisMinimum = 0f // this replaces setStartAtZero(true)
//        var leftAxis: YAxis = mBar.axisLeft
//        leftAxis.setDrawGridLines(false)
//        leftAxis.setLabelCount(7, false)
//        leftAxis.spaceTop = 0f
//        leftAxis.axisMinimum = 0f // this replaces setStartAtZero(true)
//        leftAxis.textColor = Color.WHITE

        var rightAxis: YAxis = mBar.axisRight
        rightAxis.setDrawGridLines(true)
        rightAxis.setLabelCount(7, false)
        rightAxis.spaceTop = 0f
        rightAxis.axisMinimum = 0f // this replaces setStartAtZero(true)
        rightAxis.textColor = Color.WHITE

//        var l: Legend = mBar.legend
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

        mBar.legend.isEnabled = false
        mBar.axisLeft.isEnabled = false
        mBar.setVisibleXRangeMaximum(20f)

    }

    private fun setBarData(count: Int) {

        var size: Int = CurrencyDeets.size - 1
        val yVals1 = ArrayList<BarEntry>()
        var i = 0
        while (i <= count) {
            val value = CurrencyDeets[i + (size - count)].Volume.toDouble() / 1000000
            yVals1.add(BarEntry(i.toFloat(), value.toFloat()))
            i++
        }

        val set1: BarDataSet

        if (mBar.data != null && mBar.data.dataSetCount > 0) {
            set1 = (mBar.data.getDataSetByIndex(0)) as BarDataSet
            set1.values = yVals1
            mBar.data.notifyDataChanged()
            mBar.notifyDataSetChanged()
        } else {
            set1 = BarDataSet(yVals1, null)

            set1.setDrawIcons(false)

            set1.setColors(Color.GRAY)

            val dataSets = ArrayList<IBarDataSet>()
            dataSets.add(set1)

            val data = BarData(dataSets)
            data.setValueTextSize(0f)
            data.barWidth = 0.7f

            mBar.data = data
        }
        mBar.invalidate()


    }
    //endregion

    private fun parseCoinDetails() {
        var blocks = response.substringAfter("<tbody>").split("</tr>")
        blocks.take(blocks.size - 1)
                .forEach {
                    CurrencyDeets.add(createCurrencyDetail(it))
                }
        CurrencyDeets.sortBy { it.Date }
        CurrencyDeets.forEach {
            it.Date = (parseUnix(it.Date.toLong()))
        }
        if(blocks.size >= 45)
            initAllCharts(45)
        else
            initAllCharts(blocks.size)
    }

    private fun createCurrencyDetail(block: String): CurrencyDetails {
        var data = block.substringAfter("<tr class=\"text-right\">").split("</td>")
        //data index: date,open,high,low,close,volume,marketcap

        var date = data[0].substringAfter(">").replace("</td>", "")
        var dateFormat: DateFormat = SimpleDateFormat("MMM dd, yyyy")
        var dateParsed: Date = dateFormat.parse(date)
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

    fun parseUnix(time: Long): String {
        val date = Date(time)
        val sdf = SimpleDateFormat("MM/dd/yy")
        return sdf.format(date)
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
        LocalBroadcastManager.getInstance(applicationContext)
                .unregisterReceiver(mBroadcastReceiver2)
    }
}
