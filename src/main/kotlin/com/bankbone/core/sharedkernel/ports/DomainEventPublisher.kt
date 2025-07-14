package com.bankbone.core.sharedkernel.ports

import com.bankbone.core.sharedkernel.domain.DomainEvent

interface DomainEventPublisher {
    suspend fun publish(events: List<DomainEvent>)
}