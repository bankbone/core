package com.bankbone.core.ledger.ports

import com.bankbone.core.sharedkernel.ports.UnitOfWork

// Extends the shared UoW with Ledger-specific repository factories.
interface LedgerUnitOfWork : UnitOfWork {
    fun ledgerTransactionRepository(): LedgerTransactionRepository
    fun chartOfAccountsRepository(): ChartOfAccountsRepository
}