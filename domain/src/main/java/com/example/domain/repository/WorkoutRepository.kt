package com.example.domain.repository

import com.example.domain.core.RemoteWrapper
import com.example.domain.model.workout.Workout

interface WorkoutRepository {

    fun getWorkoutsList(): RemoteWrapper<List<Workout>>

}