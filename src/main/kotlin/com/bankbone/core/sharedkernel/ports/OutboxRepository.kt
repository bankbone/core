package com.bankbone.core.sharedkernel.ports

import com.bankbone.core.sharedkernel.domain.OutboxEvent

/**
 * Interface for the Outbox repository that handles event sourcing and event publishing.
 */
interface OutboxRepository {
    /**
     * Adds a new event to the outbox.
     */
    suspend fun add(event: OutboxEvent)
    
    /**
     * Finds all pending events that need to be published.
     */
    suspend fun findPendingEvents(limit: Int = 100): List<OutboxEvent>
    
    /**
     * Marks an event as successfully published.
     */
    suspend fun markAsProcessed(event: OutboxEvent)
    
    /**
     * Marks an event as failed with the given error message.
     */
    suspend fun markAsFailed(event: OutboxEvent, error: String)
}
