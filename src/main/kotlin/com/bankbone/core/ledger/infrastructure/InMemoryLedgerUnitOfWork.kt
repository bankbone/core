package com.bankbone.core.ledger.infrastructure

import com.bankbone.core.ledger.ports.ChartOfAccountsRepository
import com.bankbone.core.ledger.ports.LedgerTransactionRepository
import com.bankbone.core.ledger.ports.LedgerUnitOfWork
import com.bankbone.core.ledger.domain.LedgerTransaction.Id
import com.bankbone.core.ledger.domain.LedgerTransaction
import com.bankbone.core.sharedkernel.infrastructure.KotlinxEventSerializer
import com.bankbone.core.sharedkernel.infrastructure.InMemoryOutboxRepository
import java.util.concurrent.ConcurrentHashMap

// In a real application, this would manage a database connection.
// Here, it ensures all in-memory repositories share the same underlying data stores.
class InMemoryLedgerUnitOfWork(
    private val outboxRepository: InMemoryOutboxRepository,
    private val chartOfAccountsRepository: InMemoryChartOfAccountsRepository,
    private val transactions: ConcurrentHashMap<Id, LedgerTransaction>,
    private val eventSerializer: KotlinxEventSerializer
) : LedgerUnitOfWork {

    override fun ledgerTransactionRepository(): LedgerTransactionRepository {
        return InMemoryLedgerTransactionRepository(outboxRepository, eventSerializer, transactions)
    }

    override fun chartOfAccountsRepository(): ChartOfAccountsRepository {
        return chartOfAccountsRepository
    }

    override suspend fun <R> executeInTransaction(block: suspend () -> R): R {
        return block() // For in-memory, we just execute the block. A real DB would manage transactions here.
    }

    override suspend fun commit() { /* No-op for in-memory */ }
    override suspend fun rollback() { /* No-op for in-memory */ }
    override fun close() { /* No-op for in-memory */ }
}