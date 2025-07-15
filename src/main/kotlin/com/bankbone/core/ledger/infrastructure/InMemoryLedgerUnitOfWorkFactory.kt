package com.bankbone.core.ledger.infrastructure

import com.bankbone.core.ledger.domain.LedgerTransaction
import com.bankbone.core.ledger.ports.LedgerUnitOfWork
import com.bankbone.core.ledger.ports.LedgerUnitOfWorkFactory
import com.bankbone.core.sharedkernel.infrastructure.InMemoryOutboxRepository
import com.bankbone.core.sharedkernel.infrastructure.serialization.KotlinxEventSerializer
import java.util.concurrent.ConcurrentHashMap

class InMemoryLedgerUnitOfWorkFactory : LedgerUnitOfWorkFactory {
    // These are shared across all UoW instances to simulate a single database.
    val outboxRepository = InMemoryOutboxRepository()
    val chartOfAccountsRepository = InMemoryChartOfAccountsRepository()
    val transactions = ConcurrentHashMap<LedgerTransaction.Id, LedgerTransaction>()

    override fun create(): LedgerUnitOfWork {
        return InMemoryLedgerUnitOfWork(
            outboxRepository,
            chartOfAccountsRepository,
            transactions,
            KotlinxEventSerializer(),
        )
    }
}