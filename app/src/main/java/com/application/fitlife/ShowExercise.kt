package com.application.fitlife

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.application.fitlife.FuzzyLogicControllerWorkoutSuggestions.Companion.suggestWorkouts
import com.application.fitlife.data.MyDatabaseHelper

class ShowExercise : AppCompatActivity() {
    companion object {
        const val USER_HEIGHT = "height"
        const val USER_WEIGHT = "weight"
        const val USER_HEART_RATE = "heartRate"
        const val USER_RESPIRATORY_RATE = "respiratoryRate"
    }

    private lateinit var muscleGroupSpinner: Spinner
    private lateinit var showExercisesButton: Button
    private lateinit var submitButton: Button
    private lateinit var selectedMuscleGroupsText: TextView
    private lateinit var selectedExerciseTypesText: TextView
    private lateinit var selectMuscleGroupsButton: Button
    private lateinit var exercisesList: RecyclerView
    private lateinit var exercisesAdapter: ExercisesAdapter
    private lateinit var selectExerciseTypeButton: Button
    private val exerciseTypes by lazy { resources.getStringArray(R.array.exercise_types) }
    private var selectedExerciseTypes: BooleanArray? = null
    private val muscleGroups by lazy { resources.getStringArray(R.array.muscle_groups) }
    private var selectedMuscleGroups: BooleanArray? = null
    private val selectedExercises = mutableListOf<Exercise>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_exercise)

        selectMuscleGroupsButton = findViewById(R.id.selectMuscleGroupsButton)
        showExercisesButton = findViewById(R.id.showExercisesButton)
        selectedMuscleGroupsText = findViewById(R.id.selectedMuscleGroupsText)
        selectedExerciseTypesText = findViewById(R.id.selectedExerciseTypesText)
        exercisesList = findViewById(R.id.exercisesList)
        selectedMuscleGroups = BooleanArray(muscleGroups.size)
        selectExerciseTypeButton = findViewById(R.id.selectExerciseTypeButton)

        selectedExerciseTypes = BooleanArray(exerciseTypes.size)

        selectExerciseTypeButton.setOnClickListener {
            showExerciseTypeDialog()
        }

        //setupSpinner()

        submitButton = findViewById(R.id.submitExercisesButton)
        selectMuscleGroupsButton.setOnClickListener {
            showMuscleGroupDialog()
        }

        showExercisesButton.setOnClickListener {
            //displaySelectedExercises()
            val muscleGroupSelections = muscleGroups.filterIndexed { index, _ ->
                selectedMuscleGroups?.get(index) == true
            }
            val exerciseTypeSelections = exerciseTypes.filterIndexed { index, _ ->
                selectedExerciseTypes?.get(index) == true
            }
            val userMetrics = mapOf(
                USER_HEIGHT to "180",  // Replace with actual user height
                USER_WEIGHT to "75",   // Replace with actual user weight
                USER_HEART_RATE to "70",  // Replace with actual user heart rate
                USER_RESPIRATORY_RATE to "16"  // Replace with actual user respiratory rate
            )
            val dbHelper = MyDatabaseHelper(this)
            val db = dbHelper.writableDatabase
            val sessionId = "session1"
            val suggestedWorkoutIds = suggestWorkouts(
                db,
                muscleGroupSelections,
                exerciseTypeSelections,
                userMetrics,
                sessionId
            )
            // Do something with the suggestedWorkoutIds, like displaying them
//            FuzzyLogicControllerWorkoutSuggestions.showSelectedExercises()
        }

        submitButton.setOnClickListener {
//            showSelectedExercises()

            // Added code

        }

        exercisesAdapter = ExercisesAdapter(emptyList())
        exercisesList.layoutManager = LinearLayoutManager(this)
        exercisesList.adapter = exercisesAdapter
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
    private fun showSelectedExercises() {
        //suggestWorkouts(db: SQLiteDatabase, muscleGroups: List<String>, workoutTypes: List<String>, userMetrics: Map<String, String>, sessionId: String): List<Long>
            // profile the user metrics and rate the user
        val selectedExercises = exercisesAdapter.exercises.filter { it.isSelected }
            .joinToString(separator = ", ") { it.name }

        if (selectedExercises.isEmpty()) {
            Toast.makeText(this, "No exercises selected", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Selected Exercises: $selectedExercises", Toast.LENGTH_LONG).show()
        }
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
        exercisesAdapter.exercises = exercises
        exercisesAdapter.notifyDataSetChanged()

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
        "Arms" -> listOf(Exercise("Bicep Curls"), Exercise("Tricep Dips"), Exercise("Hammer Curls"))
        "Chest" -> listOf(Exercise("Bench Press"), Exercise("Push Ups"), Exercise("Chest Fly"))
        "Back" -> listOf(Exercise("Pull Ups"), Exercise("Deadlifts"), Exercise("Lat Pulldowns"))
        "Legs" -> listOf(Exercise("Squats"), Exercise("Leg Press"), Exercise("Lunges"))
        "Shoulders" -> listOf(Exercise("Shoulder Press"), Exercise("Lateral Raises"), Exercise("Front Raises"))
        // Add more muscle groups and exercises as needed
        else -> emptyList()
    }
}

data class Exercise(val name: String, var isSelected: Boolean = false)

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
