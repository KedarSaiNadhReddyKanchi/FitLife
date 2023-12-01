package com.application.fitlife.data


import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns


class MyDatabaseHelper(private val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    // Table contents are grouped together in an anonymous object.
    companion object FitLife : BaseColumns {
        const val DATABASE_NAME = "fit_life_db"
        const val DATABASE_VERSION = 1

        const val TABLE_NAME_WORKOUT_SUGGESTIONS = "workout_suggestions"

        const val TABLE_NAME_USER_VITALS = "userVitals"
        const val COLUMN_NAME_TITLE = "title"
        const val COLUMN_NAME_VITALS_ID = "vitals_id"
        const val COLUMN_NAME_DESCRIPTION = "description"
        const val COLUMN_NAME_HEART_RATE = "heart_rate"

        const val COLUMN_NAME_DATE = "date"

        const val COLUMN_NAME_RESPIRATORY_RATE = "respiratory_rate"
        const val COLUMN_NAME_SCORE = "score"
        const val COLUMN_NAME_WEIGHT = "weight"
        const val COLUMN_NAME_HEIGHT = "height"

        const val COLUMN_NAME_USER_ID = "user_id"
        const val COLUMN_NAME_AGE = "age"
        const val COLUMN_NAME_USERNAME = "user_name"
    }

    override fun onCreate(db: SQLiteDatabase?) {

       

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

        onCreate(db)
    }


}
