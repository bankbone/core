package com.bankbone.core.ledger.infrastructure

import com.bankbone.core.ledger.domain.LedgerTransaction
import com.bankbone.core.ledger.domain.LedgerTransactionId
import com.bankbone.core.ledger.ports.LedgerTransactionRepository
import com.bankbone.core.sharedkernel.infrastructure.AbstractTransactionalRepository
import com.bankbone.core.sharedkernel.ports.EventSerializer
import com.bankbone.core.sharedkernel.ports.OutboxRepository
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory implementation of LedgerTransactionRepository for testing and development.
 * 
 * This implementation uses a concurrent hash map to store transactions in memory.
 * It's suitable for testing but not for production use as it doesn't persist data
 * between application restarts.
 */
class InMemoryLedgerTransactionRepository(
    outboxRepository: OutboxRepository,
    eventSerializer: EventSerializer
) : AbstractTransactionalRepository<LedgerTransaction>(outboxRepository, eventSerializer), LedgerTransactionRepository {
    
    private val transactions = ConcurrentHashMap<LedgerTransactionId, LedgerTransaction>()

    /**
     * Saves a LedgerTransaction to the in-memory store.
     * 
     * @param transaction The transaction to save
     */
    override suspend fun save(transaction: LedgerTransaction) = saveWithOutbox(transaction)
    
    /**
     * Finds a LedgerTransaction by its ID.
     * 
     * @param id The ID of the transaction to find
     * @return The found LedgerTransaction, or null if not found.
     */
    override suspend fun findById(id: LedgerTransactionId): LedgerTransaction? = transactions[id]
    
    /**
     * Internal method to save the aggregate to the in-memory store.
     * This is called by the base class after handling outbox events.
     * 
     * @param aggregate The aggregate to save
     */
    override suspend fun saveAggregate(aggregate: LedgerTransaction) {
        transactions[aggregate.id] = aggregate
    }
    
    /**
     * Clears all transactions from the in-memory store.
     * For testing purposes only.
     */
    fun clear() {
        transactions.clear()
    }
}