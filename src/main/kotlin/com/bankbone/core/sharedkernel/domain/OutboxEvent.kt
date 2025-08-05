package com.bankbone.core.sharedkernel.domain

import java.time.Instant
import java.util.UUID

enum class OutboxEventStatus {
    PENDING,
    PUBLISHED,
    FAILED
}

data class OutboxEvent(
    val id: UUID = UUID.randomUUID(),
    val aggregateId: String,
    val eventType: String,
    val payload: String,
    val status: OutboxEventStatus = OutboxEventStatus.PENDING,
    val attemptCount: Int = 0,
    val lastAttemptAt: Instant? = null,
    val lastError: String? = null,
    val createdAt: Instant = Instant.now()
)