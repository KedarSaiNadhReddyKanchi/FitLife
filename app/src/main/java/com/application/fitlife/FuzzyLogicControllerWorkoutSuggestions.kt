package com.application.fitlife

import android.database.sqlite.SQLiteDatabase
import kotlin.math.abs

class FuzzyLogicControllerWorkoutSuggestions {

    companion object {

        const val USER_HEIGHT = "height"
        const val USER_WEIGHT = "weight"
        const val USER_HEART_RATE = "heartRate"
        const val USER_RESPIRATORY_RATE = "respiratoryRate"

        // Format a Date object to a String using the specified pattern
        fun suggestWorkouts(db: SQLiteDatabase, muscleGroups: List<String>, workoutTypes: List<String>, userMetrics: Map<String, String>, sessionId: String) {
            // profile the user metrics and rate the user
            // Calculate user BMI
            var userBMI = 23.5
            if (userMetrics.containsKey(USER_HEIGHT) && userMetrics.containsKey(USER_WEIGHT)) {
                userBMI = calculateBMI(
                    userMetrics.getOrDefault(USER_HEIGHT, "0").toDouble(), userMetrics.getOrDefault(
                        USER_WEIGHT, "0"
                    ).toDouble()
                )
            }
            val bmiRating = calculateBMIRating(userBMI, 18.5, 29.9)
            val heartRateRating = calculateHeartRateRating(
                userMetrics.getOrDefault(USER_HEART_RATE, "0").toDouble(),
                60.0,
                100.0
            )
            val respiratoryRateRating = calculateRespiratoryRateRating(
                userMetrics.getOrDefault(
                    USER_RESPIRATORY_RATE, "0"
                ).toDouble(), 12.0, 20.0
            )
        }

        /**Underweight = <18.5
        Normal weight = 18.5–24.9
        Overweight = 25–29.9
        Obesity = BMI of 30 or greater */
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