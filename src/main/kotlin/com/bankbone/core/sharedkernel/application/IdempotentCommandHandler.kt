package com.bankbone.core.sharedkernel.application

import com.bankbone.core.sharedkernel.domain.IdempotencyKey
import com.bankbone.core.sharedkernel.ports.IdempotencyStore
import org.slf4j.LoggerFactory

abstract class IdempotentCommandHandler<T, R>(private val idempotencyStore: IdempotencyStore) {

    private val logger = LoggerFactory.getLogger(javaClass)

    abstract suspend fun extractIdempotencyKey(command: T): IdempotencyKey

    suspend fun handleIdempotently(command: T, handler: suspend () -> R): R {
        val key = extractIdempotencyKey(command)
        logger.info("Handling command with idempotency key: $key")
 
        // Delegate the entire "get or execute and set" logic to the idempotency store.
        // The handler lambda is only executed if the key is not already present.
        return idempotencyStore.getOrSet(key) {
            handler()
        }
    }
}