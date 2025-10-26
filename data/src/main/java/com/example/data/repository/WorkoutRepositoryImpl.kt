package com.example.data.repository

import com.example.data.api.WorkoutsApi
import com.example.data.base.BaseRepository
import com.example.domain.core.RemoteWrapper
import com.example.domain.model.workout.Workout
import com.example.domain.repository.WorkoutRepository
import javax.inject.Inject

class WorkoutRepositoryImpl @Inject constructor(
    private val api: WorkoutsApi,
) : BaseRepository(), WorkoutRepository {
    override fun getWorkoutsList(): RemoteWrapper<List<Workout>> = doNetworkRequestForList {
        api.getWorkoutsList()
    }
}