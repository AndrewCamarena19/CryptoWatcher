package com.andyisdope.cryptowatcher

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.TabHost
import android.widget.TextView
import com.andyisdope.cryptowatcher.Adapters.AssetAdapter
import com.andyisdope.cryptowatcher.Adapters.USDAdapterItem
import com.andyisdope.cryptowatcher.database.AssetDatabase
import com.andyisdope.cryptowatcher.model.Asset
import com.andyisdope.cryptowatcher.model.DateAsset
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.MPPointF
import kotlinx.android.synthetic.main.activity_vault.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class VaultActivity : AppCompatActivity() {
    //TODO: create service to retrieve and store pricing data at 11:59pm for asset graph
    //TODO: change preference to save symbol instead of full name


    private lateinit var CoinShares: PieChart
    private lateinit var AssetShares: PieChart
    private lateinit var PortfolioValuesChart: LineChart
    private lateinit var VaultShareTV: TextView
    private lateinit var VaultAssetTV: TextView
    private lateinit var VaultSharesRV: RecyclerView
    private lateinit var VaultAssetsRV: RecyclerView
    private lateinit var VaultPortfolioRV: RecyclerView
    private var entries = ArrayList<PieEntry>()
    private var PriceEntries = ArrayList<PieEntry>()
    private var PortfolioEntries = ArrayList<Entry>()
    private lateinit var CoinHoldings: SharedPreferences
    private lateinit var CoinPrices: SharedPreferences
    private var CoinData: ArrayList<Asset> = ArrayList()
    private var PriceData: ArrayList<DateAsset> = ArrayList()
    private var PortfolioData: ArrayList<DateAsset> = ArrayList()
    private lateinit var mAdapter: AssetAdapter
    private lateinit var tAdapter: USDAdapterItem
    private lateinit var pAdapter: USDAdapterItem
    private var AssetDB: AssetDatabase? = null
    private val colors = ArrayList<Int>()
    private var isLoaded = false
    private var isPortLoaded = false
    private var Min = Float.MAX_VALUE
    private var Max = Float.MIN_VALUE
    private var xValues = ArrayList<String>()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vault)
        initTabs()
        initCoinShares()
    }

    fun initTabs() {
        val tabs = findViewById<TabHost>(R.id.VaultTabs)
        tabs.setup()
        with(tabs)
        {
            var spec: TabHost.TabSpec = tabs.newTabSpec("Shares")
            spec.setContent(R.id.VaultCoinShares)
            spec.setIndicator("Shares")
            addTab(spec)
            spec = tabs.newTabSpec("Assets")
            spec.setContent(R.id.VaultAssetShares)
            spec.setIndicator("Assets")
            addTab(spec)
            spec = tabs.newTabSpec("Portfolio")
            spec.setContent(R.id.VaultPortfolioGraph)
            spec.setIndicator("Portfolio")
            addTab(spec)
            setOnTabChangedListener { tabID ->
                when (tabID) {
                    "Shares" -> {
                    }
                    "Portfolio" -> {
                        if (!isPortLoaded) {
                            initPortfolio()
                            isPortLoaded = true
                        }
                    }
                    "Assets" -> {
                        if (!isLoaded) {
                            initAssetShares()
                            isLoaded = true
                            AssetDB = AssetDatabase.getInstance(baseContext)
                        }
                    }
                }
            }
        }
    }

    //region Portfolio Chart
    private fun initPortfolio() {
        VaultPortfolioRV = findViewById(R.id.PortfolioValuesRV)
        PortfolioValuesChart = findViewById(R.id.lineChart)
        PortfolioValuesChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onNothingSelected() {
                findViewById<TextView>(R.id.PortfolioDateTV).text = "Select a graph point"
            }

            override fun onValueSelected(e: Entry?, h: Highlight?) {
                if (e == null)
                    return
                findViewById<TextView>(R.id.PortfolioDateTV).text = "$ ${e.y} in Vault on ${PortfolioData[e.x.toInt()].Date}"
            }
        })
        loadPortfolioData()
        setPortfolioData()
    }

    private fun loadPortfolioData() {
        var AssetDB = AssetDatabase.getInstance(this)
        AssetDB?.AssetDao()?.getAll()?.forEach {
            PortfolioData.add(it)
            if(it.Price > Max)
                Max = it.Price.toFloat()
            if(it.Price < Min)
                Min = it.Price.toFloat()
        }

//        for (i in 1 .. 24)
//        {
//            var tempData = DateAsset("4/$i/2018", (Math.random() * 4000))
//            PortfolioData.add(tempData)
//            if (tempData.Price > Max)
//                Max = tempData.Price.toFloat()
//            if (tempData.Price < Min)
//                Min = tempData.Price.toFloat()
//        }
        PortfolioData.withIndex().forEach {
            PortfolioEntries.add(Entry(it.index.toFloat(), it.value.Price.toFloat()))
        }

        Log.i("Data", PortfolioEntries.toString())
    }

    private fun setPortfolioData() {
        // no description text
        PortfolioValuesChart.description.isEnabled = false

        // enable touch gestures
        PortfolioValuesChart.setTouchEnabled(true)

        PortfolioValuesChart.dragDecelerationFrictionCoef = 0.9f

        // enable scaling and dragging
        PortfolioValuesChart.isDragEnabled = true
        PortfolioValuesChart.setScaleEnabled(false)
        PortfolioValuesChart.setDrawGridBackground(false)
        PortfolioValuesChart.isHighlightPerDragEnabled = false

        // if disabled, scaling can be done on x- and y-axis separately
        PortfolioValuesChart.setPinchZoom(false);

        // set an alternative background color
        PortfolioValuesChart.setBackgroundColor(resources.getColor(R.color.Transparent));

        // add data
        initPortfolioData(20, 30f);

        PortfolioValuesChart.animateX(1500);

        // get the legend (only possible after setting data)
        var l = PortfolioValuesChart.legend;

        // modify the legend ...
        l.form = Legend.LegendForm.LINE;
        l.textSize = 11f;
        l.textColor = resources.getColor(R.color.colorPrimary)
        l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM;
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT;
        l.orientation = Legend.LegendOrientation.HORIZONTAL;
        l.setDrawInside(false)
//        l.setYOffset(11f);

        var xAxis = PortfolioValuesChart.xAxis;
        xAxis.textSize = 11f;
        xAxis.textColor = resources.getColor(R.color.colorPrimary)
        xAxis.setDrawGridLines(true);
        xAxis.setDrawAxisLine(true);
        xAxis.granularity = 5f

        var i = 0
        var size = PortfolioData.size - 1
        while (i < size) {
            val value = PortfolioData[i].Date
            xValues.add(value)
            i++
        }
        xAxis.setValueFormatter({ value, axis ->
            PortfolioData[(value % xValues.size).toInt()].Date
        })

        var leftAxis = PortfolioValuesChart.axisLeft;
        leftAxis.textColor = resources.getColor(R.color.colorPrimary)
        leftAxis.axisMaximum = Max + 50f;
        leftAxis.axisMinimum = Min - 50f;
        leftAxis.setDrawGridLines(false);
        leftAxis.isGranularityEnabled = true;

        var rightAxis = PortfolioValuesChart.axisRight;
        rightAxis.textColor = resources.getColor(R.color.colorPrimary)
        rightAxis.axisMaximum = Max + 50f;
        rightAxis.axisMinimum = Min - 50f;
        rightAxis.setDrawGridLines(true);
        rightAxis.setDrawZeroLine(true);
        rightAxis.isGranularityEnabled = false;

        PortfolioValuesChart.setVisibleXRangeMaximum(10f)

    }

    private fun initPortfolioData(count: Int, range: Float) {

        var set1 = LineDataSet(PortfolioEntries, "Portfolio Value");

        set1.axisDependency = YAxis.AxisDependency.LEFT;
        set1.color = resources.getColor(R.color.TickerNameColor)
        set1.setCircleColor(Color.WHITE);
        set1.lineWidth = 2f;
        set1.circleRadius = 2f;
        set1.fillAlpha = 65;
        set1.fillColor = resources.getColor(R.color.PrimaryNew)
        set1.highLightColor = Color.rgb(244, 117, 117);
        set1.setDrawCircleHole(false);


        var data = LineData(set1)
        data.setValueTextColor(resources.getColor(R.color.PrimaryNew));
        data.setValueTextSize(0f);
        PortfolioValuesChart.data = data;
        PortfolioValuesChart.invalidate();
        initPortfolioRecycler()


    }

    private fun initPortfolioRecycler() {
        VaultPortfolioRV.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        pAdapter = USDAdapterItem(baseContext, PortfolioData)
        VaultPortfolioRV.adapter = pAdapter
        VaultPortfolioRV.adapter.notifyDataSetChanged()
    }
    //endregion

    //region Assets USD Pie Chart
    private fun initAssetShares() {
        CoinPrices = getSharedPreferences("Prices", Context.MODE_PRIVATE)
        AssetShares = findViewById<PieChart>(R.id.VaultPieChartAssets) as PieChart
        AssetShares.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onNothingSelected() {
                VaultAssetTV.text = "Select a Slice"
            }

            override fun onValueSelected(e: Entry?, h: Highlight?) {
                if (e == null)
                    return
                VaultAssetTV.text = "$ ${e.y} of ${PriceEntries[h!!.x.toInt()].label} in Vault"
            }
        })
        VaultAssetTV = findViewById(R.id.VaultAssetTV)
        setAssetData()
    }

    private fun setAssetData() {
        AssetShares.setUsePercentValues(true)
        AssetShares.description.isEnabled = false
        AssetShares.setExtraOffsets(0f, 0f, 50f, 0f)
        AssetShares.dragDecelerationFrictionCoef = 0.95f


        AssetShares.isDrawHoleEnabled = true
        AssetShares.setHoleColor(Color.WHITE)

        //AssetShares.setTransparentCircleColor(Color.WHITE)
        //AssetShares.setTransparentCircleAlpha(110)

        AssetShares.holeRadius = 30f
        AssetShares.transparentCircleRadius = 0f

        AssetShares.setDrawCenterText(true)

        AssetShares.rotationAngle = 0f
        // enable rotation of the chart by touch
        AssetShares.isRotationEnabled = true
        AssetShares.isHighlightPerTapEnabled = true

        // AssetShares.setUnit(" €")
        // AssetShares.setDrawUnitsInChart(true)

        // add a selection listener
        //AssetShares.setOnChartValueSelectedListener(this)

        setDataAssets()

        AssetShares.animateY(1400, Easing.EasingOption.EaseInOutQuad)
        // AssetShares.spin(2000, 0, 360)

        //mSeekBarX.setOnSeekBarChangeListener(this)
        //mSeekBarY.setOnSeekBarChangeListener(this)

        var l = AssetShares.legend
        l.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        l.orientation = Legend.LegendOrientation.VERTICAL
        l.setDrawInside(true)
        l.xEntrySpace = 0f
        l.yEntrySpace = 0f
        l.yOffset = 0f
        l.textColor = Color.WHITE

        // entry label styling
        //CoinShares.setEntryLabelColor(Color.WHITE)
        AssetShares.setEntryLabelTextSize(0f)
    }

    private fun setDataAssets() {

        var coindata = CoinPrices.all
        var coinAmt = CoinHoldings.all
        // NOTE: The order of the entries when being added to the entries array determines their position around the center of
        // the chart.
        coindata.entries.forEach {
            if (it.value != "USD") {
                var pricetemp: Float = coinAmt[it.key].toString().toFloat() * it.value.toString().toFloat()
                PriceData.add(DateAsset(it.key.toUpperCase(), pricetemp.toDouble()))
                PriceEntries.add(PieEntry(pricetemp, it.key.toUpperCase()))
            }
        }
        PriceData.add(DateAsset("USD", coinAmt["USD"].toString().toDouble()))
        PriceEntries.add(PieEntry(coinAmt["USD"].toString().toFloat(), "USD"))

        val dataSet = PieDataSet(PriceEntries, "")

        dataSet.setDrawIcons(false)

        dataSet.sliceSpace = 4f
        dataSet.iconsOffset = MPPointF(0f, 40f)
        dataSet.selectionShift = 10f

        for (c in ColorTemplate.JOYFUL_COLORS)
            colors.add(c)

        for (c in ColorTemplate.COLORFUL_COLORS)
            colors.add(c)

        for (c in ColorTemplate.LIBERTY_COLORS)
            colors.add(c)

        for (c in ColorTemplate.PASTEL_COLORS)
            colors.add(c)
        // add a lot of colors

        for (c in ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c)

        colors.add(ColorTemplate.getHoloBlue())
        dataSet.colors = colors
        //=dataSet.setSelectionShift(0f);

        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter())
        data.setValueTextSize(12f)
        data.setValueTextColor(Color.BLACK)
        AssetShares.data = data

        // undo all highlights
        AssetShares.highlightValues(null)

        AssetShares.invalidate()
        initAssetRecycler()
    }

    private fun initAssetRecycler() {
        VaultAssetsRV = findViewById(R.id.VaultAssetDistribution)
        VaultAssetsRV.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        PriceData.sortBy { it.Price }
        tAdapter = USDAdapterItem(baseContext, PriceData)
        VaultAssetsRV.adapter = tAdapter
        VaultAssetsRV.adapter.notifyDataSetChanged()
    }
    //endregion

    //region Coin Amount Pie Chart
    private fun initCoinShares() {
        CoinHoldings = getSharedPreferences("Coins", Context.MODE_PRIVATE)
        CoinShares = findViewById<PieChart>(R.id.VaultPieChart) as PieChart
        CoinShares.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onNothingSelected() {
                VaultShareTV.text = "Select a Slice"
            }

            override fun onValueSelected(e: Entry?, h: Highlight?) {
                if (e == null)
                    return
                VaultShareTV.text = "${e.y} coins of ${entries[h!!.x.toInt()].label} in Vault"
            }
        })
        VaultShareTV = findViewById(R.id.VaultShareTV)
        setCoinData()
    }

    private fun setCoinData() {
        CoinShares.setUsePercentValues(true)
        CoinShares.description.isEnabled = false
        CoinShares.setExtraOffsets(0f, 0f, 50f, 0f)
        CoinShares.dragDecelerationFrictionCoef = 0.95f


        CoinShares.isDrawHoleEnabled = true
        CoinShares.setHoleColor(Color.WHITE)

        //CoinShares.setTransparentCircleColor(Color.WHITE)
        //CoinShares.setTransparentCircleAlpha(110)

        CoinShares.holeRadius = 30f
        CoinShares.transparentCircleRadius = 0f

        CoinShares.setDrawCenterText(true)

        CoinShares.rotationAngle = 0f
        // enable rotation of the chart by touch
        CoinShares.isRotationEnabled = true
        CoinShares.isHighlightPerTapEnabled = true

        // CoinShares.setUnit(" €")
        // CoinShares.setDrawUnitsInChart(true)

        // add a selection listener
        //CoinShares.setOnChartValueSelectedListener(this)

        setData()

        CoinShares.animateY(1400, Easing.EasingOption.EaseInOutQuad)
        // CoinShares.spin(2000, 0, 360)

        //mSeekBarX.setOnSeekBarChangeListener(this)
        //mSeekBarY.setOnSeekBarChangeListener(this)

        var l = CoinShares.legend
        l.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        l.orientation = Legend.LegendOrientation.VERTICAL
        l.setDrawInside(true)
        l.xEntrySpace = 0f
        l.yEntrySpace = 0f
        l.yOffset = 0f
        l.textColor = Color.WHITE

        // entry label styling
        //CoinShares.setEntryLabelColor(Color.WHITE)
        CoinShares.setEntryLabelTextSize(0f)
    }

    private fun setData() {

        var coindata = CoinHoldings.all
        Log.i("Prices", coindata.toString())

        // NOTE: The order of the entries when being added to the entries array determines their position around the center of
        // the chart.
        coindata.entries.forEach {
            if (it.key != "USD") {
                CoinData.add(Asset(it.key.toUpperCase(), it.value.toString().toFloat()))
                entries.add(PieEntry(it.value.toString().toFloat(), it.key.toUpperCase()))
            }
        }

        val dataSet = PieDataSet(entries, "")

        dataSet.setDrawIcons(false)

        dataSet.sliceSpace = 4f
        dataSet.iconsOffset = MPPointF(0f, 40f)
        dataSet.selectionShift = 10f

        for (c in ColorTemplate.JOYFUL_COLORS)
            colors.add(c)

        for (c in ColorTemplate.COLORFUL_COLORS)
            colors.add(c)

        for (c in ColorTemplate.LIBERTY_COLORS)
            colors.add(c)

        for (c in ColorTemplate.PASTEL_COLORS)
            colors.add(c)
        // add a lot of colors

        for (c in ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c)

        colors.add(ColorTemplate.getHoloBlue())
        dataSet.colors = colors
        //=dataSet.setSelectionShift(0f);

        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter())
        data.setValueTextSize(12f)
        data.setValueTextColor(Color.BLACK)
        CoinShares.data = data

        // undo all highlights
        CoinShares.highlightValues(null)

        CoinShares.invalidate()
        initRecycler()
    }

    private fun initRecycler() {
        VaultSharesRV = findViewById(R.id.VaultSharesRV)
        VaultSharesRV.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        mAdapter = AssetAdapter(baseContext, CoinData)
        VaultSharesRV.adapter = mAdapter
        VaultSharesRV.adapter.notifyDataSetChanged()
    }
    //endregion
}
