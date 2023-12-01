package com.application.bhealthy.data

data class Workout(
    val id: Long,
    val title: String,
    val description: String,
    val type: String,
    val bodyPart: String,
    val equipment: String,
    val level: String,
    val rating: String,
    val ratingDesc: String
)
