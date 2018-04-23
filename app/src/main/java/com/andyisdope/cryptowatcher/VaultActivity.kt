package com.andyisdope.cryptowatcher

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.ShareActionProvider
import android.widget.TabHost
import android.widget.TextView
import com.andyisdope.cryptowatcher.Adapters.AssetAdapter
import com.andyisdope.cryptowatcher.model.Asset
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener


class VaultActivity : AppCompatActivity() {
    //TODO: create service to retrieve and store pricing data at 11:59pm for asset graph
    //TODO: change preference to save symbol instead of full name 


    private lateinit var CoinShares: PieChart
    private lateinit var VaultShareTV: TextView
    private lateinit var VaultSharesRV: RecyclerView
    private var entries = ArrayList<PieEntry>()
    private lateinit var CoinHoldings: SharedPreferences
    private var CoinData: ArrayList<Asset> = ArrayList()
    private lateinit var mAdapter: AssetAdapter
    private val colors = ArrayList<Int>()


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
                    }
                    "Assets" -> {
                    }
                }
            }
        }
    }

    private fun initRecycler() {
        VaultSharesRV = findViewById(R.id.VaultSharesRV)
        VaultSharesRV.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        mAdapter = AssetAdapter(baseContext, CoinData)
        VaultSharesRV.adapter = mAdapter
        VaultSharesRV.adapter.notifyDataSetChanged()
    }

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
}
