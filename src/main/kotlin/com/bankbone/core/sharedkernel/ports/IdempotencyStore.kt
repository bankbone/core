package com.bankbone.core.sharedkernel.ports

import com.bankbone.core.sharedkernel.domain.IdempotencyKey

interface IdempotencyStore {
    /**
     * Checks if the key exists. If not, it executes the [operation] within a transaction
     * and stores the result along with the key.
     * If the key exists, it returns the stored result (or null if no result is stored).
     */
    suspend fun <T> checkAndSet(key: IdempotencyKey, operation: suspend () -> T): T?

    /**
     * Stores the result of an idempotent operation associated with the key.
     */
    suspend fun <T> storeResult(key: IdempotencyKey, result: T)
}