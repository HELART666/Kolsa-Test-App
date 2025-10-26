package com.example.kolsatestapp.state

import com.example.domain.core.AppError
import kotlinx.coroutines.flow.StateFlow

sealed class UIState<T> {
    class Idle<T> : UIState<T>()
    class Loading<T> : UIState<T>()
    class Error<T>(val error: AppError) : UIState<T>()

    class Success<T>(val data: T) : UIState<T>()
}

/**
 * Возвращает значение для Success состояния
 *
 * @return соответствующие данные или null.
 *
 * @see StateFlow
 * @see UIState
 */
fun <T> StateFlow<UIState<T>>.getSuccessValue(): T? {
    return value.let { state ->
        when (state) {
            is UIState.Success -> state.data
            else -> null
        }
    }
}
