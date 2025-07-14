package com.bankbone.core.ledger.infrastructure

import com.bankbone.core.ledger.domain.LedgerTransaction
import com.bankbone.core.ledger.ports.LedgerTransactionRepository
import com.bankbone.core.sharedkernel.domain.OutboxEvent
import com.bankbone.core.sharedkernel.ports.OutboxRepository
import com.bankbone.core.sharedkernel.ports.EventSerializer
import java.util.concurrent.ConcurrentHashMap

class InMemoryLedgerTransactionRepository(
    private val outboxRepository: OutboxRepository,
    private val eventSerializer: EventSerializer
) : LedgerTransactionRepository {
    private val transactions = ConcurrentHashMap<String, LedgerTransaction>()

    override suspend fun save(transaction: LedgerTransaction) {
        // In a real implementation, this block would be wrapped in a database transaction.
        // 1. Save the aggregate
        transactions[transaction.id] = transaction

        // 2. Save its domain events to the outbox
        transaction.domainEvents().forEach { domainEvent ->
            val payload = eventSerializer.serialize(domainEvent)
            val outboxEvent = OutboxEvent(
                aggregateId = transaction.id,
                eventType = domainEvent::class.simpleName ?: "UnknownEvent",
                payload = payload
            )
            outboxRepository.save(outboxEvent)
        }
        transaction.clearEvents()
    }

    override suspend fun findById(id: String): LedgerTransaction? {
        return transactions[id]
    }
}
