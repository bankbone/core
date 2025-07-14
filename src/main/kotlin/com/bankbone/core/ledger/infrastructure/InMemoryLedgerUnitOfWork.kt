package com.bankbone.core.ledger.infrastructure

import com.bankbone.core.ledger.ports.ChartOfAccountsRepository
import com.bankbone.core.ledger.ports.LedgerTransactionRepository
import com.bankbone.core.ledger.ports.LedgerUnitOfWork
import com.bankbone.core.ledger.ports.LedgerUnitOfWorkFactory
import com.bankbone.core.ledger.domain.LedgerTransaction
import com.bankbone.core.sharedkernel.infrastructure.JacksonEventSerializer
import com.bankbone.core.sharedkernel.infrastructure.InMemoryOutboxRepository
import java.util.concurrent.ConcurrentHashMap

// In a real application, this would manage a database connection.
// Here, it ensures all in-memory repositories share the same underlying data stores.
class InMemoryLedgerUnitOfWork(
    private val outboxRepository: InMemoryOutboxRepository,
    private val chartOfAccountsRepository: InMemoryChartOfAccountsRepository,
    private val transactions: ConcurrentHashMap<String, LedgerTransaction>
) : LedgerUnitOfWork {

    private val eventSerializer = JacksonEventSerializer()

    override fun ledgerTransactionRepository(): LedgerTransactionRepository {
        return InMemoryLedgerTransactionRepository(outboxRepository, eventSerializer, transactions)
    }

    override fun chartOfAccountsRepository(): ChartOfAccountsRepository {
        return chartOfAccountsRepository
    }

    override suspend fun <R> transaction(block: suspend (uow: com.bankbone.core.sharedkernel.ports.UnitOfWork) -> R): R {
        return block(this) // No-op for in-memory
    }

    override suspend fun commit() { /* No-op for in-memory */ }
    override suspend fun rollback() { /* No-op for in-memory */ }
    override fun close() { /* No-op for in-memory */ }
}

class InMemoryLedgerUnitOfWorkFactory : LedgerUnitOfWorkFactory {
    // These are shared across all UoW instances to simulate a single database.
    val outboxRepository = InMemoryOutboxRepository()
    val chartOfAccountsRepository = InMemoryChartOfAccountsRepository()
    val transactions = ConcurrentHashMap<String, LedgerTransaction>()

    override fun create(): LedgerUnitOfWork {
        return InMemoryLedgerUnitOfWork(outboxRepository, chartOfAccountsRepository, transactions)
    }
}