package com.application.fitlife

import kotlin.math.abs
import com.application.fitlife.data.MyDatabaseHelper
import com.application.fitlife.data.WorkoutSuggestion
import android.database.sqlite.SQLiteDatabase
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ScoringEngine {
    companion object{

        const val USER_HEIGHT = "height"
        const val USER_WEIGHT = "weight"
        const val USER_HEART_RATE = "heartRate"
        const val USER_RESPIRATORY_RATE = "respiratoryRate"

        fun calculateScore(db: SQLiteDatabase, userMetrics: Map<String, String>): Double{
            var userBMI = 23.5
            if (userMetrics.containsKey(ScoringEngine.USER_HEIGHT) && userMetrics.containsKey(
                    ScoringEngine.USER_WEIGHT
                )) {
                userBMI = ScoringEngine.calculateBMI(
                    userMetrics.getOrDefault(
                        ScoringEngine.USER_HEIGHT,
                        "0"
                    ).toDouble(), userMetrics.getOrDefault(
                        ScoringEngine.USER_WEIGHT, "0"
                    ).toDouble()
                )
            }
            val bmiRating =
                ScoringEngine.calculateBMIRating(userBMI, 18.5, 29.9)
            val heartRateRating = ScoringEngine.calculateHeartRateRating(
                userMetrics.getOrDefault(
                    ScoringEngine.USER_HEART_RATE,
                    "0"
                ).toDouble(), 60.0, 100.0
            )
            val respiratoryRateRating =
                ScoringEngine.calculateRespiratoryRateRating(
                    userMetrics.getOrDefault(
                        ScoringEngine.USER_RESPIRATORY_RATE, "0"
                    ).toDouble(), 12.0, 20.0
                )
            val bmiWeight = 0.2
            val heartRateWeight = 0.4
            val respiratoryRateWeight = 0.4

            val overallRating = (bmiRating * bmiWeight + heartRateRating * heartRateWeight + respiratoryRateRating * respiratoryRateWeight)

            val workoutSuggestions = getPerformedExercisesByDate(db, LocalDate.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd")))
            val top10Exercise = workoutSuggestions.slice(0 until 10)
            val averageScoreTop10 = top10Exercise
                .map { it.score.toInt() }
                .toList()
                .average()
            val totalAvg = workoutSuggestions
                .map { it.score.toInt() }
                .toList()
                .average()

            val restAvg = (totalAvg * workoutSuggestions.size - averageScoreTop10 * top10Exercise.size) / (workoutSuggestions.size - top10Exercise.size)
            return 0.0
        }

        private fun getPerformedExercisesByDate(db: SQLiteDatabase, targetDate: String): List<WorkoutSuggestion> {
            val query = """
                        SELECT *
                        FROM ${MyDatabaseHelper.TABLE_NAME_WORKOUT_SUGGESTIONS} 
                        WHERE ${MyDatabaseHelper.COLUMN_NAME_DATE} = '$targetDate'
                        ORDER BY ${MyDatabaseHelper.COLUMN_NAME_SCORE} DESC
                    """.trimIndent()

            val cursor = db.rawQuery(query, null)

            val workoutSuggestions = mutableListOf<WorkoutSuggestion>()

            while (cursor.moveToNext()) {
                val suggestion = WorkoutSuggestion(
                    id = cursor.getLong(0),
                    sessionId = cursor.getString(1),
                    date = cursor.getString(2),
                    exerciseId = cursor.getString(3),
                    score = cursor.getString(4),
                    isPerformed = cursor.getString(5)
                )
                workoutSuggestions.add(suggestion)
            }

            cursor.close()

            return workoutSuggestions
        }

        private fun calculateBMI(height: Double, weight: Double): Double {
            if (height == 0.0 || weight == 0.0)
                return 23.5

            return (weight * 100 * 100) / (height * height)
        }

        private fun calculateBMIRating(value: Double, lowerBound: Double, upperBound: Double): Double {
            val peakValue = 23.0
            val rating = 10.0 - 9.0 * (abs(value - peakValue) * 1.5 / (upperBound - lowerBound))
            return rating.coerceIn(1.0, 10.0)
        }

        private fun calculateHeartRateRating(value: Double, lowerBound: Double, upperBound: Double): Double {
            val peakValue = 72
            val rating = 10.0 - 9.0 * (abs(value - peakValue) * 1.5 / (upperBound - lowerBound))
            return rating.coerceIn(1.0, 10.0)
        }

        private fun calculateRespiratoryRateRating(value: Double, lowerBound: Double, upperBound: Double): Double {
            val peakValue = 15
            val rating = 10.0 - 9.0 * (abs(value - peakValue) * 1.5 / (upperBound - lowerBound))
            return rating.coerceIn(1.0, 10.0)
        }
    }
}