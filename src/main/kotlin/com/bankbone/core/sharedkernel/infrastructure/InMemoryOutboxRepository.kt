package com.bankbone.core.sharedkernel.infrastructure

import com.bankbone.core.sharedkernel.domain.OutboxEvent
import com.bankbone.core.sharedkernel.domain.OutboxEventStatus
import com.bankbone.core.sharedkernel.domain.OutboxRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class InMemoryOutboxRepository : OutboxRepository {
    internal val events = ConcurrentHashMap<UUID, OutboxEvent>()
    private val mutex = Mutex()

    override suspend fun add(event: OutboxEvent) {
        mutex.withLock {
            events[event.id] = event
        }
    }

    override suspend fun findPendingEvents(limit: Int): List<OutboxEvent> {
        return events.values
            .filter { it.status == OutboxEventStatus.PENDING || it.status == OutboxEventStatus.FAILED }
            .sortedBy { it.createdAt }
            .take(limit)
    }

    override suspend fun markAsProcessed(event: OutboxEvent) {
        mutex.withLock {
            val currentEvent = events[event.id] ?: event
            events[event.id] = currentEvent.copy(
                status = OutboxEventStatus.PUBLISHED,
                attemptCount = currentEvent.attemptCount + 1,
                lastAttemptAt = Instant.now()
            )
        }
    }

    override suspend fun markAsFailed(event: OutboxEvent, error: String) {
        mutex.withLock {
            val currentEvent = events[event.id] ?: event
            events[event.id] = currentEvent.copy(
                status = OutboxEventStatus.FAILED,
                attemptCount = currentEvent.attemptCount + 1,
                lastAttemptAt = Instant.now(),
                lastError = error
            )
        }
    }
}