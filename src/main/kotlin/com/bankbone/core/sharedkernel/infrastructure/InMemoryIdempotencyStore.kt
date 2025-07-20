package com.bankbone.core.sharedkernel.infrastructure

import com.bankbone.core.sharedkernel.domain.IdempotencyKey
import com.bankbone.core.sharedkernel.ports.IdempotencyStore
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

/**
 * An in-memory implementation of [IdempotencyStore], suitable for testing or
 * simple scenarios.  In a real application, you'd use a persistent store
 * like a database or a distributed cache.
 *
 * This implementation is thread-safe and uses a coroutine-native [Mutex] for locking.
 */
class InMemoryIdempotencyStore : IdempotencyStore {
    private val store = ConcurrentHashMap<IdempotencyKey, Any>()
    private val locks = ConcurrentHashMap<IdempotencyKey, Mutex>()

    override suspend fun <T> getOrSet(key: IdempotencyKey, operation: suspend () -> T): T {
        val lock = locks.computeIfAbsent(key) { Mutex() }

        // Fast path check without locking
        store[key]?.let {
            @Suppress("UNCHECKED_CAST")
            return it as T
        }

        return lock.withLock {
            // Double-check inside the lock to prevent race conditions
            store[key]?.let {
                @Suppress("UNCHECKED_CAST")
                return@withLock it as T
            }

            val result = operation()
            storeResult(key, result)
            result
        }
    }

    override suspend fun <T> storeResult(key: IdempotencyKey, result: T) {
        // The result of a successful operation should not be null.
        // If an operation can fail, it should throw an exception, which prevents storing a result.
        store[key] = result as Any
    }
}