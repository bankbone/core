package com.bankbone.core.sharedkernel.domain

import com.bankbone.core.sharedkernel.domain.DomainEvent

abstract class AggregateRoot {
    @Transient
    private val _domainEvents = mutableListOf<DomainEvent>()

    fun domainEvents(): List<DomainEvent> {
        return _domainEvents.toList()
    }

    protected fun recordEvent(event: DomainEvent) {
        _domainEvents.add(event)
    }

    fun clearEvents() {
        _domainEvents.clear()
    }
}