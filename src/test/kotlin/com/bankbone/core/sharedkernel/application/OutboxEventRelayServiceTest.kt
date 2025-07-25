package com.bankbone.core.sharedkernel.application

import com.bankbone.core.ledger.domain.LedgerTransaction
import com.bankbone.core.ledger.domain.events.LedgerTransactionPosted
import com.bankbone.core.sharedkernel.domain.Asset
import com.bankbone.core.sharedkernel.domain.OutboxEvent
import com.bankbone.core.sharedkernel.domain.OutboxEventStatus
import com.bankbone.core.sharedkernel.infrastructure.serialization.KotlinxEventDeserializer
import com.bankbone.core.sharedkernel.infrastructure.serialization.KotlinxEventSerializer
import com.bankbone.core.sharedkernel.infrastructure.InMemoryDomainEventPublisher
import com.bankbone.core.sharedkernel.infrastructure.InMemoryOutboxRepository
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OutboxEventRelayServiceTest {

    private lateinit var outboxRepository: InMemoryOutboxRepository
    private lateinit var domainEventPublisher: InMemoryDomainEventPublisher
    private lateinit var eventDeserializer: KotlinxEventDeserializer
    private lateinit var eventSerializer: KotlinxEventSerializer
    private lateinit var relayService: OutboxEventRelayService

    @BeforeEach
    fun setUp() {
        outboxRepository = InMemoryOutboxRepository()
        domainEventPublisher = InMemoryDomainEventPublisher()
        eventDeserializer = KotlinxEventDeserializer()
        eventSerializer = KotlinxEventSerializer()
        relayService = OutboxEventRelayService(outboxRepository, domainEventPublisher, eventDeserializer)
    }

    @Test
    fun `should publish pending events and mark them as processed`() = runBlocking {
        // Arrange: Create a pending event in the outbox
        val (pendingEvent, domainEvent) = createTestLedgerTransactionPostedEvent()
        outboxRepository.save(pendingEvent)

        // Act: Run the relay service
        relayService.relayPendingEvents()

        // Assert: The event was published
        assertEquals(1, domainEventPublisher.publishedEvents.size)
        val publishedEvent = domainEventPublisher.publishedEvents.first()
        assertTrue(publishedEvent is LedgerTransactionPosted, "Published event should be of the correct type")
        assertEquals(domainEvent.transactionId, publishedEvent.transactionId)
        assertEquals(BigDecimal("150.50"), publishedEvent.totalAmount)
        assertEquals(Asset("BRL"), publishedEvent.asset)

        // Assert: The event in the outbox is now marked as PUBLISHED
        assertEquals(OutboxEventStatus.PUBLISHED, outboxRepository.events[pendingEvent.id]?.status)
    }

    @Test
    fun `should retry on failure and succeed`() = runBlocking {
        // Arrange: Simulate 2 failures before success
        domainEventPublisher.setFailures(2)
        val (pendingEvent, _) = createTestLedgerTransactionPostedEvent()
        outboxRepository.save(pendingEvent)

        // Act
        relayService.relayPendingEvents()

        // Assert: Publisher was called 3 times
        assertEquals(3, domainEventPublisher.attempts)
        // Assert: Event was eventually published
        assertEquals(1, domainEventPublisher.publishedEvents.size)
        // Assert: Event is marked as PUBLISHED
        assertEquals(OutboxEventStatus.PUBLISHED, outboxRepository.events[pendingEvent.id]?.status)
    }

    @Test
    fun `should mark as FAILED after max attempts`() = runBlocking {
        // Arrange: Simulate 5 failures
        domainEventPublisher.setFailures(5)
        val (pendingEvent, _) = createTestLedgerTransactionPostedEvent()
        outboxRepository.save(pendingEvent)

        // Act
        relayService.relayPendingEvents()

        // Assert: Event is marked as FAILED with an error message
        val failedEvent = outboxRepository.events[pendingEvent.id]
        assertEquals(OutboxEventStatus.FAILED, failedEvent?.status)
        assertEquals(5, failedEvent?.attemptCount)
        assertTrue(failedEvent?.lastError!!.contains("Simulated publisher failure on attempt 5"))
    }

    private fun createTestLedgerTransactionPostedEvent(): Pair<OutboxEvent, LedgerTransactionPosted> {
        val domainEvent = LedgerTransactionPosted(
            transactionId = LedgerTransaction.Id.random(),
            totalAmount = BigDecimal("150.50"),
            asset = Asset("BRL"),
            occurredAt = Instant.parse("2024-07-14T12:00:00Z")
        )

        val outboxEvent = OutboxEvent(
            aggregateId = domainEvent.aggregateId.toString(),
            eventType = domainEvent.eventType,
            payload = eventSerializer.serialize(domainEvent)
        )

        return outboxEvent to domainEvent
    }
}