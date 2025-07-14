package com.bankbone.core.sharedkernel.infrastructure

import com.bankbone.core.sharedkernel.domain.DomainEvent
import com.bankbone.core.sharedkernel.ports.DomainEventPublisher

class InMemoryDomainEventPublisher : DomainEventPublisher {
    val publishedEvents = mutableListOf<DomainEvent>()

    override suspend fun publish(events: List<DomainEvent>) {
        publishedEvents.addAll(events)
    }
}