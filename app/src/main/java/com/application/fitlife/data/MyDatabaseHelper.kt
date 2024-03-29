package com.application.fitlife.data

import AppPreferences
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import com.opencsv.CSVReaderBuilder
import java.io.InputStreamReader



class MyDatabaseHelper(private val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    // Table contents are grouped together in an anonymous object.
    companion object FitLife : BaseColumns {
        const val DATABASE_NAME = "fit_life_db"
        const val DATABASE_VERSION = 1

        const val TABLE_NAME_WORKOUT_SUGGESTIONS = "workout_suggestions"
        const val WORKOUTS_INFORMATION_FILENAME = "WorkoutsInformation.csv"
        const val TABLE_NAME_WORKOUTS_INFORMATION = "workouts_information"
        const val TABLE_NAME_USER_METRICS = "userMetrics"
        const val COLUMN_NAME_TITLE = "title"
        const val COLUMN_NAME_DESCRIPTION = "description"
        const val COLUMN_NAME_HEART_RATE = "heart_rate"
        const val COLUMN_NAME_WORKOUT_ID = "workout_id"
        const val COLUMN_NAME_DATE = "date"
        const val COLUMN_NAME_TYPE = "type"
        const val COLUMN_NAME_BODYPART = "bodyPart"
        const val COLUMN_NAME_EQUIPMENT = "equipment"
        const val COLUMN_NAME_LEVEL = "level"
        const val COLUMN_NAME_RATING = "rating"
        const val COLUMN_NAME_RATING_DESC = "ratingDesc"
        const val USER_TABLE_NAME = "userDetails"
        const val COLUMN_NAME_IS_PERFORMED = "is_performed"
        const val COLUMN_NAME_SUGGESTION_ID = "suggestion_id"
        const val COLUMN_NAME_SESSION_ID = "session_id"
        const val COLUMN_NAME_RESPIRATORY_RATE = "respiratory_rate"
        const val COLUMN_NAME_SCORE = "score"
        const val COLUMN_NAME_WEIGHT = "weight"
        const val COLUMN_NAME_HEIGHT = "height"
        const val COLUMN_NAME_EXERCISE_ID = "workout_id"
        const val COLUMN_NAME_USER_ID = "user_id"
        const val COLUMN_NAME_AGE = "age"
        const val COLUMN_NAME_USERNAME = "user_name"

        fun getUserMetrics(db: SQLiteDatabase, targetDate: String): UserMetrics {
            val query = """
                        SELECT *
                        FROM ${MyDatabaseHelper.TABLE_NAME_USER_METRICS} 
                        WHERE ${MyDatabaseHelper.COLUMN_NAME_DATE} = '$targetDate'
                    """.trimIndent()

            val cursor = db.rawQuery(query, null)

            val userMetrics = UserMetrics(
                id = 0,
                height = 0.0,
                weight = 0.0,
                score = 0.0,
                heartRate = 0.0,
                respiratoryRate = 0.0
            )

            while (cursor.moveToNext()) {
                val userMetrics = UserMetrics(
                    id = cursor.getLong(0),
                    height = cursor.getDouble(1),
                    weight = cursor.getDouble(2),
                    score = cursor.getDouble(3),
                    heartRate = cursor.getDouble(4),
                    respiratoryRate = cursor.getDouble(5)
                )
            }

            cursor.close()

            return userMetrics
        }
    }

    override fun onCreate(db: SQLiteDatabase?) {

        println("Creating database and tables required")

        val createUserMetricsTableQuery = "CREATE TABLE $TABLE_NAME_USER_METRICS (" +
                "$COLUMN_NAME_DATE TEXT PRIMARY KEY, " +
                "$COLUMN_NAME_HEIGHT REAL," +
                "$COLUMN_NAME_WEIGHT REAL," +
                "$COLUMN_NAME_SCORE REAL," +
                "$COLUMN_NAME_HEART_RATE REAL," +
                "$COLUMN_NAME_RESPIRATORY_RATE REAL)"
        db?.execSQL(createUserMetricsTableQuery)

        val createUserDetailsTableQuery = "CREATE TABLE $USER_TABLE_NAME (" +
                "$COLUMN_NAME_USER_ID TEXT PRIMARY KEY, " +
                "$COLUMN_NAME_AGE REAL," +
                "$COLUMN_NAME_USERNAME TEXT)"
        db?.execSQL(createUserDetailsTableQuery)

        val workoutsInformationTableQuery = "CREATE TABLE $TABLE_NAME_WORKOUTS_INFORMATION (" +
                "$COLUMN_NAME_WORKOUT_ID INTEGER PRIMARY KEY, " +
                "$COLUMN_NAME_TITLE TEXT," +
                "$COLUMN_NAME_DESCRIPTION TEXT," +
                "$COLUMN_NAME_TYPE TEXT," +
                "$COLUMN_NAME_BODYPART TEXT," +
                "$COLUMN_NAME_EQUIPMENT TEXT," +
                "$COLUMN_NAME_LEVEL TEXT," +
                "$COLUMN_NAME_RATING REAL," +
                "$COLUMN_NAME_RATING_DESC TEXT)"

        val workoutSuggestionsTableQuery = "CREATE TABLE $TABLE_NAME_WORKOUT_SUGGESTIONS (" +
                "$COLUMN_NAME_SUGGESTION_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_NAME_SESSION_ID TEXT," +
                "$COLUMN_NAME_DATE TEXT," +
                "$COLUMN_NAME_EXERCISE_ID INTEGER," +
                "$COLUMN_NAME_SCORE REAL," +
                "$COLUMN_NAME_IS_PERFORMED INTEGER," +
                "FOREIGN KEY($COLUMN_NAME_EXERCISE_ID) REFERENCES $TABLE_NAME_WORKOUTS_INFORMATION($COLUMN_NAME_WORKOUT_ID))"

        db?.execSQL(workoutsInformationTableQuery)
        insertDataFromCsvIfNotLoaded(db, WORKOUTS_INFORMATION_FILENAME)
        db?.execSQL(workoutSuggestionsTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_USER_METRICS")
        db?.execSQL("DROP TABLE IF EXISTS $USER_TABLE_NAME")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_WORKOUTS_INFORMATION")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_WORKOUT_SUGGESTIONS")
        onCreate(db)
    }
    
    fun readCsvFile(fileName: String): List<Array<String>> {
        context.assets.open(fileName).use { inputStream ->
            InputStreamReader(inputStream).use { inputStreamReader ->
                return CSVReaderBuilder(inputStreamReader)
                    .withSkipLines(1)
                    .build()
                    .readAll()
            }
        }
    }
    
    fun insertDataFromCsvIfNotLoaded(db: SQLiteDatabase?, fileName: String) {
        val preferences = AppPreferences(context)
        println("Loading workout data to app")
        if (!preferences.isDataLoaded()) {
            val csvData = readCsvFile(fileName)
            for (row in csvData) {
                val values = ContentValues().apply {
                    put(COLUMN_NAME_WORKOUT_ID, row[0])
                    put(COLUMN_NAME_TITLE, row[1])
                    put(COLUMN_NAME_DESCRIPTION, row[2])
                    put(COLUMN_NAME_TYPE, row[3])
                    put(COLUMN_NAME_BODYPART, row[4])
                    put(COLUMN_NAME_EQUIPMENT, row[5])
                    put(COLUMN_NAME_LEVEL, row[6])
                    put(COLUMN_NAME_RATING, row[7])
                    put(COLUMN_NAME_RATING_DESC, row[8])
                }
                db?.insert(TABLE_NAME_WORKOUTS_INFORMATION, null, values)
            }
            // Mark data as loaded
            preferences.setDataLoaded(true)
        }
    }

}
