package com.bankbone.core.sharedkernel.domain

import java.time.Instant
import java.util.UUID

interface DomainEvent {
    val aggregateId: UUID
    val occurredAt: Instant
    val eventType: String
        get() = this::class.simpleName!!
}