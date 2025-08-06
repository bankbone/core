package com.bankbone.core.sharedkernel.infrastructure

import com.bankbone.core.sharedkernel.domain.AggregateId
import com.bankbone.core.sharedkernel.domain.AggregateRoot
import com.bankbone.core.sharedkernel.domain.OutboxEvent
import com.bankbone.core.sharedkernel.domain.OutboxRepository
import com.bankbone.core.sharedkernel.ports.EventSerializer

/**
 * Abstract base class for repositories that need to handle transactions and event publishing.
 *
 * @param T The aggregate root type
 * @param outboxRepository The outbox repository for storing domain events
 * @param eventSerializer The serializer for converting domain events to strings
 */
abstract class AbstractTransactionalRepository<T : AggregateRoot<out AggregateId>>(
    private val outboxRepository: OutboxRepository,
    private val eventSerializer: EventSerializer
) {
    /**
     * Saves an aggregate and publishes its domain events to the outbox.
     *
     * @param aggregate The aggregate to save
     */
    protected suspend fun saveWithOutbox(aggregate: T) {
        // First, save the aggregate state itself.
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

    /**
     * Saves the aggregate's state to the underlying storage.
     *
     * @param aggregate The aggregate to save
     */
    protected abstract suspend fun saveAggregate(aggregate: T)
}