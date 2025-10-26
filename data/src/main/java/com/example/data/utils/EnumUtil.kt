package com.example.data.utils

/**
 * Для безопасного сопоставления экземпляра Enum
 *
 * @return экземпляр Enum или значение по умолчанию
 *
 * ## Как использовать:
 * ```
 * fun findEnumByValueSample(myValue : String): MyEnum  {
 *    return valueOf(myValue, MyEnum.DEFAULT)
 * }
 */
inline fun <reified T : Enum<T>> valueOf(type: String?, default: T): T =
    valueOfOrNull<T>(type) ?: default

/**
 * Для безопасного сопоставления экземпляра Enum
 *
 * @return экземпляр Enum или null
 *
 * ## Как использовать:
 * ```
 * fun findEnumByValueSample(myValue: String): MyEnum?  {
 *    return valueOfOrNull(myValue)
 * }
 */
inline fun <reified T : Enum<T>> valueOfOrNull(type: String?): T? {
    return try {
        java.lang.Enum.valueOf(
            T::class.java,
            type ?: throw NullPointerException("arguments type in method valueOfOrNull is null")
        )
    } catch (e: Exception) {
        null
    }
}
