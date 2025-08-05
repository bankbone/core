package com.bankbone.core.sharedkernel.domain

import java.util.Collections

/**
 * Represents a root entity in an aggregate, which is a cluster of associated objects
 * that are treated as a single unit for data changes.
 *
 * @param T The type of the aggregate's unique identifier.
 */
abstract class AggregateRoot<T : AggregateId> {
    abstract val id: T

    @Transient
    private val _domainEvents: MutableList<DomainEvent> = mutableListOf()

    val domainEvents: List<DomainEvent>
        get() = Collections.unmodifiableList(_domainEvents)

    protected fun addDomainEvent(event: DomainEvent) {
        _domainEvents.add(event)
    }

    fun clearEvents() {
        _domainEvents.clear()
    }
}