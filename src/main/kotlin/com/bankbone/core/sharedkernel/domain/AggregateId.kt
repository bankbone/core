package com.bankbone.core.sharedkernel.domain

import com.bankbone.core.sharedkernel.domain.ids.TypedId
import java.util.UUID

/**
 * Marker interface for aggregate root IDs.
 * Implemented by [TypedId] to provide type-safe IDs for aggregates.
 */
interface AggregateId {
    val value: UUID
    
    companion object {
        /**
         * Creates a new [TypedId] for the specified type.
         */
        inline fun <reified T> typedId(value: UUID): TypedId<T> = TypedId(value)
        
        /**
         * Creates a new random [TypedId] for the specified type.
         */
        inline fun <reified T> randomTypedId(): TypedId<T> = TypedId.random()
        
        /**
         * Creates a [TypedId] from a string representation of a UUID.
         */
        inline fun <reified T> typedIdFromString(uuid: String): TypedId<T> = TypedId.fromString(uuid)
    }
}
