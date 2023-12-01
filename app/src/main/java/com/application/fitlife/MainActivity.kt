package com.application.fitlife

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.application.fitlife.data.MyDatabaseHelper

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val dbHelper = MyDatabaseHelper(this)
        val db = dbHelper.writableDatabase

        val userMetricsMap = mapOf(FuzzyLogicControllerWorkoutSuggestions.USER_HEIGHT to "170",
            FuzzyLogicControllerWorkoutSuggestions.USER_WEIGHT to "70",
            FuzzyLogicControllerWorkoutSuggestions.USER_HEART_RATE to "72",
            FuzzyLogicControllerWorkoutSuggestions.USER_RESPIRATORY_RATE to "15")
        // Create a list from four strings
        val muscleGroups: List<String> = listOf("Chest", "Biceps")
        val workoutTypes: List<String> = listOf("Strength", "Stretching")
        val result = FuzzyLogicControllerWorkoutSuggestions.suggestWorkouts(db, muscleGroups, workoutTypes, userMetricsMap, "sessionId")
        println(result)
    }
}