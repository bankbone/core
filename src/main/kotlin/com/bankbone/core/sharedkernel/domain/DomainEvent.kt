package com.bankbone.core.sharedkernel.domain

import java.time.Instant

interface DomainEvent {
    val occurredAt: Instant
}