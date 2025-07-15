package com.bankbone.core.sharedkernel.application

import com.bankbone.core.sharedkernel.domain.DomainEvent
import com.bankbone.core.sharedkernel.domain.OutboxEvent
import com.bankbone.core.sharedkernel.domain.OutboxEventStatus
import com.bankbone.core.sharedkernel.ports.EventDeserializer
import com.bankbone.core.sharedkernel.ports.DomainEventPublisher
import com.bankbone.core.sharedkernel.ports.OutboxRepository
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import kotlin.math.pow
import kotlin.random.Random

private const val MAX_ATTEMPTS = 5
private const val INITIAL_DELAY_MS = 100L
private const val BACKOFF_FACTOR = 2.0
private const val BATCH_SIZE = 100 // TODO: Make configurable

class OutboxEventRelayService(
    private val outboxRepository: OutboxRepository,
    private val domainEventPublisher: DomainEventPublisher,
    private val eventDeserializer: EventDeserializer
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    suspend fun relayPendingEvents() {
        val pendingEvents = outboxRepository.findPendingEvents(BATCH_SIZE)
        if (pendingEvents.isEmpty()) {
            return
        }

        pendingEvents.forEach { event ->
            processEvent(event)
        }
    }

    private suspend fun processEvent(event: OutboxEvent) {
        try {
            val domainEvent = eventDeserializer.deserialize(event)
            if (domainEvent == null) {
                markEventAsFailed(event, "Deserialization failed", 1)
                return
            }

            publishEventWithRetries(event, domainEvent)

        } catch (e: Exception) {
            logger.error("Error processing outbox event ${event.id}", e)
            markEventAsFailed(event, "Unexpected error: ${e.message}", 1) // Initial attempt
        }
    }

    private suspend fun publishEventWithRetries(event: OutboxEvent, domainEvent: DomainEvent) {
        for (attempt in 1..MAX_ATTEMPTS) {
            try {
                domainEventPublisher.publish(listOf(domainEvent)) // Publish individually
                outboxRepository.markAsProcessed(listOf(event))
                logger.info("Successfully published event ${event.id} after $attempt attempts")
                return // Success, exit the function

            } catch (e: Exception) {
                val errorMessage = e.message ?: "Unknown error during publishing"
                logger.warn("Attempt $attempt failed for event ${event.id}: $errorMessage")

                if (attempt == MAX_ATTEMPTS) {
                    markEventAsFailed(event, errorMessage, attempt)
                    return
                }

                val delayTime = (INITIAL_DELAY_MS * BACKOFF_FACTOR.pow(attempt - 1)).toLong()
                val jitter = Random.nextLong(delayTime / 10) // Add up to 10% jitter
                delay(delayTime + jitter)
            }
        }
    }

    private suspend fun markEventAsFailed(event: OutboxEvent, errorMessage: String, attempt: Int) {
        outboxRepository.markAsFailed(listOf(event), "$errorMessage (attempt $attempt)", attempt)
        logger.error("Event ${event.id} marked as FAILED: $errorMessage after $attempt attempts")
        // Consider moving to a Dead Letter Queue here
    }
}
