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
        const val TABLE_NAME_USER_VITALS = "userVitals"
        const val COLUMN_NAME_TITLE = "title"
        const val COLUMN_NAME_VITALS_ID = "vitals_id"
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
    }

    override fun onCreate(db: SQLiteDatabase?) {

       println("Creating database and tables required")
      
        val workoutsInformation = "CREATE TABLE $TABLE_NAME_WORKOUTS_INFORMATION (" +
        val createTableQuery = "CREATE TABLE $TABLE_NAME_USER_VITALS (" +
                "$COLUMN_NAME_WORKOUT_ID INTEGER PRIMARY KEY, " +
                "$COLUMN_NAME_VITALS_ID TEXT PRIMARY KEY, " +
                "$COLUMN_NAME_TITLE TEXT," +
                "$COLUMN_NAME_DESCRIPTION TEXT," +
                "$COLUMN_NAME_TYPE TEXT," +
                "$COLUMN_NAME_HEIGHT REAL," +
                "$COLUMN_NAME_BODYPART TEXT," +
                "$COLUMN_NAME_EQUIPMENT TEXT," +
                "$COLUMN_NAME_WEIGHT REAL," +
                "$COLUMN_NAME_LEVEL TEXT," +
                "$COLUMN_NAME_RATING REAL," +
                "$COLUMN_NAME_SCORE REAL," +
                "$COLUMN_NAME_HEART_RATE REAL," +
                "$COLUMN_NAME_RATING_DESC TEXT)"
                "$COLUMN_NAME_RESPIRATORY_RATE REAL)"
        db?.execSQL(createTableQuery)
        
        val workoutSuggestions = "CREATE TABLE $TABLE_NAME_WORKOUT_SUGGESTIONS (" +
        val createTableQueryForUserDetails = "CREATE TABLE $USER_TABLE_NAME (" +
                "$COLUMN_NAME_SUGGESTION_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_NAME_SESSION_ID TEXT," +
                "$COLUMN_NAME_USER_ID TEXT PRIMARY KEY, " +
                "$COLUMN_NAME_DATE TEXT," +
                "$COLUMN_NAME_EXERCISE_ID INTEGER," +
                "$COLUMN_NAME_SCORE REAL," +
                "$COLUMN_NAME_AGE REAL," +
                "$COLUMN_NAME_IS_PERFORMED INTEGER," +
                "$COLUMN_NAME_USERNAME TEXT)"
                "FOREIGN KEY($COLUMN_NAME_EXERCISE_ID) REFERENCES $TABLE_NAME_WORKOUTS_INFORMATION($COLUMN_NAME_WORKOUT_ID))"
        db?.execSQL(workoutsInformation)
        insertDataFromCsvIfNotLoaded(db, WORKOUTS_INFORMATION_FILENAME)
        db?.execSQL(workoutSuggestions)
        
        val createTableQuery = "CREATE TABLE $TABLE_NAME_USER_VITALS (" +
                "$COLUMN_NAME_VITALS_ID TEXT PRIMARY KEY, " +
                "$COLUMN_NAME_HEIGHT REAL," +
                "$COLUMN_NAME_WEIGHT REAL," +
                "$COLUMN_NAME_SCORE REAL," +
                "$COLUMN_NAME_HEART_RATE REAL," +
                "$COLUMN_NAME_RESPIRATORY_RATE REAL)"
        db?.execSQL(createTableQuery)

        val createTableQueryForUserDetails = "CREATE TABLE $USER_TABLE_NAME (" +
                "$COLUMN_NAME_USER_ID TEXT PRIMARY KEY, " +
                "$COLUMN_NAME_AGE REAL," +
                "$COLUMN_NAME_USERNAME TEXT)"
        db?.execSQL(createTableQueryForUserDetails)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_USER_VITALS")
        db?.execSQL("DROP TABLE IF EXISTS $USER_TABLE_NAME")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_WORKOUTS_INFORMATION")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_WORKOUT_SUGGESTIONS")
        onCreate(db)
    }


}
