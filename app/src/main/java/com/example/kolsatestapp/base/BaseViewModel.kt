package com.example.kolsatestapp.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.core.Either
import com.example.domain.core.NetworkError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import com.example.kolsatestapp.extensions.launchIO
import com.example.kolsatestapp.state.UIState


/**
 * Base class for all [ViewModel]s
 *
 */
abstract class BaseViewModel : ViewModel() {

    /**
     * Creates a [MutableStateFlow] with [UIState] and the given initial value [UIState.Idle]
     */
    @Suppress("FunctionName")
    protected fun <T> MutableUIStateFlow() = MutableStateFlow<UIState<T>>(UIState.Idle())

    /**
     * Reset [MutableUIStateFlow] to [UIState.Idle]
     */
    protected fun <T> MutableStateFlow<UIState<T>>.reset() {
        value = UIState.Idle()
    }

    /**
     * Collect network request result without mapping for primitive types
     *
     * @receiver [collectEither]
     */
    protected fun <T> Flow<Either<NetworkError, T>>.collectNetworkRequest(
        state: MutableStateFlow<UIState<T>>
    ) = collectEither(state) {
        UIState.Success(it)
    }

    /**
     * Collect network request result with mapping
     *
     * @receiver [collectEither]
     */
    protected fun <T, S> Flow<Either<NetworkError, T>>.collectNetworkRequest(
        state: MutableStateFlow<UIState<S>>,
        mapToUI: (T) -> S
    ) = collectEither(state) {
        UIState.Success(mapToUI(it))
    }

    /*
        protected fun <T, S> Flow<Either<NetworkError, List<S>>>.collectNetworkForListRequest(
            state: MutableStateFlow<UIState<S>>,
            mapToUI: (T) -> S
        ) = collectEither(state) { list ->
            val newList = list.map { mapToUI(it) }
            UIState.Success(newList)
        }*/

    /**
     * Collect network request result and mapping [Either] to [UIState]
     *
     * @receiver [NetworkError] or [data][T] in [Flow] with [Either]
     *
     * @param T domain layer model
     * @param S presentation layer model
     * @param state [MutableStateFlow] with [UIState]
     *
     * @see viewModelScope
     * @see [Flow.collect]
     */
    private fun <T, S> Flow<Either<NetworkError, T>>.collectEither(
        state: MutableStateFlow<UIState<S>>,
        successful: (T) -> UIState.Success<S>
    ) {
        viewModelScope.launchIO(
            safeAction = {
                state.value = UIState.Loading()
                this@collectEither.collect {
                    when (it) {
                        is Either.Left -> state.value = UIState.Error(it.value)
                        is Either.Right -> state.value = successful(it.value)
                    }
                }
            }, onError = {
                state.value = UIState.Error(NetworkError.Unexpected(it.message.toString()))
            }
        )
    }

    /**
     * Collect local request to database with mapping
     *
     * @receiver [T] with [Flow]
     *
     * @param T domain layer model
     * @param S presentation layer model
     * @param mapToUI high-order function for setup mapper functions
     */
    protected fun <T, S> Flow<T>.collectLocalRequest(
        mapToUI: (T) -> S
    ): Flow<S> = map { value: T ->
        mapToUI(value)
    }

    /**
     * Collect local request to database with mapping with [List]
     *
     * @receiver [T] in [List] with [Flow]
     *
     * @param T domain layer model
     * @param S presentation layer model
     * @param mapToUI high-order function for setup mapper functions
     */
    protected fun <T, S> Flow<List<T>>.collectLocalRequestForList(
        mapToUI: (T) -> S
    ): Flow<List<S>> = map { value: List<T> ->
        value.map { data: T ->
            mapToUI(data)
        }
    }

    /**
     * Расширение для комбинированного запроса более чем 5 потоков
     */
    inline fun <T1, T2, T3, T4, T5, T6, T7, R> combine(
        flow: Flow<T1>,
        flow2: Flow<T2>,
        flow3: Flow<T3>,
        flow4: Flow<T4>,
        flow5: Flow<T5>,
        flow6: Flow<T6>,
        flow7: Flow<T7>,
        crossinline transform: suspend (T1, T2, T3, T4, T5, T6, T7) -> R
    ): Flow<R> {
        return kotlinx.coroutines.flow.combine(
            flow,
            flow2,
            flow3,
            flow4,
            flow5,
            flow6,
            flow7
        ) { args: Array<*> ->
            @Suppress("UNCHECKED_CAST")
            transform(
                args[0] as T1,
                args[1] as T2,
                args[2] as T3,
                args[3] as T4,
                args[4] as T5,
                args[5] as T6,
                args[6] as T7,
            )
        }
    }
}
