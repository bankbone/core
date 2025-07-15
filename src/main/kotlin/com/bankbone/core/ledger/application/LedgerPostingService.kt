package com.bankbone.core.ledger.application

import com.bankbone.core.ledger.application.commands.PostTransactionCommand
import com.bankbone.core.ledger.domain.LedgerTransaction
import com.bankbone.core.ledger.ports.LedgerUnitOfWorkFactory
import com.bankbone.core.ledger.ports.transaction

class LedgerPostingService(
    private val uowFactory: LedgerUnitOfWorkFactory,
    private val validator: PostTransactionCommandValidator
) {
    suspend fun postTransaction(command: PostTransactionCommand): LedgerTransaction {
        return uowFactory.create().transaction { uow ->
            val chartOfAccountsRepo = uow.chartOfAccountsRepository()
            val transactionRepo = uow.ledgerTransactionRepository()

            // 1. Delegate validation to the dedicated validator class, using the repo from the UoW.
            validator.validate(command, chartOfAccountsRepo)

            // 2. The service's core responsibility: orchestrate the domain and repository.
            val transaction = LedgerTransaction(id = LedgerTransaction.Id.random(), sourceTransactionId = command.sourceTransactionId, description = command.description, entries = command.entries)

            transactionRepo.save(transaction)
            transaction
        }
    }
}
