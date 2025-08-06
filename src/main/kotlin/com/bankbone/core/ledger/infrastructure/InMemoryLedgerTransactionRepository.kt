package com.bankbone.core.ledger.infrastructure

import com.bankbone.core.ledger.domain.LedgerTransaction
import com.bankbone.core.ledger.ports.LedgerTransactionRepository
import com.bankbone.core.sharedkernel.ports.OutboxRepository
import com.bankbone.core.sharedkernel.infrastructure.AbstractTransactionalRepository
import com.bankbone.core.sharedkernel.ports.EventSerializer
import java.util.concurrent.ConcurrentHashMap

class InMemoryLedgerTransactionRepository(
    outboxRepository: OutboxRepository,
    eventSerializer: EventSerializer
) : AbstractTransactionalRepository<LedgerTransaction>(outboxRepository, eventSerializer), LedgerTransactionRepository {
    private val transactions = ConcurrentHashMap<LedgerTransaction.Id, LedgerTransaction>()

    override suspend fun save(transaction: LedgerTransaction) = saveWithOutbox(transaction)
    override suspend fun findById(id: LedgerTransaction.Id): LedgerTransaction? = transactions[id]
    override suspend fun saveAggregate(aggregate: LedgerTransaction) {
        transactions[aggregate.id] = aggregate
    }
}