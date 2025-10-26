package com.example.kolsatestapp.fragments.workouts

import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.elveum.elementadapter.simpleAdapter
import com.example.domain.model.workout.Workout
import com.example.kolsatestapp.R
import com.example.kolsatestapp.base.BaseFragment
import com.example.kolsatestapp.databinding.FragmentWorkoutListBinding
import com.example.kolsatestapp.databinding.ItemWorkoutBinding
import com.example.kolsatestapp.fragments.utils.Consts
import com.example.kolsatestapp.state.UIState
import dagger.hilt.android.AndroidEntryPoint
import dev.androidbroadcast.vbpd.viewBinding

@AndroidEntryPoint
class WorkoutListFragment :
    BaseFragment<WorkoutListViewModel, FragmentWorkoutListBinding>(R.layout.fragment_workout_list) {

    override val binding by viewBinding(FragmentWorkoutListBinding::bind)
    override val viewModel: WorkoutListViewModel by viewModels()

    private val workoutListAdapter = simpleAdapter<Workout, ItemWorkoutBinding> {

        areItemsSame = { oldItem, newItem -> oldItem.title == newItem.title }

        bind { workout ->
            tvWorkoutTitle.text = workout.title
            tvWorkoutDesc.text = workout.description
            tvWorkoutType.text = workout.type.toString()
            tvWorkoutVideoDuration.text = workout.duration
        }

        listeners {
            root.onClick { workout ->
                val bundle = bundleOf(Consts.VIDEO_ID_KEY to workout.id)

                findNavController().navigate(
                    resId = R.id.action_workoutListFragment_to_videoFragment,
                    args = bundle
                )
            }
        }
    }

    override fun initialize() {
        super.initialize()
        viewModel.loadWorkoutList()
    }

    override fun setupSubscribers() {
        super.setupSubscribers()
        subscribeWorkoutList()
        setupAdapters()
    }

    private fun setupAdapters() {
        binding.rvWorkouts.adapter = workoutListAdapter
    }

    private fun subscribeWorkoutList() {
        viewModel.workoutListUiState.collectUIState(
            lifecycleState = Lifecycle.State.STARTED,
            state = {
                when (it) {
                    is UIState.Error -> {
                        binding.tvError.visibility = View.VISIBLE
                    }
                    is UIState.Loading, is UIState.Idle -> {
                        binding.loadIndicator.visibility = View.VISIBLE
                    }
                    is UIState.Success -> {
                        binding.loadIndicator.visibility = View.GONE
                    }
                } },
            onError = {
                it.setupErrors()
            },
            onSuccess = {
                workoutListAdapter.submitList(it)
            }
        )
    }
}