package com.application.fitlife

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.application.fitlife.data.MyDatabaseHelper
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.BarGraphSeries
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import com.application.fitlife.GraphSampleDataGenerator

class GraphsAndAnalyticsActivity : AppCompatActivity() {

    private lateinit var spinner: Spinner
    private lateinit var graphView: GraphView
    private lateinit var scoreButton: Button
    private lateinit var weightButton: Button
    private lateinit var heartRateButton: Button
    private lateinit var respiratoryRateButton: Button
    private var progressBar: ProgressBar? = null
    private var progressText: TextView? = null
    private var currentAttribute = "score"
    private var todaysDailyScore = 0;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graphs_and_analytics)

        spinner = findViewById(R.id.spinner)
        graphView = findViewById(R.id.graphView)
        scoreButton = findViewById(R.id.scoreButton)
        weightButton = findViewById(R.id.weightButton)
        heartRateButton = findViewById(R.id.heartRateButton)
        respiratoryRateButton = findViewById(R.id.respiratoryRateButton)
        // set the id for the progressbar and progress text
        progressBar = findViewById(R.id.progress_bar);
        progressText = findViewById(R.id.progress_text);

        ArrayAdapter.createFromResource(
            this,
            R.array.graph_types,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
            graphView.removeAllSeries()
        }

        // by default showing the line graph for score for the past recent data
        defaultUpdateGraph("score")

        scoreButton.setOnClickListener {
            currentAttribute = "score"
            updateGraph("score")
        }
        weightButton.setOnClickListener {
            currentAttribute = "weight"
            updateGraph("weight")
        }
        heartRateButton.setOnClickListener {
            currentAttribute = "heart_rate"
            updateGraph("heart_rate")
        }
        respiratoryRateButton.setOnClickListener {
            currentAttribute = "respiratory_rate"
            updateGraph("respiratory_rate")
        }
        graphView.removeAllSeries()

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                // Handle the selected item here
                val selectedItem = parentView?.getItemAtPosition(position).toString()
                // You can perform actions based on the selected item
                // For example, update the UI, trigger a function, etc.
                // Example: updateGraph(selectedItem)
                Log.d("dropDownSelectedItem" , selectedItem)
                println("dropDown Selected Item - $selectedItem")
                updateGraph(currentAttribute)
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                // Do nothing here
            }
        }
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
        // need to update this to show case the past 10 days data so the maximum value of x should be 10.
        graphView.viewport.setMaxX(30.0)

        // Enable scrolling
        graphView.viewport.isScrollable = true
    }

    private fun defaultUpdateGraph(metric: String) {
        val series = LineGraphSeries<DataPoint>(getDataPoints(metric))

        graphView.removeAllSeries()
        graphView.addSeries(series)

        //graphView.viewport.isXAxisBoundsManual = true
        graphView.viewport.setMinX(0.0)
        // need to update this to show case the past 10 days data so the maximum value of x should be 10.
        graphView.viewport.setMaxX(30.0)

        // Enable scrolling
        graphView.viewport.isScrollable = true
    }

    private fun getDataPoints(metric: String): Array<DataPoint> {
        // Replace this with your actual logic to get the data points

        val dbHelper = MyDatabaseHelper(this)
        val db = dbHelper.writableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM ${MyDatabaseHelper.TABLE_NAME_USER_METRICS} ORDER BY ${MyDatabaseHelper.COLUMN_NAME_DATE} DESC LIMIT 30",
            arrayOf()
        )
        val scoresDataArray = mutableListOf<DataPoint>()
        val weightDataArray = mutableListOf<DataPoint>()
        val heartRateDataArray = mutableListOf<DataPoint>()
        val respiratoryRateDataArray = mutableListOf<DataPoint>()
        val emptyDataArray = mutableListOf<DataPoint>()

        if (cursor != null && cursor.moveToFirst()) {
            var day = 1
            do {
                // You can retrieve data from the cursor using column indices or column names
                val dateIDIndex = cursor.getColumnIndex("date")
                val dateValue = cursor.getString(dateIDIndex)

                val scoreIndex = cursor.getColumnIndex("score")
                val scoreValue = cursor.getDouble(scoreIndex)

                val heartRateIndex = cursor.getColumnIndex("heart_rate")
                val heartRateValue = cursor.getDouble(heartRateIndex)

                val respiratoryRateIndex = cursor.getColumnIndex("respiratory_rate")
                val respiratoryRateValue = cursor.getDouble(respiratoryRateIndex)

                val weightIndex = cursor.getColumnIndex("weight")
                val weightValue = cursor.getDouble(weightIndex)

                // Now you can use the retrieved data as needed
                // Example: Log the values
                Log.d("getDataPointsQueryResponse", "Primary Key Date = $dateValue Score: $scoreValue, Weight: $weightValue, Heart Rate: $heartRateValue, Respiratory Rate: $respiratoryRateValue")

                // update the mutable scores data array
                var scoreDataPoint = DataPoint(day.toDouble(), scoreValue.toDouble())
                scoresDataArray.add(scoreDataPoint)

                // update the mutable weights data array
                var weightDataPoint = DataPoint(day.toDouble(), weightValue.toDouble())
                weightDataArray.add(weightDataPoint)

                // update the mutable heart rate data array
                var heartRateDataPoint = DataPoint(day.toDouble(), heartRateValue.toDouble())
                heartRateDataArray.add(heartRateDataPoint)

                // update the mutable respiratory rate data array
                var respiratoryRateDataPoint = DataPoint(day.toDouble(), respiratoryRateValue.toDouble())
                respiratoryRateDataArray.add(respiratoryRateDataPoint)

                if (day == 1) {
                    todaysDailyScore = scoreValue.toInt();
                    if (todaysDailyScore == 0) {
                        progressText?.text = "00"
                    } else {
                        progressText?.text = "" + todaysDailyScore
                    }
                    progressBar?.setProgress(todaysDailyScore)
                    Log.d("todaysDailyScore" , todaysDailyScore.toString())
                }

                day += 1

                // Continue iterating if there are more rows

            } while (false)
        }

        // while (cursor.moveToNext())

//        val dataArray = mutableListOf<DataPoint>()
//
//        var dataPoint = DataPoint(0.0, 1.0)
//        dataArray.add(dataPoint)
//
//        dataPoint = DataPoint(1.0, 5.0)
//        dataArray.add(dataPoint)
//
//        dataPoint = DataPoint(2.0, 3.0)
//        dataArray.add(dataPoint)
//
//        dataPoint = DataPoint(3.0, 2.0)
//        dataArray.add(dataPoint)
//
//        dataPoint = DataPoint(4.0, 6.0)
//        dataArray.add(dataPoint)
//
//        dataPoint = DataPoint(5.0, 7.0)
//        dataArray.add(dataPoint)
//
//        dataPoint = DataPoint(6.0, 8.0)
//        dataArray.add(dataPoint)
//
//        dataPoint = DataPoint(7.0, 9.0)
//        dataArray.add(dataPoint)
//
//        dataPoint = DataPoint(8.0, 10.0)
//        dataArray.add(dataPoint)
//
//        dataPoint = DataPoint(9.0, 11.0)
//        dataArray.add(dataPoint)
//
//        dataPoint = DataPoint(10.0, 12.0)
//        dataArray.add(dataPoint)
//
//        dataPoint = DataPoint(11.0, 13.0)
//        dataArray.add(dataPoint)

        var graphSampleDataGeneratorClassObject = GraphSampleDataGenerator()

        // getting random data for scores
        val randomDailyScoresDataArray = graphSampleDataGeneratorClassObject.dailyScoresArrayData()
        var day = 2
        for (randomScoreValue in randomDailyScoresDataArray) {
            var dataPoint = DataPoint(day.toDouble(), randomScoreValue.toDouble())
            scoresDataArray.add(dataPoint)
            day += 1
        }

        // getting random data for heart rates
        val randomHeartRatesDataArray = graphSampleDataGeneratorClassObject.heartRatesArrayData()
        day = 2
        for (randomHeartRate in randomHeartRatesDataArray) {
            var dataPoint = DataPoint(day.toDouble(), randomHeartRate.toDouble())
            heartRateDataArray.add(dataPoint)
            day += 1
        }

        // getting random data for respiratory rates
        val randomRespiratoryRatesDataArray = graphSampleDataGeneratorClassObject.respiratoryRatesArrayData()
        day = 2
        for (randomRespiratoryRate in randomRespiratoryRatesDataArray) {
            var dataPoint = DataPoint(day.toDouble(), randomRespiratoryRate.toDouble())
            respiratoryRateDataArray.add(dataPoint)
            day += 1
        }

        // getting random data for respiratory rates
        val randomWeightDataArray = graphSampleDataGeneratorClassObject.userWeightArrayData()
        day = 2
        for (randomWeight in randomWeightDataArray) {
            var dataPoint = DataPoint(day.toDouble(), randomWeight.toDouble())
            weightDataArray.add(dataPoint)
            day += 1
        }



        if (metric == "score") {
            return scoresDataArray.toTypedArray()
        }

        if (metric == "weight") {
            return weightDataArray.toTypedArray()
        }

        if (metric == "heart_rate") {
            return heartRateDataArray.toTypedArray()
        }

        if (metric == "respiratory_rate") {
            return respiratoryRateDataArray.toTypedArray()
        }

        return emptyDataArray.toTypedArray()


//        return arrayOf(
//            DataPoint(0.0, 1.0),
//            DataPoint(1.0, 5.0),
//            DataPoint(2.0, 3.0),
//            DataPoint(3.0, 2.0),
//            DataPoint(4.0, 6.0),
//            DataPoint(5.0, 7.0),
//            DataPoint(6.0, 8.0),
//            DataPoint(7.0, 9.0),
//            DataPoint(8.0, 10.0),
//            DataPoint(9.0, 11.0),
//            DataPoint(10.0, 12.0),
//            DataPoint(11.0, 13.0)
//        )
    }

}