package com.application.fitlife

import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.application.fitlife.FuzzyLogicControllerWorkoutSuggestions.Companion.suggestWorkouts
import com.application.fitlife.data.MyDatabaseHelper
import com.application.fitlife.data.Workout
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

class ShowExercise<SQLiteDatabase> : AppCompatActivity() {
    companion object {
        const val USER_HEIGHT = "height"
        const val USER_WEIGHT = "weight"
        const val USER_HEART_RATE = "heartRate"
        const val USER_RESPIRATORY_RATE = "respiratoryRate"
    }

    private lateinit var muscleGroupSpinner: Spinner
    private lateinit var showExercisesButton: Button
    private lateinit var showExercisesText: TextView
    private lateinit var submitButton: Button
    private lateinit var selectedMuscleGroupsText: TextView
    private lateinit var selectedExerciseTypesText: TextView
    private lateinit var selectMuscleGroupsButton: Button
    private lateinit var exercisesList: RecyclerView
    private lateinit var exercisesAdapter: ExercisesAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var workoutAdapter: WorkoutCheckboxAdapter
    private lateinit var selectExerciseTypeButton: Button
    private val exerciseTypes by lazy { resources.getStringArray(R.array.exercise_types) }
    private var selectedExerciseTypes: BooleanArray? = null
    private val muscleGroups by lazy { resources.getStringArray(R.array.muscle_groups) }
    private var selectedMuscleGroups: BooleanArray? = null
    private val selectedExercises = mutableListOf<Exercise>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_exercise)
        val sessionId = intent.getStringExtra(getString(R.string.unique_id))?: "default_value"

        selectMuscleGroupsButton = findViewById(R.id.selectMuscleGroupsButton)
        showExercisesButton = findViewById(R.id.showExercisesButton)
        selectedMuscleGroupsText = findViewById(R.id.selectedMuscleGroupsText)
        selectedExerciseTypesText = findViewById(R.id.selectedExerciseTypesText)
        //showExercisesText = findViewById(R.id.showExercisesText)
        //exercisesList = findViewById(R.id.exercisesList)
        selectedMuscleGroups = BooleanArray(muscleGroups.size)
        selectExerciseTypeButton = findViewById(R.id.selectExerciseTypeButton)

        recyclerView = findViewById(R.id.exercisesList)
        recyclerView.layoutManager = LinearLayoutManager(this)

        selectedExerciseTypes = BooleanArray(exerciseTypes.size)

        selectExerciseTypeButton.setOnClickListener {
            showExerciseTypeDialog()
        }

        //setupSpinner()

        submitButton = findViewById(R.id.submitExercisesButton)
        selectMuscleGroupsButton.setOnClickListener {
            showMuscleGroupDialog()
        }

        val dbHelper = MyDatabaseHelper(this)
        val db = dbHelper.writableDatabase

        showExercisesButton.setOnClickListener {
            //displaySelectedExercises()
            val muscleGroupSelections = muscleGroups.filterIndexed { index, _ ->
                selectedMuscleGroups?.get(index) == true
            }
            //Toast.makeText(this, "Selected Exercises: $muscleGroupSelections", Toast.LENGTH_LONG).show()
            val exerciseTypeSelections = exerciseTypes.filterIndexed { index, _ ->
                selectedExerciseTypes?.get(index) == true
            }

            val userMetrics = MyDatabaseHelper.getUserMetrics(db, LocalDate.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd")))

            val suggestedWorkoutIds = suggestWorkouts(
                db,
                muscleGroupSelections,
                exerciseTypeSelections,
                userMetrics,
                sessionId
            )

            //Toast.makeText(this, "Number of Suggested workout Ids: ${suggestedWorkoutIds.size}", Toast.LENGTH_SHORT).show()
            val workouts = getWorkoutsByIds(db, suggestedWorkoutIds)
            Toast.makeText(this, "Number of workouts: ${workouts.size}", Toast.LENGTH_SHORT).show()
            workoutAdapter = WorkoutCheckboxAdapter(workouts)
            recyclerView.adapter = workoutAdapter
//            val adapter = WorkoutAdapter(workouts)
//            recyclerView.adapter = adapter
            //displayWorkouts(workouts)

            //Toast.makeText(this, "$suggestedWorkoutIds.size()", Toast.LENGTH_LONG).show();
            // Do something with the suggestedWorkoutIds, like displaying them
//            FuzzyLogicControllerWorkoutSuggestions.showSelectedExercises()
        }

        submitButton.setOnClickListener {
            updateSuggestionScore(db , sessionId)

        }

//        exercisesAdapter = ExercisesAdapter(emptyList())
//        exercisesList.layoutManager = LinearLayoutManager(this)
//        exercisesList.adapter = exercisesAdapter
    }

    private fun displayWorkouts(workouts: List<Workout>) {
        //: ${workout.description}Type: ${workout.type}, Body Part: ${workout.bodyPart}
        val workoutDetails = workouts.map { workout ->
            "${workout.title}"
        }

        // Assuming you have a TextView or any other component to display the workouts
        val workoutsDisplay = showExercisesText // Replace with your actual TextView ID
        workoutsDisplay.text = workoutDetails.joinToString("\n\n")
    }

    private fun getWorkoutsByIds(db: android.database.sqlite.SQLiteDatabase, ids: List<Long>): List<Workout> {
        val workouts = mutableListOf<Workout>()
        // Convert the List<Long> of workout IDs to a comma-separated string for the SQL query
        val workoutIdList = ids.joinToString(separator = ", ")
        //Toast.makeText(this, "Workout ID list length: ${workoutIdList.length}", Toast.LENGTH_SHORT).show()
        Log.d("workoutId", workoutIdList)
        Toast.makeText(this, "All the Ids: $workoutIdList", Toast.LENGTH_SHORT).show()

        // SQL query to retrieve exercises
        val query = """
            SELECT wi.*, ws.${MyDatabaseHelper.COLUMN_NAME_SUGGESTION_ID}, ws.${MyDatabaseHelper.COLUMN_NAME_SCORE}
            FROM ${MyDatabaseHelper.TABLE_NAME_WORKOUT_SUGGESTIONS} ws
            JOIN ${MyDatabaseHelper.TABLE_NAME_WORKOUTS_INFORMATION} wi ON  wi.${MyDatabaseHelper.COLUMN_NAME_WORKOUT_ID} = ws.${MyDatabaseHelper.COLUMN_NAME_EXERCISE_ID}
            WHERE ws.${MyDatabaseHelper.COLUMN_NAME_SUGGESTION_ID} IN ($workoutIdList)
        """

        Log.d("SQLQuery", query)
        Toast.makeText(this, "All the query: $query", Toast.LENGTH_SHORT).show()

        val cursor = db.rawQuery(query, null)
        try {
            while (cursor.moveToNext()) {
                val workout = Workout(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_NAME_WORKOUT_ID)),
                    title = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_NAME_TITLE)),
                    description = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_NAME_DESCRIPTION)),
                    type = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_NAME_TYPE)),
                    bodyPart = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_NAME_BODYPART)),
                    equipment = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_NAME_EQUIPMENT)),
                    level = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_NAME_LEVEL)),
                    rating = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_NAME_RATING)),
                    ratingDesc = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_NAME_RATING_DESC)),
                    score = cursor.getDouble(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_NAME_SCORE)),
                    suggestionId = cursor.getLong(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_NAME_SUGGESTION_ID))
                )
                workouts.add(workout)
            }
        } finally {
            cursor.close() // Ensure the cursor is closed after use
        }
        return workouts
    }

    private fun showExerciseTypeDialog() {
        AlertDialog.Builder(this)
            .setTitle("Select Exercise Types")
            .setMultiChoiceItems(exerciseTypes, selectedExerciseTypes) { _, which, isChecked ->
                selectedExerciseTypes?.set(which, isChecked)
            }
            .setPositiveButton("OK") { dialog, _ ->
                updateSelectedExerciseTypesText()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateSelectedExerciseTypesText() {
        val selectedTypes = exerciseTypes
            .filterIndexed { index, _ -> selectedExerciseTypes?.get(index) == true }
            .joinToString(separator = ", ")

        selectedExerciseTypesText.text = if (selectedTypes.isEmpty()) {
            "Selected Exercise Types: None"
        } else {
            "Selected Exercise Types: $selectedTypes"
        }
    }
    private fun showMuscleGroupDialog() {
        AlertDialog.Builder(this)
            .setTitle("Select Muscle Groups")
            .setMultiChoiceItems(muscleGroups, selectedMuscleGroups) { _, which, isChecked ->
                selectedMuscleGroups?.set(which, isChecked)
            }
            .setPositiveButton("OK") { dialog, _ ->
                //updateExercisesList()
                prepareSelectedExercises()
                updateSelectedMuscleGroupsText()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    private fun updateSelectedMuscleGroupsText() {
        val selectedGroups = muscleGroups
            .filterIndexed { index, _ -> selectedMuscleGroups?.get(index) == true }
            .joinToString(separator = ", ")

        selectedMuscleGroupsText.text = if (selectedGroups.isEmpty()) {
            "Selected Muscle Groups: None"
        } else {
            "Selected Muscle Groups: $selectedGroups"
        }
    }
    private fun displaySelectedExercises() {
        exercisesAdapter.exercises = selectedExercises
        exercisesAdapter.notifyDataSetChanged()
    }

    private fun prepareSelectedExercises() {
        selectedExercises.clear()
        muscleGroups.forEachIndexed { index, muscleGroup ->
            selectedMuscleGroups?.let {
                if (it[index]) {
                    selectedExercises.addAll(getExercisesForMuscleGroup(muscleGroup))
                }
            }
        }
    }

    private fun updateSuggestionScore(db: android.database.sqlite.SQLiteDatabase , sessionId: String) {
        // If workouts are not yet suggested, nothing to update
        if (!::workoutAdapter.isInitialized) {
            return
        }
        // profile the user metrics and rate the user
        val selectedExercises = workoutAdapter.workouts
            .filter { it.isSelected }
            .map { it.id }

        // Iterate through selected exercises and update the score in the original list
        for (suggestionId in selectedExercises) {
            val selectedWorkout = workoutAdapter.workouts.find { it.id == suggestionId }
            selectedWorkout?.let {
                println("Score cxheck ${selectedWorkout.score}")
//                val updateScoreQuery = """
//                        UPDATE ${MyDatabaseHelper.TABLE_NAME_WORKOUT_SUGGESTIONS} SET
//                        ${MyDatabaseHelper.COLUMN_NAME_SCORE} = ${selectedWorkout.score}
//                        WHERE ${MyDatabaseHelper.COLUMN_NAME_SUGGESTION_ID} = ${selectedWorkout.suggestionId}
//                    """
//                println(updateScoreQuery)
                val dbHelper = MyDatabaseHelper(this)
                val db = dbHelper.writableDatabase
                var values = ContentValues()
                values.put("score", selectedWorkout.score)
                db.update(
                    MyDatabaseHelper.TABLE_NAME_WORKOUT_SUGGESTIONS,
                    values,
                    "${MyDatabaseHelper.COLUMN_NAME_SUGGESTION_ID}=?",
                    arrayOf(selectedWorkout.suggestionId.toString())
                )
                //val cursor = db.rawQuery(updateScoreQuery, null)
                //cursor.close()
            }
        }

        val dailyScoreCalculatedForUser = ScoringEngine.calculateScore(db , sessionId)
        Toast.makeText(baseContext, "Today's workout score : $dailyScoreCalculatedForUser", Toast.LENGTH_SHORT).show()

        val uniqueDate = generateUniqueDate();
        val cursor = db.rawQuery("SELECT * FROM ${MyDatabaseHelper.TABLE_NAME_USER_METRICS} WHERE ${MyDatabaseHelper.COLUMN_NAME_DATE}=?", arrayOf(uniqueDate))
        var values = ContentValues()
        values.put("score", dailyScoreCalculatedForUser)
        if (cursor.count > 0) {
            // Entry with the vitals_id already exists, update it
            db.update(
                MyDatabaseHelper.TABLE_NAME_USER_METRICS,
                values,
                "${MyDatabaseHelper.COLUMN_NAME_DATE}=?",
                arrayOf(uniqueDate)
            )
        } else {
            values.put("date", uniqueDate)
            // Entry with the vitals_id doesn't exist, insert a new record
            db.insert(MyDatabaseHelper.TABLE_NAME_USER_METRICS, null, values)
        }
    }

    fun generateUniqueDate(): String {
        val currentDate = LocalDate.now(ZoneId.systemDefault())
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        var formattedDate = currentDate.format(formatter)
        formattedDate = formattedDate.toString()

        return formattedDate
    }

//    private fun setupSpinner() {
//        ArrayAdapter.createFromResource(
//            this,
//            R.array.muscle_groups, // Make sure this array is defined in your resources
//            android.R.layout.simple_spinner_item
//        ).also { adapter ->
//            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//            muscleGroupSpinner.adapter = adapter
//        }
//
//        muscleGroupSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
//                val muscleGroup = parent.getItemAtPosition(position).toString()
//                updateExercisesList(muscleGroup)
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>) {}
//        }
//    }

    private fun updateExercisesList() {
        val exercises = mutableListOf<Exercise>()
        muscleGroups.forEachIndexed { index, muscleGroup ->
            selectedMuscleGroups?.let {
                if (it[index]) {
                    exercises.addAll(getExercisesForMuscleGroup(muscleGroup))
                }
            }
        }
//        exercisesAdapter.exercises = exercises
//        exercisesAdapter.notifyDataSetChanged()

//    private fun updateExercisesList(muscleGroup: String) {
//        val exercises = getExercisesForMuscleGroup(muscleGroup)
//        exercisesAdapter = ExercisesAdapter(exercises)
//        exercisesList.adapter = exercisesAdapter
//        exercisesList.layoutManager = LinearLayoutManager(this)
//    }
    }
}

fun getExercisesForMuscleGroup(muscleGroup: String): List<Exercise> {
    return when (muscleGroup) {
        "Abdominals" -> listOf(Exercise("Bicep Curls"), Exercise("Tricep Dips"), Exercise("Hammer Curls"))
        "Adductors" -> listOf(Exercise("Bench Press"), Exercise("Push Ups"), Exercise("Chest Fly"))
        "Abductors" -> listOf(Exercise("Pull Ups"), Exercise("Deadlifts"), Exercise("Lat Pulldowns"))
        "Biceps" -> listOf(Exercise("Squats"), Exercise("Leg Press"), Exercise("Lunges"))
        "Calves" -> listOf(Exercise("Shoulder Press"), Exercise("Lateral Raises"), Exercise("Front Raises"))
        "Chest" -> listOf(Exercise("Shoulder Press"), Exercise("Lateral Raises"), Exercise("Front Raises"))
        // Add more muscle groups and exercises as needed
        else -> emptyList()
    }
}

data class Exercise(val name: String, var isSelected: Boolean = false)

class WorkoutCheckboxAdapter(val workouts: List<Workout>) :
    RecyclerView.Adapter<WorkoutCheckboxAdapter.WorkoutViewHolder>() {

    class WorkoutViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val workoutCheckBox: CheckBox = view.findViewById(R.id.workoutCheckBox)
        val workoutTitleTextView: TextView = view.findViewById(R.id.workoutTitleTextView)
        val percentageEditText: EditText = view.findViewById(R.id.percentageEditText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.workout_item_checkbox, parent, false)
        return WorkoutViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        val workout = workouts[position]
        holder.workoutTitleTextView.text = workout.title
        holder.workoutCheckBox.isChecked = false // or any logic you want to determine checked state

        // Add any click listener if required
        holder.workoutCheckBox.setOnCheckedChangeListener { _, isChecked ->
            // Handle checkbox check changes if needed
            workout.isSelected = isChecked
        }

        holder.percentageEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                // This method is called to notify you that characters within `charSequence` are about to be replaced.
            }

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                // This method is called to notify you that somewhere within `charSequence` has been changed.
            }

            override fun afterTextChanged(editable: Editable?) {
                // This method is called to notify you that somewhere within `editable` has been changed.
                // This is the ideal place to perform your actions after the text has been changed.
                val enteredText = editable.toString()
                var convertedScore = enteredText.toDoubleOrNull() ?: 0.0
                workout.score = convertedScore
                // Do something with the entered text, e.g., update a variable, trigger some logic, etc.
            }
        })
    }

    override fun getItemCount() = workouts.size
}

class ExercisesAdapter(var exercises: List<Exercise>) :
    RecyclerView.Adapter<ExercisesAdapter.ExerciseViewHolder>() {

    class ExerciseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkBox: CheckBox = view.findViewById(R.id.exerciseCheckBox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.exercise_item, parent, false)
        return ExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val exercise = exercises[position]
        holder.checkBox.text = exercise.name
        holder.checkBox.isChecked = exercise.isSelected

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            exercise.isSelected = isChecked
        }
    }

    override fun getItemCount() = exercises.size
}
