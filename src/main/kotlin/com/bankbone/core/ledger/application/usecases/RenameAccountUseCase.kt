package com.bankbone.core.ledger.application.usecases

import com.bankbone.core.ledger.application.commands.RenameAccountCommand
import com.bankbone.core.ledger.ports.LedgerUnitOfWorkFactory
import com.bankbone.core.ledger.ports.transaction
import com.bankbone.core.sharedkernel.application.CommandHandler

class RenameAccountUseCase(
    private val uowFactory: LedgerUnitOfWorkFactory
) : CommandHandler<RenameAccountCommand, Unit> {
    override suspend fun handle(command: RenameAccountCommand) {
        require(command.newName.isNotBlank()) { "New account name must not be blank." }

        uowFactory.create().transaction { uow ->
            val repo = uow.chartOfAccountsRepository()
            val account = repo.findById(command.accountId)
                ?: throw IllegalArgumentException("Account with ID ${command.accountId} not found.")

            val updatedAccount = account.copy(name = command.newName)
            repo.update(updatedAccount)
        }
    }
}