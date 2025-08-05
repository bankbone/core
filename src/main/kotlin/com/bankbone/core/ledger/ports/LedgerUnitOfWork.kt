package com.bankbone.core.ledger.ports

/**
 * Defines the contract for a Unit of Work within the Ledger bounded context.
 * It provides access to all repositories relevant to a single transaction.
 */
interface LedgerUnitOfWork {
    fun chartOfAccountsRepository(): ChartOfAccountsRepository
    fun ledgerTransactionRepository(): LedgerTransactionRepository

    suspend fun commit()
    suspend fun rollback()
}