package com.example.domain.core

/**
 * Wrapper class for network errors
 *
 */
sealed class NetworkError : AppError() {

    /**
     * State for response with empty body (response.body() == null)
     */
    data object EmptyBody : NetworkError()

    /**
     * State for unexpected exceptions, for example «HTTP code - 500» or exceptions when mapping models
     */
    class Unexpected(val error: String) : NetworkError()

    /**
     * State for default errors from server size
     */
    class Api(val error: String, val code: Int = 0) : NetworkError()
}
