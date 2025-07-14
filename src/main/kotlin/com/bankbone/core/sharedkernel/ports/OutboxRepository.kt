package com.bankbone.core.sharedkernel.ports

import com.bankbone.core.sharedkernel.domain.OutboxEvent

interface OutboxRepository {
    suspend fun save(event: OutboxEvent)
    suspend fun findPendingEvents(limit: Int): List<OutboxEvent>
    suspend fun markAsProcessed(event: OutboxEvent)
}