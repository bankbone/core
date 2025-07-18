package com.bankbone.core.sharedkernel.domain

import java.time.Instant
import java.util.UUID

interface DomainEvent {
    val aggregateId: UUID
    val eventType: String
    val occurredAt: Instant
}
