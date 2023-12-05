package com.application.fitlife

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.AsyncTask
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.application.fitlife.data.MyDatabaseHelper
import java.util.*
import kotlin.math.abs
import kotlin.math.pow
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {

    // defining the UI elements
    private lateinit var heartRateText: TextView
    private lateinit var userAge: EditText
    private lateinit var userWeight: EditText
    private lateinit var userHeight: EditText

    // maintaining a unique ID to acts a primary key reference for the database table.
    // for this project we are using the current date as the primary key as we are interested in only maintaining one row per day.
    // so any new data coming into the table for the current date which is already present in the table, then the code logic would only update the table instead of inserting a new row.
    private val uniqueIdKey = "unique_id"
    private lateinit var sharedPreferences: SharedPreferences

    // camera implementation pre requisites
    val CAMERA_ACTIVITY_REQUEST_CODE = 1 // Define a request code (any integer)
    private val eventHandler = Handler()

    // respiratory rate accelerometer pre-requisites
    private val dataCollectionIntervalMillis = 100 // Sampling interval in milliseconds
    private val accelValuesX = ArrayList<Float>()
    private val accelValuesY = ArrayList<Float>()
    private val accelValuesZ = ArrayList<Float>()
    private var isMeasuringRespiratoryRate = false
    private val measurementDuration = 45000L // 45 seconds

    private val slowTask = SlowTask()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // sharedPreferences is something similar to the localStorage and sessionStorage we get in JavaScript
        sharedPreferences = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)

        // generating a new unique ID
        val uniqueId = generateUniqueId()

        // Store the unique ID in SharedPreferences
        val editor = sharedPreferences.edit()
        editor.putString(uniqueIdKey, uniqueId)
        editor.apply()

        // accessing the heart Rate UI element and setting the TextView to be disabled so that the user cannot edit it.
        heartRateText = findViewById<TextView>(R.id.heartRateText)
        heartRateText.text = "0"
        heartRateText.isEnabled = false

        // accessing the respiratory Rate UI element and setting the TextView to be disabled so that the user cannot edit it.
        val respiratoryRateText = findViewById<TextView>(R.id.respiratoryRateText)
        respiratoryRateText.isEnabled = false

        // accessing the user age, weight and height metric fields.
        userAge = findViewById(R.id.editTextAge)
        userHeight = findViewById(R.id.editTextHeight)
        userWeight = findViewById(R.id.editTextWeight)

        val userAgeValue = userAge.text.toString()
        val userHeightValue = userHeight.text.toString()
        val userWeightValue = userWeight.text.toString()

        // making sure that the text view components that is:  app title and the tag line for the app title are disabled so that the user cannot edit it.
        val tagLinePartViewTwo: TextView = findViewById(R.id.tagLinePart2)
        tagLinePartViewTwo.isEnabled = false
        val appTitleView: TextView = findViewById(R.id.appTitle)
        appTitleView.isEnabled = false

        // Measure heart rate button
        val measureHeartRateButton = findViewById<Button>(R.id.heartRate)
        measureHeartRateButton.setOnClickListener {
            val intent = Intent(this@MainActivity, CameraActivity::class.java)
            intent.putExtra("unique_id", uniqueId)
            startActivityForResult(intent, CAMERA_ACTIVITY_REQUEST_CODE)
        }

        // Respiratory rate calculation
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val sensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                // Handle accelerometer data here
                if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]

                    accelValuesX.add(x)
                    accelValuesY.add(y)
                    accelValuesZ.add(z)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // Handle accuracy changes if needed
            }
        }

        val measureRespiratoryRate = findViewById<Button>(R.id.respiratoryRate)
        measureRespiratoryRate.setOnClickListener {
            Toast.makeText(baseContext, "Started measuring respiratory rate", Toast.LENGTH_SHORT).show()
            if (!isMeasuringRespiratoryRate) {
                isMeasuringRespiratoryRate = true
                accelValuesX.clear()
                accelValuesY.clear()
                accelValuesZ.clear()
                sensorManager.registerListener(sensorListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
                eventHandler.postDelayed({
                    sensorManager.unregisterListener(sensorListener)
                    val respiratoryRate = calculateRespiratoryRate()
                    respiratoryRateText.text = respiratoryRate.toString()
                    Toast.makeText(baseContext, "Respiratory rate calculation completed :" + respiratoryRate.toString(), Toast.LENGTH_SHORT).show()
                    isMeasuringRespiratoryRate = false
                }, measurementDuration)
            }
        }

        val dbHelper = MyDatabaseHelper(this)
        val recordExerciseButton = findViewById<Button>(R.id.recordMetrics)
        recordExerciseButton.setOnClickListener {
            insertOrUpdateDatabaseEntry(dbHelper, uniqueId, heartRateText.text.toString(), respiratoryRateText.text.toString(), userAgeValue, userWeightValue, userHeightValue)
            Toast.makeText(baseContext, "Uploaded Metrics to database", Toast.LENGTH_SHORT).show()
        }

        val recentAnalyticsButton = findViewById<Button>(R.id.recentAnalytics)
        recentAnalyticsButton.setOnClickListener {
            val intent = Intent(this@MainActivity, GraphsAndAnalyticsActivity::class.java)
            intent.putExtra("unique_id", uniqueId)
            startActivity(intent)
        }

        val startExerciseButton = findViewById<Button>(R.id.startExercise)
        startExerciseButton.setOnClickListener {
            val intent = Intent(this@MainActivity, ShowExercise::class.java)
            intent.putExtra("unique_id", uniqueId)
            startActivity(intent)
        }

    }

    fun insertOrUpdateDatabaseEntry(dbHelper: MyDatabaseHelper, uniqueDate: String, heart_rate: String, respiratory_rate: String, age: String, weight: String, height: String) {
        val db = dbHelper.writableDatabase

        // Check if the entry with the given vitals_id exists
        Log.d("uniqueDate", uniqueDate)
        val cursor = db.rawQuery("SELECT * FROM ${MyDatabaseHelper.TABLE_NAME_USER_METRICS} WHERE ${MyDatabaseHelper.COLUMN_NAME_DATE}=?", arrayOf(uniqueDate))
        val values = ContentValues()

        if (heart_rate == "") {
            values.put("heart_rate", "0")
        } else {
            values.put("heart_rate", heart_rate)
        }
        if (respiratory_rate == "") {
            values.put("respiratory_rate", "0")
        } else {
            values.put("respiratory_rate", respiratory_rate)
        }
        if (weight == "") {
            values.put("weight", "90")
        } else {
            values.put("weight", height)
        }
        if (height == "") {
            values.put("height", "170")
        } else {
            values.put("height", height)
        }

//        values.put("heart_rate", heart_rate.toFloatOrNull() ?: 0.0f)
//        values.put("respiratory_rate", respiratory_rate.toFloatOrNull() ?: 0.0f)
//        values.put("weight", weight.toFloatOrNull() ?: 0.0f)
//        values.put("height", height.toFloatOrNull() ?: 0.0f)

        Log.d("insertOrUpdateDatabaseEntry", "Primary Key Date = $uniqueDate heart_rate: $heart_rate, respiratory_rate: $respiratory_rate, weight: $weight, height: $height")

        if (cursor.count > 0) {
            // Entry with the vitals_id already exists, update it
            db.update(
                MyDatabaseHelper.TABLE_NAME_USER_METRICS,
                values,
                "${MyDatabaseHelper.COLUMN_NAME_DATE}=?",
                arrayOf(uniqueDate)
            )
            Toast.makeText(baseContext, "Entry with today's date already exists therefore the corresponding row has been updated", Toast.LENGTH_LONG).show()
        } else {
            values.put("date", uniqueDate)
            // Entry with the vitals_id doesn't exist, insert a new record
            db.insert(MyDatabaseHelper.TABLE_NAME_USER_METRICS, null, values)
            Toast.makeText(baseContext, "Entry with today's date doesn't exist therefore inserted a new record", Toast.LENGTH_LONG).show()
        }

        cursor.close()
        db.close()
    }

    fun generateUniqueId(): String {
        // Get current timestamp in milliseconds
        val timestamp = System.currentTimeMillis()

        // Generate a random UUID
        val randomUUID = UUID.randomUUID()

        val currentDate = LocalDate.now(ZoneId.systemDefault())
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        var formattedDate = currentDate.format(formatter)
        formattedDate = formattedDate.toString()

        return formattedDate
    }

    private fun calculateRespiratoryRate():Int {
        var previousValue = 0f
        var currentValue = 0f
        previousValue = 10f
        var k = 0
        println("x size : " + accelValuesX.size)
        println("Y size : " + accelValuesY.size)
        println("Z size : " + accelValuesZ.size)
        for (i in 11 until accelValuesX.size) {
            currentValue = kotlin.math.sqrt(
                accelValuesZ[i].toDouble().pow(2.0) + accelValuesX[i].toDouble().pow(2.0) + accelValuesY[i].toDouble()
                    .pow(2.0)
            ).toFloat()
            if (abs(x = previousValue - currentValue) > 0.10) {
                k++
            }
            previousValue=currentValue
        }
        val ret= (k/45.00)
        return (ret*30).toInt()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CAMERA_ACTIVITY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val videoUri = data?.getStringExtra("videoUri")
                if (videoUri != null) {
                    Toast.makeText(baseContext, "Calculating heart rate", Toast.LENGTH_SHORT).show()
                    val path = convertMediaUriToPath(Uri.parse(videoUri))
                    slowTask.execute(path)

//                    heartRateText.text = data?.getStringExtra("heartRate")
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(baseContext, "Video not recorded", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun convertMediaUriToPath(uri: Uri?): String {
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri!!, proj, null, null, null)
        val column_index =
            cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        val path = cursor.getString(column_index)
        cursor.close()
        return path
    }

    inner class SlowTask : AsyncTask<String, String, String?>() {

        public override fun doInBackground(vararg params: String?): String? {
            Log.d("MainActivity", "Executing slow task in background")
            var m_bitmap: Bitmap? = null
            var retriever = MediaMetadataRetriever()
            var frameList = ArrayList<Bitmap>()
            try {
                retriever.setDataSource(params[0])
                var duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT)
                var aduration = duration!!.toInt()
                var i = 10
                while (i < aduration) {
                    val bitmap = retriever.getFrameAtIndex(i)
                    frameList.add(bitmap!!)
                    i += 5
                }
            } catch (m_e: Exception) {
            } finally {
                retriever?.release()
                var redBucket: Long = 0
                var pixelCount: Long = 0
                val a = mutableListOf<Long>()
                for (i in frameList) {
                    redBucket = 0
                    i.width
                    i.height
                    for (y in 0 until i.height) {
                        for (x in 0 until i.width) {
                            val c: Int = i.getPixel(x, y)
                            pixelCount++
                            redBucket += Color.red(c) + Color.blue(c) + Color.green(c)
                        }
                    }
                    a.add(redBucket)
                }
                val b = mutableListOf<Long>()
                for (i in 0 until a.lastIndex - 5) {
                    var temp =
                        (a.elementAt(i) + a.elementAt(i + 1) + a.elementAt(i + 2)
                                + a.elementAt(i + 3) + a.elementAt(i + 4)) / 4
                    b.add(temp)
                }
                var x = b.elementAt(0)
                var count = 0
                for (i in 1 until b.lastIndex) {
                    var p=b.elementAt(i.toInt())
                    if ((p-x) > 3500) {
                        count = count + 1
                    }
                    x = b.elementAt(i.toInt())
                }
                var rate = ((count.toFloat() / 45) * 60).toInt()
                return (rate/2).toString()
            }
        }
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (result != null) {
                Toast.makeText(baseContext, "Heart rate calculation completed : $result", Toast.LENGTH_SHORT).show()
                heartRateText.text = result
            }
        }
    }
}