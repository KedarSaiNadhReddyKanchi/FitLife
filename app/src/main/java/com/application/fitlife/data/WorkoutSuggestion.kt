package com.application.bhealthy.data

data class WorkoutSuggestion(
    val id: Long,
    val sessionId: String,
    val date: String,
    val exerciseId: String,
    val score: String,
    val isPerformed: String,
)
