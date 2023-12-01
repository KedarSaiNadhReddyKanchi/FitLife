package com.application.fitlife

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.application.bhealthy.data.Workout
import com.application.fitlife.data.MyDatabaseHelper
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.random.Random

class FuzzyLogicControllerWorkoutSuggestions {

    companion object {

        private const val USER_HEIGHT = "height"
        private const val USER_WEIGHT = "weight"
        private const val USER_HEART_RATE = "heartRate"
        private const val USER_RESPIRATORY_RATE = "respiratoryRate"

        // Format a Date object to a String using the specified pattern
        fun suggestWorkouts(db: SQLiteDatabase, muscleGroups: List<String>, workoutTypes: List<String>, userMetrics: Map<String, String>, sessionId: String): List<Long> {
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

            val bmiWeight = 0.4
            val heartRateWeight = 0.3
            val respiratoryRateWeight = 0.3

            val overallRating = (bmiRating * bmiWeight + heartRateRating * heartRateWeight + respiratoryRateRating * respiratoryRateWeight)
            val selectedWorkouts: List<Workout> = getWorkoutsByFiltersAndRating(db, muscleGroups, workoutTypes, overallRating)
            for (workout in selectedWorkouts) {
                println("Workout ID: ${workout.id} Title: ${workout.title} ${workout.description}")
            }
            return insertWorkoutSuggestions(db, sessionId, selectedWorkouts)
        }

        private fun insertWorkoutSuggestions(db: SQLiteDatabase, sessionId: String, selectedWorkouts: List<Workout>): List<Long> {
            val contentValues = ContentValues()
            contentValues.put(MyDatabaseHelper.COLUMN_NAME_SESSION_ID, sessionId)
            contentValues.put(MyDatabaseHelper.COLUMN_NAME_DATE, LocalDate.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd")))

            val insertedSuggestionIds = mutableListOf<Long>()

            for (workout in selectedWorkouts) {
                contentValues.put(MyDatabaseHelper.COLUMN_NAME_WORKOUT_ID, workout.id)
//                contentValues.put(MyDatabaseHelper.COLUMN_NAME_SCORE, 0.0)  // You may adjust the default score
                contentValues.put(MyDatabaseHelper.COLUMN_NAME_SCORE, Random.nextInt(101))
                contentValues.put(MyDatabaseHelper.COLUMN_NAME_IS_PERFORMED, 0)  // Assuming 0 for not performed

                val suggestionId = db.insert(MyDatabaseHelper.TABLE_NAME_WORKOUT_SUGGESTIONS, null, contentValues)
                if (suggestionId != -1L) {
                    insertedSuggestionIds.add(suggestionId)
                }
            }

            return insertedSuggestionIds
        }

        private fun getWorkoutsByFiltersAndRating(db: SQLiteDatabase, bodyParts: List<String>, types: List<String>, targetRating: Double): List<Workout> {
            val bodyPartsCondition = if (bodyParts.isNotEmpty()) {
                "AND ${MyDatabaseHelper.COLUMN_NAME_BODYPART} IN (${bodyParts.joinToString(", ") { "'$it'" }})"
            } else {
                "" // Empty string when the list is empty
            }

            val typesCondition = if (types.isNotEmpty()) {
                "AND ${MyDatabaseHelper.COLUMN_NAME_TYPE} IN (${types.joinToString(", ") { "'$it'" }})"
            } else {
                "" // Empty string when the list is empty
            }

            val lowerBound = targetRating - 2.0
            val upperBound = targetRating + 2.0

            println("Running query to fetch workouts matching user preferences")

            val query = """
                SELECT * FROM ${MyDatabaseHelper.TABLE_NAME_WORKOUTS_INFORMATION} 
                WHERE 1 = 1
                $bodyPartsCondition
                $typesCondition
                AND ${MyDatabaseHelper.COLUMN_NAME_RATING} >= $lowerBound AND ${MyDatabaseHelper.COLUMN_NAME_RATING} <= $upperBound
                """.trimIndent()

            val cursor = db.rawQuery(query, null)
            val workoutsByMuscleGroup = mutableMapOf<String, MutableList<Workout>>()

            while (cursor.moveToNext()) {
                val workout = Workout(
                    id = cursor.getLong(0),
                    title = cursor.getString(1),
                    description = cursor.getString(2),
                    type = cursor.getString(3),
                    bodyPart = cursor.getString(4),
                    equipment = cursor.getString(5),
                    level = cursor.getString(6),
                    rating = cursor.getString(7),
                    ratingDesc = cursor.getString(8),
                )
                workoutsByMuscleGroup.computeIfAbsent(workout.bodyPart) { mutableListOf() }.add(workout)
            }

            val totalExercisesToSelect = 10.0
            val exercisesPerGroup = if (workoutsByMuscleGroup.isNotEmpty()) {
                ceil(totalExercisesToSelect / workoutsByMuscleGroup.size)
            } else {
                0
            }

            // Randomly select proportional number of exercises from each group
            val selectedWorkouts = workoutsByMuscleGroup.flatMap { (_, workouts) ->
                workouts.shuffled().take(exercisesPerGroup.toInt())
            }.take(totalExercisesToSelect.toInt())

            return selectedWorkouts
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