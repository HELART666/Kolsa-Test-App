package com.example.kolsatestapp.extensions

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Безопасный запуск корутин в IO потоке
 *
 * ## Как использовать:
 * ```
 *
 * fun myFunc() {
 *     iewModelScope.launchMain(
 *         safeAction = { //сделать что-то },
 *         onError = { //обработать ошибку }
 *     )
 * }
 * ```
 */
inline fun CoroutineScope.launchIO(
    crossinline safeAction: suspend () -> Unit,
    crossinline onError: (Throwable) -> Unit
): Job {
    val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        launch(Dispatchers.Main) {
            onError.invoke(throwable)
        }
    }
    return this.launch(exceptionHandler + Dispatchers.IO) {
        safeAction.invoke()
    }
}

/**
 * Безопасный запуск корутин в Main потоке
 *
 * ## Как использовать:
 * ```
 *
 * fun myFunc() {
 *     iewModelScope.launchMain(
 *         safeAction = { //сделать что-то },
 *         onError = { //обработать ошибку }
 *     )
 * }
 * ```
 */
inline fun CoroutineScope.launchMain(
    crossinline safeAction: suspend () -> Unit,
    crossinline onError: (Throwable) -> Unit
): Job {
    val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        launch(Dispatchers.Main) {
            onError.invoke(throwable)
        }
    }
    return this.launch(exceptionHandler + Dispatchers.Main) {
        safeAction.invoke()
    }
}

/**
 * Функция расширения для переключения на IO поток
 *
 * ## Как использовать:
 * ```
 *
 * fun myFunc() {
 *     iewModelScope.launchMain(
 *         safeAction = {
 *              //какой-то код
 *              withIO {
 *                  //сделать что-то в IO потоке
 *              }
 *         },
 *         onError = { //обработать ошибку }
 *     )
 * }
 * ```
 */
@Suppress("NeedToUseCustomWithContextRule")
suspend inline fun <T> withIO(noinline block: suspend CoroutineScope.() -> T): T {
    return withContext(Dispatchers.IO, block)
}

/**
 * Функция расширения для переключения на IO поток
 *
 * ## Как использовать:
 * ```
 *
 * fun myFunc() {
 *     iewModelScope.launchIO(
 *         safeAction = {
 *              //какой-то код
 *              withMain {
 *                  //сделать что-то в main потоке
 *              }
 *         },
 *         onError = { //обработать ошибку }
 *     )
 * }
 * ```
 */
@Suppress("NeedToUseCustomWithContextRule")
suspend inline fun <T> withMain(noinline block: suspend CoroutineScope.() -> T): T {
    return withContext(Dispatchers.Main, block)
}