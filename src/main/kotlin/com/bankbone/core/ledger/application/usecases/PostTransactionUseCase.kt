package com.bankbone.core.ledger.application.usecases

import com.bankbone.core.ledger.application.PostTransactionCommandValidator
import com.bankbone.core.ledger.application.commands.PostTransactionCommand
import com.bankbone.core.ledger.domain.LedgerTransaction
import com.bankbone.core.ledger.infrastructure.InMemoryLedgerUnitOfWorkFactory
import com.bankbone.core.ledger.ports.LedgerUnitOfWorkFactory
import com.bankbone.core.ledger.ports.transaction
import com.bankbone.core.sharedkernel.application.CommandHandler

/**
 * The core use case handler for posting a transaction.
 * Its single responsibility is to orchestrate the business logic within a transaction.
 */
class PostTransactionUseCase(
    private val uowFactory: LedgerUnitOfWorkFactory,
    private val validator: PostTransactionCommandValidator
) : CommandHandler<PostTransactionCommand, LedgerTransaction> {
    override suspend fun handle(command: PostTransactionCommand): LedgerTransaction {
        return uowFactory.create().transaction { uow ->
            validator.validate(command, uow.chartOfAccountsRepository())

            val transaction = LedgerTransaction.create(
                sourceTransactionId = command.sourceTransactionId,
                description = command.description,
                entries = command.entries
            )

            uow.ledgerTransactionRepository().save(transaction)
            transaction
        }
    }
}