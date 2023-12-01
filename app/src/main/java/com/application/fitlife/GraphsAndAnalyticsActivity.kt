package com.application.bhealthy
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.BarGraphSeries
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries

class GraphsAndAnalyticsActivity : AppCompatActivity() {

    private lateinit var spinner: Spinner
    private lateinit var graphView: GraphView
    private lateinit var scoreButton: Button
    private lateinit var weightButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graphs_and_analytics)

        spinner = findViewById(R.id.spinner)
        graphView = findViewById(R.id.graphView)
        scoreButton = findViewById(R.id.scoreButton)
        weightButton = findViewById(R.id.weightButton)

        ArrayAdapter.createFromResource(
            this,
            R.array.graph_types,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
            graphView.removeAllSeries()
        }

        scoreButton.setOnClickListener { updateGraph("score") }
        weightButton.setOnClickListener { updateGraph("weight") }
        graphView.removeAllSeries()
    }

    private fun updateGraph(metric: String) {
        val series = when (spinner.selectedItem.toString()) {
            "Line Graph" -> LineGraphSeries<DataPoint>(getDataPoints(metric))
            "Bar Graph" -> BarGraphSeries<DataPoint>(getDataPoints(metric))
            else -> throw IllegalArgumentException("Invalid graph type")
        }

        graphView.removeAllSeries()
        graphView.addSeries(series)

        //graphView.viewport.isXAxisBoundsManual = true
        graphView.viewport.setMinX(0.0)
        graphView.viewport.setMaxX(50.0)

        // Enable scrolling
        graphView.viewport.isScrollable = true
    }

    private fun getDataPoints(metric: String): Array<DataPoint> {
        // Replace this with your actual logic to get the data points
        return arrayOf(
            DataPoint(0.0, 1.0),
            DataPoint(1.0, 5.0),
            DataPoint(2.0, 3.0),
            DataPoint(3.0, 2.0),
            DataPoint(4.0, 6.0),
            DataPoint(5.0, 7.0),
            DataPoint(6.0, 8.0),
            DataPoint(7.0, 9.0),
            DataPoint(8.0, 10.0),
            DataPoint(9.0, 11.0),
            DataPoint(10.0, 12.0),
            DataPoint(11.0, 13.0)
        )
    }
}