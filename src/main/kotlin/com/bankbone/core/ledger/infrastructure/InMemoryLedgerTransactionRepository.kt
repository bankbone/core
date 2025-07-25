package com.bankbone.core.ledger.infrastructure

import com.bankbone.core.ledger.domain.LedgerTransaction
import com.bankbone.core.ledger.domain.LedgerTransaction.Id
import com.bankbone.core.ledger.ports.LedgerTransactionRepository
import com.bankbone.core.sharedkernel.infrastructure.AbstractTransactionalRepository
import com.bankbone.core.sharedkernel.ports.OutboxRepository
import com.bankbone.core.sharedkernel.ports.EventSerializer
import java.util.concurrent.ConcurrentHashMap

class InMemoryLedgerTransactionRepository(
    outboxRepository: OutboxRepository,
    eventSerializer: EventSerializer,
    private val transactions: ConcurrentHashMap<Id, LedgerTransaction>
) : AbstractTransactionalRepository<LedgerTransaction, Id>(outboxRepository, eventSerializer), LedgerTransactionRepository {

    override suspend fun save(transaction: LedgerTransaction) {
        saveWithEvents(transaction)
    }

    override suspend fun persist(aggregate: LedgerTransaction) {
        transactions[aggregate.id] = aggregate
    }

    override fun getAggregateId(aggregate: LedgerTransaction): Id {
        return aggregate.id
    }

    override suspend fun findById(id: Id): LedgerTransaction? {
        return transactions[id]
    }
}
