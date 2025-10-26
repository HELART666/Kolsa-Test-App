package com.example.data.base

import com.example.data.utils.DataMapper
import com.example.domain.core.Either
import com.example.domain.core.NetworkError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.InputStream

private const val ERROR_RARE_CASE = "Произошла ошибка сервера"

abstract class BaseRepository {

    /**
     * Выполнить сетевой запрос с помощью [DataMapper.mapToDomain]
     *
     * @receiver [doNetworkRequest]
     */
    protected fun <T : DataMapper<S>, S> doNetworkRequestWithMapping(
        request: suspend () -> Response<T>
    ): Flow<Either<NetworkError, S>> = doNetworkRequest(request) { body ->
        Either.Right(body.mapToDomain())
    }

    /**
     * Выполнить сетевой запрос без сопоставления(для примитивных типов)
     *
     * @receiver [doNetworkRequest]
     */
    protected fun <T> doNetworkRequestWithoutMapping(
        request: suspend () -> Response<T>
    ): Flow<Either<NetworkError, T>> = doNetworkRequest(request) { body ->
        Either.Right(body)
    }

    /**
     * Выполнить сетевой запрос с для списка
     *
     * @receiver [doNetworkRequest]
     */
    protected fun <T : DataMapper<S>, S> doNetworkRequestForList(
        request: suspend () -> Response<List<T>>
    ): Flow<Either<NetworkError, List<S>>> = doNetworkRequest(request) { body ->
        Either.Right(body.map { it.mapToDomain() })
    }

    /**
     * Выполнить сетевой запрос и вернуть [Unit]
     *
     * @receiver [doNetworkRequest]
     */
    protected fun <T> doNetworkRequestUnit(
        request: suspend () -> Response<T>
    ): Flow<Either<NetworkError, Unit>> = doNetworkRequest(request) {
        Either.Right(Unit)
    }

    /**
     * Выполнить сетевой запрос ResponseBody
     *
     * @receiver [doNetworkRequest]
     */
    protected fun <T> doNetworkRequestResponseBody(
        request: suspend () -> Response<ResponseBody>
    ): Flow<Either<NetworkError, InputStream>> = doNetworkRequest(request) { body ->
        Either.Right(body.byteStream())
    }

    /**
     * Базовая функция для сетевых запросов
     *
     * @param T - Модель уровня данных (DATA)
     * @param S - Модель уровня предметной области(domain)
     * @param request Функция HTTP-запроса от API-сервиса
     * @param successful handle response body with custom mapping
     *
     * @return [NetworkError] or [Response.body] in [Flow] with [Either]
     *
     * @see [Response]
     * @see [Flow]
     * @see [Either]
     * @see [NetworkError]
     */
    private fun <T, S> doNetworkRequest(
        request: suspend () -> Response<T>,
        successful: (T) -> Either.Right<S>
    ) = flow<Either<NetworkError, S>> {
        request().let { response ->
            when {
                response.isSuccessful && response.body() != null -> {
                    emit(successful.invoke(response.body()!!))
                }

                response.isSuccessful && response.body() == null -> {
                    emit(Either.Left(NetworkError.EmptyBody))
                }

                else -> {
                    val message = try {
                        response.errorBody()?.string() ?: "Неизвестная ошибка"
                    } catch (e: Throwable) {
                        if (!response.message().isNullOrEmpty()) {
                            response.message()
                        } else {
                            ERROR_RARE_CASE
                        }
                    }
                    emit(Either.Left(NetworkError.Api(message, response.code())))
                }
            }
        }
    }.flowOn(Dispatchers.IO)
        .catch { exception ->
            val message = exception.localizedMessage ?: "Error Occurred!"

            val error = NetworkError.Unexpected(message)
            emit(Either.Left(error))
        }


    /**
     * Get non-nullable body from network request
     *
     * &nbsp
     *
     * ## How to use:
     * ```
     * override fun getData() = doNetworkRequestWithMapping {
     *     serviceApi.getData().onSuccess { data ->
     *         make something with data
     *     }
     * }
     * ```
     *
     * @see Response.body
     * @see let
     */
    protected inline fun <T : Response<S>, S> T.onSuccess(block: (S) -> Unit): T {
        this.body()?.let(block)
        return this
    }

    /**
     * Выполнить запрос к локальной базе данных с помощью [DataMapper.mapToDomain]
     *
     * @param request - Лямбда запроса к базе данных
     */
    protected fun <T : DataMapper<S?>, S : Any?> doLocalRequest(
        request: () -> Flow<T?>
    ): Flow<S?> = request().map { data -> data?.mapToDomain() }

    protected fun <T> doLocalRequestWithoutMapping(
        request: () -> Flow<T>
    ): Flow<T> = request()

    /**
     * Выполнить запрос к локальной базе данных с помощью [DataMapper.mapToDomain] для [List]
     *
     * @param request - Лямбда запроса к базе данных
     */
    protected fun <T : DataMapper<S?>, S : Any?> doLocalRequestForList(
        request: () -> Flow<List<T?>>
    ): Flow<List<S?>> = request().map { list -> list.map { data -> data?.mapToDomain() } }
}
