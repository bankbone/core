package com.bankbone.core.sharedkernel.infrastructure

import com.bankbone.core.sharedkernel.domain.OutboxEvent
import com.bankbone.core.sharedkernel.domain.OutboxEventStatus
import com.bankbone.core.sharedkernel.ports.OutboxRepository
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class InMemoryOutboxRepository : OutboxRepository {
    val events = ConcurrentHashMap<UUID, OutboxEvent>()

    override suspend fun add(event: OutboxEvent) {
        events[event.id] = event
    }

    override suspend fun findPendingEvents(limit: Int): List<OutboxEvent> {
        return events.values
            .filter { it.status == OutboxEventStatus.PENDING || it.status == OutboxEventStatus.FAILED }
            .sortedBy { it.createdAt }
            .take(limit)
    }

    override suspend fun markAsProcessed(event: OutboxEvent) {
        events[event.id] = event.copy(
            status = OutboxEventStatus.PUBLISHED,
            attemptCount = event.attemptCount + 1,
            lastAttemptAt = Instant.now()
        )
    }

    override suspend fun markAsFailed(event: OutboxEvent, error: String) {
        events[event.id] = event.copy(
            status = OutboxEventStatus.FAILED,
            attemptCount = event.attemptCount + 1,
            lastAttemptAt = Instant.now(),
            lastError = error
        )
    }
}