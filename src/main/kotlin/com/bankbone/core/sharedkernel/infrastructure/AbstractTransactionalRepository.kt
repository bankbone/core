package com.bankbone.core.sharedkernel.infrastructure

import com.bankbone.core.sharedkernel.domain.AggregateId
import com.bankbone.core.sharedkernel.domain.AggregateRoot
import com.bankbone.core.sharedkernel.domain.OutboxEvent
import com.bankbone.core.sharedkernel.ports.EventSerializer
import com.bankbone.core.sharedkernel.ports.OutboxRepository

abstract class AbstractTransactionalRepository<T : AggregateRoot<out AggregateId>>(
    private val outboxRepository: OutboxRepository,
    private val eventSerializer: EventSerializer
) {
    protected suspend fun saveWithOutbox(aggregate: T) {
        // First, save the aggregate state itself.
        // The concrete implementation will handle this.
        saveAggregate(aggregate)

        // Then, process its domain events and save them to the outbox.
        aggregate.domainEvents.forEach { domainEvent ->
            val outboxEvent = OutboxEvent(
                aggregateId = domainEvent.aggregateId.toString(),
                eventType = domainEvent.eventType,
                payload = eventSerializer.serialize(domainEvent)
            )
            outboxRepository.add(outboxEvent)
        }
        aggregate.clearEvents()
    }

    protected abstract suspend fun saveAggregate(aggregate: T)
}