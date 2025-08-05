package com.bankbone.core.ledger.infrastructure

import com.bankbone.core.ledger.ports.ChartOfAccountsRepository
import com.bankbone.core.ledger.ports.LedgerTransactionRepository
import com.bankbone.core.ledger.ports.LedgerUnitOfWork

/**
 * An in-memory implementation of the Unit of Work. In this simple case, it just holds
 * references to the singleton repository instances provided by its factory.
 */
class InMemoryLedgerUnitOfWork(
    private val chartOfAccountsRepo: ChartOfAccountsRepository,
    private val ledgerTransactionRepo: LedgerTransactionRepository
) : LedgerUnitOfWork {
    override fun chartOfAccountsRepository(): ChartOfAccountsRepository = chartOfAccountsRepo
    override fun ledgerTransactionRepository(): LedgerTransactionRepository = ledgerTransactionRepo
    override suspend fun commit() { /* No-op for in-memory implementation */ }
    override suspend fun rollback() { /* No-op for in-memory implementation */ }
}