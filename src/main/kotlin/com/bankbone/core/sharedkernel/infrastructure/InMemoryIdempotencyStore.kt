package com.bankbone.core.sharedkernel.infrastructure

import com.bankbone.core.sharedkernel.domain.IdempotencyKey
import com.bankbone.core.sharedkernel.ports.IdempotencyStore
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.*
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.ConcurrentHashMap

/**
 * An in-memory implementation of [IdempotencyStore], suitable for testing or
 * simple scenarios.  In a real application, you'd use a persistent store
 * like a database or a distributed cache.
 */
class InMemoryIdempotencyStore : IdempotencyStore {
    private val store = ConcurrentHashMap<IdempotencyKey, Any?>()
    private val locks = ConcurrentHashMap<IdempotencyKey, ReentrantLock>()

    override suspend fun <T> checkAndSet(key: IdempotencyKey, operation: suspend () -> T): T? {
        val lock = locks.computeIfAbsent(key) { ReentrantLock() }
        lock.lock()

        try {
            return if (store.containsKey(key)) {
                null // Key exists, return null to indicate duplicate
            } else {
                val result = operation() // Key doesn't exist, execute operation
                storeResult(key, result)
                result
            }
        } finally {
            lock.unlock()
        }
    }

    override suspend fun <T> storeResult(key: IdempotencyKey, result: T) {
        store[key] = result
    }
}