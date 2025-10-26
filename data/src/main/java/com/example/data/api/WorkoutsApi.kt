package com.example.data.api

import com.example.data.model.workout.WorkoutData
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET

interface WorkoutsApi {

    @GET("get_workouts")
    suspend fun getWorkoutsList(): Response<List<WorkoutData>>

    companion object {
        fun create(retrofit: Retrofit): WorkoutsApi {
            return retrofit.create(WorkoutsApi::class.java)
        }
    }
}