package com.example.kolsatestapp.base

import android.R
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.constraintlayout.widget.Group
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import com.example.domain.core.AppError
import com.example.domain.core.NetworkError
import com.example.domain.core.UnknownAppError
import com.example.kolsatestapp.extensions.launchMain
import com.example.kolsatestapp.state.UIState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow


abstract class BaseFragment<ViewModel : BaseViewModel, Binding : ViewBinding>(
    @LayoutRes private val layoutId: Int
) : Fragment(layoutId) {

    protected abstract val viewModel: ViewModel
    protected abstract val binding: Binding
    private var _savedInstanceState: Bundle? = null
    protected val savedInstanceState get() = _savedInstanceState

    /**
     * Возвращает `true`, если фрагмент создается впервые, иначе `false`.
     * P.S. Это свойство полезно для определения, следует ли выполнять действия,
     * которые должны происходить только при первом создании фрагмента
     */
    protected val isNewInstance get() = savedInstanceState == null

    protected fun screenOrientationLocked(locked: Boolean) {
        requireActivity().requestedOrientation =
            if (locked) {
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            } else {
                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
    }

    final override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _savedInstanceState = savedInstanceState
        setupToolbar()
        initialize()
        setupListeners()
        setupRequests()
        setupSubscribers()
        showSkeleton()
    }

    protected open fun initialize() {}

    protected open fun setupListeners() {}

    protected open fun setupRequests() {}

    protected open fun setupSubscribers() {}

    protected open fun setupToolbar() {}

    protected open fun showSkeleton() {}

    protected open fun hideSkeleton() {}

    /**
     * Collect [UIState] with [launchRepeatOnLifecycle]
     *
     * @receiver [StateFlow] with [UIState]
     *
     * @param state optional, for working with all states
     * @param onError for error handling
     * @param onSuccess for working with data
     */
    protected fun <T> StateFlow<UIState<T>>.collectUIState(
        lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
        state: ((UIState<T>) -> Unit)? = null,
        onError: ((error: AppError) -> Unit),
        onSuccess: ((data: T) -> Unit)
    ) {
        launchRepeatOnLifecycle(lifecycleState) {
            this@collectUIState.collect {
                state?.invoke(it)
                when (it) {
                    is UIState.Idle -> {}
                    is UIState.Loading -> {}
                    is UIState.Error -> onError.invoke(it.error)
                    is UIState.Success -> {
                        hideSkeleton()
                        onSuccess.invoke(it.data)
                    }
                }
            }
        }
    }

    /**
     * Setup views visibility depending on [UIState] states.
     *
     * @receiver [UIState]
     *
     * @param willShowViewIfSuccess whether to show views if the request is successful
     */
    protected fun <T> UIState<T>.setupViewVisibility(
        group: Group? = null,
        willShowViewIfSuccess: Boolean = true
    ) {
        fun showLoader(isVisible: Boolean) {
            group?.isVisible = !isVisible
        }

        when (this) {
            is UIState.Idle -> {}
            is UIState.Loading -> showLoader(true)
            is UIState.Error -> showLoader(false)
            is UIState.Success -> showLoader(!willShowViewIfSuccess)
        }
    }

    protected fun AppError.setupErrors() {
        when (this) {
            is NetworkError -> setupErrors()
            is UnknownAppError -> setupAppErrors()
        }
    }

    private fun UnknownAppError.setupAppErrors() {
        println(error)
        Toast.makeText(requireActivity(), error, Toast.LENGTH_SHORT).show()
    }

    /**
     * Extension function for setup errors from server side
     *
     * @receiver [NetworkError]
     */
    private fun NetworkError.setupErrors() = when (this) {
        is NetworkError.EmptyBody -> {}

        is NetworkError.Unexpected -> {
            println(error)
            Toast.makeText(requireActivity(), error, Toast.LENGTH_SHORT).show()
        }

        is NetworkError.Api -> {
            println(error)
            Toast.makeText(requireActivity(), error, Toast.LENGTH_SHORT).show()
        }

    }

    /**
     * Collect flow safely with [launchRepeatOnLifecycle]
     */
    protected fun <T> Flow<T>.collectSafely(
        state: Lifecycle.State = Lifecycle.State.STARTED,
        collector: (T) -> Unit
    ) {
        launchRepeatOnLifecycle(state) {
            this@collectSafely.collect {
                collector(it)
            }
        }
    }

    /**
     * Launch coroutine with [repeatOnLifecycle] API
     *
     * @param state [Lifecycle.State][Lifecycle.State] in which `block` runs in a new coroutine. That coroutine
     * will cancel if the lifecycle falls below that state, and will restart if it's in that state
     * again.
     * @param block The block to run when the lifecycle is at least in [state] state.
     */
    private fun launchRepeatOnLifecycle(
        state: Lifecycle.State,
        block: suspend CoroutineScope.() -> Unit
    ) {
        viewLifecycleOwner.lifecycleScope.launchMain(
            safeAction = {
                viewLifecycleOwner.repeatOnLifecycle(state) {
                    block()
                }
            },
            onError = {
                showToast(requireActivity(), it.message ?: "Неизвестная ошибка")
            }
        )
    }
}

private fun showToast(context: Context, message: String) {
    println(message)
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}