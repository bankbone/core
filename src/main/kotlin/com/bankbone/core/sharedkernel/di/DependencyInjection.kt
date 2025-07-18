package com.bankbone.core.sharedkernel.di

/**
 * A simple abstraction for a dependency injection container.
 * This allows us to switch DI frameworks more easily if needed.
 */
interface DependencyInjection {
    fun <T : Any> get(clazz: Class<T>): T
    fun <T : Any> getAll(clazz: Class<T>): List<T>
}

/**
 * An extension function to simplify retrieval of dependencies.
 */
inline fun <reified T : Any> DependencyInjection.get(): T {
    return get(T::class.java)
}