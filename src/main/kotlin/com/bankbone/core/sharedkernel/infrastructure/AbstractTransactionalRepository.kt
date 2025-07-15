package com.bankbone.core.sharedkernel.infrastructure

import com.bankbone.core.sharedkernel.domain.AggregateRoot
import com.bankbone.core.sharedkernel.domain.OutboxEvent
import com.bankbone.core.sharedkernel.ports.EventSerializer
import com.bankbone.core.sharedkernel.ports.OutboxRepository

abstract class AbstractTransactionalRepository<T : AggregateRoot, ID>(
    private val outboxRepository: OutboxRepository,
    private val eventSerializer: EventSerializer
) {
    protected suspend fun saveWithEvents(aggregate: T) {
        // In a real implementation with a database, this entire method would be wrapped
        // in a single, shared database transaction.

        // 1. Persist the aggregate's state.
        persist(aggregate)

        // 2. Save its domain events to the outbox.
        val aggregateId = getAggregateId(aggregate)
        aggregate.domainEvents().forEach { domainEvent ->
            val payload = eventSerializer.serialize(domainEvent)
            val outboxEvent = OutboxEvent(aggregateId = aggregateId.toString(), eventType = domainEvent.eventType, payload = payload)
            outboxRepository.save(outboxEvent)
        }
        aggregate.clearEvents()
    }

    protected abstract suspend fun persist(aggregate: T)
    protected abstract fun getAggregateId(aggregate: T): ID
}