package com.example.domain.useCase

import com.example.domain.repository.WorkoutRepository
import javax.inject.Inject

class GetWorkoutListUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {

    fun getWorkoutsList() = repository.getWorkoutsList()

}