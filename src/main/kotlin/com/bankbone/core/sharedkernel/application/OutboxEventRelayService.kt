package com.bankbone.core.sharedkernel.application

import com.bankbone.core.sharedkernel.ports.EventDeserializer
import com.bankbone.core.sharedkernel.ports.DomainEventPublisher
import com.bankbone.core.sharedkernel.ports.OutboxRepository
import kotlinx.coroutines.delay
import kotlin.math.pow
import kotlin.random.Random

private const val MAX_ATTEMPTS = 5
private const val INITIAL_DELAY_MS = 100L
private const val BACKOFF_FACTOR = 2.0

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
        if (pendingEvents.isEmpty()) {
            return
        }

        val domainEvents = pendingEvents.mapNotNull { eventDeserializer.deserialize(it) }
        if (domainEvents.isEmpty()) {
            // Handle cases where deserialization fails for all events
            outboxRepository.markAsFailed(pendingEvents, "Deserialization failed for all events in the batch.", 1)
            return
        }

        for (attempt in 1..MAX_ATTEMPTS) {
            try {
                domainEventPublisher.publish(domainEvents)
                outboxRepository.markAsProcessed(pendingEvents)
                return // Success, exit the function
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Unknown error during publishing"
                if (attempt == MAX_ATTEMPTS) {
                    outboxRepository.markAsFailed(pendingEvents, errorMessage, attempt)
                    return
                }

                val delayTime = (INITIAL_DELAY_MS * BACKOFF_FACTOR.pow(attempt - 1)).toLong()
                val jitter = Random.nextLong(delayTime / 10) // Add up to 10% jitter
                delay(delayTime + jitter)
            }
        }
    }
}