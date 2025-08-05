package com.bankbone.core.ledger.application.usecases

import com.bankbone.core.ledger.application.commands.CreateAccountCommand
import com.bankbone.core.ledger.domain.Account
import com.bankbone.core.ledger.ports.LedgerUnitOfWorkFactory
import com.bankbone.core.ledger.ports.transaction
import com.bankbone.core.sharedkernel.application.CommandHandler

class CreateAccountUseCase(
    private val uowFactory: LedgerUnitOfWorkFactory
) : CommandHandler<CreateAccountCommand, Account> {
    override suspend fun handle(command: CreateAccountCommand): Account {
        require(command.name.isNotBlank()) { "Account name must not be blank." }

        return uowFactory.create().transaction { uow ->
            val repo = uow.chartOfAccountsRepository()

            val parentId = command.parentAccountId?.let { Account.Id.fromString(it) }?.also {
                if (!repo.exists(it)) {
                    throw IllegalArgumentException("Parent account with ID $it does not exist.")
                }
            }

            val newAccount = Account(
                id = Account.Id.random(),
                name = command.name,
                type = command.type,
                asset = command.asset,
                parentAccountId = parentId,
                metadata = command.metadata
            )

            repo.add(newAccount)
            newAccount
        }
    }
}