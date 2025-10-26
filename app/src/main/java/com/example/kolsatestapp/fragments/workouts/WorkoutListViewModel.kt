package com.example.kolsatestapp.fragments.workouts

import com.example.domain.model.workout.Workout
import com.example.domain.useCase.GetWorkoutListUseCase
import com.example.kolsatestapp.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class WorkoutListViewModel @Inject constructor(
    private val getWorkoutListUseCase: GetWorkoutListUseCase,
): BaseViewModel() {

    private val _workoutListUiState = MutableUIStateFlow<List<Workout>>()
    val workoutListUiState = _workoutListUiState.asStateFlow()

    private val _workoutListState = MutableStateFlow<List<Workout>?>(null)
    val workoutListState = _workoutListState.asStateFlow()

    fun loadWorkoutList() {
        getWorkoutListUseCase.getWorkoutsList().collectNetworkRequest(_workoutListUiState)
    }

    fun onLoadWorkoutList(workoutList: List<Workout>) {
        _workoutListState.value = workoutList
    }

}