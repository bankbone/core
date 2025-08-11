package com.bankbone.core.ledger.ports

import com.bankbone.core.ledger.domain.LedgerTransaction
import com.bankbone.core.ledger.domain.LedgerTransactionId

/**
 * Repository interface for persisting and retrieving LedgerTransaction aggregates.
 * 
 * This interface defines the contract for data access operations related to LedgerTransaction.
 * Implementations should handle persistence concerns while the domain layer remains
 * unaware of the underlying storage mechanism.
 */
interface LedgerTransactionRepository {
    /**
     * Saves a LedgerTransaction aggregate.
     * 
     * @param transaction The transaction to save
     */
    suspend fun save(transaction: LedgerTransaction)
    
    /**
     * Finds a LedgerTransaction by its ID.
     * 
     * @param id The ID of the transaction to find
     * @return The found LedgerTransaction, or null if not found.
     */
    suspend fun findById(id: LedgerTransactionId): LedgerTransaction?
}
