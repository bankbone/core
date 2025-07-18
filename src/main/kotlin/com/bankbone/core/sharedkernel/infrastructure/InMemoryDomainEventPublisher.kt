package com.bankbone.core.sharedkernel.infrastructure

import com.bankbone.core.sharedkernel.domain.DomainEvent
import com.bankbone.core.sharedkernel.ports.DomainEventPublisher

class InMemoryDomainEventPublisher : DomainEventPublisher {
    private var failCount = 0
    var attempts = 0
    val publishedEvents = mutableListOf<DomainEvent>()

    fun setFailures(count: Int) {
        this.failCount = count
        this.attempts = 0
    }

    override suspend fun publish(events: List<DomainEvent>) {
        attempts++
        if (attempts <= failCount) throw RuntimeException("Simulated publisher failure on attempt $attempts")
        publishedEvents.addAll(events)
    }
}