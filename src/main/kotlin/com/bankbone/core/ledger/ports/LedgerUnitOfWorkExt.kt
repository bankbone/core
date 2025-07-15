package com.bankbone.core.ledger.ports

/**
 * Provides a type-safe transactional boundary for Ledger-specific operations.
 * This extension function ensures that the block receives a `LedgerUnitOfWork`
 * instance, avoiding the need for casting.
 *
 * In a real implementation with a database, this would be responsible for
 * beginning the transaction before executing the block and committing or rolling back after.
 */
suspend fun <R> LedgerUnitOfWork.transaction(block: suspend (uow: LedgerUnitOfWork) -> R): R {
    return this.executeInTransaction { block(this) }
}