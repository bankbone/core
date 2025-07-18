package com.bankbone.core.sharedkernel.infrastructure

import com.bankbone.core.sharedkernel.domain.OutboxEvent
import com.bankbone.core.sharedkernel.domain.OutboxEventStatus
import com.bankbone.core.sharedkernel.ports.OutboxRepository
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class InMemoryOutboxRepository : OutboxRepository {
    val events = ConcurrentHashMap<UUID, OutboxEvent>()

    override suspend fun save(event: OutboxEvent) {
        events[event.id] = event
    }

    override suspend fun findPendingEvents(limit: Int): List<OutboxEvent> {
        return events.values
            .filter { it.status == OutboxEventStatus.PENDING }
            .take(limit)
    }

    override suspend fun markAsProcessed(events: List<OutboxEvent>) {
        events.forEach { event ->
            val currentEvent = this.events[event.id] ?: event
            this.events[event.id] = currentEvent.copy(status = OutboxEventStatus.PUBLISHED, processedAt = Instant.now())
        }
    }

    override suspend fun markAsFailed(events: List<OutboxEvent>, error: String?, finalAttemptCount: Int) {
        events.forEach { event ->
            val currentEvent = this.events[event.id] ?: event
            this.events[event.id] = currentEvent.copy(status = OutboxEventStatus.FAILED, lastError = error, attemptCount = finalAttemptCount)
        }
    }
}