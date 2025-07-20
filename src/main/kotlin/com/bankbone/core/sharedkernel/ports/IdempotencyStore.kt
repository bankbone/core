package com.bankbone.core.sharedkernel.ports

import com.bankbone.core.sharedkernel.domain.IdempotencyKey

interface IdempotencyStore {
    /**
     * Checks if an operation for the given [key] has already been executed.
     * If it has, it returns the previously stored result.
     * If not, it executes the [operation], stores its result, and returns it.
     * This ensures that the operation is performed exactly once.
     */
    suspend fun <T> getOrSet(key: IdempotencyKey, operation: suspend () -> T): T

    /**
     * Stores the result of an idempotent operation associated with the key.
     */
    suspend fun <T> storeResult(key: IdempotencyKey, result: T)
}