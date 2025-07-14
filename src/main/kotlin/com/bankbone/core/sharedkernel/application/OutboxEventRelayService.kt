package com.bankbone.core.sharedkernel.application

import com.bankbone.core.sharedkernel.ports.EventDeserializer
import com.bankbone.core.sharedkernel.ports.DomainEventPublisher
import com.bankbone.core.sharedkernel.ports.OutboxRepository

/**
 * This service is responsible for relaying events from the outbox to the message broker.
 * In a real application, this would be triggered by a scheduled job (e.g., a cron job)
 * or a database trigger, not called directly via an API.
 */
class OutboxEventRelayService(
    private val outboxRepository: OutboxRepository,
    private val domainEventPublisher: DomainEventPublisher,
    private val eventDeserializer: EventDeserializer
) {
    suspend fun relayPendingEvents(limit: Int = 100) {
        val pendingEvents = outboxRepository.findPendingEvents(limit)
        if (pendingEvents.isNotEmpty()) {
            val domainEvents = pendingEvents.mapNotNull { eventDeserializer.deserialize(it) }

            // Only publish if deserialization was successful
            domainEventPublisher.publish(domainEvents)
            pendingEvents.forEach { outboxRepository.markAsProcessed(it) }
        }
    }
}