package com.example.data.utils

/**
 * Base mapper interface
 *
 * @param T - Модель уровня предметной области(domain)
 *
 */
interface DataMapper<T> {

    /**
     * Функция для сопоставления модели слоя данных с моделью слоя предметной области(domain)
     */
    fun mapToDomain(): T
}