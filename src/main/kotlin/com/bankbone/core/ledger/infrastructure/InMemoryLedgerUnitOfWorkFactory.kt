package com.bankbone.core.ledger.infrastructure

import com.bankbone.core.ledger.ports.LedgerUnitOfWorkFactory
import com.bankbone.core.sharedkernel.infrastructure.InMemoryOutboxRepository
import com.bankbone.core.sharedkernel.infrastructure.serialization.KotlinxEventSerializer

/**
 * A factory for creating in-memory units of work.
 *
 * For the in-memory implementation, all repositories are singletons for the lifetime
 * of the application to simulate a persistent data store. The factory holds these
 * singleton instances.
 */
class InMemoryLedgerUnitOfWorkFactory : LedgerUnitOfWorkFactory {
    val outboxRepository = InMemoryOutboxRepository()
    val eventSerializer = KotlinxEventSerializer()
    val chartOfAccountsRepository = InMemoryChartOfAccountsRepository()
    val ledgerTransactionRepository = InMemoryLedgerTransactionRepository(outboxRepository, eventSerializer)

    override fun create() = InMemoryLedgerUnitOfWork(chartOfAccountsRepository, ledgerTransactionRepository)
}